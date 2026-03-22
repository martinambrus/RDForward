package com.github.martinambrus.rdforward.server.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Op-level permission system following vanilla Minecraft's 4-tier model.
 *
 * <p>Operators are stored in "ops.txt" with format {@code username:level}
 * (one per line, lines starting with {@code #} are comments). A bare
 * username without a colon defaults to level 4 for backwards compatibility.
 * Console commands always bypass permission checks entirely.
 *
 * <p>Usernames are normalized to lowercase for case-insensitive matching.
 *
 * <h3>Op levels (matching vanilla Minecraft):</h3>
 * <ul>
 *   <li><b>Level 1</b> — Can bypass spawn protection</li>
 *   <li><b>Level 2</b> — Can use cheat commands: /tp, /time, /weather, etc.</li>
 *   <li><b>Level 3</b> — Can manage players: /op, /deop, /kick, /ban, /unban, etc.</li>
 *   <li><b>Level 4</b> — Full server control: /stop, /save, etc.</li>
 * </ul>
 */
public final class PermissionManager {

    private PermissionManager() {}

    public static final int MAX_OP_LEVEL = 4;

    /** Bypass spawn protection. */
    public static final int OP_BYPASS_SPAWN = 1;
    /** Cheat commands: /tp, /time, /weather, etc. */
    public static final int OP_CHEAT = 2;
    /** Player management: /op, /deop, /kick, /ban, /unban, etc. */
    public static final int OP_MANAGE = 3;
    /** Full server control: /stop, /save, etc. */
    public static final int OP_ADMIN = 4;

    private static final String OPS_FILE_NAME = "ops.txt";
    private static final String CMD_PERMS_FILE = "config/op-permissions.properties";
    private static final Object LOCK = new Object();
    private static final ConcurrentHashMap<String, Integer> ops = new ConcurrentHashMap<>();
    private static volatile File opsFile = new File(OPS_FILE_NAME);
    private static final Properties cmdPermsDefaults = new Properties();
    private static final Properties cmdPerms = new Properties();

    private static int clampLevel(int level) {
        return Math.max(1, Math.min(MAX_OP_LEVEL, level));
    }

    /**
     * Load operator list from ops.txt. Creates the file if it doesn't exist.
     */
    public static void load() {
        load(null);
    }

    /**
     * Load operator list, resolving the file relative to dataDir.
     * Supports both new format ({@code username:level}) and legacy format
     * (bare {@code username}, defaults to level 4).
     */
    public static void load(File dataDir) {
        File dir = (dataDir != null) ? dataDir : new File(".");
        ConcurrentHashMap<String, Integer> loaded = new ConcurrentHashMap<>();
        File file = new File(dir, OPS_FILE_NAME);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int colon = line.indexOf(':');
                    String name;
                    int level;
                    if (colon >= 0) {
                        name = line.substring(0, colon).trim().toLowerCase();
                        try {
                            level = clampLevel(Integer.parseInt(line.substring(colon + 1).trim()));
                        } catch (NumberFormatException e) {
                            System.err.println("[WARN] Malformed op level in ops.txt: '" + line
                                    + "', defaulting to level 1");
                            level = OP_BYPASS_SPAWN;
                        }
                    } else {
                        // Legacy format: bare username defaults to level 4
                        name = line.toLowerCase();
                        level = MAX_OP_LEVEL;
                    }
                    if (name.isEmpty()) {
                        System.err.println("[WARN] Skipping empty operator name in ops.txt: '" + line + "'");
                        continue;
                    }
                    if (name.contains(":")) {
                        System.err.println("[WARN] Skipping operator with colon in name: '" + line + "'");
                        continue;
                    }
                    loaded.put(name, level);
                }
                System.out.println("Loaded " + loaded.size() + " operator(s) from " + file);
            } catch (IOException e) {
                System.err.println("Failed to load ops: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
        // Atomic swap under lock
        synchronized (LOCK) {
            opsFile = file;
            ops.clear();
            ops.putAll(loaded);
        }

        // Load command permission overrides
        loadCommandPermissions(dir);
    }

    /**
     * Load command-to-op-level mappings from config/op-permissions.properties.
     * Creates the file with defaults if it doesn't exist. Admins can edit
     * this file to customize which op level each command requires.
     */
    private static void loadCommandPermissions(File dataDir) {
        File configDir = new File(dataDir, "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        File file = new File(dataDir, CMD_PERMS_FILE);

        // Populate defaults (written to file if missing)
        initCommandPermissionDefaults();

        // Start from defaults
        cmdPerms.clear();
        cmdPerms.putAll(cmdPermsDefaults);

        // Override with saved values
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                cmdPerms.load(fis);
            } catch (IOException e) {
                System.err.println("[WARN] Failed to load " + CMD_PERMS_FILE + ": " + e.getMessage());
            }
        }

        // Save to ensure all defaults are written (new commands from updates get added)
        try (FileOutputStream fos = new FileOutputStream(file)) {
            cmdPerms.store(fos,
                    "Command permission levels. Set the op level required for each command (0-4).\n"
                    + "# 0 = all players, 1 = spawn bypass, 2 = cheats, 3 = player management, 4 = server admin.\n"
                    + "# Delete a line to restore its default. Unrecognized commands are ignored.");
        } catch (IOException e) {
            System.err.println("[WARN] Failed to save " + CMD_PERMS_FILE + ": " + e.getMessage());
        }

        int overrides = 0;
        for (String key : cmdPerms.stringPropertyNames()) {
            String defaultVal = cmdPermsDefaults.getProperty(key);
            if (defaultVal != null && !defaultVal.equals(cmdPerms.getProperty(key))) {
                overrides++;
            }
        }
        System.out.println("Loaded command permissions from " + file
                + " (" + cmdPerms.size() + " commands" + (overrides > 0 ? ", " + overrides + " override(s)" : "") + ")");
    }

    /**
     * Initialize default command-to-op-level mappings for all vanilla MC commands.
     * Follows the vanilla Minecraft permission model with BukkitOpFix-style granularity.
     */
    private static void initCommandPermissionDefaults() {
        cmdPermsDefaults.clear();

        // Level 0 — available to all players
        for (String cmd : new String[]{
                "help", "list", "me", "msg", "tell", "w", "teammsg", "tm", "trigger", "seed"
        }) {
            cmdPermsDefaults.setProperty(cmd, "0");
        }

        // Level 1 — bypass spawn protection
        // (no vanilla commands default to level 1; kept for custom commands)

        // Level 2 — cheat commands (gameplay modifications)
        for (String cmd : new String[]{
                // Core gameplay cheats
                "tp", "teleport", "time", "weather", "gamemode", "gamerule",
                "give", "clear", "effect", "enchant", "xp", "experience",
                "spawnpoint", "setworldspawn", "difficulty",
                // World editing
                "setblock", "fill", "clone", "summon", "kill",
                "playsound", "stopsound", "particle", "title",
                "worldborder", "spreadplayers", "locate", "locatebiome",
                // Entity/data manipulation
                "data", "attribute", "execute", "function", "schedule",
                "scoreboard", "tag", "team", "bossbar", "recipe",
                "advancement", "loot", "replaceitem", "item",
                // Redstone/technical
                "forceload",
                // Post-1.16 commands (Level 2)
                "place", "fillbiome", "damage", "ride", "return", "random",
                "rotate", "dialog", "waypoint",
                // Miscellaneous gameplay
                "say", "defaultgamemode", "toggledownfall",
                "testfor", "testforblock", "testforblocks",
                "blockdata", "entitydata", "stats"
        }) {
            cmdPermsDefaults.setProperty(cmd, "2");
        }

        // Level 3 — player management
        for (String cmd : new String[]{
                "op", "deop", "kick", "ban", "banip", "ban-ip",
                "pardon", "pardon-ip", "unban",
                "whitelist", "setidletimeout",
                "debug", "transfer",
                // Post-1.16 additions (Level 3)
                "tick", "stopwatch", "fetchprofile"
        }) {
            cmdPermsDefaults.setProperty(cmd, "3");
        }

        // Level 4 — full server control
        for (String cmd : new String[]{
                "stop", "save", "save-all", "save-off", "save-on",
                "publish", "reload",
                // Post-1.16 additions (Level 4)
                "perf", "jfr"
        }) {
            cmdPermsDefaults.setProperty(cmd, "4");
        }
    }

    /**
     * Save operator list to ops.txt in {@code username:level} format.
     * Writes to a temp file first, then atomically renames.
     * Must be called while holding LOCK.
     */
    private static void saveLocked() {
        File tempFile = new File(opsFile.getParentFile(), OPS_FILE_NAME + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("# Server operators (format: username:level, levels 1-4)");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : ops.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to write ops temp file: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }
        try {
            Files.move(tempFile.toPath(), opsFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // ATOMIC_MOVE not supported — fall back to plain rename
            try {
                Files.move(tempFile.toPath(), opsFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e2) {
                System.err.println("Failed to save ops file: " + e2.getMessage());
                e2.printStackTrace(System.err);
            }
        }
    }

    /** Check if a player has operator permissions (any level). */
    public static boolean isOp(String username) {
        return ops.containsKey(username.toLowerCase());
    }

    /** Get a player's op level. Returns 0 for non-operators. */
    public static int getOpLevel(String username) {
        Integer level = ops.get(username.toLowerCase());
        return level != null ? level : 0;
    }

    /** Grant operator permissions at the maximum level (4). */
    public static void addOp(String username) {
        addOp(username, MAX_OP_LEVEL);
    }

    /**
     * Grant operator permissions at a specific level (clamped to 1-4).
     *
     * @throws IllegalArgumentException if username is empty or contains ':'
     */
    public static void addOp(String username, int level) {
        String normalized = username.toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (normalized.contains(":")) {
            throw new IllegalArgumentException("Username cannot contain ':'");
        }
        level = clampLevel(level);
        synchronized (LOCK) {
            Integer prev = ops.put(normalized, level);
            if (prev == null || prev != level) {
                saveLocked();
            }
        }
    }

    /** Revoke operator permissions from a player. */
    public static void removeOp(String username) {
        synchronized (LOCK) {
            if (ops.remove(username.toLowerCase()) != null) {
                saveLocked();
            }
        }
    }

    /** Get all operator names (unmodifiable, lowercase). */
    public static Set<String> getOps() {
        return Collections.unmodifiableSet(ops.keySet());
    }

    /** Get all operators with their levels (unmodifiable). */
    public static Map<String, Integer> getOpsWithLevels() {
        return Collections.unmodifiableMap(ops);
    }

    /**
     * Get the configured op level for a command.
     * Returns the config file override if present, otherwise the provided default.
     *
     * @param commandName the command name (without "/"), case-insensitive
     * @param defaultLevel the default level if not configured
     * @return the op level from config, or defaultLevel if not configured
     */
    public static int getCommandOpLevel(String commandName, int defaultLevel) {
        String value = cmdPerms.getProperty(commandName.toLowerCase());
        if (value == null) {
            return defaultLevel;
        }
        try {
            int level = Integer.parseInt(value.trim());
            if (level < 0 || level > MAX_OP_LEVEL) {
                System.err.println("[WARN] Invalid op level for command '" + commandName
                        + "': " + level + ", using default " + defaultLevel);
                return defaultLevel;
            }
            return level;
        } catch (NumberFormatException e) {
            System.err.println("[WARN] Malformed op level for command '" + commandName
                    + "': '" + value + "', using default " + defaultLevel);
            return defaultLevel;
        }
    }

    /** Clear all state. Package-private, for testing only. */
    static void clearForTesting() {
        synchronized (LOCK) {
            ops.clear();
            cmdPerms.clear();
            cmdPermsDefaults.clear();
        }
    }
}
