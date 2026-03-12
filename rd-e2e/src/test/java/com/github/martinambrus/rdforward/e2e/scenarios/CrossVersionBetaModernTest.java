package com.github.martinambrus.rdforward.e2e.scenarios;

import com.github.martinambrus.rdforward.e2e.CrossVersionRegistry;
import com.github.martinambrus.rdforward.e2e.CrossVersionRegistry.Category;
import com.github.martinambrus.rdforward.e2e.CrossVersionRegistry.Weight;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Beta as primary vs Modern secondaries (1.19+).
 */
class CrossVersionBetaModernTest extends CrossVersionMatrixTestBase {

    @BeforeAll
    static void setUp() throws Exception {
        initServer(128);
    }

    @AfterAll
    static void tearDown() {
        tearDownBase();
    }

    @ParameterizedTest(name = "{0} vs {1}")
    @MethodSource("pairs")
    void crossVersion(String pd, String sd, String pk, String sk) throws Exception {
        runCrossClientTest(pk, sk, pd, sd);
    }

    static Stream<Arguments> pairs() {
        return CrossVersionRegistry.nonRdPairsByPrimaryCategory(
                Category.BETA, Weight.MODERN);
    }
}
