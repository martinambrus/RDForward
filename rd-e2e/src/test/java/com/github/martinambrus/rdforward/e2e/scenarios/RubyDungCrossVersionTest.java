package com.github.martinambrus.rdforward.e2e.scenarios;

import com.github.martinambrus.rdforward.e2e.ClientLauncher;
import com.github.martinambrus.rdforward.e2e.E2ETestServer;
import com.github.martinambrus.rdforward.e2e.HeadlessDisplay;
import com.github.martinambrus.rdforward.e2e.ScreenshotBaselineVerifier;
import com.github.martinambrus.rdforward.e2e.StatusMonitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cross-version tests involving RubyDung clients.
 * Uses a 64-height server since RubyDung only supports 64-block worlds.
 *
 * Run with: ./gradlew :rd-e2e:test --tests "*.RubyDungCrossVersionTest" -Pe2e
 */
class RubyDungCrossVersionTest {

    private static final long TIMEOUT_MS = 180_000; // 3 minutes total

    private static E2ETestServer server;
    private static HeadlessDisplay display1;
    private static HeadlessDisplay display2;

    @BeforeAll
    static void setUp() throws Exception {
        server = new E2ETestServer(64);
        server.start();

        display1 = HeadlessDisplay.forFork();
        display1.start();
        display2 = HeadlessDisplay.secondForFork();
        display2.start();
    }

    @AfterAll
    static void tearDown() {
        if (display2 != null) display2.stop();
        if (display1 != null) display1.stop();
        if (server != null) server.stop();
    }

    @Test
    void rubyDungAndAlphaCrossClient() throws Exception {
        runCrossClientTest("rubydung", "alpha", "RDPlayer", "AlphaTester");
    }

    @Test
    void rubyDungAndBetaCrossClient() throws Exception {
        runCrossClientTest("rubydung", "beta", "RDPlayer", "BetaTester");
    }

    private void runCrossClientTest(String primaryVersion, String secondaryVersion,
            String primaryUsername, String secondaryUsername) throws Exception {
        File primaryStatusDir = Files.createTempDirectory("e2e-cross-primary-").toFile();
        File secondaryStatusDir = Files.createTempDirectory("e2e-cross-secondary-").toFile();
        File syncDir = Files.createTempDirectory("e2e-cross-sync-").toFile();

        String agentJar = findAgentJar();
        assertNotNull(agentJar, "rd-e2e-agent fat JAR not found. Run ./gradlew :rd-e2e-agent:fatJar first.");

        ClientLauncher launcher1 = new ClientLauncher();
        ClientLauncher launcher2 = new ClientLauncher();

        Process primary = launchClient(launcher1, primaryVersion, agentJar,
                server.getPort(), primaryStatusDir, display1.getDisplay(),
                "cross_client", primaryUsername, "primary", syncDir);

        try {
            StatusMonitor primaryMonitor = new StatusMonitor(primaryStatusDir);
            assertTrue(primaryMonitor.waitForState("RUNNING_SCENARIO", 60_000),
                    primaryVersion + " primary did not reach RUNNING_SCENARIO. Last status: "
                            + primaryMonitor.readStatus());

            Process secondary = launchClient(launcher2, secondaryVersion, agentJar,
                    server.getPort(), secondaryStatusDir, display2.getDisplay(),
                    "cross_client", secondaryUsername, "secondary", syncDir);

            try {
                StatusMonitor secondaryMonitor = new StatusMonitor(secondaryStatusDir);

                String primaryFinal = primaryMonitor.waitForTerminal(TIMEOUT_MS);
                assertNotNull(primaryFinal, primaryVersion + " primary did not finish within timeout. "
                        + "Last status: " + primaryMonitor.readStatus());

                String secondaryFinal = secondaryMonitor.waitForTerminal(TIMEOUT_MS);
                assertNotNull(secondaryFinal, secondaryVersion + " secondary did not finish within timeout. "
                        + "Last status: " + secondaryMonitor.readStatus());

                String primaryState = StatusMonitor.extractJsonField(primaryFinal, "state");
                String primaryError = StatusMonitor.extractJsonField(primaryFinal, "error");
                assertEquals("COMPLETE", primaryState,
                        primaryVersion + " primary error: " + primaryError);

                String secondaryState = StatusMonitor.extractJsonField(secondaryFinal, "state");
                String secondaryError = StatusMonitor.extractJsonField(secondaryFinal, "error");
                assertEquals("COMPLETE", secondaryState,
                        secondaryVersion + " secondary error: " + secondaryError);

                String crossVersion = "cross_" + primaryVersion + "_" + secondaryVersion;
                new ScreenshotBaselineVerifier(crossVersion, "primary", 0.80)
                        .verifyAll(primaryStatusDir, "cross_client_primary.png");
                new ScreenshotBaselineVerifier(crossVersion, "secondary", 0.80)
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
        }
    }

    private Process launchClient(ClientLauncher launcher, String version, String agentJar,
            int port, File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws Exception {
        if ("alpha".equals(version)) {
            return launcher.launchAlpha126(agentJar, port, statusDir, display,
                    scenario, username, role, syncDir);
        } else if ("rubydung".equals(version)) {
            String moddedJar = findModdedClientJar();
            assertNotNull(moddedJar, "rd-client modded JAR not found. "
                    + "Run ./gradlew :rd-client:fatModdedJar first.");
            return launcher.launchModdedClient(moddedJar, agentJar, "rubydung",
                    port, username, statusDir, display, scenario, role, syncDir);
        } else {
            return launcher.launchBeta181(agentJar, port, statusDir, display,
                    scenario, username, role, syncDir);
        }
    }

    private String findModdedClientJar() {
        File buildLibs = new File("rd-client/build/libs");
        if (!buildLibs.exists()) return null;
        File[] jars = buildLibs.listFiles((dir, name) -> name.endsWith("-all.jar"));
        if (jars == null || jars.length == 0) return null;
        return jars[0].getAbsolutePath();
    }

    private String findAgentJar() {
        File buildLibs = new File("rd-e2e-agent/build/libs");
        if (!buildLibs.exists()) return null;
        File[] jars = buildLibs.listFiles((dir, name) -> name.endsWith("-all.jar"));
        if (jars == null || jars.length == 0) return null;
        return jars[0].getAbsolutePath();
    }
}
