package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.PingPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;

import com.github.martinambrus.rdforward.server.api.BlockOwnerRegistry;
import com.github.martinambrus.rdforward.server.api.ServerProperties;
import com.github.martinambrus.rdforward.server.bedrock.BedrockSessionWrapper;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * Server tick loop running at 20 TPS (50ms per tick), matching Minecraft's
 * standard server tick rate. Uses nanosecond-precision fixed-rate scheduling
 * to prevent cumulative drift: each tick advances the deadline by exactly
 * 50ms regardless of actual tick duration. If the server falls behind
 * (e.g. GC pause), it catches up by running ticks back-to-back, capped
 * at {@link #MAX_CATCH_UP_TICKS} to prevent death spirals.
 *
 * Each tick:
 *   1. Sends keep-alive pings to all clients (interval from keep-alive-interval config)
 *   2. Future: process queued player actions
 *   3. Future: update world state (physics, redstone, etc.)
 *   4. Future: dispatch tick events to mods
 *
 * The tick loop runs on its own daemon thread so it doesn't block
 * the Netty event loop or prevent JVM shutdown.
 */
public class ServerTickLoop implements Runnable {

    private static final long TICK_NANOS = 50_000_000L; // 50ms = 20 TPS
    /** Maximum ticks to run back-to-back when catching up after a stall. */
    private static final int MAX_CATCH_UP_TICKS = 10;
    private final int pingIntervalTicks; // Derived from keep-alive-interval config
    private static final int CHUNK_UPDATE_INTERVAL_TICKS = 5; // Every 250ms
    private static final int TIME_BROADCAST_INTERVAL_TICKS = 20; // Every 1 second
    private static final int BEDROCK_PROBE_INTERVAL_TICKS = 100; // Every 5 seconds
    private static final int SAVE_INTERVAL_TICKS = 6000; // Every 5 minutes
    private static final int INCREMENTAL_SAVE_INTERVAL_TICKS = 100; // Every 5 seconds

    private final PlayerManager playerManager;
    private final ServerWorld world;
    private final ChunkManager chunkManager;
    private volatile boolean running;
    private Thread thread;
    private long tickCount;

    public ServerTickLoop(PlayerManager playerManager, ServerWorld world, ChunkManager chunkManager) {
        this.playerManager = playerManager;
        this.world = world;
        this.chunkManager = chunkManager;
        this.pingIntervalTicks = Math.max(1, ServerProperties.getKeepAliveIntervalSeconds() * 20);
    }

    /**
     * Start the tick loop on a new daemon thread.
     */
    public void start() {
        if (running) return;
        running = true;
        thread = new Thread(this, "RDForward-TickLoop");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Stop the tick loop and wait for the thread to finish.
     */
    public void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void run() {
        long nextTick = System.nanoTime();

        while (running) {
            long now = System.nanoTime();

            if (now >= nextTick) {
                // Check how far behind we are and skip if necessary
                int ticksBehind = (int) ((now - nextTick) / TICK_NANOS);
                if (ticksBehind > MAX_CATCH_UP_TICKS) {
                    long skipped = ticksBehind - MAX_CATCH_UP_TICKS;
                    nextTick += skipped * TICK_NANOS;
                    System.err.println("[TickLoop] Skipped " + skipped
                            + " tick(s) - can't keep up!");
                }

                tick();
                nextTick += TICK_NANOS;
            } else {
                // Sleep until the next tick, using appropriate precision
                long sleepNanos = nextTick - now;
                if (sleepNanos > 2_000_000L) {
                    // >2ms remaining: Thread.sleep is accurate enough
                    try {
                        Thread.sleep(sleepNanos / 1_000_000 - 1);
                    } catch (InterruptedException e) {
                        break;
                    }
                } else if (sleepNanos > 0) {
                    // Sub-ms remaining: use LockSupport for finer precision
                    LockSupport.parkNanos(sleepNanos);
                }
            }
        }
    }

    private void tick() {
        tickCount++;

        // Advance world time (day/night cycle + weather duration)
        world.tickTime();

        // Process queued block changes and broadcast results
        List<SetBlockServerPacket> blockChanges = world.processPendingBlockChanges();
        for (SetBlockServerPacket sb : blockChanges) {
            playerManager.broadcastWrite(sb);
            chunkManager.setBlock(sb.getX(), sb.getY(), sb.getZ(), (byte) sb.getBlockType());
        }

        // Send keep-alive pings periodically
        if (tickCount % pingIntervalTicks == 0) {
            playerManager.broadcastWrite(new PingPacket());
        }

        // Bedrock clients need server-initiated NetworkStackLatencyPacket probes.
        // The Classic PingPacket is dropped by ClassicToBedrockTranslator (RakNet
        // handles connection-level keep-alive), but the Bedrock client's application
        // layer times out without periodic server probes. Sent every 5 seconds,
        // well within the 10-second default RakNet session timeout.
        if (tickCount % BEDROCK_PROBE_INTERVAL_TICKS == 0) {
            long timestamp = System.currentTimeMillis();
            for (ConnectedPlayer player : playerManager.getAllPlayers()) {
                BedrockSessionWrapper bsw = player.getBedrockSession();
                if (bsw != null) {
                    NetworkStackLatencyPacket probe = new NetworkStackLatencyPacket();
                    probe.setTimestamp(timestamp);
                    probe.setFromServer(true);
                    bsw.sendDirect(probe);
                }
            }
        }

        // Broadcast time update periodically
        if (tickCount % TIME_BROADCAST_INTERVAL_TICKS == 0) {
            long timeOfDay = world.isTimeFrozen() ? -world.getWorldTime() : world.getWorldTime();
            playerManager.broadcastTimeUpdateWrite(tickCount, timeOfDay);
        }

        // Update chunk loading/unloading for all players periodically
        if (tickCount % CHUNK_UPDATE_INTERVAL_TICKS == 0) {
            for (ConnectedPlayer player : playerManager.getAllPlayers()) {
                chunkManager.updatePlayerChunks(player);
            }
        }

        // Adaptive block change batching: if any chunk accumulated too many
        // individual block changes, resend the full chunk to affected players.
        chunkManager.checkBatchResend();
        chunkManager.resetChangeCounters();

        // Incremental chunk saves: spread disk I/O over time by saving
        // a few dirty chunks every 5 seconds on the worker pool thread.
        // Skip on full-save ticks to avoid racing with saveAllDirty().
        if (tickCount % INCREMENTAL_SAVE_INTERVAL_TICKS == 0
                && tickCount % SAVE_INTERVAL_TICKS != 0) {
            chunkManager.saveIncrementally();
        }

        // Auto-save world and player positions asynchronously.
        // Snapshots are taken on this thread (fast), disk I/O runs on
        // the background save thread so the tick loop doesn't stall.
        if (tickCount % SAVE_INTERVAL_TICKS == 0) {
            ServerEvents.WORLD_SAVE.invoker().onWorldSave();
            world.saveIfDirtyAsync();
            world.savePlayersAsync(playerManager.getAllPlayers());
            BlockOwnerRegistry.saveIfDirty();
            chunkManager.saveAllDirty();
        }

        // Fire tick event for mods
        ServerEvents.SERVER_TICK.invoker().onServerTick(tickCount);

        // Flush all buffered writes for this tick in a single batch.
        // This coalesces block changes, pings, time updates, and chunk
        // sends into one network flush per player instead of per-packet.
        playerManager.flushAll();
    }

    public long getTickCount() {
        return tickCount;
    }

    public boolean isRunning() {
        return running;
    }
}
