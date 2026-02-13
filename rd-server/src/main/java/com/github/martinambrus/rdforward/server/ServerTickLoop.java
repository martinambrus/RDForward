package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.PingPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;

import com.github.martinambrus.rdforward.server.event.ServerEvents;

import java.util.List;

/**
 * Server tick loop running at 20 TPS (50ms per tick), matching Minecraft's
 * standard server tick rate.
 *
 * Each tick:
 *   1. Sends keep-alive pings to all clients (every 2 seconds = 40 ticks)
 *   2. Future: process queued player actions
 *   3. Future: update world state (physics, redstone, etc.)
 *   4. Future: dispatch tick events to mods
 *
 * The tick loop runs on its own daemon thread so it doesn't block
 * the Netty event loop or prevent JVM shutdown.
 */
public class ServerTickLoop implements Runnable {

    private static final long TICK_MS = 50; // 20 TPS
    private static final int PING_INTERVAL_TICKS = 40; // Every 2 seconds
    private static final int CHUNK_UPDATE_INTERVAL_TICKS = 5; // Every 250ms
    private static final int SAVE_INTERVAL_TICKS = 6000; // Every 5 minutes

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
        long lastTick = System.currentTimeMillis();

        while (running) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastTick;

            if (elapsed >= TICK_MS) {
                lastTick = now;
                tick();
            } else {
                // Sleep for the remaining time, but wake up a bit early
                // to avoid overshooting
                long sleepTime = TICK_MS - elapsed - 1;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
    }

    private void tick() {
        tickCount++;

        // Process queued block changes and broadcast results
        List<SetBlockServerPacket> blockChanges = world.processPendingBlockChanges();
        for (SetBlockServerPacket sb : blockChanges) {
            playerManager.broadcastPacket(sb);
            // Keep Alpha chunk data in sync with ServerWorld
            chunkManager.setBlock(sb.getX(), sb.getY(), sb.getZ(), (byte) sb.getBlockType());
        }

        // Send keep-alive pings periodically
        if (tickCount % PING_INTERVAL_TICKS == 0) {
            playerManager.broadcastPacket(new PingPacket());
        }

        // Update chunk loading/unloading for all players periodically
        if (tickCount % CHUNK_UPDATE_INTERVAL_TICKS == 0) {
            for (ConnectedPlayer player : playerManager.getAllPlayers()) {
                chunkManager.updatePlayerChunks(player);
            }
        }

        // Auto-save world and player positions periodically
        if (tickCount % SAVE_INTERVAL_TICKS == 0) {
            ServerEvents.WORLD_SAVE.invoker().onWorldSave();
            world.saveIfDirty();
            world.savePlayers(playerManager.getAllPlayers());
            chunkManager.saveAllDirty();
        }

        // Fire tick event for mods
        ServerEvents.SERVER_TICK.invoker().onServerTick(tickCount);
    }

    public long getTickCount() {
        return tickCount;
    }

    public boolean isRunning() {
        return running;
    }
}
