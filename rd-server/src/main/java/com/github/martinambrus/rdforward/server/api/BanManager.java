package com.github.martinambrus.rdforward.server.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

    private static final String BANNED_PLAYERS_FILE = "banned-players.txt";
    private static final String BANNED_IPS_FILE = "banned-ips.txt";
    private static final Set<String> bannedPlayers = ConcurrentHashMap.newKeySet();
    private static final Set<String> bannedIps = ConcurrentHashMap.newKeySet();

    /**
     * Load ban lists from disk. Creates files if they don't exist.
     */
    public static void load() {
        loadFile(BANNED_PLAYERS_FILE, bannedPlayers);
        loadFile(BANNED_IPS_FILE, bannedIps);
        int total = bannedPlayers.size() + bannedIps.size();
        if (total > 0) {
            System.out.println("Loaded " + bannedPlayers.size() + " banned player(s) and "
                    + bannedIps.size() + " banned IP(s)");
        }
    }

    private static void loadFile(String filename, Set<String> target) {
        File file = new File(filename);
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
            System.err.println("Failed to load " + filename + ": " + e.getMessage());
        }
    }

    private static void saveFile(String filename, Set<String> data, String header) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(header);
            writer.newLine();
            for (String entry : data) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save " + filename + ": " + e.getMessage());
        }
    }

    // ---- Player bans (case-insensitive) ----

    public static void banPlayer(String username) {
        if (bannedPlayers.add(username.toLowerCase())) {
            saveFile(BANNED_PLAYERS_FILE, bannedPlayers, "# Banned players (one per line)");
        }
    }

    public static void unbanPlayer(String username) {
        if (bannedPlayers.remove(username.toLowerCase())) {
            saveFile(BANNED_PLAYERS_FILE, bannedPlayers, "# Banned players (one per line)");
        }
    }

    public static boolean isPlayerBanned(String username) {
        return bannedPlayers.contains(username.toLowerCase());
    }

    // ---- IP bans (exact match) ----

    public static void banIp(String ip) {
        if (bannedIps.add(ip)) {
            saveFile(BANNED_IPS_FILE, bannedIps, "# Banned IPs (one per line)");
        }
    }

    public static void unbanIp(String ip) {
        if (bannedIps.remove(ip)) {
            saveFile(BANNED_IPS_FILE, bannedIps, "# Banned IPs (one per line)");
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
}
