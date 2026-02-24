package com.github.martinambrus.rdforward.e2e.agent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Writes agent status to a JSON file for the orchestrator to poll.
 * Uses atomic write (temp file + rename) to prevent partial reads.
 */
public class StatusWriter {

    private final File statusFile;
    private final File statusDir;

    public StatusWriter(File statusDir) {
        this.statusDir = statusDir;
        this.statusFile = new File(statusDir, "status.json");
        statusDir.mkdirs();
    }

    /**
     * Write the current agent state to status.json.
     * Minimal hand-written JSON to avoid pulling in a JSON library for Java 8.
     */
    public void write(String state, int tick, List<String> screenshots,
                      double[] playerPosition, String error) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"state\": \"").append(escape(state)).append("\",\n");
        sb.append("  \"tick\": ").append(tick).append(",\n");
        sb.append("  \"screenshots\": [");
        for (int i = 0; i < screenshots.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(escape(screenshots.get(i))).append("\"");
        }
        sb.append("],\n");
        if (playerPosition != null && playerPosition.length == 3) {
            sb.append("  \"playerPosition\": [")
              .append(playerPosition[0]).append(", ")
              .append(playerPosition[1]).append(", ")
              .append(playerPosition[2]).append("],\n");
        } else {
            sb.append("  \"playerPosition\": null,\n");
        }
        if (error != null) {
            sb.append("  \"error\": \"").append(escape(error)).append("\"\n");
        } else {
            sb.append("  \"error\": null\n");
        }
        sb.append("}\n");

        writeAtomic(sb.toString());
    }

    /**
     * Extended status write with scenario progress information.
     */
    public void write(String state, int tick, List<String> screenshots,
                      double[] playerPosition, String error,
                      String scenarioStep, int stepIndex, int totalSteps,
                      Map<String, String> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"state\": \"").append(escape(state)).append("\",\n");
        sb.append("  \"tick\": ").append(tick).append(",\n");
        sb.append("  \"screenshots\": [");
        for (int i = 0; i < screenshots.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(escape(screenshots.get(i))).append("\"");
        }
        sb.append("],\n");
        if (playerPosition != null && playerPosition.length == 3) {
            sb.append("  \"playerPosition\": [")
              .append(playerPosition[0]).append(", ")
              .append(playerPosition[1]).append(", ")
              .append(playerPosition[2]).append("],\n");
        } else {
            sb.append("  \"playerPosition\": null,\n");
        }
        if (scenarioStep != null) {
            sb.append("  \"scenarioStep\": \"").append(escape(scenarioStep)).append("\",\n");
        }
        sb.append("  \"stepIndex\": ").append(stepIndex).append(",\n");
        sb.append("  \"totalSteps\": ").append(totalSteps).append(",\n");
        if (results != null && !results.isEmpty()) {
            sb.append("  \"results\": {");
            boolean first = true;
            for (Map.Entry<String, String> entry : results.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\n    \"").append(escape(entry.getKey())).append("\": \"")
                  .append(escape(entry.getValue())).append("\"");
                first = false;
            }
            sb.append("\n  },\n");
        }
        if (error != null) {
            sb.append("  \"error\": \"").append(escape(error)).append("\"\n");
        } else {
            sb.append("  \"error\": null\n");
        }
        sb.append("}\n");

        writeAtomic(sb.toString());
    }

    private void writeAtomic(String content) {
        File tmp = new File(statusDir, "status.json.tmp");
        FileWriter fw = null;
        try {
            fw = new FileWriter(tmp);
            fw.write(content);
            fw.flush();
            fw.close();
            fw = null;
            if (!tmp.renameTo(statusFile)) {
                statusFile.delete();
                tmp.renameTo(statusFile);
            }
        } catch (IOException e) {
            System.err.println("[McTestAgent] Failed to write status: " + e.getMessage());
        } finally {
            if (fw != null) {
                try { fw.close(); } catch (IOException ignored) {}
            }
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
