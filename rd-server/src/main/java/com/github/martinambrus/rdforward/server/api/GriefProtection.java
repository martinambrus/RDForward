package com.github.martinambrus.rdforward.server.api;

import com.github.martinambrus.rdforward.protocol.event.EventResult;
import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.DebugLog;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.server.event.ServerEvents;

import java.util.ArrayDeque;
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
 *   <li>Breaking a trusted teammate's blocks: <b>0 points</b> (see {@link TeamManager})</li>
 *   <li>New players (&lt;30 min session time): <b>2x grief points</b></li>
 *   <li>Points decay: halved every 60 seconds of no grief activity</li>
 * </ul>
 *
 * <h3>Escalating responses</h3>
 * <ul>
 *   <li>Score 5: Warning message (mentions /griefinfo)</li>
 *   <li>Score 10: Kicked from the server</li>
 *   <li>Score 20: Temporarily banned (escalating: 30m / 90m / 24h)</li>
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

    /** Grief score at which the player receives a warning. Configurable via grief-threshold-warn. */
    private static double THRESHOLD_WARN = 5.0;
    /** Grief score at which the player is kicked. Configurable via grief-threshold-kick. */
    private static double THRESHOLD_KICK = 10.0;
    /** Grief score at which the player is temporarily banned. Configurable via grief-threshold-tempban. */
    private static double THRESHOLD_TEMPBAN = 20.0;
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
    private static volatile ChunkManager chunkManager;
    private static volatile boolean enabled = false;

    // ========================================================================
    // Initialization
    // ========================================================================

    /**
     * Initialize and register grief protection on block events.
     *
     * @param maxPerSecond max block changes per second per player (0 = disable rate limit only)
     * @param pm           the player manager
     * @param world        the server world (finite worlds)
     * @param cm           the chunk manager (Alpha worlds, may be null for Classic-only)
     */
    public static void init(int maxPerSecond, PlayerManager pm, ServerWorld world, ChunkManager cm) {
        playerManager = pm;
        serverWorld = world;
        chunkManager = cm;
        maxChangesPerSecond = maxPerSecond;
        enabled = true;

        // Load configurable thresholds
        try {
            THRESHOLD_WARN = ServerProperties.getGriefThresholdWarn();
            THRESHOLD_KICK = ServerProperties.getGriefThresholdKick();
            THRESHOLD_TEMPBAN = ServerProperties.getGriefThresholdTempban();
        } catch (Exception e) {
            // Keep defaults if properties not loaded
        }

        // Track block ownership on placement
        ServerEvents.BLOCK_PLACE.register((player, x, y, z, blockType) -> {
            if (!enabled || BYPASSED.get()) return EventResult.PASS;

            boolean isOp = PermissionManager.getOpLevel(player) >= PermissionManager.OP_BYPASS_SPAWN;

            // Reach distance check (non-OPs only)
            if (!isOp) {
                EventResult reachResult = checkReach(player, x, y, z, false);
                if (reachResult == EventResult.CANCEL) {
                    if (DebugLog.blocks() && DebugLog.forPlayer(player)) {
                        DebugLog.log(DebugLog.BLOCK, player + " PLACE DENIED reach (" + x + "," + y + "," + z + ")");
                    }
                    return reachResult;
                }
            }

            // Rate limit check FIRST (before consuming budget) so a rate-limited
            // placement doesn't permanently leak a protection slot.
            if (!isOp) {
                EventResult rateResult = checkRateLimit(player);
                if (rateResult == EventResult.CANCEL) {
                    if (DebugLog.blocks() && DebugLog.forPlayer(player)) {
                        DebugLog.log(DebugLog.BLOCK, player + " PLACE DENIED rateLimit (" + x + "," + y + "," + z + ")");
                    }
                    return rateResult;
                }
            }

            // Capture old state for rollback
            byte oldBlockType = getBlockType(x, y, z);
            short oldOwnerId = getBlockOwnerId(x, y, z);

            // Record ownership if player has remaining protection budget
            // OPs always get protected blocks (no budget limit)
            short newOwnerId = 0;
            if (isOp || BlockOwnerRegistry.useProtectionSlot(player)) {
                short ownerId = BlockOwnerRegistry.getOrCreateId(player);
                if (ownerId > 0) {
                    // Return old owner's slot if overwriting ANY protected block
                    // (including own blocks — prevents budget leak on self-overwrite)
                    if (oldOwnerId > 0) {
                        BlockOwnerRegistry.returnProtectionSlot(oldOwnerId);
                    }
                    setBlockOwner(x, y, z, ownerId);
                    newOwnerId = ownerId;
                }
                if (!isOp) {
                    checkBudgetWarning(player);
                }
            } else {
                notifyBudgetDepleted(player);
            }

            if (DebugLog.blocks() && DebugLog.forPlayer(player)) {
                String oldOwner = oldOwnerId > 0 ? BlockOwnerRegistry.getPlayerName(oldOwnerId) : "none";
                String newOwner = newOwnerId > 0 ? BlockOwnerRegistry.getPlayerName(newOwnerId) : "unprotected";
                DebugLog.log(DebugLog.BLOCK, player + " PLACE (" + x + "," + y + "," + z
                        + ") old=" + (oldBlockType & 0xFF) + "/" + oldOwner
                        + " new_owner=" + newOwner);
            }

            // Record for rollback (non-OPs only)
            if (!isOp) {
                recordBlockChange(player, x, y, z, oldBlockType, oldOwnerId, newOwnerId);
            }

            return EventResult.PASS;
        });

        // Check grief score on block breaking
        ServerEvents.BLOCK_BREAK.register((player, x, y, z, blockType) -> {
            if (!enabled || BYPASSED.get()) return EventResult.PASS;

            // Rate limit check first
            EventResult rateResult = checkRateLimit(player);
            if (rateResult == EventResult.CANCEL) return rateResult;

            // OPs: remove ownership and return budget, skip grief/reach checks
            if (PermissionManager.getOpLevel(player) >= PermissionManager.OP_BYPASS_SPAWN) {
                short ownerId = getBlockOwnerId(x, y, z);
                if (ownerId > 0) {
                    BlockOwnerRegistry.returnProtectionSlot(ownerId);
                    clearBlockOwner(x, y, z);
                }
                return EventResult.PASS;
            }

            // Reach distance check
            EventResult reachResult = checkReach(player, x, y, z, true);
            if (reachResult == EventResult.CANCEL) {
                if (DebugLog.blocks() && DebugLog.forPlayer(player)) {
                    DebugLog.log(DebugLog.BLOCK, player + " BREAK DENIED reach (" + x + "," + y + "," + z + ")");
                }
                return reachResult;
            }

            // Capture old state BEFORE checkGriefScore modifies ownership
            byte oldBlockType = getBlockType(x, y, z);
            short oldOwnerId = getBlockOwnerId(x, y, z);

            EventResult result = checkGriefScore(player, x, y, z);

            if (DebugLog.blocks() && DebugLog.forPlayer(player)) {
                String ownerName = oldOwnerId > 0 ? BlockOwnerRegistry.getPlayerName(oldOwnerId) : "none";
                DebugLog.log(DebugLog.BLOCK, player + " BREAK (" + x + "," + y + "," + z
                        + ") type=" + (oldBlockType & 0xFF) + " owner=" + ownerName
                        + " result=" + result);
            }

            // Record for rollback only if the block will actually be broken (PASS)
            if (result == EventResult.PASS) {
                recordBlockChange(player, x, y, z, oldBlockType, oldOwnerId, (short) 0);
            }

            return result;
        });

        // Clean up when players leave
        // Note: playerData is NOT cleared on disconnect. Grief scores
        // persist across reconnects to prevent the logout/login bypass.
        // The score decays naturally via the half-life mechanism.

        System.out.println("Grief protection enabled"
                + (maxPerSecond > 0 ? " (rate limit: " + maxPerSecond + " blocks/sec)" : "")
                + " — persistent ownership tracking active");
    }

    /**
     * Backward-compatible init without chunk manager parameter.
     */
    public static void init(int maxPerSecond, PlayerManager pm, ServerWorld world) {
        init(maxPerSecond, pm, world, null);
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
     * are silently blocked but do NOT add grief score — protocol quirks
     * (e.g. Alpha cancel-digging at 0,0,0) can trigger false positives.
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

        // Out of reach — block silently (no grief score, no warning spam)
        return EventResult.CANCEL;
    }

    // ========================================================================
    // Grief scoring (behavioral detection)
    // ========================================================================

    private static EventResult checkGriefScore(String player, int x, int y, int z) {
        short ownerId = getBlockOwnerId(x, y, z);

        // Natural/unplaced block — no grief points
        if (ownerId == 0) return EventResult.PASS;

        // Expired ownership — treat as unowned, clear it, no budget return
        if (BlockOwnerRegistry.isExpired(ownerId)) {
            clearBlockOwner(x, y, z);
            return EventResult.PASS;
        }

        String owner = BlockOwnerRegistry.getPlayerName(ownerId);

        // Unknown owner, own block, or trusted teammate — no grief points
        if (owner == null || owner.equals(player.toLowerCase())
                || TeamManager.isTeammate(owner, player)) {
            BlockOwnerRegistry.returnProtectionSlot(ownerId);
            clearBlockOwner(x, y, z);
            return EventResult.PASS;
        }

        // Breaking another player's block — grief!
        // Do NOT clear ownership yet — if we escalate to kick/ban with rollback,
        // the ownership needs to stay in place. Only clear if we allow the break.

        PlayerGriefData data = getOrCreateData(player);

        // Check freeze (from a previous escalation)
        if (data.isFrozen()) {
            return EventResult.CANCEL;
        }

        // Decay existing score based on time since last grief
        data.decayScore();

        // Add grief points (new players get double)
        double points = 1.0;
        if (data.isNewPlayer(player)) {
            points *= NEW_PLAYER_MULTIPLIER;
        }
        data.griefScore += points;
        data.lastGriefTime = System.currentTimeMillis();

        System.out.println("[GriefProtection] " + player + " broke " + owner
                + "'s block at (" + x + ", " + y + ", " + z
                + ") — score: " + String.format("%.1f", data.griefScore));

        // Escalating responses
        if (data.griefScore >= THRESHOLD_TEMPBAN) {
            // Ownership stays — rollback restores the rest
            rollbackChanges(player);

            String key = player.toLowerCase();
            int priorOffenses = offenseHistory.getOrDefault(key, 0);
            offenseHistory.put(key, priorOffenses + 1);

            int banIndex = Math.min(priorOffenses, TEMPBAN_DURATIONS_MS.length - 1);
            long banDuration = TEMPBAN_DURATIONS_MS[banIndex];
            String banDurationStr = BanManager.formatDuration(banDuration);
            BanManager.tempBanPlayer(player, banDuration);
            data.wasGriefKicked = true;
            System.out.println("[GriefProtection] Temp-banned " + player
                    + " for " + banDurationStr + " (grief score "
                    + String.format("%.1f", data.griefScore)
                    + ", offense #" + (priorOffenses + 1) + ")");
            if (playerManager != null && serverWorld != null) {
                playerManager.kickPlayer(player,
                        "Kicked for breaking " + owner + "'s blocks (grief score: "
                        + String.format("%.1f", data.griefScore) + "). "
                        + "Banned for " + banDurationStr + ". Use /griefinfo after rejoining.",
                        serverWorld);
            }
            return EventResult.CANCEL;

        } else if (data.griefScore >= THRESHOLD_KICK && !data.hasBeenKicked) {
            // Ownership stays — rollback restores the rest
            rollbackChanges(player);

            data.hasBeenKicked = true;
            data.wasGriefKicked = true;
            System.out.println("[GriefProtection] Kicking " + player
                    + " (grief score " + String.format("%.1f", data.griefScore) + ")");
            if (playerManager != null && serverWorld != null) {
                playerManager.kickPlayer(player,
                        "Kicked for breaking " + owner + "'s blocks (grief score: "
                        + String.format("%.1f", data.griefScore) + "). "
                        + "Use /griefinfo to learn about block protection.",
                        serverWorld);
            }
            return EventResult.CANCEL;

        } else if (data.griefScore >= THRESHOLD_WARN && !data.hasBeenWarned) {
            data.hasBeenWarned = true;
            // Allow the break but warn
            BlockOwnerRegistry.returnProtectionSlot(ownerId);
            clearBlockOwner(x, y, z);
            warn(player, "That block belongs to " + owner
                    + ". Breaking others' blocks will result in a kick. Use /griefinfo for details.");
            return EventResult.PASS;

        } else if (!data.hasBeenNotifiedFirstGrief) {
            // First grief block — subtle notification before any threshold
            data.hasBeenNotifiedFirstGrief = true;
            BlockOwnerRegistry.returnProtectionSlot(ownerId);
            clearBlockOwner(x, y, z);
            warn(player, "That block belongs to " + owner
                    + ". Breaking others' blocks gives grief points.");
            return EventResult.PASS;
        }

        // Below all thresholds — allow the break, return budget to victim
        BlockOwnerRegistry.returnProtectionSlot(ownerId);
        clearBlockOwner(x, y, z);
        return EventResult.PASS;
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private static PlayerGriefData getOrCreateData(String player) {
        return playerData.computeIfAbsent(player.toLowerCase(), k -> new PlayerGriefData());
    }

    /**
     * Evict stale grief data entries to prevent unbounded memory growth.
     * Removes entries with zero grief score that haven't been active for 10+ minutes.
     * Called periodically from the tick loop.
     */
    public static void evictStaleEntries() {
        long now = System.currentTimeMillis();
        playerData.entrySet().removeIf(entry -> {
            PlayerGriefData data = entry.getValue();
            data.decayScore();
            return data.griefScore == 0
                    && !data.wasGriefKicked
                    && (now - data.lastGriefTime) > 600_000
                    && data.recentChanges.isEmpty();
        });
    }

    /**
     * Check if a player was recently kicked or banned by grief protection.
     * Clears the flag after reading (one-shot).
     */
    public static boolean wasGriefKicked(String player) {
        PlayerGriefData data = playerData.get(player.toLowerCase());
        if (data == null) return false;
        boolean was = data.wasGriefKicked;
        data.wasGriefKicked = false;
        return was;
    }

    /** Warn a player about low protection budget (>= 80% used). Throttled to once per 30s. */
    private static void checkBudgetWarning(String player) {
        int total = BlockOwnerRegistry.getTotalBudget(player);
        if (total <= 0) return;
        int remaining = BlockOwnerRegistry.getRemainingBudget(player);
        // Only warn when remaining drops to 20% or below
        if (remaining * 5 > total) return; // remaining > 20% of total

        PlayerGriefData data = getOrCreateData(player);
        long now = System.currentTimeMillis();
        if (now - data.lastBudgetWarning < 30_000) return;
        data.lastBudgetWarning = now;

        int pct = (total > 0) ? (remaining * 100 / total) : 0;
        warn(player, "Low protection budget: " + pct + "% ("
                + remaining + " blocks) remaining.");
    }

    /** Notify a player that their protection budget is fully depleted. Throttled to once per 30s. */
    private static void notifyBudgetDepleted(String player) {
        PlayerGriefData data = getOrCreateData(player);
        long now = System.currentTimeMillis();
        if (now - data.lastBudgetWarning < 30_000) return;
        data.lastBudgetWarning = now;

        // Calculate how many blocks the next 10 minutes of play will earn
        int nextAccrual = BlockOwnerRegistry.ACCRUAL_PER_HOUR / 6; // per 10-min interval
        warn(player, "Protection budget depleted! Blocks you place now are"
                + " unprotected. You will earn " + nextAccrual
                + " more protected blocks for every 10 minutes of play.");
    }

    private static void warn(String player, String message) {
        if (playerManager == null) return;
        ConnectedPlayer cp = playerManager.getPlayerByName(player);
        if (cp != null) {
            playerManager.sendChat(cp, "[GriefProtection] " + message);
        }
    }

    /**
     * Get the block type at world coordinates from the appropriate storage.
     */
    private static byte getBlockType(int x, int y, int z) {
        if (serverWorld != null && serverWorld.inBounds(x, y, z)) {
            return serverWorld.getBlock(x, y, z);
        }
        if (chunkManager != null) {
            return chunkManager.getBlock(x, y, z);
        }
        return 0;
    }

    /**
     * Set a block type at world coordinates in the appropriate storage.
     * For finite worlds, queues the change for tick-loop processing.
     * For Alpha worlds, sets directly (marks chunk dirty for resend).
     */
    private static void restoreBlock(int x, int y, int z, byte blockType) {
        if (serverWorld != null && serverWorld.inBounds(x, y, z)) {
            serverWorld.queueBlockChange(x, y, z, blockType);
        } else if (chunkManager != null) {
            chunkManager.setBlock(x, y, z, blockType);
        }
    }

    /**
     * Record a block change for potential rollback.
     * Keeps a rolling buffer of the last {@link #MAX_RECENT_CHANGES} changes.
     */
    private static void recordBlockChange(String player, int x, int y, int z,
                                           byte oldBlockType, short oldOwnerId, short newOwnerId) {
        PlayerGriefData data = getOrCreateData(player);
        synchronized (data) {
            if (data.recentChanges.size() >= MAX_RECENT_CHANGES) {
                data.recentChanges.pollFirst(); // drop oldest
            }
            data.recentChanges.addLast(new BlockChangeRecord(x, y, z, oldBlockType, oldOwnerId, newOwnerId));
        }
    }

    /**
     * Roll back all recorded block changes for a player.
     * Called when a player is kicked or temp-banned for griefing.
     * Restores original blocks and ownership, fixes protection budgets.
     */
    private static void rollbackChanges(String player) {
        PlayerGriefData data = playerData.get(player.toLowerCase());
        if (data == null) return;

        int count = 0;
        synchronized (data) {
        while (!data.recentChanges.isEmpty()) {
            BlockChangeRecord record = data.recentChanges.pollLast();

            // Restore original block type
            restoreBlock(record.x, record.y, record.z, record.oldBlockType);

            // Restore original ownership
            setBlockOwner(record.x, record.y, record.z, record.oldOwnerId);

            // Fix budget: return the new owner's slot (griefer's placement, or 0 for breaks)
            if (record.newOwnerId > 0) {
                BlockOwnerRegistry.returnProtectionSlot(record.newOwnerId);
            }

            // Fix budget: re-consume the old owner's slot (was returned when their block was broken)
            if (record.oldOwnerId > 0) {
                BlockOwnerRegistry.restoreUsedSlot(record.oldOwnerId);
            }

            count++;
        }
        } // synchronized

        System.out.println("[GriefProtection] Rolled back " + count + " block change(s) by " + player);
    }

    /**
     * Get the owner ID of a block from the appropriate storage.
     * Tries the finite world first (if in bounds), then the chunk manager.
     */
    private static short getBlockOwnerId(int x, int y, int z) {
        if (serverWorld != null && serverWorld.inBounds(x, y, z)) {
            return serverWorld.getBlockOwnerId(x, y, z);
        }
        if (chunkManager != null) {
            return chunkManager.getBlockOwnerId(x, y, z);
        }
        return 0;
    }

    /**
     * Set the owner ID of a block in the appropriate storage.
     */
    private static void setBlockOwner(int x, int y, int z, short ownerId) {
        if (serverWorld != null && serverWorld.inBounds(x, y, z)) {
            serverWorld.setBlockOwnerId(x, y, z, ownerId);
        } else if (chunkManager != null) {
            chunkManager.setBlockOwnerId(x, y, z, ownerId);
        }
    }

    /**
     * Clear the owner ID of a block in the appropriate storage.
     */
    private static void clearBlockOwner(int x, int y, int z) {
        setBlockOwner(x, y, z, (short) 0);
    }

    // ========================================================================
    // Per-player tracking data
    // ========================================================================

    /** A recorded block change for rollback purposes. */
    static final class BlockChangeRecord {
        final int x, y, z;
        final byte oldBlockType;
        final short oldOwnerId;
        final short newOwnerId;

        BlockChangeRecord(int x, int y, int z, byte oldBlockType, short oldOwnerId, short newOwnerId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.oldBlockType = oldBlockType;
            this.oldOwnerId = oldOwnerId;
            this.newOwnerId = newOwnerId;
        }
    }

    /** Maximum number of recent block changes to track per player for rollback. */
    private static final int MAX_RECENT_CHANGES = 50;

    static final class PlayerGriefData {
        /** Current grief score (decays over time). */
        volatile double griefScore = 0.0;
        /** Timestamp of last grief action (for decay calculation). */
        volatile long lastGriefTime = 0;
        /** Timestamp until which block interactions are frozen. */
        volatile long freezeUntil = 0;
        /** Whether the warning threshold message has been sent. */
        volatile boolean hasBeenWarned = false;
        /** Whether the player has been kicked this session (prevent re-kick on reconnect decay). */
        volatile boolean hasBeenKicked = false;
        /** Whether the player was kicked/banned by grief protection (for RTP hint on rejoin). */
        volatile boolean wasGriefKicked = false;
        /** Whether the first-grief-block notification has been sent. */
        volatile boolean hasBeenNotifiedFirstGrief = false;
        /** Timestamp of last rate-limit warning (throttle to once per 3s). */
        volatile long lastRateWarning = 0;
        /** Timestamp of last budget warning (throttle to once per 30s). */
        volatile long lastBudgetWarning = 0;
        /** Token bucket for rate limiting. */
        final RateTracker rateTracker = new RateTracker();
        /** Rolling buffer of recent block changes for rollback on kick/ban. */
        final ArrayDeque<BlockChangeRecord> recentChanges = new ArrayDeque<>();

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

        /** Whether this player is considered "new" (lifetime play time < 30 min). */
        boolean isNewPlayer(String playerName) {
            return BlockOwnerRegistry.getEffectivePlayTimeMs(playerName) < NEW_PLAYER_THRESHOLD_MS;
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
            // Reset escalation flags when score decays below their thresholds
            // so reconnecting players get fresh warnings instead of skipping to tempban
            if (hasBeenWarned && griefScore < THRESHOLD_WARN) {
                hasBeenWarned = false;
            }
            if (hasBeenKicked && griefScore < THRESHOLD_KICK) {
                hasBeenKicked = false;
            }
            if (hasBeenNotifiedFirstGrief && griefScore == 0) {
                hasBeenNotifiedFirstGrief = false;
            }
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
