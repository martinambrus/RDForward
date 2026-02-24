package com.github.martinambrus.rdforward.e2e;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.ImageComparisonUtil;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Wraps romankh3/image-comparison for screenshot baseline diffing.
 * Compares a test screenshot against a golden baseline and reports
 * whether the similarity threshold is met.
 */
public class ImageComparator {

    private final double similarityThreshold;

    /**
     * @param similarityThreshold minimum similarity ratio (0.0 to 1.0). Default 0.90.
     */
    public ImageComparator(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public ImageComparator() {
        this(0.90);
    }

    /**
     * Compare a test screenshot against a baseline image.
     *
     * @param baseline  the golden baseline PNG
     * @param actual    the test screenshot PNG
     * @param diffDest  where to save the diff image on failure (may be null)
     * @return comparison result
     */
    public ComparisonResult compare(File baseline, File actual, File diffDest) throws IOException {
        BufferedImage baselineImg = ImageComparisonUtil.readImageFromResources(baseline.getAbsolutePath());
        BufferedImage actualImg = ImageComparisonUtil.readImageFromResources(actual.getAbsolutePath());

        ImageComparison comparison = new ImageComparison(baselineImg, actualImg);
        // Allow minor per-pixel variation (lighting, anti-aliasing)
        comparison.setPixelToleranceLevel(0.1);
        comparison.setAllowingPercentOfDifferentPixels(
                (1.0 - similarityThreshold) * 100.0);

        ImageComparisonResult result = comparison.compareImages();
        boolean passed = result.getImageComparisonState() == ImageComparisonState.MATCH
                || result.getDifferencePercent() <= (1.0 - similarityThreshold) * 100.0;

        if (!passed && diffDest != null) {
            ImageComparisonUtil.saveImage(diffDest, result.getResult());
            System.out.println("[E2E] Diff image saved: " + diffDest.getAbsolutePath());
        }

        return new ComparisonResult(passed, result.getDifferencePercent());
    }

    public static class ComparisonResult {
        public final boolean passed;
        public final double differencePercent;

        public ComparisonResult(boolean passed, double differencePercent) {
            this.passed = passed;
            this.differencePercent = differencePercent;
        }
    }
}
