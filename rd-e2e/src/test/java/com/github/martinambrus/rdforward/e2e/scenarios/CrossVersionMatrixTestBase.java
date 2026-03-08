package com.github.martinambrus.rdforward.e2e.scenarios;

import com.github.martinambrus.rdforward.e2e.ClientLauncher;
import com.github.martinambrus.rdforward.e2e.CrossVersionRegistry;
import com.github.martinambrus.rdforward.e2e.E2ETestServer;
import com.github.martinambrus.rdforward.e2e.HeadlessDisplay;
import com.github.martinambrus.rdforward.e2e.ScreenshotBaselineVerifier;
import com.github.martinambrus.rdforward.e2e.StatusMonitor;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for cross-version matrix tests. Provides shared infrastructure
 * for launching two clients, running the cross_client scenario, and verifying
 * screenshots.
 *
 * Subclasses must call {@link #initServer(int)} from @BeforeAll and
 * {@link #tearDownBase()} from @AfterAll.
 */
abstract class CrossVersionMatrixTestBase {

    private static final long TIMEOUT_MS = 300_000; // 5 minutes per client

    static E2ETestServer server;
    static HeadlessDisplay display1;
    static HeadlessDisplay display2;

    static void initServer(int worldHeight) throws Exception {
        server = new E2ETestServer(worldHeight);
        server.start();
        // Pre-seed positions for pre-1.2.0 Alpha clients to bypass the
        // first-connect kick (server kicks these versions once to warn about
        // the TimSort JVM flag, then accepts on the next connect).
        for (String key : new String[]{"Alpha1015", "Alpha1016", "Alpha1017", "Alpha110"}) {
            server.preSeedPlayerPosition("P_" + key);
            server.preSeedPlayerPosition("S_" + key);
        }
        display1 = HeadlessDisplay.forFork();
        display1.start();
        display2 = HeadlessDisplay.secondForFork();
        display2.start();
    }

    static void tearDownBase() {
        if (display2 != null) display2.stop();
        if (display1 != null) display1.stop();
        if (server != null) server.stop();
    }

    /**
     * Run a cross-client test between two versions.
     *
     * @param primaryKey   launcher key for the primary client
     * @param secondaryKey launcher key for the secondary client
     * @param primaryName  display name for error messages
     * @param secondaryName display name for error messages
     */
    void runCrossClientTest(String primaryKey, String secondaryKey,
            String primaryName, String secondaryName) throws Exception {
        File primaryStatusDir = Files.createTempDirectory("e2e-xv-pri-").toFile();
        File secondaryStatusDir = Files.createTempDirectory("e2e-xv-sec-").toFile();
        File syncDir = Files.createTempDirectory("e2e-xv-sync-").toFile();

        String agentJar = findAgentJar();
        assertNotNull(agentJar,
                "rd-e2e-agent fat JAR not found. Run ./gradlew :rd-e2e-agent:fatJar first.");

        ClientLauncher launcher1 = new ClientLauncher();
        ClientLauncher launcher2 = new ClientLauncher();

        // Derive usernames from version keys (must be unique per player)
        String primaryUser = "P_" + primaryKey.substring(0, Math.min(14, primaryKey.length()));
        String secondaryUser = "S_" + secondaryKey.substring(0, Math.min(14, secondaryKey.length()));

        // Reset saved positions to default spawn so both players start fresh.
        // Without this, the primary's position drifts +Z across sequential test
        // pairs (the scenario walks backward 2 blocks each time), eventually
        // moving too far from the secondary to be visible.
        // Using preSeed (not clear) so pre-1.2.0 Alpha clients retain their
        // "known player" status and don't get kicked on first connect.
        server.preSeedPlayerPosition(primaryUser);
        server.preSeedPlayerPosition(secondaryUser);

        // Launch primary
        Process primary = launcher1.launchByVersionKey(primaryKey, agentJar,
                server.getPort(), primaryStatusDir, display1.getDisplay(),
                "cross_client", primaryUser, "primary", syncDir);

        try {
            // Wait for primary to reach RUNNING_SCENARIO before launching secondary
            StatusMonitor primaryMonitor = new StatusMonitor(primaryStatusDir);
            assertTrue(primaryMonitor.waitForState("RUNNING_SCENARIO", 120_000),
                    primaryName + " primary did not reach RUNNING_SCENARIO. Last status: "
                            + primaryMonitor.readStatus());

            // Launch secondary
            Process secondary = launcher2.launchByVersionKey(secondaryKey, agentJar,
                    server.getPort(), secondaryStatusDir, display2.getDisplay(),
                    "cross_client", secondaryUser, "secondary", syncDir);

            try {
                StatusMonitor secondaryMonitor = new StatusMonitor(secondaryStatusDir);

                String primaryFinal = primaryMonitor.waitForTerminal(TIMEOUT_MS);
                assertNotNull(primaryFinal, primaryName + " primary did not finish within timeout. "
                        + "Last status: " + primaryMonitor.readStatus());

                String secondaryFinal = secondaryMonitor.waitForTerminal(TIMEOUT_MS);
                assertNotNull(secondaryFinal, secondaryName + " secondary did not finish within timeout. "
                        + "Last status: " + secondaryMonitor.readStatus());

                // Assert both completed successfully
                String primaryState = StatusMonitor.extractJsonField(primaryFinal, "state");
                String primaryError = StatusMonitor.extractJsonField(primaryFinal, "error");
                assertEquals("COMPLETE", primaryState,
                        primaryName + " primary error: " + primaryError);

                String secondaryState = StatusMonitor.extractJsonField(secondaryFinal, "state");
                String secondaryError = StatusMonitor.extractJsonField(secondaryFinal, "error");
                assertEquals("COMPLETE", secondaryState,
                        secondaryName + " secondary error: " + secondaryError);

                // Verify screenshots against baselines
                // Baseline dir: cross-tests/{categoryFolder}/{primary}_{secondary}/
                String crossId = CrossVersionRegistry.crossBaselineId(primaryKey, secondaryKey);
                new ScreenshotBaselineVerifier(crossId, "primary", 0.70)
                        .verifyAll(primaryStatusDir, "cross_client_primary.png");
                new ScreenshotBaselineVerifier(crossId, "secondary", 0.70)
                        .verifyAll(secondaryStatusDir, "cross_client_secondary.png");
            } finally {
                if (secondary.isAlive()) {
                    secondary.destroyForcibly();
                    secondary.waitFor();
                }
                launcher2.stop();
            }
        } finally {
            if (primary.isAlive()) {
                primary.destroyForcibly();
                primary.waitFor();
            }
            launcher1.stop();
            // Wait for the server to fully process both disconnections.
            // channelInactive fires asynchronously on the Netty event loop
            // after the TCP connection closes — without this wait, the next
            // test's preSeedPlayerPosition can be overwritten by a stale
            // rememberPlayerPosition from this pair's disconnect.
            server.waitForPlayerDisconnect(primaryUser, 5000);
            server.waitForPlayerDisconnect(secondaryUser, 5000);
        }
    }

    private String findAgentJar() {
        File buildLibs = new File("rd-e2e-agent/build/libs");
        if (!buildLibs.exists()) return null;
        File[] jars = buildLibs.listFiles((dir, name) -> name.endsWith("-all.jar"));
        if (jars == null || jars.length == 0) return null;
        return jars[0].getAbsolutePath();
    }
}
