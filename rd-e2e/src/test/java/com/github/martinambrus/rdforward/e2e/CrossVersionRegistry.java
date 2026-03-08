package com.github.martinambrus.rdforward.e2e;

import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Central registry of all client versions for cross-version matrix testing.
 * Versions are listed in chronological order. The triangular matrix
 * generator produces (i,j) pairs where j >= i, avoiding duplicate
 * reverse-order tests since CrossClientScenario tests bidirectional
 * visibility.
 */
public final class CrossVersionRegistry {

    public enum Category {
        RUBYDUNG, ALPHA, BETA, PRE_NETTY_RELEASE,
        NETTY_LWJGL2, NETTY_LWJGL3, MODERN
    }

    /** Weight tier for parallelism: determined by the heaviest client in a pair. */
    public enum Weight {
        /** Both clients are LWJGL2 or lighter — 6 parallel forks. */
        LIGHT,
        /** Heaviest client is LWJGL3 (1.13-1.18.2) — 2 parallel forks. */
        LWJGL3,
        /** Heaviest client is Modern (1.19+) — 2 parallel forks. */
        MODERN
    }

    public static final class Version {
        /** Launcher method suffix, e.g. "Alpha126" -> ClientLauncher.launchAlpha126().
         *  Special value "rubydung" uses launchModdedClient(). */
        public final String launchKey;
        /** Human-readable name for test reports */
        public final String displayName;
        public final Category category;

