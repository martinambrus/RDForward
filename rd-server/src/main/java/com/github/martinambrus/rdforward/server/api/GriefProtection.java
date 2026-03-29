package com.github.martinambrus.rdforward.server.api;

import com.github.martinambrus.rdforward.protocol.event.EventResult;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.server.event.ServerEvents;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Smart grief detection and prevention system.
 *
 * <h3>How it works</h3>
 * <p>Tracks block ownership (who placed each block) and scores players based
 * on behavioral patterns rather than simple rate limits. This allows fast
 * builders to work unimpeded while catching griefers who destroy other
 * players' builds.
 *
 * <h3>Scoring</h3>
 * <ul>
 *   <li>Breaking a block placed by another player: <b>+1 grief point</b></li>
 *   <li>Breaking natural/unplaced blocks (mining): <b>0 points</b></li>
 *   <li>Breaking your own blocks: <b>0 points</b></li>
 *   <li>New players (&lt;30 min session time): <b>2x grief points</b></li>
 *   <li>Points decay: halved every 60 seconds of no grief activity</li>
 * </ul>
 *
 * <h3>Escalating responses</h3>
 * <ul>
 *   <li>Score 5: Warning message</li>
 *   <li>Score 10: Block interactions frozen for 5 seconds</li>
 *   <li>Score 20: Kicked from the server</li>
 * </ul>
 *
 * <h3>Mod bypass API</h3>
 * Mods performing bulk block operations (WorldEdit, arena resets, etc.) can
 * bypass all grief checks using the thread-local bypass flag:
 * <pre>
 *   GriefProtection.runBypassed(() -> {
 *       // bulk block changes — no grief scoring or ownership tracking
 *   });
 * </pre>
 *
 * Or manually:
 * <pre>
 *   GriefProtection.setBypassed(true);
 *   try {
 *       // bulk block changes
 *   } finally {
 *       GriefProtection.setBypassed(false);
 *   }
 * </pre>
 */
public final class GriefProtection {

    private GriefProtection() {}

    // ========================================================================
    // Thresholds
    // ========================================================================

    /** Grief score at which the player receives a warning. */
    private static final double THRESHOLD_WARN = 5.0;
    /** Grief score at which the player's block interactions are frozen. */
    private static final double THRESHOLD_FREEZE = 10.0;
    /** Grief score at which the player is kicked. */
    private static final double THRESHOLD_KICK = 20.0;
    /** Duration of interaction freeze in milliseconds. */
    private static final long FREEZE_DURATION_MS = 5_000;
    /** Escalating tempban durations: 1st=30m, 2nd=90m, 3rd+=24h. */
    private static final long[] TEMPBAN_DURATIONS_MS = {
        30 * 60 * 1000L,   // 30 minutes
        90 * 60 * 1000L,   // 90 minutes
        24 * 60 * 60 * 1000L // 24 hours
    };
    /** Score half-life: grief points halve after this many ms of no grief. */
    private static final long SCORE_HALFLIFE_MS = 60_000;
    /** Session age (ms) below which a player is considered "new" (30 minutes). */
    private static final long NEW_PLAYER_THRESHOLD_MS = 30 * 60 * 1000;
    /** Grief point multiplier for new players. */
    private static final double NEW_PLAYER_MULTIPLIER = 2.0;
    /** Rate limit: max block changes per second (token bucket, applies to all players). */
    private static volatile int maxChangesPerSecond = 17;
    /**
     * Maximum reach distance for block interactions (blocks).
     * Creative mode reach is ~5 blocks; we add tolerance for latency.
     */
    private static final double MAX_REACH_DISTANCE = 7.0;
    /** Extra grief score for out-of-reach block breaks (suspicious behavior). */
    private static final double OUT_OF_REACH_GRIEF_BONUS = 2.0;

    // ========================================================================
    // Bypass API
    // ========================================================================

    private static final ThreadLocal<Boolean> BYPASSED = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    /** Set the bypass flag for the current thread. Always reset in a finally block. */
    public static void setBypassed(boolean bypassed) {
        BYPASSED.set(bypassed);
    }

    /** Check if the current thread has grief protection bypassed. */
    public static boolean isBypassed() {
        return BYPASSED.get();
    }

    /** Run an action with grief protection bypassed. */
    public static void runBypassed(Runnable action) {
        BYPASSED.set(true);
        try {
            action.run();
        } finally {
            BYPASSED.set(false);
        }
    }

