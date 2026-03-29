package com.github.martinambrus.rdforward.server.api;

import com.github.martinambrus.rdforward.protocol.event.EventResult;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.event.ServerEvents;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Global anti-grief protection for block placement and breaking.
 *
 * <p>Enforces a configurable per-player rate limit on block changes per second.
 * Players exceeding the limit have their actions cancelled and are warned.
 *
 * <h3>Mod bypass API</h3>
 * Mods performing bulk block operations (WorldEdit, arena resets, etc.) can
 * bypass the rate limiter using the thread-local bypass flag:
 * <pre>
 *   GriefProtection.setBypassed(true);
 *   try {
 *       // bulk block changes — no rate limiting
 *   } finally {
 *       GriefProtection.setBypassed(false);
 *   }
 * </pre>
 *
 * Or using the convenience wrapper:
 * <pre>
 *   GriefProtection.runBypassed(() -> {
 *       // bulk block changes
 *   });
 * </pre>
 */
public final class GriefProtection {

    private GriefProtection() {}

    /**
     * Thread-local bypass flag. When true, the current thread's block changes
     * skip all grief protection checks. Mods set this around bulk operations.
     */
    private static final ThreadLocal<Boolean> BYPASSED = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    /** Per-player sliding window: maps player name → tracker. */
    private static final ConcurrentHashMap<String, RateTracker> trackers = new ConcurrentHashMap<>();

    private static volatile int maxChangesPerSecond = 30;
    private static volatile PlayerManager playerManager;
    private static volatile boolean enabled = false;

    /**
     * Set the bypass flag for the current thread. Mods should always reset
     * this in a finally block.
     */
    public static void setBypassed(boolean bypassed) {
        BYPASSED.set(bypassed);
    }

    /**
     * Check if the current thread has grief protection bypassed.
     */
    public static boolean isBypassed() {
        return BYPASSED.get();
    }

    /**
     * Run an action with grief protection bypassed. Ensures the flag is
     * always cleared afterward.
     */
    public static void runBypassed(Runnable action) {
        BYPASSED.set(true);
        try {
            action.run();
        } finally {
            BYPASSED.set(false);
        }
    }

    /**
     * Initialize and register grief protection on the block events.
     *
     * @param maxPerSecond max block changes per second per player (0 = disabled)
     * @param pm           the player manager for sending warnings
     */
    public static void init(int maxPerSecond, PlayerManager pm) {
        playerManager = pm;
        maxChangesPerSecond = maxPerSecond;
        if (maxPerSecond <= 0) {
            enabled = false;
            System.out.println("Grief protection rate limiting disabled (max-block-changes-per-second=0)");
            return;
        }
        enabled = true;

        ServerEvents.BLOCK_BREAK.register((player, x, y, z, blockType) ->
                checkRateLimit(player));
        ServerEvents.BLOCK_PLACE.register((player, x, y, z, blockType) ->
                checkRateLimit(player));

        // Clean up when players leave
        ServerEvents.PLAYER_LEAVE.register(trackers::remove);

        System.out.println("Grief protection enabled: max " + maxPerSecond
                + " block changes/sec per player");
    }

    private static EventResult checkRateLimit(String player) {
        if (!enabled || BYPASSED.get()) {
            return EventResult.PASS;
        }

        // OPs bypass rate limiting (level 1+)
        if (PermissionManager.getOpLevel(player) >= PermissionManager.OP_BYPASS_SPAWN) {
            return EventResult.PASS;
        }

        RateTracker tracker = trackers.computeIfAbsent(player, k -> new RateTracker());
        if (tracker.recordAndCheck(maxChangesPerSecond)) {
            return EventResult.PASS;
        }

        // Rate exceeded — warn (throttled to once per 3 seconds)
        long now = System.currentTimeMillis();
        if (now - tracker.lastWarning >= 3_000) {
            tracker.lastWarning = now;
            System.out.println("[GriefProtection] Rate limited " + player
                    + " (>" + maxChangesPerSecond + " blocks/sec)");
            if (playerManager != null) {
                ConnectedPlayer cp = playerManager.getPlayerByName(player);
                if (cp != null) {
                    playerManager.sendChat(cp,
                            "You are placing/breaking blocks too fast!");
                }
            }
        }
        return EventResult.CANCEL;
    }

    /**
     * Sliding window rate tracker using a simple token bucket approach.
     * Allows up to maxPerSecond changes, refilling at a steady rate.
     */
    static final class RateTracker {
        private long lastRefillTime;
        private double tokens;
        volatile long lastWarning;

        RateTracker() {
            this.lastRefillTime = System.currentTimeMillis();
            this.tokens = maxChangesPerSecond;
        }

        /**
         * Record a block change attempt. Returns true if allowed.
         */
        synchronized boolean recordAndCheck(int maxPerSecond) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            lastRefillTime = now;

            // Refill tokens based on elapsed time
            tokens = Math.min(maxPerSecond, tokens + (elapsed * maxPerSecond / 1000.0));

            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }
}
