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
}