        Version(String launchKey, String displayName, Category category) {
            this.launchKey = launchKey;
            this.displayName = displayName;
            this.category = category;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // All 117 versions in chronological order.
    // The index in this list determines the triangular matrix pairing:
    // version at index i pairs with all versions at index j >= i.
    public static final List<Version> ALL_VERSIONS;

    static {
        List<Version> v = new ArrayList<>();

        // RubyDung (1)
        v.add(new Version("rubydung", "RubyDung", Category.RUBYDUNG));

        // Alpha (8)
        v.add(new Version("Alpha1015", "Alpha 1.0.15", Category.ALPHA));
        v.add(new Version("Alpha1016", "Alpha 1.0.16", Category.ALPHA));
        v.add(new Version("Alpha1017", "Alpha 1.0.17", Category.ALPHA));
        v.add(new Version("Alpha110", "Alpha 1.1.0", Category.ALPHA));
        v.add(new Version("Alpha120", "Alpha 1.2.0", Category.ALPHA));
        v.add(new Version("Alpha122", "Alpha 1.2.2", Category.ALPHA));
        v.add(new Version("Alpha123", "Alpha 1.2.3", Category.ALPHA));
        v.add(new Version("Alpha126", "Alpha 1.2.6", Category.ALPHA));

        // Beta (10)
        v.add(new Version("Beta10", "Beta 1.0", Category.BETA));
        v.add(new Version("Beta122", "Beta 1.2.2", Category.BETA));
        v.add(new Version("Beta131", "Beta 1.3.1", Category.BETA));
        v.add(new Version("Beta141", "Beta 1.4.1", Category.BETA));
        v.add(new Version("Beta151", "Beta 1.5.1", Category.BETA));
        v.add(new Version("Beta16", "Beta 1.6", Category.BETA));
        v.add(new Version("Beta17", "Beta 1.7", Category.BETA));
        v.add(new Version("Beta173", "Beta 1.7.3", Category.BETA));
        v.add(new Version("Beta181", "Beta 1.8.1", Category.BETA));
        v.add(new Version("Beta19Pre5", "Beta 1.9-pre5", Category.BETA));

        // Pre-Netty Release (19)
        v.add(new Version("Release10", "Release 1.0", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release11", "Release 1.1", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release1_2_1", "Release 1.2.1", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release1_2_2", "Release 1.2.2", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release1_2_3", "Release 1.2.3", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release1_2_4", "Release 1.2.4", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release1_2_5", "Release 1.2.5", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release131", "Release 1.3.1", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release132", "Release 1.3.2", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release142", "Release 1.4.2", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release144", "Release 1.4.4", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release145", "Release 1.4.5", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release146", "Release 1.4.6", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release147", "Release 1.4.7", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release151", "Release 1.5.1", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release152", "Release 1.5.2", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release161", "Release 1.6.1", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release162", "Release 1.6.2", Category.PRE_NETTY_RELEASE));
        v.add(new Version("Release164", "Release 1.6.4", Category.PRE_NETTY_RELEASE));

        // Netty LWJGL2 Release (33): 1.7.2-1.12.2
        v.add(new Version("Release172", "Release 1.7.2", Category.NETTY_LWJGL2));
        v.add(new Version("Release173", "Release 1.7.3", Category.NETTY_LWJGL2));
        v.add(new Version("Release174", "Release 1.7.4", Category.NETTY_LWJGL2));
        v.add(new Version("Release175", "Release 1.7.5", Category.NETTY_LWJGL2));
        v.add(new Version("Release176", "Release 1.7.6", Category.NETTY_LWJGL2));
        v.add(new Version("Release177", "Release 1.7.7", Category.NETTY_LWJGL2));
        v.add(new Version("Release178", "Release 1.7.8", Category.NETTY_LWJGL2));
        v.add(new Version("Release179", "Release 1.7.9", Category.NETTY_LWJGL2));
        v.add(new Version("Release1710", "Release 1.7.10", Category.NETTY_LWJGL2));
        v.add(new Version("Release18", "Release 1.8", Category.NETTY_LWJGL2));
        v.add(new Version("Release181", "Release 1.8.1", Category.NETTY_LWJGL2));
        v.add(new Version("Release182", "Release 1.8.2", Category.NETTY_LWJGL2));
        v.add(new Version("Release183", "Release 1.8.3", Category.NETTY_LWJGL2));
        v.add(new Version("Release184", "Release 1.8.4", Category.NETTY_LWJGL2));
        v.add(new Version("Release185", "Release 1.8.5", Category.NETTY_LWJGL2));
        v.add(new Version("Release186", "Release 1.8.6", Category.NETTY_LWJGL2));
        v.add(new Version("Release187", "Release 1.8.7", Category.NETTY_LWJGL2));
        v.add(new Version("Release188", "Release 1.8.8", Category.NETTY_LWJGL2));
        v.add(new Version("Release189", "Release 1.8.9", Category.NETTY_LWJGL2));
        v.add(new Version("Release19", "Release 1.9", Category.NETTY_LWJGL2));
        v.add(new Version("Release191", "Release 1.9.1", Category.NETTY_LWJGL2));
        v.add(new Version("Release192", "Release 1.9.2", Category.NETTY_LWJGL2));
        v.add(new Version("Release193", "Release 1.9.3", Category.NETTY_LWJGL2));
        v.add(new Version("Release194", "Release 1.9.4", Category.NETTY_LWJGL2));
        v.add(new Version("Release110", "Release 1.10", Category.NETTY_LWJGL2));
        v.add(new Version("Release1101", "Release 1.10.1", Category.NETTY_LWJGL2));
        v.add(new Version("Release1102", "Release 1.10.2", Category.NETTY_LWJGL2));
        v.add(new Version("Release111", "Release 1.11", Category.NETTY_LWJGL2));
        v.add(new Version("Release1111", "Release 1.11.1", Category.NETTY_LWJGL2));
        v.add(new Version("Release1112", "Release 1.11.2", Category.NETTY_LWJGL2));
        v.add(new Version("Release112", "Release 1.12", Category.NETTY_LWJGL2));
        v.add(new Version("Release1121", "Release 1.12.1", Category.NETTY_LWJGL2));
        v.add(new Version("Release1122", "Release 1.12.2", Category.NETTY_LWJGL2));

        // Netty LWJGL3 Release (22): 1.13-1.18.2
        v.add(new Version("Release113", "Release 1.13", Category.NETTY_LWJGL3));
        v.add(new Version("Release1131", "Release 1.13.1", Category.NETTY_LWJGL3));
        v.add(new Version("Release1132", "Release 1.13.2", Category.NETTY_LWJGL3));
        v.add(new Version("Release114", "Release 1.14", Category.NETTY_LWJGL3));
        v.add(new Version("Release1141", "Release 1.14.1", Category.NETTY_LWJGL3));
        v.add(new Version("Release1142", "Release 1.14.2", Category.NETTY_LWJGL3));
        v.add(new Version("Release1143", "Release 1.14.3", Category.NETTY_LWJGL3));
        v.add(new Version("Release1144", "Release 1.14.4", Category.NETTY_LWJGL3));
        v.add(new Version("Release115", "Release 1.15", Category.NETTY_LWJGL3));
        v.add(new Version("Release1151", "Release 1.15.1", Category.NETTY_LWJGL3));
        v.add(new Version("Release1152", "Release 1.15.2", Category.NETTY_LWJGL3));
        v.add(new Version("Release116", "Release 1.16", Category.NETTY_LWJGL3));
        v.add(new Version("Release1161", "Release 1.16.1", Category.NETTY_LWJGL3));
        v.add(new Version("Release1162", "Release 1.16.2", Category.NETTY_LWJGL3));
        v.add(new Version("Release1163", "Release 1.16.3", Category.NETTY_LWJGL3));
        v.add(new Version("Release1164", "Release 1.16.4", Category.NETTY_LWJGL3));
        v.add(new Version("Release1165", "Release 1.16.5", Category.NETTY_LWJGL3));
        v.add(new Version("Release117", "Release 1.17", Category.NETTY_LWJGL3));
        v.add(new Version("Release1171", "Release 1.17.1", Category.NETTY_LWJGL3));
        v.add(new Version("Release118", "Release 1.18", Category.NETTY_LWJGL3));
        v.add(new Version("Release1181", "Release 1.18.1", Category.NETTY_LWJGL3));
        v.add(new Version("Release1182", "Release 1.18.2", Category.NETTY_LWJGL3));

        // Modern Release (24): 1.19+
        v.add(new Version("Release119", "Release 1.19", Category.MODERN));
        v.add(new Version("Release1191", "Release 1.19.1", Category.MODERN));
        v.add(new Version("Release1192", "Release 1.19.2", Category.MODERN));
        v.add(new Version("Release1193", "Release 1.19.3", Category.MODERN));
        v.add(new Version("Release1194", "Release 1.19.4", Category.MODERN));
        v.add(new Version("Release120", "Release 1.20", Category.MODERN));
        v.add(new Version("Release1201", "Release 1.20.1", Category.MODERN));
        v.add(new Version("Release1202", "Release 1.20.2", Category.MODERN));
        v.add(new Version("Release1203", "Release 1.20.3", Category.MODERN));
        v.add(new Version("Release1204", "Release 1.20.4", Category.MODERN));
        v.add(new Version("Release1205", "Release 1.20.5", Category.MODERN));
        v.add(new Version("Release1206", "Release 1.20.6", Category.MODERN));
        v.add(new Version("Release121", "Release 1.21", Category.MODERN));
        v.add(new Version("Release1211", "Release 1.21.1", Category.MODERN));
        v.add(new Version("Release1212", "Release 1.21.2", Category.MODERN));
        v.add(new Version("Release1213", "Release 1.21.3", Category.MODERN));
        v.add(new Version("Release1214", "Release 1.21.4", Category.MODERN));
        v.add(new Version("Release1215", "Release 1.21.5", Category.MODERN));
        v.add(new Version("Release1216", "Release 1.21.6", Category.MODERN));
        v.add(new Version("Release1217", "Release 1.21.7", Category.MODERN));
        v.add(new Version("Release1218", "Release 1.21.8", Category.MODERN));
        v.add(new Version("Release1219", "Release 1.21.9", Category.MODERN));
        v.add(new Version("Release12110", "Release 1.21.10", Category.MODERN));
        v.add(new Version("Release12111", "Release 1.21.11", Category.MODERN));

        ALL_VERSIONS = v;
    }

    /** Index of the first non-RubyDung version (Alpha 1.0.15). */
    public static final int FIRST_NON_RD = 1;

    /**
     * Generate triangular pairs for @ParameterizedTest.
     * For each primary at index i (within [fromIndex, toIndex)),
     * pairs with all secondaries at index j >= i (from the full list).
     *
     * @param fromIndex first primary index (inclusive)
     * @param toIndex   last primary index (exclusive), or -1 for all
     * @return stream of Arguments(primaryDisplay, secondaryDisplay, primaryKey, secondaryKey)
     */
    public static Stream<Arguments> triangularPairs(int fromIndex, int toIndex) {
        if (toIndex < 0) toIndex = ALL_VERSIONS.size();
        List<Arguments> pairs = new ArrayList<>();
        for (int i = fromIndex; i < toIndex; i++) {
            Version primary = ALL_VERSIONS.get(i);
            for (int j = i; j < ALL_VERSIONS.size(); j++) {
                Version secondary = ALL_VERSIONS.get(j);
                pairs.add(Arguments.of(
                        primary.displayName, secondary.displayName,
                        primary.launchKey, secondary.launchKey));
            }
        }
        return pairs.stream();
    }

    /**
     * Generate triangular pairs where the primary belongs to a specific category.
     */
    public static Stream<Arguments> triangularPairsForCategory(Category primaryCategory) {
        List<Arguments> pairs = new ArrayList<>();
        for (int i = 0; i < ALL_VERSIONS.size(); i++) {
            Version primary = ALL_VERSIONS.get(i);
            if (primary.category != primaryCategory) continue;
            for (int j = i; j < ALL_VERSIONS.size(); j++) {
                Version secondary = ALL_VERSIONS.get(j);
                pairs.add(Arguments.of(
                        primary.displayName, secondary.displayName,
                        primary.launchKey, secondary.launchKey));
            }
        }
        return pairs.stream();
    }

    /**
     * Generate all non-RubyDung triangular pairs.
     */
    public static Stream<Arguments> allNonRubyDungPairs() {
        return triangularPairs(FIRST_NON_RD, -1);
    }

    /**
     * Generate all RubyDung pairs (RubyDung vs every version including itself).
     */
    public static Stream<Arguments> rubyDungPairs() {
        return triangularPairs(0, 1);
    }

    /**
     * Determine the weight tier for a pair based on the heaviest category.
     */
    public static Weight weightOf(Category a, Category b) {
        if (a == Category.MODERN || b == Category.MODERN) return Weight.MODERN;
        if (a == Category.NETTY_LWJGL3 || b == Category.NETTY_LWJGL3) return Weight.LWJGL3;
        return Weight.LIGHT;
    }

    /**
     * Generate triangular pairs filtered by weight tier.
     *
     * @param weight         the weight tier to filter for
     * @param includeRubyDung if true, include pairs with RubyDung as primary;
     *                        if false, exclude them
     */
    public static Stream<Arguments> pairsByWeight(Weight weight, boolean includeRubyDung) {
        List<Arguments> pairs = new ArrayList<>();
        int startIdx = includeRubyDung ? 0 : FIRST_NON_RD;
        int endIdx = includeRubyDung ? 1 : ALL_VERSIONS.size();
        for (int i = startIdx; i < endIdx; i++) {
            Version primary = ALL_VERSIONS.get(i);
            for (int j = i; j < ALL_VERSIONS.size(); j++) {
                Version secondary = ALL_VERSIONS.get(j);
                if (weightOf(primary.category, secondary.category) != weight) continue;
                pairs.add(Arguments.of(
                        primary.displayName, secondary.displayName,
                        primary.launchKey, secondary.launchKey));
            }
        }
        return pairs.stream();
    }

    /**
     * Generate a shard of weight-filtered non-RubyDung pairs.
     * Pairs are divided evenly into {@code totalShards} shards.
     */
    public static Stream<Arguments> nonRdPairsByWeightShard(Weight weight,
            int shardIndex, int totalShards) {
        List<Arguments> all = pairsByWeight(weight, false).collect(Collectors.toList());
        int size = all.size();
        int from = shardIndex * size / totalShards;
        int to = (shardIndex + 1) * size / totalShards;
        return all.subList(from, to).stream();
    }

    /**
     * Generate RubyDung pairs filtered by weight tier.
     * RubyDung is always the primary (index 0), paired with versions
     * whose weight matches.
     */
    public static Stream<Arguments> rdPairsByWeight(Weight weight) {
        return pairsByWeight(weight, true);
    }

    /**
     * Generate a shard of RubyDung pairs filtered by weight tier.
     * Used to split large RD pair sets across multiple test classes to
     * avoid Xvfb resource exhaustion from too many sequential client launches.
     */
    public static Stream<Arguments> rdPairsByWeightShard(Weight weight,
            int shardIndex, int totalShards) {
        List<Arguments> all = rdPairsByWeight(weight).collect(Collectors.toList());
        int size = all.size();
        int from = shardIndex * size / totalShards;
        int to = (shardIndex + 1) * size / totalShards;
        return all.subList(from, to).stream();
    }

    /**
     * Look up a version's category by its launch key.
     */
    public static Category categoryOf(String launchKey) {
        for (Version v : ALL_VERSIONS) {
            if (v.launchKey.equals(launchKey)) return v.category;
        }
        throw new IllegalArgumentException("Unknown launch key: " + launchKey);
    }

    /**
     * Get the cross-test subfolder name for a category.
     */
    public static String categoryFolderName(Category cat) {
        switch (cat) {
            case RUBYDUNG: return "cross-RD";
            case ALPHA: return "cross-Alpha";
            case BETA: return "cross-Beta";
            case PRE_NETTY_RELEASE: return "cross-PreNettyRelease";
            case NETTY_LWJGL2: return "cross-NettyLwjgl2";
            case NETTY_LWJGL3: return "cross-NettyLwjgl3";
            case MODERN: return "cross-Modern";
            default: throw new IllegalArgumentException("Unknown category: " + cat);
        }
    }

    /**
     * Build the baseline directory path for a cross-version pair.
     * Structure: cross-tests/{categoryFolder}/{primaryKey}_{secondaryKey}
     */
    public static String crossBaselineId(String primaryKey, String secondaryKey) {
        Category primaryCat = categoryOf(primaryKey);
        return "cross-tests/" + categoryFolderName(primaryCat) + "/"
                + primaryKey.toLowerCase() + "_" + secondaryKey.toLowerCase();
    }

    private CrossVersionRegistry() {}
}
