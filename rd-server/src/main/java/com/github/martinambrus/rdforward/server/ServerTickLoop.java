package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.packet.classic.PingPacket;

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
    private static final int SAVE_INTERVAL_TICKS = 6000; // Every 5 minutes

    private final PlayerManager playerManager;
    private final ServerWorld world;
    private volatile boolean running;
    private Thread thread;
    private long tickCount;

    public ServerTickLoop(PlayerManager playerManager, ServerWorld world) {
        this.playerManager = playerManager;
        this.world = world;
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

        // Send keep-alive pings periodically
        if (tickCount % PING_INTERVAL_TICKS == 0) {
            playerManager.broadcastPacket(new PingPacket());
        }

        // Auto-save world periodically
        if (tickCount % SAVE_INTERVAL_TICKS == 0) {
            world.saveIfDirty();
        }
    }

    public long getTickCount() {
        return tickCount;
    }

    public boolean isRunning() {
        return running;
    }
}
