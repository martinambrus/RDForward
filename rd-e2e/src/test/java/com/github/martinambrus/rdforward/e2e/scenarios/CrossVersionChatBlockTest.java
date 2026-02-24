package com.github.martinambrus.rdforward.e2e.scenarios;

import com.github.martinambrus.rdforward.e2e.ClientLauncher;
import com.github.martinambrus.rdforward.e2e.E2ETestServer;
import com.github.martinambrus.rdforward.e2e.HeadlessDisplay;
import com.github.martinambrus.rdforward.e2e.StatusMonitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cross-version multi-client test: launches an Alpha 1.2.6 client and a
 * Beta 1.8.1 client simultaneously on the same server, verifying:
 * - Chat messages sent by one client are visible to the other
 * - Block placement by one client is visible to the other
 * - Block breaking by one client is visible to the other
 *
 * Run with: ./gradlew :rd-e2e:test --tests "*.CrossVersionChatBlockTest" -Pe2e
 */
class CrossVersionChatBlockTest {

    private static final long TIMEOUT_MS = 180_000; // 3 minutes total

    private static E2ETestServer server;
    private static HeadlessDisplay display1; // :99 for primary
    private static HeadlessDisplay display2; // :100 for secondary
    private static File primaryStatusDir;
    private static File secondaryStatusDir;
    private static File syncDir;
    private static ClientLauncher launcher1;
    private static ClientLauncher launcher2;

    @BeforeAll
    static void setUp() throws Exception {
        server = new E2ETestServer();
        server.start();

        display1 = new HeadlessDisplay(99);
        display1.start();
        display2 = new HeadlessDisplay(100);
        display2.start();

        primaryStatusDir = Files.createTempDirectory("e2e-cross-primary-").toFile();
        secondaryStatusDir = Files.createTempDirectory("e2e-cross-secondary-").toFile();
        syncDir = Files.createTempDirectory("e2e-cross-sync-").toFile();

        launcher1 = new ClientLauncher();
        launcher2 = new ClientLauncher();
    }

    @AfterAll
    static void tearDown() {
        if (launcher1 != null) launcher1.stop();
        if (launcher2 != null) launcher2.stop();
        if (display2 != null) display2.stop();
        if (display1 != null) display1.stop();
        if (server != null) server.stop();
    }

    @Test
    void alphaAndBetaCrossClient() throws Exception {
        String agentJar = findAgentJar();
        assertNotNull(agentJar, "rd-e2e-agent fat JAR not found. Run ./gradlew :rd-e2e-agent:fatJar first.");

        // Launch primary (Alpha) first
        Process primary = launcher1.launchAlpha126(agentJar, server.getPort(),
                primaryStatusDir, display1.getDisplay(), "cross_client",
                "AlphaPlayer", "primary", syncDir);

        try {
            // Wait for primary to reach RUNNING_SCENARIO before launching secondary
            StatusMonitor primaryMonitor = new StatusMonitor(primaryStatusDir);
            assertTrue(primaryMonitor.waitForState("RUNNING_SCENARIO", 60_000),
                    "Primary did not reach RUNNING_SCENARIO. Last status: "
                            + primaryMonitor.readStatus());

            // Launch secondary (Beta)
            Process secondary = launcher2.launchBeta181(agentJar, server.getPort(),
                    secondaryStatusDir, display2.getDisplay(), "cross_client",
                    "BetaTester", "secondary", syncDir);

            try {
                // Wait for both to complete
                StatusMonitor secondaryMonitor = new StatusMonitor(secondaryStatusDir);

                String primaryFinal = primaryMonitor.waitForTerminal(TIMEOUT_MS);
                assertNotNull(primaryFinal, "Primary did not finish within timeout. "
                        + "Last status: " + primaryMonitor.readStatus());

                String secondaryFinal = secondaryMonitor.waitForTerminal(TIMEOUT_MS);
                assertNotNull(secondaryFinal, "Secondary did not finish within timeout. "
                        + "Last status: " + secondaryMonitor.readStatus());

                // Assert both completed successfully
                String primaryState = StatusMonitor.extractJsonField(primaryFinal, "state");
                String primaryError = StatusMonitor.extractJsonField(primaryFinal, "error");
                assertEquals("COMPLETE", primaryState,
                        "Primary error: " + primaryError);

                String secondaryState = StatusMonitor.extractJsonField(secondaryFinal, "state");
                String secondaryError = StatusMonitor.extractJsonField(secondaryFinal, "error");
                assertEquals("COMPLETE", secondaryState,
                        "Secondary error: " + secondaryError);

                // Verify screenshots
                assertTrue(new File(primaryStatusDir, "cross_client_primary.png").exists(),
                        "Primary screenshot not produced");
                assertTrue(new File(secondaryStatusDir, "cross_client_secondary.png").exists(),
                        "Secondary screenshot not produced");
            } finally {
                if (secondary.isAlive()) {
                    secondary.destroyForcibly();
                    secondary.waitFor();
                }
            }
        } finally {
            if (primary.isAlive()) {
                primary.destroyForcibly();
                primary.waitFor();
            }
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
