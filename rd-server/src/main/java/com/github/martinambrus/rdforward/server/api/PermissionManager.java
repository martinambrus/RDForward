package com.github.martinambrus.rdforward.server.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple op/non-op permission system.
 *
 * Operators are stored in an "ops.txt" file (one username per line),
 * matching Minecraft's classic server behavior. Console commands always
 * have operator permissions.
 *
 * Mods can check permissions via {@link #isOp(String)} and modify them
 * via {@link #addOp(String)} / {@link #removeOp(String)}.
 */
public final class PermissionManager {

    private PermissionManager() {}

    private static final String OPS_FILE = "ops.txt";
    private static final Set<String> ops = ConcurrentHashMap.newKeySet();

    /**
     * Load operator list from ops.txt. Creates the file if it doesn't exist.
     */
    public static void load() {
        File file = new File(OPS_FILE);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    ops.add(line);
                }
            }
            System.out.println("Loaded " + ops.size() + " operator(s) from " + OPS_FILE);
        } catch (IOException e) {
            System.err.println("Failed to load ops: " + e.getMessage());
        }
    }

    /**
     * Save operator list to ops.txt.
     */
    public static void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OPS_FILE))) {
            writer.write("# Server operators (one per line)");
            writer.newLine();
            for (String op : ops) {
                writer.write(op);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save ops: " + e.getMessage());
        }
    }

    /** Check if a player has operator permissions. */
    public static boolean isOp(String username) {
        return ops.contains(username);
    }

    /** Grant operator permissions to a player. */
    public static void addOp(String username) {
        if (ops.add(username)) {
            save();
        }
    }

    /** Revoke operator permissions from a player. */
    public static void removeOp(String username) {
        if (ops.remove(username)) {
            save();
        }
    }

    /** Get all operator names (unmodifiable). */
    public static Set<String> getOps() {
        return Collections.unmodifiableSet(ops);
    }
}
