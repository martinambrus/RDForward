package com.github.martinambrus.rdforward.server.api;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.world.AlphaWorldGenerator;
import com.github.martinambrus.rdforward.world.FlatWorldGenerator;
import com.github.martinambrus.rdforward.world.RubyDungWorldGenerator;
import com.github.martinambrus.rdforward.world.WorldGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Loads and provides typed access to server.properties, following the
 * vanilla Minecraft server.properties format.
 *
 * <p>Override precedence (highest to lowest):
 * <ol>
 *   <li>CLI args (port only)</li>
 *   <li>System properties (-Drdforward.*, -De2e.viewDistance)</li>
 *   <li>server.properties file values</li>
 *   <li>Hardcoded defaults</li>
 * </ol>
 *
 * <p>System property overrides are applied in memory but NOT saved back
 * to the file (they are transient per-run overrides).
 */
public final class ServerProperties {

    private ServerProperties() {}

    private static final String FILE_NAME = "server.properties";

    /** Ordered defaults for deterministic file output. */
    private static final LinkedHashMap<String, String> DEFAULTS = new LinkedHashMap<String, String>();
    static {
        DEFAULTS.put("server-port", "25565");
        DEFAULTS.put("bedrock-port", "19132");
        DEFAULTS.put("server-version", "rd-132211");
        DEFAULTS.put("gamemode", "creative");
        DEFAULTS.put("difficulty", "easy");
        DEFAULTS.put("max-players", "128");
        DEFAULTS.put("view-distance", "5");
        DEFAULTS.put("motd", "A Minecraft Server");
        DEFAULTS.put("level-name", "world");
        DEFAULTS.put("level-seed", "");
        DEFAULTS.put("level-type", "flat");
        DEFAULTS.put("world-width", "256");
        DEFAULTS.put("world-height", "64");
        DEFAULTS.put("world-depth", "256");
        DEFAULTS.put("spawn-protection", "16");
        DEFAULTS.put("pvp", "true");
        DEFAULTS.put("online-mode", "false");
        DEFAULTS.put("white-list", "false");
        DEFAULTS.put("enforce-whitelist", "false");
        DEFAULTS.put("enable-command-block", "false");
        DEFAULTS.put("keep-alive-interval", "15");
        DEFAULTS.put("keep-alive-timeout", "30");
    }

    /** Properties that exist in the file but have no effect yet. */
    private static final Set<String> PLACEHOLDER_KEYS = new HashSet<String>(
            Arrays.asList("pvp", "enable-command-block"));

    private static final Properties props = new Properties();
    private static volatile boolean loaded = false;
    private static volatile File loadedFile;

    /**
     * Load server.properties from the current working directory.
     */
    public static void load() {
        load(null);
    }

    /**
     * Load server.properties relative to the given data directory.
     * Creates the file with defaults if it doesn't exist.
     * Saves back to ensure newly added properties appear in the file.
     * System property overrides are applied AFTER saving so they are NOT persisted.
     */
    public static void load(File dataDir) {
        File dir = (dataDir != null) ? dataDir : new File(".");
        File file = new File(dir, FILE_NAME);
        loadedFile = file;

        // Start from defaults
        props.clear();
        for (Map.Entry<String, String> e : DEFAULTS.entrySet()) {
            props.setProperty(e.getKey(), e.getValue());
        }

        // Override with file values
        boolean needsSave = false;
        try (FileInputStream fis = new FileInputStream(file)) {
            // Remember which keys existed in the file before merging defaults
            Properties fileProps = new Properties();
            fileProps.load(fis);
            // Merge file values over defaults
            for (String key : fileProps.stringPropertyNames()) {
                props.setProperty(key, fileProps.getProperty(key));
            }
            // Check if the file is missing any default keys (added in a code update)
            for (String key : DEFAULTS.keySet()) {
                if (fileProps.getProperty(key) == null) {
                    needsSave = true;
                    break;
                }
            }
        } catch (FileNotFoundException ignored) {
            needsSave = true; // File doesn't exist yet — create it
        } catch (IOException e) {
            System.err.println("[WARN] Failed to load " + FILE_NAME + ": " + e.getMessage());
        }

        // Save to disk BEFORE applying transient overrides so they are NOT persisted
        if (needsSave) {
            saveOrdered(file);
        }

        // Apply system property overrides (transient, not saved)
        applySystemPropertyOverrides();

        loaded = true;

        // Log effective configuration
        System.out.println("Loaded server properties from " + file);
        System.out.println("  server-version=" + props.getProperty("server-version")
                + ", gamemode=" + getGameModeName() + ", difficulty=" + getDifficultyName()
                + ", max-players=" + getMaxPlayers());
        System.out.println("  motd=\"" + getMotd() + "\", view-distance=" + getViewDistance()
                + ", server-port=" + getServerPort() + ", bedrock-port=" + getBedrockPort()
                + ", online-mode=" + isOnlineMode());
    }

