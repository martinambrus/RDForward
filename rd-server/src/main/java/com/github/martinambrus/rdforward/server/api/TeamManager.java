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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistent trust/team system for grief protection.
 *
 * <p>Players can add others to their trust list. Trusted players can break
 * the owner's blocks without triggering grief protection scoring.
 *
 * <p>Trust is asymmetric: if Alice trusts Bob, Bob can break Alice's blocks
 * freely — but Alice cannot break Bob's blocks unless Bob also trusts her.
 *
 * <p>Thread-safe: uses ConcurrentHashMap.
 */
public final class TeamManager {

    private TeamManager() {}

    private static final String TEAMS_FILE_NAME = "teams.txt";
    private static final ConcurrentHashMap<String, Set<String>> teams = new ConcurrentHashMap<>();
    /** Tracks the last player who trusted each player (for /trustback). */
    private static final ConcurrentHashMap<String, String> lastTruster = new ConcurrentHashMap<>();
    private static volatile File teamsFile = new File(TEAMS_FILE_NAME);
    private static volatile boolean dirty = false;

    /**
     * Load teams from disk, resolving file relative to dataDir.
     */
    public static void load(File dataDir) {
        File dir = (dataDir != null) ? dataDir : new File(".");
        teamsFile = new File(dir, TEAMS_FILE_NAME);
        teams.clear();
        if (!teamsFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(teamsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int colon = line.indexOf(':');
                if (colon < 0) continue;
                String owner = line.substring(0, colon).trim().toLowerCase();
                String membersStr = line.substring(colon + 1).trim();
                if (membersStr.isEmpty()) continue;
                Set<String> members = ConcurrentHashMap.newKeySet();
                for (String m : membersStr.split(",")) {
                    String trimmed = m.trim().toLowerCase();
                    if (!trimmed.isEmpty()) members.add(trimmed);
                }
                if (!members.isEmpty()) {
                    teams.put(owner, members);
                }
            }
            int total = teams.values().stream().mapToInt(Set::size).sum();
            System.out.println("Loaded " + teams.size() + " trust list(s) (" + total + " total entries) from " + teamsFile);
        } catch (IOException e) {
            System.err.println("Failed to load " + teamsFile + ": " + e.getMessage());
        }
    }

    /** Save only if there are unsaved changes. */
    public static void saveIfDirty() {
        if (dirty) save();
    }

    private static synchronized void save() {
        File tempFile = new File(teamsFile.getParentFile(), TEAMS_FILE_NAME + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("# Player trust lists (format: owner:member1,member2,...)");
            writer.newLine();
            writer.write("# Players in your trust list can break your blocks without triggering grief protection.");
            writer.newLine();
            ArrayList<String> owners = new ArrayList<>(teams.keySet());
            Collections.sort(owners);
            for (String owner : owners) {
                Set<String> members = teams.get(owner);
                if (members == null || members.isEmpty()) continue;
                ArrayList<String> sorted = new ArrayList<>(members);
                Collections.sort(sorted);
                writer.write(owner + ":" + String.join(",", sorted));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to write teams temp file: " + e.getMessage());
            return;
        }
        try {
            Files.move(tempFile.toPath(), teamsFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.move(tempFile.toPath(), teamsFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e2) {
                System.err.println("Failed to save teams file: " + e2.getMessage());
            }
        }
        dirty = false;
    }

    /**
     * Add a teammate to the owner's trust list.
     * @return true if added, false if already trusted
     */
    public static boolean addTeammate(String owner, String teammate) {
        Set<String> members = teams.computeIfAbsent(owner.toLowerCase(),
                k -> ConcurrentHashMap.newKeySet());
        if (members.add(teammate.toLowerCase())) {
            dirty = true;
            return true;
        }
        return false;
    }

    /**
     * Remove a teammate from the owner's trust list.
     * @return true if removed, false if not in list
     */
    public static boolean removeTeammate(String owner, String teammate) {
        Set<String> members = teams.get(owner.toLowerCase());
        if (members != null && members.remove(teammate.toLowerCase())) {
            if (members.isEmpty()) teams.remove(owner.toLowerCase());
            dirty = true;
            return true;
        }
        return false;
    }

    /**
     * Check if the breaker is trusted by the block owner.
     * Both names are compared case-insensitively.
     */
    public static boolean isTeammate(String owner, String breaker) {
        Set<String> members = teams.get(owner.toLowerCase());
        return members != null && members.contains(breaker.toLowerCase());
    }

    /**
     * Get the owner's trusted teammates (unmodifiable snapshot, lowercase).
     */
    public static Set<String> getTeammates(String owner) {
        Set<String> members = teams.get(owner.toLowerCase());
        if (members == null || members.isEmpty()) return Collections.emptySet();
        return Collections.unmodifiableSet(members);
    }

    /**
     * Record that {@code truster} just trusted {@code trustee}, so
     * {@code trustee} can use /trustback to reciprocate.
     */
    public static void setLastTruster(String trustee, String truster) {
        lastTruster.put(trustee.toLowerCase(), truster);
    }

    /**
     * Get the last player who trusted this player (for /trustback).
     * Returns the original-case name, or null if nobody has trusted them.
     */
    public static String getLastTruster(String player) {
        return lastTruster.get(player.toLowerCase());
    }

    /**
     * Get the owner's trusted teammates as a sorted list (for numbered removal).
     */
    public static java.util.List<String> getTeammatesList(String owner) {
        Set<String> members = teams.get(owner.toLowerCase());
        if (members == null || members.isEmpty()) return java.util.Collections.emptyList();
        java.util.List<String> sorted = new java.util.ArrayList<>(members);
        java.util.Collections.sort(sorted);
        return sorted;
    }

    /** Clear all state. Package-private, for testing only. */
    static void clearForTesting() {
        teams.clear();
        lastTruster.clear();
        dirty = false;
    }
}
