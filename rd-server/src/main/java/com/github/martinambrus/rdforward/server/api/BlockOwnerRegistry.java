package com.github.martinambrus.rdforward.server.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistent registry mapping player names to compact short IDs for block
 * ownership tracking, with a play-time-based protection budget.
 *
 * <p>Each player who places a block gets a unique short ID (1–32767) stored
 * in the world/chunk data alongside the block. Players have a limited
 * <em>protection budget</em> — only blocks placed within budget are tracked
 * as owned (and thus protected from grief scoring). Blocks placed beyond
 * budget are unprotected.
 *
 * <h3>Protection budget (inspired by GriefPrevention):</h3>
 * <ul>
 *   <li>Initial budget: {@value #INITIAL_BUDGET} blocks (enough for a small house)</li>
 *   <li>Accrual: {@value #ACCRUAL_PER_HOUR} blocks per hour of active play</li>
 *   <li>Maximum: {@value #MAX_BUDGET} blocks</li>
 *   <li>Budget is returned when a protected block is broken (by owner or others)</li>
 * </ul>
 *
 * <h3>Inactivity expiry:</h3>
 * <p>Players who haven't logged in for {@value #EXPIRY_DAYS} days have their
 * ownership treated as expired (blocks become unprotected). This is the only
 * time-based expiry — active players' blocks never expire.
 *
 * <h3>File format ({@code block-owners.txt}):</h3>
 * <pre>
 * # Block owner registry (format: name:id:lastLoginSec:playTimeMs:usedBlocks)
 * nextId:123
 * alice:1:1711756800:3600000:150
 * bob:2:1711843200:7200000:300
 * </pre>
 */
public final class BlockOwnerRegistry {

    private BlockOwnerRegistry() {}

    private static final String REGISTRY_FILE_NAME = "block-owners.txt";

    // ========================================================================
    // Budget constants
    // ========================================================================

    /** Initial protection budget for new players (blocks). */
    static final int INITIAL_BUDGET = 200;
    /** Blocks earned per hour of active play. */
    static final int ACCRUAL_PER_HOUR = 100;
    /** Maximum total budget a player can accumulate. */
    static final int MAX_BUDGET = 50_000;

    /** Days of inactivity after which a player's block ownership expires. */
    static final int EXPIRY_DAYS = 30;
    private static final long EXPIRY_MS = EXPIRY_DAYS * 24L * 60 * 60 * 1000;

    // ========================================================================
    // State
    // ========================================================================

    /** Name → short ID (1–32767). */
    private static final ConcurrentHashMap<String, Short> nameToId = new ConcurrentHashMap<>();
    /** Short ID → name. */
    private static final ConcurrentHashMap<Short, String> idToName = new ConcurrentHashMap<>();
    /** Name → last login epoch millis. */
    private static final ConcurrentHashMap<String, Long> lastLogin = new ConcurrentHashMap<>();
    /** Name → accumulated active play time in milliseconds. */
    private static final ConcurrentHashMap<String, Long> playTimeMs = new ConcurrentHashMap<>();
    /** Name → number of currently protected blocks in the world. */
    private static final ConcurrentHashMap<String, Integer> usedBlocks = new ConcurrentHashMap<>();
    /** Name → session start time (for accumulating play time on leave). Not persisted. */
    private static final ConcurrentHashMap<String, Long> sessionStart = new ConcurrentHashMap<>();

    /** Next ID to assign. Starts at 1 (0 = unowned). */
    private static volatile short nextId = 1;
    private static volatile File registryFile = new File(REGISTRY_FILE_NAME);
    private static volatile boolean dirty = false;

    // ========================================================================
    // Load / Save
    // ========================================================================

    /**
     * Load the registry from disk, resolving the file relative to dataDir.
     */
    public static void load(File dataDir) {
        File dir = (dataDir != null) ? dataDir : new File(".");
        registryFile = new File(dir, REGISTRY_FILE_NAME);
        nameToId.clear();
        idToName.clear();
        lastLogin.clear();
        playTimeMs.clear();
        usedBlocks.clear();
        sessionStart.clear();
        nextId = 1;

        if (!registryFile.exists()) {
            System.out.println("No block owner registry found, starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(registryFile))) {
            String line;
            short maxId = 0;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("nextId:")) {
                    try {
                        nextId = Short.parseShort(line.substring(7).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("[BlockOwnerRegistry] Malformed nextId: " + line);
                    }
                    continue;
                }

                // Format: name:id:lastLoginSec:playTimeMs:usedBlocks
                // (backwards compat: old format was name:id:lastLoginSec)
                String[] parts = line.split(":");
                if (parts.length < 2) continue;

                String name = parts[0].trim().toLowerCase();
                short id;
                try {
                    id = Short.parseShort(parts[1].trim());
                } catch (NumberFormatException e) {
                    System.err.println("[BlockOwnerRegistry] Malformed id: " + line);
                    continue;
                }

                long login = 0;
                if (parts.length >= 3) {
                    try {
                        login = Long.parseLong(parts[2].trim()) * 1000L;
                    } catch (NumberFormatException e) { /* default 0 */ }
                }

                long playTime = 0;
                if (parts.length >= 4) {
                    try {
                        playTime = Long.parseLong(parts[3].trim());
                    } catch (NumberFormatException e) { /* default 0 */ }
                }

                int used = 0;
                if (parts.length >= 5) {
                    try {
                        used = Integer.parseInt(parts[4].trim());
                    } catch (NumberFormatException e) { /* default 0 */ }
                }

                if (id <= 0 || name.isEmpty()) continue;

                nameToId.put(name, id);
                idToName.put(id, name);
                if (login > 0) lastLogin.put(name, login);
                if (playTime > 0) playTimeMs.put(name, playTime);
                if (used > 0) usedBlocks.put(name, used);
                if (id > maxId) maxId = id;
                count++;
            }

            if (maxId >= nextId) {
                nextId = (short) (maxId + 1);
            }

            System.out.println("Loaded " + count + " block owner(s) from " + registryFile
                    + " (next ID: " + nextId + ")");
        } catch (IOException e) {
            System.err.println("Failed to load block owner registry: " + e.getMessage());
        }
    }

    /**
     * Save the registry to disk. Writes to a temp file then atomically renames.
     */
    public static synchronized void save() {
        File tempFile = new File(registryFile.getParentFile(), REGISTRY_FILE_NAME + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("# Block owner registry (format: name:id:lastLoginSec:playTimeMs:usedBlocks)");
            writer.newLine();
            writer.write("nextId:" + nextId);
            writer.newLine();
            for (Map.Entry<String, Short> entry : nameToId.entrySet()) {
                String name = entry.getKey();
                short id = entry.getValue();
                long loginMs = lastLogin.getOrDefault(name, 0L);
                long loginSec = loginMs / 1000L;
                long playMs = getEffectivePlayTimeMs(name);
                int used = usedBlocks.getOrDefault(name, 0);
                writer.write(name + ":" + id + ":" + loginSec + ":" + playMs + ":" + used);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to write block owner registry: " + e.getMessage());
            return;
        }
        try {
            Files.move(tempFile.toPath(), registryFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.move(tempFile.toPath(), registryFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e2) {
                System.err.println("Failed to save block owner registry: " + e2.getMessage());
            }
        }
        dirty = false;
    }

    /** Save only if there are unsaved changes. */
    public static void saveIfDirty() {
        if (dirty) save();
    }

    // ========================================================================
    // ID management
    // ========================================================================

    /**
     * Get or create a short ID for a player name.
     * The ID is stable: once assigned, the same name always gets the same ID.
     */
    public static short getOrCreateId(String playerName) {
        String key = playerName.toLowerCase();
        Short existing = nameToId.get(key);
        if (existing != null) return existing;

        synchronized (BlockOwnerRegistry.class) {
            existing = nameToId.get(key);
            if (existing != null) return existing;

            if (nextId == Short.MAX_VALUE) {
                System.err.println("[BlockOwnerRegistry] WARNING: ID space exhausted (32767 players)."
                        + " Block ownership for new players will not be tracked.");
                return 0;
            }

            short id = nextId++;
            nameToId.put(key, id);
            idToName.put(id, key);
            dirty = true;
            return id;
        }
    }

    /** Get the player name for a short ID. Returns null if unknown. */
    public static String getPlayerName(short id) {
        if (id <= 0) return null;
        return idToName.get(id);
    }

    /** Get the short ID for a player name. Returns 0 if not registered. */
    public static short getId(String playerName) {
        Short id = nameToId.get(playerName.toLowerCase());
        return id != null ? id : 0;
    }

    // ========================================================================
    // Login and play time tracking
    // ========================================================================

    /** Update the last login time for a player to now. */
    public static void updateLastLogin(String playerName) {
        lastLogin.put(playerName.toLowerCase(), System.currentTimeMillis());
        dirty = true;
    }

    /** Get the last login time for a player in epoch millis. Returns 0 if unknown. */
    public static long getLastLoginMs(String playerName) {
        return lastLogin.getOrDefault(playerName.toLowerCase(), 0L);
    }

    /** Record the start of a player's session (for play time accumulation). */
    public static void startSession(String playerName) {
        sessionStart.put(playerName.toLowerCase(), System.currentTimeMillis());
    }

    /**
     * End a player's session and accumulate play time.
     * Call on PLAYER_LEAVE.
     */
    public static void endSession(String playerName) {
        String key = playerName.toLowerCase();
        Long start = sessionStart.remove(key);
        if (start == null) return;
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed <= 0) return;
        playTimeMs.merge(key, elapsed, Long::sum);
        dirty = true;
    }

    /**
     * Get the effective play time in milliseconds, including any ongoing session.
     */
    private static long getEffectivePlayTimeMs(String playerName) {
        String key = playerName.toLowerCase();
        long accumulated = playTimeMs.getOrDefault(key, 0L);
        Long start = sessionStart.get(key);
        if (start != null) {
            long sessionElapsed = System.currentTimeMillis() - start;
            if (sessionElapsed > 0) accumulated += sessionElapsed;
        }
        return accumulated;
    }

    // ========================================================================
    // Protection budget
    // ========================================================================

    /**
     * Get the total protection budget for a player.
     * Budget = initial + (play hours * accrual rate), capped at max.
     */
    public static int getTotalBudget(String playerName) {
        long playMs = getEffectivePlayTimeMs(playerName);
        long earned = (playMs * ACCRUAL_PER_HOUR) / (60 * 60 * 1000L);
        long total = INITIAL_BUDGET + earned;
        return (int) Math.min(total, MAX_BUDGET);
    }

    /**
     * Get the remaining protection budget for a player (total - used).
     */
    public static int getRemainingBudget(String playerName) {
        int total = getTotalBudget(playerName);
        int used = usedBlocks.getOrDefault(playerName.toLowerCase(), 0);
        return Math.max(0, total - used);
    }

    /** Get the number of protected blocks currently placed by a player. */
    public static int getUsedBlocks(String playerName) {
        return usedBlocks.getOrDefault(playerName.toLowerCase(), 0);
    }

    /**
     * Try to use one protection slot from the player's budget.
     * @return true if the player had remaining budget (block will be protected),
     *         false if over budget (block will be unprotected)
     */
    public static boolean useProtectionSlot(String playerName) {
        String key = playerName.toLowerCase();
        int total = getTotalBudget(key);
        // Atomic increment with budget check
        synchronized (BlockOwnerRegistry.class) {
            int used = usedBlocks.getOrDefault(key, 0);
            if (used >= total) return false;
            usedBlocks.put(key, used + 1);
            dirty = true;
            return true;
        }
    }

    /**
     * Return one protection slot to the player's budget (block was broken).
     */
    public static void returnProtectionSlot(String playerName) {
        String key = playerName.toLowerCase();
        synchronized (BlockOwnerRegistry.class) {
            int used = usedBlocks.getOrDefault(key, 0);
            if (used > 0) {
                usedBlocks.put(key, used - 1);
                dirty = true;
            }
        }
    }

    /**
     * Return one protection slot by owner ID.
     */
    public static void returnProtectionSlot(short ownerId) {
        if (ownerId <= 0) return;
        String name = idToName.get(ownerId);
        if (name != null) returnProtectionSlot(name);
    }

    /**
     * Force-increment a player's used blocks count (for rollback restoration).
     * Unlike {@link #useProtectionSlot}, this bypasses the budget check —
     * it re-claims a slot that was previously returned during a grief action.
     */
    public static void restoreUsedSlot(short ownerId) {
        if (ownerId <= 0) return;
        String name = idToName.get(ownerId);
        if (name == null) return;
        synchronized (BlockOwnerRegistry.class) {
            usedBlocks.merge(name, 1, Integer::sum);
            dirty = true;
        }
    }

    // ========================================================================
    // Inactivity expiry
    // ========================================================================

    /**
     * Check if a player's ownership has expired (inactive for more than
     * {@link #EXPIRY_DAYS} days). Players with no recorded login are NOT
     * considered expired (they may predate the tracking system).
     */
    public static boolean isExpired(short ownerId) {
        if (ownerId <= 0) return false;
        String name = idToName.get(ownerId);
        if (name == null) return true; // unknown ID = effectively expired
        Long login = lastLogin.get(name);
        if (login == null || login == 0) return false;
        return (System.currentTimeMillis() - login) > EXPIRY_MS;
    }

    /**
     * Get days remaining before a player's blocks expire due to inactivity.
     * Returns -1 if not applicable (no login data or player is active).
     */
    public static int getDaysUntilExpiry(String playerName) {
        Long login = lastLogin.get(playerName.toLowerCase());
        if (login == null || login == 0) return -1;
        long elapsed = System.currentTimeMillis() - login;
        if (elapsed <= 0) return EXPIRY_DAYS;
        long remainingMs = EXPIRY_MS - elapsed;
        if (remainingMs <= 0) return 0;
        return (int) (remainingMs / (24L * 60 * 60 * 1000));
    }

    // ========================================================================
    // Misc
    // ========================================================================

    /** Get all registered players and their IDs (unmodifiable). */
    public static Map<String, Short> getAllPlayers() {
        return Collections.unmodifiableMap(nameToId);
    }

    /** Clear all state. Package-private, for testing only. */
    static void clearForTesting() {
        nameToId.clear();
        idToName.clear();
        lastLogin.clear();
        playTimeMs.clear();
        usedBlocks.clear();
        sessionStart.clear();
        nextId = 1;
        dirty = false;
    }
}