    /**
     * Save current properties to disk. System property overrides are
     * excluded (transient per-run values are not persisted).
     */
    public static void save() {
        if (loadedFile != null) {
            saveOrdered(loadedFile);
        }
    }

    /**
     * Apply system property overrides. These take precedence over file values
     * but are NOT saved back to the file.
     */
    private static void applySystemPropertyOverrides() {
        override("rdforward.world.width", "world-width");
        override("rdforward.world.height", "world-height");
        override("rdforward.world.depth", "world-depth");
        override("rdforward.generator", "level-type");
        override("rdforward.seed", "level-seed");
        override("e2e.viewDistance", "view-distance");
    }

    private static void override(String systemProperty, String propsKey) {
        String value = System.getProperty(systemProperty);
        if (value != null) {
            System.out.println("[CONFIG] System property -D" + systemProperty + "=" + value
                    + " overrides " + propsKey + "=" + props.getProperty(propsKey));
            props.setProperty(propsKey, value);
        }
    }

    /**
     * Save properties in a deterministic order (matching DEFAULTS key order)
     * with a descriptive header. Writes to a temp file first, then renames
     * atomically to avoid corruption on crash.
     */
    private static void saveOrdered(File file) {
        File tmpFile = new File(file.getParentFile(), FILE_NAME + ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
            StringBuilder sb = new StringBuilder();
            sb.append("# Minecraft server properties\n");
            sb.append("# server-version uses launcher-style IDs, e.g.: rd-132211, c0.30, a1.2.6,\n");
            sb.append("#   b1.7.3, 1.8.9, 1.21.4. Set an invalid value and start the server to\n");
            sb.append("#   see a full list of supported version IDs.\n");
            sb.append("# level-type: flat, rubydung, classic, alpha\n");
            for (String key : DEFAULTS.keySet()) {
                if (PLACEHOLDER_KEYS.contains(key)) {
                    sb.append("# (not yet implemented)\n");
                }
                String value = props.getProperty(key, DEFAULTS.get(key));
                sb.append(key).append('=').append(escapeValue(value)).append('\n');
            }
            // Write any extra keys not in DEFAULTS (user-added custom properties)
            for (String key : props.stringPropertyNames()) {
                if (!DEFAULTS.containsKey(key)) {
                    sb.append(key).append('=').append(escapeValue(props.getProperty(key))).append('\n');
                }
            }
            fos.write(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            fos.getFD().sync();
        } catch (IOException e) {
            System.err.println("[WARN] Failed to save " + FILE_NAME + ": " + e.getMessage());
            tmpFile.delete();
            return;
        }
        // Atomic rename (on most filesystems)
        if (!tmpFile.renameTo(file)) {
            // Fallback: rename failed (e.g. Windows cross-drive), try direct overwrite
            file.delete();
            if (!tmpFile.renameTo(file)) {
                System.err.println("[WARN] Failed to rename " + tmpFile + " to " + file);
                tmpFile.delete();
            }
        }
    }

    /**
     * Escape special characters in property values for .properties format.
     * Handles backslash, leading #/!, =, :, newlines, tabs, and leading whitespace.
     */
    private static String escapeValue(String value) {
        if (value == null || value.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    // Escape leading whitespace, #, and !
                    if (i == 0 && (c == ' ' || c == '#' || c == '!')) {
                        sb.append('\\');
                    }
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Log a warning if load() has not been called yet. */
    private static void warnIfNotLoaded() {
        if (!loaded) {
            System.err.println("[WARN] ServerProperties accessed before load() was called"
                    + " — using defaults. This may indicate a startup ordering bug.");
        }
    }

    // --- Typed getters ---

    public static int getServerPort() {
        warnIfNotLoaded();
        int port = getInt("server-port", 25565);
        if (port < 1 || port > 65535) {
            System.err.println("[WARN] server-port=" + port + " is out of range (1-65535), using 25565");
            return 25565;
        }
        return port;
    }

    public static int getBedrockPort() {
        warnIfNotLoaded();
        int port = getInt("bedrock-port", 19132);
        if (port < 1 || port > 65535) {
            System.err.println("[WARN] bedrock-port=" + port + " is out of range (1-65535), using 19132");
            return 19132;
        }
        return port;
    }

    /**
     * Get game mode as an integer (0=survival, 1=creative, 2=adventure, 3=spectator).
     * Accepts both names and numbers.
     */
    public static int getGameMode() {
        warnIfNotLoaded();
        String value = props.getProperty("gamemode", "creative").trim().toLowerCase();
        switch (value) {
            case "survival": case "0": return 0;
            case "creative": case "1": return 1;
            case "adventure": case "2": return 2;
            case "spectator": case "3": return 3;
            default:
                System.err.println("[WARN] Unknown gamemode '" + value + "', defaulting to creative."
                        + " Valid values: survival, creative, adventure, spectator (or 0-3)");
                return 1;
        }
    }

    /** Get game mode name suitable for display (e.g., "Survival", "Creative"). */
    public static String getGameModeName() {
        switch (getGameMode()) {
            case 0: return "Survival";
            case 1: return "Creative";
            case 2: return "Adventure";
            case 3: return "Spectator";
            default: return "Creative";
        }
    }

    /**
     * Get difficulty as an integer (0=peaceful, 1=easy, 2=normal, 3=hard).
     * Accepts both names and numbers.
     */
    public static int getDifficulty() {
        warnIfNotLoaded();
        String value = props.getProperty("difficulty", "easy").trim().toLowerCase();
        switch (value) {
            case "peaceful": case "0": return 0;
            case "easy": case "1": return 1;
            case "normal": case "2": return 2;
            case "hard": case "3": return 3;
            default:
                System.err.println("[WARN] Unknown difficulty '" + value + "', defaulting to easy."
                        + " Valid values: peaceful, easy, normal, hard (or 0-3)");
                return 1;
        }
    }

    /** Get difficulty name suitable for display. */
    public static String getDifficultyName() {
        switch (getDifficulty()) {
            case 0: return "Peaceful";
            case 1: return "Easy";
            case 2: return "Normal";
            case 3: return "Hard";
            default: return "Easy";
        }
    }

    public static int getMaxPlayers() {
        warnIfNotLoaded();
        return getInt("max-players", 128);
    }

    public static int getViewDistance() {
        warnIfNotLoaded();
        int value = getInt("view-distance", 5);
        if (value < 1 || value > 32) {
            System.err.println("[WARN] view-distance=" + value
                    + " is out of range (1-32), clamping");
        }
        return Math.max(1, Math.min(value, 32));
    }

    public static String getMotd() {
        warnIfNotLoaded();
        return props.getProperty("motd", "A Minecraft Server");
    }

    public static String getLevelName() {
        warnIfNotLoaded();
        String name = props.getProperty("level-name", "world").trim();
        if (name.isEmpty()) return "world";
        // Reject path traversal characters
        if (name.contains("..") || name.contains("/") || name.contains("\\")
                || name.contains(File.separator)) {
            System.err.println("[WARN] level-name '" + name
                    + "' contains path separators or '..', using 'world'");
            return "world";
        }
        return name;
    }

    /**
     * Parse the level seed. Empty string returns 0L, numeric strings are parsed
     * as long, non-numeric strings use hashCode (matching vanilla behavior).
     */
    public static long getLevelSeed() {
        warnIfNotLoaded();
        String value = props.getProperty("level-seed", "").trim();
        if (value.isEmpty()) return 0L;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return value.hashCode();
        }
    }

    public static String getLevelType() {
        warnIfNotLoaded();
        return props.getProperty("level-type", "flat").trim().toLowerCase();
    }

    public static int getWorldWidth() {
        warnIfNotLoaded();
        return Math.max(1, getInt("world-width", 256));
    }

    public static int getWorldHeight() {
        warnIfNotLoaded();
        return Math.max(1, getInt("world-height", 64));
    }

    public static int getWorldDepth() {
        warnIfNotLoaded();
        return Math.max(1, getInt("world-depth", 256));
    }

    /** Get the raw server-version string (launcher-style ID). */
    public static String getServerVersion() {
        warnIfNotLoaded();
        return props.getProperty("server-version", "rd-132211").trim();
    }

    public static int getSpawnProtection() {
        warnIfNotLoaded();
        return Math.max(0, getInt("spawn-protection", 16));
    }

    public static boolean isPvp() {
        warnIfNotLoaded();
        return getBoolean("pvp", true);
    }

    public static boolean isOnlineMode() {
        warnIfNotLoaded();
        return getBoolean("online-mode", false);
    }

    public static boolean isWhiteList() {
        warnIfNotLoaded();
        return getBoolean("white-list", false);
    }

    /** Set white-list enabled/disabled at runtime. */
    public static void setWhiteList(boolean enabled) {
        props.setProperty("white-list", String.valueOf(enabled));
    }

    public static boolean isEnforceWhitelist() {
        warnIfNotLoaded();
        return getBoolean("enforce-whitelist", false);
    }

    public static boolean isEnableCommandBlock() {
        warnIfNotLoaded();
        return getBoolean("enable-command-block", false);
    }

    /**
     * Get keep-alive send interval in seconds.
     * Vanilla Minecraft uses 15 seconds.
     */
    public static int getKeepAliveIntervalSeconds() {
        warnIfNotLoaded();
        return Math.max(1, getInt("keep-alive-interval", 15));
    }

    /**
     * Get keep-alive timeout in seconds. If no response is received
     * within this period, the player is disconnected.
     * Vanilla Minecraft uses 30 seconds (2x the send interval).
     */
    public static int getKeepAliveTimeoutSeconds() {
        warnIfNotLoaded();
        return Math.max(1, getInt("keep-alive-timeout", 30));
    }

    /**
     * Resolve the server-version string to a ProtocolVersion enum.
     * Throws IllegalArgumentException if unrecognized (caller should handle and exit).
     */
    public static ProtocolVersion getProtocolVersion() {
        ProtocolVersion pv = ProtocolVersion.fromLauncherId(getServerVersion());
        if (pv == null) {
            StringBuilder msg = new StringBuilder();
            msg.append("Unknown server-version '").append(getServerVersion())
                    .append("' in server.properties.\n\n");
            msg.append("Valid server-version values:\n");
            ProtocolVersion.Family currentFamily = null;
            for (ProtocolVersion v : ProtocolVersion.values()) {
                if (v.getFamily() != currentFamily) {
                    currentFamily = v.getFamily();
                    msg.append("  ").append(currentFamily).append(":\n");
                }
                msg.append("    ").append(v.getLauncherId())
                        .append("  (").append(v.getDisplayName()).append(")\n");
            }
            msg.append("\nEdit the server-version property in server.properties and restart.");
            throw new IllegalArgumentException(msg.toString());
        }
        return pv;
    }

    /**
     * Create a WorldGenerator matching the level-type property.
     */
    public static WorldGenerator createWorldGenerator() {
        String type = getLevelType();
        switch (type) {
            case "rubydung":
            case "classic":
                return new RubyDungWorldGenerator();
            case "alpha":
                return new AlphaWorldGenerator();
            case "flat":
            default:
                return new FlatWorldGenerator();
        }
    }

    /**
     * Compute PlayerAbilities flags byte matching the configured game mode.
     * Bit layout: 0x01=invulnerable, 0x02=flying, 0x04=allowFlying, 0x08=instabuild.
     */
    public static byte getAbilitiesFlags() {
        switch (getGameMode()) {
            case 1: return 0x0D; // creative: invulnerable | allowFlying | instabuild
            case 3: return 0x06; // spectator: flying | allowFlying
            default: return 0x00; // survival, adventure: no special abilities
        }
    }

    // --- Internal helpers ---

    private static int getInt(String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("[WARN] Invalid integer for " + key + ": '" + value
                    + "', using default " + defaultValue);
            return defaultValue;
        }
    }

    private static boolean getBoolean(String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value.trim());
    }

    /** Clear all state. Package-private, for testing only. */
    static void clearForTesting() {
        props.clear();
        loaded = false;
        System.clearProperty("e2e.viewDistance");
    }
}
