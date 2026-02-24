package com.github.martinambrus.rdforward.e2e.agent.scenario;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * File-based inter-client synchronization for cross-client testing.
 * Each client writes marker files when reaching a sync point and polls
 * for the other client's marker. Data exchange uses separate key-value files.
 */
public class SyncBarrier {

    private final File syncDir;
    private final String role; // "primary" or "secondary"

    public SyncBarrier(File syncDir, String role) {
        this.syncDir = syncDir;
        this.role = role;
    }

    /**
     * Signal that this client reached the named barrier.
     */
    public void signal(String barrierName) {
        try {
            new File(syncDir, role + "_" + barrierName).createNewFile();
            System.out.println("[McTestAgent] Signaled barrier: " + role + "_" + barrierName);
        } catch (IOException e) {
            System.err.println("[McTestAgent] Failed to signal barrier " + barrierName + ": " + e.getMessage());
        }
    }

    /**
     * Check if the other client has reached the named barrier.
     * Non-blocking — returns immediately.
     *
     * @return true if the other client's marker exists
     */
    public boolean waitFor(String barrierName) {
        String otherRole = "primary".equals(role) ? "secondary" : "primary";
        File marker = new File(syncDir, otherRole + "_" + barrierName);
        return marker.exists();
    }

    /**
     * Write a data value for cross-client communication.
     */
    public void writeData(String key, String value) {
        File dataFile = new File(syncDir, role + "_data_" + key);
        try {
            FileWriter fw = new FileWriter(dataFile);
            fw.write(value);
            fw.close();
        } catch (IOException e) {
            System.err.println("[McTestAgent] Failed to write data " + key + ": " + e.getMessage());
        }
    }

    /**
     * Read a data value written by the other client.
     *
     * @return the value, or null if not yet available
     */
    public String readData(String key) {
        String otherRole = "primary".equals(role) ? "secondary" : "primary";
        File dataFile = new File(syncDir, otherRole + "_data_" + key);
        if (!dataFile.exists()) return null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataFile));
            String value = reader.readLine();
            reader.close();
            return value;
        } catch (IOException e) {
            return null;
        }
    }
}