    // ========================================================================
    // State
    // ========================================================================

    /**
     * Block ownership map: packed position → player name who placed it.
     * Blocks not in this map are considered natural/unplaced.
     */
    private static final ConcurrentHashMap<Long, String> blockOwners = new ConcurrentHashMap<>();

    /** Per-player grief tracking data (cleared on disconnect). */
    private static final ConcurrentHashMap<String, PlayerGriefData> playerData = new ConcurrentHashMap<>();

    /**
     * Offense history: tracks how many times a player has hit the kick threshold.
     * Persists across sessions (until server restart) for escalating penalties.
     * Maps lowercase player name → offense count.
     */
    private static final ConcurrentHashMap<String, Integer> offenseHistory = new ConcurrentHashMap<>();

    private static volatile PlayerManager playerManager;
    private static volatile ServerWorld serverWorld;
    private static volatile boolean enabled = false;

    // ========================================================================
    // Initialization
    // ========================================================================

    /**
     * Initialize and register grief protection on block events.
     *
     * @param maxPerSecond max block changes per second per player (0 = disable rate limit only)
     * @param pm           the player manager
     * @param world        the server world
     */
    public static void init(int maxPerSecond, PlayerManager pm, ServerWorld world) {
        playerManager = pm;
        serverWorld = world;
        maxChangesPerSecond = maxPerSecond;
        enabled = true;

        // Track block ownership on placement
        ServerEvents.BLOCK_PLACE.register((player, x, y, z, blockType) -> {
            if (BYPASSED.get()) return EventResult.PASS;

            // Reach distance check (non-OPs only)
            if (PermissionManager.getOpLevel(player) < PermissionManager.OP_BYPASS_SPAWN) {
                EventResult reachResult = checkReach(player, x, y, z, false);
                if (reachResult == EventResult.CANCEL) return reachResult;
            }

            // Record ownership
            blockOwners.put(packPosition(x, y, z), player);

            // Rate limit check (applies to all non-OP players)
            return checkRateLimit(player);
        });

        // Check grief score on block breaking
        ServerEvents.BLOCK_BREAK.register((player, x, y, z, blockType) -> {
            if (BYPASSED.get()) return EventResult.PASS;

            // Rate limit check first
            EventResult rateResult = checkRateLimit(player);
            if (rateResult == EventResult.CANCEL) return rateResult;

            // Check grief score for non-OPs
            if (PermissionManager.getOpLevel(player) >= PermissionManager.OP_BYPASS_SPAWN) {
                // OPs: remove ownership but skip grief and reach checks
                blockOwners.remove(packPosition(x, y, z));
                return EventResult.PASS;
            }

            // Reach distance check
            EventResult reachResult = checkReach(player, x, y, z, true);
            if (reachResult == EventResult.CANCEL) return reachResult;

            return checkGriefScore(player, x, y, z);
        });

        // Clean up when players leave
        ServerEvents.PLAYER_LEAVE.register(name -> {
            playerData.remove(name);
            // Note: block ownership persists — a player's blocks are still
            // tracked even after they leave (until server restart)
        });

        System.out.println("Grief protection enabled"
                + (maxPerSecond > 0 ? " (rate limit: " + maxPerSecond + " blocks/sec)" : "")
                + " — behavioral scoring active");
    }

    /**
     * Backward-compatible init without world parameter.
     */
    public static void init(int maxPerSecond, PlayerManager pm) {
        init(maxPerSecond, pm, null);
    }

    // ========================================================================
    // Rate limiting (token bucket — applies to all non-OP players)
    // ========================================================================

    private static EventResult checkRateLimit(String player) {
        if (maxChangesPerSecond <= 0) return EventResult.PASS;

        // OPs bypass rate limiting
        if (PermissionManager.getOpLevel(player) >= PermissionManager.OP_BYPASS_SPAWN) {
            return EventResult.PASS;
        }

        PlayerGriefData data = getOrCreateData(player);

        // Check freeze
        if (data.isFrozen()) {
            return EventResult.CANCEL;
        }

        if (data.rateTracker.recordAndCheck(maxChangesPerSecond)) {
            return EventResult.PASS;
        }

        // Rate exceeded
        long now = System.currentTimeMillis();
        if (now - data.lastRateWarning >= 3_000) {
            data.lastRateWarning = now;
            warn(player, "You are placing/breaking blocks too fast!");
        }
        return EventResult.CANCEL;
    }

