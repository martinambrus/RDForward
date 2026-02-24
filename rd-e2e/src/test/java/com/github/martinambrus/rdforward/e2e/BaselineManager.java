package com.github.martinambrus.rdforward.e2e;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Manages golden screenshot baselines.
 * On first run (no baseline exists), records the screenshot as the new baseline.
 * On subsequent runs, provides the baseline for comparison.
 *
 * Baselines are stored at: rd-e2e/baselines/{version}/{checkpoint}.png
 */
public class BaselineManager {

    private final File baselinesRoot;

    public BaselineManager() {
        this.baselinesRoot = new File("rd-e2e/baselines");
    }

    /**
     * Get the baseline file path for a given version and checkpoint.
     */
    public File getBaselinePath(String version, String checkpoint) {
        return new File(baselinesRoot, version + "/" + checkpoint + ".png");
    }

    /**
     * Get the baseline file path for a given version, scenario, and checkpoint.
     */
    public File getBaselinePath(String version, String scenario, String checkpoint) {
        return new File(baselinesRoot, version + "/" + scenario + "/" + checkpoint + ".png");
    }

    /**
     * Check if a baseline exists for the given version and checkpoint.
     */
    public boolean hasBaseline(String version, String checkpoint) {
        return getBaselinePath(version, checkpoint).exists();
    }

    /**
     * Check if a baseline exists for the given version, scenario, and checkpoint.
     */
    public boolean hasBaseline(String version, String scenario, String checkpoint) {
        return getBaselinePath(version, scenario, checkpoint).exists();
    }

    /**
     * Record a screenshot as the new baseline.
     *
     * @param screenshotFile the screenshot to use as baseline
     * @param version        client version identifier
     * @param checkpoint     checkpoint name (e.g. "world_loaded")
     */
    public void recordBaseline(File screenshotFile, String version, String checkpoint)
            throws IOException {
        File baseline = getBaselinePath(version, checkpoint);
        baseline.getParentFile().mkdirs();
        Files.copy(screenshotFile.toPath(), baseline.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        System.out.println("[E2E] Baseline recorded: " + baseline.getAbsolutePath());
    }

    /**
     * Record a screenshot as the new baseline (with scenario subdirectory).
     */
    public void recordBaseline(File screenshotFile, String version, String scenario,
            String checkpoint) throws IOException {
        File baseline = getBaselinePath(version, scenario, checkpoint);
        baseline.getParentFile().mkdirs();
        Files.copy(screenshotFile.toPath(), baseline.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        System.out.println("[E2E] Baseline recorded: " + baseline.getAbsolutePath());
    }

    /**
     * Get a path for the diff image (saved alongside the baseline on comparison failure).
     */
    public File getDiffPath(String version, String checkpoint) {
        return new File(baselinesRoot, version + "/" + checkpoint + "_diff.png");
    }

    /**
     * Get a path for the diff image (with scenario subdirectory).
     */
    public File getDiffPath(String version, String scenario, String checkpoint) {
        return new File(baselinesRoot, version + "/" + scenario + "/" + checkpoint + "_diff.png");
    }
}
