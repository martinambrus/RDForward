package com.github.martinambrus.rdforward.e2e;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Shared utility that composes {@link BaselineManager} + {@link ImageComparator}
 * to verify screenshot baselines for any e2e test.
 *
 * On first run (no baseline exists): records baseline.
 * On subsequent runs: compares against baseline, collects all failures (soft assertions).
 */
public class ScreenshotBaselineVerifier {

    private final String version;
    private final String scenario;
    private final double threshold;
    private final BaselineManager baselines;
    private final ImageComparator comparator;

    public ScreenshotBaselineVerifier(String version, String scenario, double threshold) {
        this.version = version;
        this.scenario = scenario;
        this.threshold = threshold;
        this.baselines = new BaselineManager();
        this.comparator = new ImageComparator(threshold);
    }

    public ScreenshotBaselineVerifier(String version, String scenario) {
        this(version, scenario, 0.70);
    }

    /**
     * Constructor without scenario subdirectory (for flat baseline layouts).
     */
    public ScreenshotBaselineVerifier(String version, double threshold) {
        this(version, null, threshold);
    }

    /**
     * Verify all named screenshots exist, are non-empty, and match their baselines.
     *
     * @param statusDir the directory containing screenshots from the test run
     * @param filenames screenshot filenames to verify (e.g. "grass_broken.png")
     */
    public void verifyAll(File statusDir, String... filenames) throws IOException {
        List<String> failures = new ArrayList<>();

        for (String filename : filenames) {
            File screenshot = new File(statusDir, filename);

            // Assert exists + non-empty
            if (!screenshot.exists()) {
                failures.add("Screenshot " + filename + " not produced");
                continue;
            }
            if (screenshot.length() == 0) {
                failures.add("Screenshot " + filename + " is empty");
                continue;
            }

            // Derive checkpoint from filename (strip .png)
            String checkpoint = filename.endsWith(".png")
                    ? filename.substring(0, filename.length() - 4)
                    : filename;

            if (!baselines.hasBaseline(version, scenario, checkpoint)) {
                // First run: record baseline
                baselines.recordBaseline(screenshot, version, scenario, checkpoint);
                System.out.println("[E2E] First run -- baseline recorded for "
                        + version + "/" + scenario + "/" + checkpoint);
            } else {
                // Subsequent runs: compare against baseline
                File baseline = baselines.getBaselinePath(version, scenario, checkpoint);
                File diff = baselines.getDiffPath(version, scenario, checkpoint);

                ImageComparator.ComparisonResult result =
                        comparator.compare(baseline, screenshot, diff);

                if (!result.passed) {
                    failures.add(filename + " differs from baseline by "
                            + String.format("%.2f", result.differencePercent)
                            + "% (threshold: " + ((1.0 - threshold) * 100.0)
                            + "%). Diff: " + diff.getAbsolutePath());
                }
            }
        }

        if (!failures.isEmpty()) {
            fail("Screenshot baseline verification failed:\n- "
                    + String.join("\n- ", failures));
        }
    }

    /**
     * Verify a single screenshot against a flat baseline (no scenario subdirectory).
     * The source file in statusDir may have a different name than the baseline.
     *
     * @param statusDir          directory containing the screenshot from the test run
     * @param sourceFilename     filename of the screenshot in statusDir
     * @param baselineCheckpoint baseline checkpoint name (without .png)
     */
    public void verifyCross(File statusDir, String sourceFilename,
            String baselineCheckpoint) throws IOException {
        File screenshot = new File(statusDir, sourceFilename);

        if (!screenshot.exists()) {
            fail("Screenshot " + sourceFilename + " not produced");
            return;
        }
        if (screenshot.length() == 0) {
            fail("Screenshot " + sourceFilename + " is empty");
            return;
        }

        if (!baselines.hasBaseline(version, baselineCheckpoint)) {
            baselines.recordBaseline(screenshot, version, baselineCheckpoint);
            System.out.println("[E2E] First run -- baseline recorded for "
                    + version + "/" + baselineCheckpoint);
        } else {
            File baseline = baselines.getBaselinePath(version, baselineCheckpoint);
            File diff = baselines.getDiffPath(version, baselineCheckpoint);

            ImageComparator.ComparisonResult result =
                    comparator.compare(baseline, screenshot, diff);

            if (!result.passed) {
                fail(baselineCheckpoint + " differs from baseline by "
                        + String.format("%.2f", result.differencePercent)
                        + "% (threshold: " + ((1.0 - threshold) * 100.0)
                        + "%). Diff: " + diff.getAbsolutePath());
            }
        }
    }
}
