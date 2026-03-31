package com.github.martinambrus.rdforward.server;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight debug logging for production diagnostics.
 *
 * <p>Toggled at runtime via the console-only {@code /debug} command.
 * When disabled (the default), the only cost at each instrumentation
 * point is a single {@code volatile boolean} read — no string
 * concatenation, no allocation, no method dispatch.
 *
 * <p>Debug mode automatically disables after {@link #AUTO_OFF_MINUTES}
 * minutes to prevent accidental log flooding in production.
 *
 * <h3>Usage at call sites:</h3>
 * <pre>
 *   if (DebugLog.blocks() &amp;&amp; DebugLog.forPlayer(name)) {
 *       DebugLog.log(DebugLog.BLOCK, name + " PLACE at (" + x + "," + y + "," + z + ")");
 *   }
 * </pre>
 *
 * <h3>Categories:</h3>
 * <ul>
 *   <li>{@link #BLOCK} — block place/break, grief protection decisions, ownership</li>
 *   <li>{@link #POS}   — player position updates, teleport, save/restore</li>
 *   <li>{@link #CHUNK} — chunk load/unload/send decisions</li>
 * </ul>
 */
public final class DebugLog {

    private DebugLog() {}

    // Category constants — use these at call sites to avoid typos
    public static final String BLOCK = "BLOCK";
    public static final String POS   = "POS";
    public static final String CHUNK = "CHUNK";

    /** Minutes before debug auto-disables. */
    public static final int AUTO_OFF_MINUTES = 15;

    // Master toggle — single volatile read is the ONLY cost when off
    private static volatile boolean enabled = false;

    // Category toggles — checked ONLY after master toggle passes
    private static volatile boolean blocks = true;
    private static volatile boolean pos    = true;
    private static volatile boolean chunks = true;

    // Verbose mode — disables sampling/rate-limiting so every event is logged
    private static volatile boolean verbose = false;

    // Player filter — null means "log all players"
    private static volatile Set<String> playerFilter = null;

    // Auto-off: timestamp when debug should auto-disable (0 = no auto-off)
    private static volatile long autoOffAt = 0;

    // ========================================================================
    // Guards — call sites use these to short-circuit before any work
    // ========================================================================

    public static boolean blocks() { return enabled && checkAutoOff() && blocks; }
    public static boolean pos()    { return enabled && checkAutoOff() && pos; }
    public static boolean chunks() { return enabled && checkAutoOff() && chunks; }

    /**
     * Check if logging is active for a specific player.
     * Returns true if name is null, no filter is set (log all),
     * or the player is in the filter.
     */
    public static boolean forPlayer(String name) {
        if (name == null) return true;
        Set<String> f = playerFilter; // single volatile read
        return f == null || f.contains(name.toLowerCase());
    }

    // ========================================================================
    // Output
    // ========================================================================

    public static void log(String category, String msg) {
        System.out.println("[DEBUG/" + category + "] " + msg);
    }

    /**
     * Strip control characters from a player name for safe log output.
     * Keeps alphanumeric, underscore, hyphen, period, and space.
     */
    public static String sanitizeName(String name) {
        if (name == null) return "null";
        StringBuilder sb = null;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c >= 0x20 && c != 0x7F) {
                if (sb != null) sb.append(c);
            } else {
                if (sb == null) {
                    sb = new StringBuilder(name.length());
                    sb.append(name, 0, i);
                }
                // Replace control char with '?'
                sb.append('?');
            }
        }
        return sb != null ? sb.toString() : name;
    }

    // ========================================================================
    // Toggle methods (called from /debug command only)
    // ========================================================================

    public static void setEnabled(boolean on) {
        enabled = on;
        if (on) {
            autoOffAt = System.currentTimeMillis() + AUTO_OFF_MINUTES * 60_000L;
        } else {
            autoOffAt = 0;
        }
    }
    public static boolean isEnabled()         { return enabled; }

    public static void setBlocks(boolean on) { blocks = on; }
    public static void setPos(boolean on)    { pos = on; }
    public static void setChunks(boolean on) { chunks = on; }

    public static boolean isBlocks() { return blocks; }
    public static boolean isPos()    { return pos; }
    public static boolean isChunks() { return chunks; }

    public static void setVerbose(boolean on) { verbose = on; }
    public static boolean isVerbose()         { return verbose; }

    public static synchronized void addPlayerFilter(String name) {
        if (name == null || name.isEmpty()) return;
        Set<String> f = playerFilter;
        if (f == null) {
            f = ConcurrentHashMap.newKeySet();
        }
        f.add(name.toLowerCase());
        playerFilter = f;
    }

    public static synchronized void removePlayerFilter(String name) {
        if (name == null || name.isEmpty()) return;
        Set<String> f = playerFilter;
        if (f != null) {
            f.remove(name.toLowerCase());
            if (f.isEmpty()) playerFilter = null;
        }
    }

    public static synchronized void clearPlayerFilter() { playerFilter = null; }

    /** Get a description of the current player filter for status display. */
    public static String getPlayerFilterDesc() {
        Set<String> f = playerFilter;
        if (f == null) return "all";
        return String.join(", ", f);
    }

    /** Get remaining auto-off time description, or empty string if not set. */
    public static String getAutoOffDesc() {
        long remaining = autoOffAt - System.currentTimeMillis();
        if (remaining <= 0) return "";
        long mins = remaining / 60_000;
        long secs = (remaining % 60_000) / 1000;
        return " (auto-off in " + mins + "m" + secs + "s)";
    }

    // ========================================================================
    // Auto-off check
    // ========================================================================

    private static boolean checkAutoOff() {
        long deadline = autoOffAt;
        if (deadline > 0 && System.currentTimeMillis() >= deadline) {
            enabled = false;
            autoOffAt = 0;
            System.out.println("[Server] Debug auto-disabled after " + AUTO_OFF_MINUTES + " minutes");
            return false;
        }
        return true;
    }

    // ========================================================================
    // Testing support
    // ========================================================================

    /** Reset all state to defaults. For tests only. */
    public static synchronized void resetForTesting() {
        enabled = false;
        blocks = true;
        pos = true;
        chunks = true;
        verbose = false;
        playerFilter = null;
        autoOffAt = 0;
    }
}
