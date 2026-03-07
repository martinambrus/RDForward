package com.github.martinambrus.rdforward.e2e.scenarios;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Quick smoke test: single cross-version pair (Alpha 1.2.6 vs Alpha 1.2.6).
 * Verifies the matrix infrastructure works before running the full matrix.
 *
 * Run with: ./gradlew :rd-e2e:testCrossVersion --tests "*.CrossVersionSmokeTest" -Pe2e
 */
class CrossVersionSmokeTest extends CrossVersionMatrixTestBase {

    @BeforeAll
    static void setUp() throws Exception {
        initServer(128);
    }

    @AfterAll
    static void tearDown() {
        tearDownBase();
    }

    @Test
    void alpha126VsAlpha126() throws Exception {
        runCrossClientTest("Alpha126", "Alpha126", "Alpha 1.2.6", "Alpha 1.2.6");
    }
}
