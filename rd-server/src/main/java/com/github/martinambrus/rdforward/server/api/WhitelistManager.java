package com.github.martinambrus.rdforward.server.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistent whitelist system for player usernames.
 *
 * <p>Stores whitelisted players in a text file (one entry per line,
 * stored lowercase for case-insensitive matching). When the whitelist
 * is enabled via {@code white-list=true} in server.properties, only
 * whitelisted players and operators are allowed to join.
 *
 * <p>Thread-safe: uses ConcurrentHashMap-backed set.
 */
public final class WhitelistManager {

    private WhitelistManager() {}

    private static final String WHITELIST_FILE_NAME = "white-list.txt";
    private static final Set<String> whitelistedPlayers = ConcurrentHashMap.newKeySet();
    private static volatile File whitelistFile = new File(WHITELIST_FILE_NAME);

    /**
     * Load whitelist from disk.
     */
    public static void load() {
        load(null);
    }

    /**
     * Load whitelist from disk, resolving file relative to dataDir.
     */
    public static void load(File dataDir) {
        File dir = (dataDir != null) ? dataDir : new File(".");
        whitelistFile = new File(dir, WHITELIST_FILE_NAME);
        whitelistedPlayers.clear();
        loadFile(whitelistFile, whitelistedPlayers);
        System.out.println("Loaded " + whitelistedPlayers.size() + " whitelisted player(s) from " + whitelistFile);
    }

    /**
     * Reload whitelist from the current file.
     */
    public static void reload() {
        whitelistedPlayers.clear();
        loadFile(whitelistFile, whitelistedPlayers);
    }

    private static void loadFile(File file, Set<String> target) {
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    target.add(line.toLowerCase());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load " + file + ": " + e.getMessage());
        }
    }

    private static void saveFile() {
        File tempFile = new File(whitelistFile.getParentFile(), WHITELIST_FILE_NAME + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("# Whitelisted players (one per line)");
            writer.newLine();
            ArrayList<String> sorted = new ArrayList<>(whitelistedPlayers);
            Collections.sort(sorted);
            for (String entry : sorted) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to write whitelist temp file: " + e.getMessage());
            return;
        }
        try {
            Files.move(tempFile.toPath(), whitelistFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.move(tempFile.toPath(), whitelistFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e2) {
                System.err.println("Failed to save whitelist file: " + e2.getMessage());
            }
        }
    }

    /**
     * Add a player to the whitelist.
     *
     * @return true if the player was added, false if already whitelisted
     */
    public static boolean addPlayer(String username) {
        if (whitelistedPlayers.add(username.toLowerCase())) {
            saveFile();
            return true;
        }
        return false;
    }

    /**
     * Remove a player from the whitelist.
     *
     * @return true if the player was removed, false if not whitelisted
     */
    public static boolean removePlayer(String username) {
        if (whitelistedPlayers.remove(username.toLowerCase())) {
            saveFile();
            return true;
        }
        return false;
    }

    /** Check if a player is on the whitelist. */
    public static boolean isWhitelisted(String username) {
        return whitelistedPlayers.contains(username.toLowerCase());
    }

    /**
     * Check if a player is allowed to join considering whitelist state.
     *
     * <p>Returns true if:
     * <ul>
     *   <li>Whitelist is disabled ({@code white-list=false}), OR</li>
     *   <li>The player is an operator (ops bypass whitelist), OR</li>
     *   <li>The player is on the whitelist</li>
     * </ul>
     */
    public static boolean isAllowed(String username) {
        if (!ServerProperties.isWhiteList()) return true;
        if (PermissionManager.isOp(username)) return true;
        return isWhitelisted(username);
    }

    /** Get all whitelisted player names (unmodifiable snapshot, lowercase). */
    public static Set<String> getWhitelistedPlayers() {
        return Collections.unmodifiableSet(whitelistedPlayers);
    }

    /** Clear all state. Package-private, for testing only. */
    static void clearForTesting() {
        whitelistedPlayers.clear();
    }
}
