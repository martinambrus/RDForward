package com.github.martinambrus.rdforward.e2e;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Polls the agent's status.json file for state transitions.
 * The agent writes JSON atomically (temp+rename); this reader
 * handles transient read failures gracefully.
 */
public class StatusMonitor {

    private static final long POLL_INTERVAL_MS = 200;

    private final File statusFile;

    public StatusMonitor(File statusDir) {
        this.statusFile = new File(statusDir, "status.json");
    }

    /**
     * Wait for the agent to reach a terminal state (COMPLETE or ERROR).
     *
     * @param timeoutMs maximum wait time
     * @return the final status JSON string, or null on timeout
     */
    public String waitForTerminal(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            String json = readStatus();
            if (json != null) {
                String state = extractJsonField(json, "state");
                if ("COMPLETE".equals(state) || "ERROR".equals(state)) {
                    return json;
                }
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        return null; // timeout
    }

    /**
     * Wait for a specific state.
     *
     * @param targetState the state to wait for
     * @param timeoutMs   maximum wait time
     * @return true if state was reached, false on timeout
     */
    public boolean waitForState(String targetState, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            String json = readStatus();
            if (json != null) {
                String state = extractJsonField(json, "state");
                if (targetState.equals(state)) return true;
                // If already past the target (ERROR/COMPLETE), don't keep waiting
                if ("ERROR".equals(state) || "COMPLETE".equals(state)) return false;
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        return false;
    }

    /**
     * Read the current status JSON. Returns null if file doesn't exist or is unreadable.
     */
    public String readStatus() {
        if (!statusFile.exists()) return null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(statusFile));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            // Transient: file may be mid-write
            return null;
        }
    }

    /**
     * Extract the error field from status JSON.
     */
    public String getError() {
        String json = readStatus();
        if (json == null) return null;
        return extractJsonField(json, "error");
    }

    /**
     * Extract a numeric field from status JSON.
     */
    public static int extractJsonIntField(String json, String field) {
        String pattern = "\"" + field + "\": ";
        int start = json.indexOf(pattern);
        if (start < 0) return -1;
        start += pattern.length();
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == ',' || c == '\n' || c == '}') break;
            end++;
        }
        try {
            return Integer.parseInt(json.substring(start, end).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Wait for a specific scenario step to appear in the status.
     */
    public boolean waitForStep(String stepDescription, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            String json = readStatus();
            if (json != null) {
                String step = extractJsonField(json, "scenarioStep");
                if (stepDescription.equals(step)) return true;
                String state = extractJsonField(json, "state");
                if ("ERROR".equals(state) || "COMPLETE".equals(state)) return false;
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        return false;
    }

    /**
     * Minimal JSON string field extractor. No library dependency for simple status parsing.
     */
    public static String extractJsonField(String json, String field) {
        String pattern = "\"" + field + "\": \"";
        int start = json.indexOf(pattern);
        if (start < 0) {
            // Check for null value
            String nullPattern = "\"" + field + "\": null";
            if (json.contains(nullPattern)) return null;
            return null;
        }
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }
}