    // ========================================================================
    // Reach distance check
    // ========================================================================

    /**
     * Check if a player can reach the target block. Out-of-reach interactions
     * are always blocked and boost the player's grief score.
     *
     * @param isBreak true if breaking (adds grief bonus), false if placing
     */
    private static EventResult checkReach(String player, int x, int y, int z, boolean isBreak) {
        if (playerManager == null) return EventResult.PASS;
        ConnectedPlayer cp = playerManager.getPlayerByName(player);
        if (cp == null) return EventResult.PASS;

        double px = cp.getDoubleX();
        double py = cp.getDoubleY();
        double pz = cp.getDoubleZ();

        // If position is 0,0,0 the player may not have sent a position update yet
        if (px == 0 && py == 0 && pz == 0) return EventResult.PASS;

        // Distance from player eyes to center of target block
        double eyeY = py + 1.62; // eye height
        double dx = (x + 0.5) - px;
        double dy = (y + 0.5) - eyeY;
        double dz = (z + 0.5) - pz;
        double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq <= MAX_REACH_DISTANCE * MAX_REACH_DISTANCE) {
            return EventResult.PASS;
        }

        // Out of reach — block the action and boost grief score
        double dist = Math.sqrt(distSq);
        System.out.println("[GriefProtection] " + player + " tried to "
                + (isBreak ? "break" : "place") + " block at (" + x + ", " + y + ", " + z
                + ") from distance " + String.format("%.1f", dist)
                + " (max " + MAX_REACH_DISTANCE + ")");

        if (isBreak) {
            // Boost grief score for suspicious out-of-reach breaking
            PlayerGriefData data = getOrCreateData(player);
            data.decayScore();
            data.griefScore += OUT_OF_REACH_GRIEF_BONUS;
            data.lastGriefTime = System.currentTimeMillis();
        }

