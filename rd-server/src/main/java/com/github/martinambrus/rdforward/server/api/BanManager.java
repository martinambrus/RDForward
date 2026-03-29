package com.github.martinambrus.rdforward.server.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistent ban system for player usernames and IP addresses.
 *
 * Stores bans in two text files (one entry per line):
 * - banned-players.txt (usernames, stored lowercase for case-insensitive matching)
 * - banned-ips.txt (IP addresses, exact match)
 *
 * Thread-safe: uses ConcurrentHashMap-backed sets.
 */
public final class BanManager {

    private BanManager() {}

    private static final String BANNED_PLAYERS_FILE_NAME = "banned-players.txt";
    private static final String BANNED_IPS_FILE_NAME = "banned-ips.txt";
    private static final Set<String> bannedPlayers = ConcurrentHashMap.newKeySet();
    private static final Set<String> bannedIps = ConcurrentHashMap.newKeySet();
    private static volatile File bannedPlayersFile = new File(BANNED_PLAYERS_FILE_NAME);
    private static volatile File bannedIpsFile = new File(BANNED_IPS_FILE_NAME);

    /**
     * Load ban lists from disk. Creates files if they don't exist.
     */
    public static void load() {
        load(null);
    }

    /**
     * Load ban lists from disk, resolving files relative to dataDir.
     */
    public static void load(File dataDir) {
        File dir = (dataDir != null) ? dataDir : new File(".");
        bannedPlayersFile = new File(dir, BANNED_PLAYERS_FILE_NAME);
        bannedIpsFile = new File(dir, BANNED_IPS_FILE_NAME);
        bannedPlayers.clear();
        bannedIps.clear();
        loadFile(bannedPlayersFile, bannedPlayers);
        loadFile(bannedIpsFile, bannedIps);
        int total = bannedPlayers.size() + bannedIps.size();
        if (total > 0) {
            System.out.println("Loaded " + bannedPlayers.size() + " banned player(s) and "
                    + bannedIps.size() + " banned IP(s)");
        }
    }

    private static void loadFile(File file, Set<String> target) {
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    target.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load " + file + ": " + e.getMessage());
        }
    }

    private static void saveFile(File file, Set<String> data, String header) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(header);
            writer.newLine();
            for (String entry : data) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save " + file + ": " + e.getMessage());
        }
    }

    // ---- Player bans (case-insensitive) ----

    public static void banPlayer(String username) {
        if (bannedPlayers.add(username.toLowerCase())) {
            saveFile(bannedPlayersFile, bannedPlayers, "# Banned players (one per line)");
        }
    }

    public static void unbanPlayer(String username) {
        if (bannedPlayers.remove(username.toLowerCase())) {
            saveFile(bannedPlayersFile, bannedPlayers, "# Banned players (one per line)");
        }
    }

    public static boolean isPlayerBanned(String username) {
        return bannedPlayers.contains(username.toLowerCase());
    }

    // ---- IP bans (exact match) ----

    public static void banIp(String ip) {
        if (bannedIps.add(ip)) {
            saveFile(bannedIpsFile, bannedIps, "# Banned IPs (one per line)");
        }
    }

    public static void unbanIp(String ip) {
        if (bannedIps.remove(ip)) {
            saveFile(bannedIpsFile, bannedIps, "# Banned IPs (one per line)");
        }
    }

    public static boolean isIpBanned(String ip) {
        return bannedIps.contains(ip);
    }

    /** Get all banned player names (unmodifiable snapshot). */
    public static Set<String> getBannedPlayers() {
        return java.util.Collections.unmodifiableSet(bannedPlayers);
    }

    /** Get all banned IPs (unmodifiable snapshot). */
    public static Set<String> getBannedIps() {
        return java.util.Collections.unmodifiableSet(bannedIps);
    }

    // ---- Temporary bans (in-memory, not persisted across restarts) ----

    /** Maps lowercase player name → expiry timestamp (epoch ms). */
    private static final ConcurrentHashMap<String, Long> tempBans = new ConcurrentHashMap<>();

    /**
     * Temporarily ban a player for the given duration.
     *
     * @param username player name (case-insensitive)
     * @param durationMs ban duration in milliseconds
     */
    public static void tempBanPlayer(String username, long durationMs) {
        tempBans.put(username.toLowerCase(), System.currentTimeMillis() + durationMs);
    }

    /**
     * Check if a player is temporarily banned.
     * Automatically clears expired bans.
     *
     * @return true if the player has an active temp ban
     */
    public static boolean isTempBanned(String username) {
        Long expiry = tempBans.get(username.toLowerCase());
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            tempBans.remove(username.toLowerCase());
            return false;
        }
        return true;
    }

    /**
     * Get the remaining temp ban duration in milliseconds.
     * Returns 0 if not temp-banned.
     */
    public static long getTempBanRemaining(String username) {
        Long expiry = tempBans.get(username.toLowerCase());
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        if (remaining <= 0) {
            tempBans.remove(username.toLowerCase());
            return 0;
        }
        return remaining;
    }

    /**
     * Remove a temporary ban.
     */
    public static void removeTempBan(String username) {
        tempBans.remove(username.toLowerCase());
    }

    /**
     * Get all active temp bans (name → expiry timestamp).
     */
    public static Map<String, Long> getActiveTempBans() {
        long now = System.currentTimeMillis();
        // Purge expired entries
        tempBans.entrySet().removeIf(e -> e.getValue() <= now);
        return java.util.Collections.unmodifiableMap(tempBans);
    }

    /**
     * Format a duration in ms to a human-readable string (e.g., "30m", "1h 30m").
     */
    public static String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes = minutes % 60;
        if (hours > 0 && minutes > 0) return hours + "h " + minutes + "m";
        if (hours > 0) return hours + "h";
        if (minutes > 0) return minutes + "m";
        return seconds + "s";
    }
}