        warn(player, "You cannot interact with blocks that far away.");
        return EventResult.CANCEL;
    }

    // ========================================================================
    // Grief scoring (behavioral detection)
    // ========================================================================

    private static EventResult checkGriefScore(String player, int x, int y, int z) {
        long packedPos = packPosition(x, y, z);
        String owner = blockOwners.get(packedPos);

        // Natural/unplaced block or own block — no grief points
        if (owner == null || owner.equals(player)) {
            // Remove ownership on break
            if (owner != null) blockOwners.remove(packedPos);
            return EventResult.PASS;
        }

        // Breaking another player's block — grief!
        blockOwners.remove(packedPos);

        PlayerGriefData data = getOrCreateData(player);

        // Check freeze (from a previous escalation)
        if (data.isFrozen()) {
            return EventResult.CANCEL;
        }

        // Decay existing score based on time since last grief
        data.decayScore();

        // Add grief points (new players get double)
        double points = 1.0;
        if (data.isNewPlayer()) {
            points *= NEW_PLAYER_MULTIPLIER;
        }
        data.griefScore += points;
        data.lastGriefTime = System.currentTimeMillis();

        System.out.println("[GriefProtection] " + player + " broke " + owner
                + "'s block at (" + x + ", " + y + ", " + z
                + ") — score: " + String.format("%.1f", data.griefScore));

        // Escalating responses
        if (data.griefScore >= THRESHOLD_KICK) {
            String key = player.toLowerCase();
            int priorOffenses = offenseHistory.getOrDefault(key, 0);
            offenseHistory.put(key, priorOffenses + 1);

            if (priorOffenses == 0) {
                // First offense: kick only
                System.out.println("[GriefProtection] Kicking " + player
                        + " (grief score " + String.format("%.1f", data.griefScore)
                        + ", 1st offense)");
                if (playerManager != null && serverWorld != null) {
                    playerManager.kickPlayer(player,
                            "Excessive griefing detected", serverWorld);
                }
            } else {
                // Repeat offense: escalating tempban
                int banIndex = Math.min(priorOffenses - 1, TEMPBAN_DURATIONS_MS.length - 1);
                long banDuration = TEMPBAN_DURATIONS_MS[banIndex];
                String banDurationStr = BanManager.formatDuration(banDuration);
                BanManager.tempBanPlayer(player, banDuration);
                System.out.println("[GriefProtection] Temp-banned " + player
                        + " for " + banDurationStr + " (grief score "
                        + String.format("%.1f", data.griefScore)
                        + ", offense #" + (priorOffenses + 1) + ")");
                if (playerManager != null && serverWorld != null) {
                    playerManager.kickPlayer(player,
                            "Temporarily banned for griefing (" + banDurationStr + ")",
                            serverWorld);
                }
            }
            return EventResult.CANCEL;

        } else if (data.griefScore >= THRESHOLD_FREEZE && !data.isFrozen()) {
            data.freezeUntil = System.currentTimeMillis() + FREEZE_DURATION_MS;
            System.out.println("[GriefProtection] Froze " + player
                    + " for " + (FREEZE_DURATION_MS / 1000) + "s (grief score "
                    + String.format("%.1f", data.griefScore) + ")");
            warn(player, "Block interactions frozen for "
                    + (FREEZE_DURATION_MS / 1000)
                    + " seconds — stop destroying other players' builds!");
            return EventResult.CANCEL;

        } else if (data.griefScore >= THRESHOLD_WARN && !data.hasBeenWarned) {
            data.hasBeenWarned = true;
            warn(player, "Warning: do not destroy other players' builds. "
                    + "Continued griefing will result in a kick.");
            return EventResult.PASS; // Allow this one but warn
        }

        return EventResult.PASS;
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private static PlayerGriefData getOrCreateData(String player) {
        return playerData.computeIfAbsent(player, k -> new PlayerGriefData());
    }

    private static void warn(String player, String message) {
        if (playerManager == null) return;
        ConnectedPlayer cp = playerManager.getPlayerByName(player);
        if (cp != null) {
            playerManager.sendChat(cp, "[GriefProtection] " + message);
        }
    }

    /**
     * Pack (x, y, z) into a single long key.
     * Supports coordinates up to ±2 million (more than enough for our world sizes).
     * Layout: x in bits 42-63, z in bits 21-41, y in bits 0-20 (all 21-bit signed).
     */
    static long packPosition(int x, int y, int z) {
        return ((long)(x & 0x1FFFFF) << 42) | ((long)(z & 0x1FFFFF) << 21) | (y & 0x1FFFFF);
    }

    // ========================================================================
    // Per-player tracking data
    // ========================================================================

    static final class PlayerGriefData {
        /** Current grief score (decays over time). */
        volatile double griefScore = 0.0;
        /** Timestamp of last grief action (for decay calculation). */
        volatile long lastGriefTime = 0;
        /** Timestamp until which block interactions are frozen. */
        volatile long freezeUntil = 0;
        /** Whether the warning threshold message has been sent. */
        volatile boolean hasBeenWarned = false;
        /** Timestamp of last rate-limit warning (throttle to once per 3s). */
        volatile long lastRateWarning = 0;
        /** When this player's session started (for new-player detection). */
        final long sessionStart = System.currentTimeMillis();
        /** Token bucket for rate limiting. */
        final RateTracker rateTracker = new RateTracker();

        /** Check if the player is currently frozen. Automatically unfreezes. */
        boolean isFrozen() {
            long freeze = freezeUntil;
            if (freeze == 0) return false;
            if (System.currentTimeMillis() >= freeze) {
                freezeUntil = 0;
                return false;
            }
            return true;
        }

        /** Whether this player is considered "new" (session < 30 min). */
        boolean isNewPlayer() {
            return (System.currentTimeMillis() - sessionStart) < NEW_PLAYER_THRESHOLD_MS;
        }

        /** Apply time-based decay to the grief score. */
        void decayScore() {
            if (griefScore <= 0 || lastGriefTime == 0) return;
            long elapsed = System.currentTimeMillis() - lastGriefTime;
            if (elapsed <= 0) return;
            // Exponential decay: halve every SCORE_HALFLIFE_MS
            double decayFactor = Math.pow(0.5, (double) elapsed / SCORE_HALFLIFE_MS);
            griefScore *= decayFactor;
            // Floor very small values to zero
            if (griefScore < 0.1) griefScore = 0;
        }
    }

    /**
     * Token bucket rate tracker. Allows up to maxPerSecond changes,
     * refilling at a steady rate.
     */
    static final class RateTracker {
        private long lastRefillTime = System.currentTimeMillis();
        private double tokens = maxChangesPerSecond;

        synchronized boolean recordAndCheck(int maxPerSecond) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            lastRefillTime = now;
            tokens = Math.min(maxPerSecond, tokens + (elapsed * maxPerSecond / 1000.0));
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }
}
