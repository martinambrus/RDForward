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
 * Tests Q-drop (item dropping) and cobblestone replenishment.
 *
 * Run with: ./gradlew :rd-e2e:test --tests "*.AlphaQDropTest" -Pe2e
 */
class AlphaQDropTest {

    private static final long TIMEOUT_MS = 120_000;

    private static E2ETestServer server;
    private static HeadlessDisplay display;
    private static ClientLauncher launcher;
    private static File statusDir;

    @BeforeAll
    static void setUp() throws Exception {
        server = new E2ETestServer();
        server.start();

        display = new HeadlessDisplay();
        display.start();

        statusDir = Files.createTempDirectory("e2e-qdrop-test-").toFile();
        statusDir.deleteOnExit();

        launcher = new ClientLauncher();
    }

    @AfterAll
    static void tearDown() {
        if (launcher != null) launcher.stop();
        if (display != null) display.stop();
        if (server != null) server.stop();
    }

    @Test
    void alphaQDropPasses() throws Exception {
        String agentJar = findAgentJar();
        assertNotNull(agentJar, "rd-e2e-agent fat JAR not found. Run ./gradlew :rd-e2e-agent:fatJar first.");

        Process client = launcher.launchAlpha126(agentJar, server.getPort(),
                statusDir, display.getDisplay(), "q_drop");

        try {
            StatusMonitor monitor = new StatusMonitor(statusDir);
            String finalStatus = monitor.waitForTerminal(TIMEOUT_MS);

            assertNotNull(finalStatus, "Agent did not reach terminal state within timeout. "
                    + "Last status: " + monitor.readStatus());

            String state = StatusMonitor.extractJsonField(finalStatus, "state");
            String error = StatusMonitor.extractJsonField(finalStatus, "error");

            assertEquals("COMPLETE", state,
                    "Agent ended in " + state + " state. Error: " + error);

            new ScreenshotBaselineVerifier("alpha126", "q_drop")
                    .verifyAll(statusDir, "q_drop_complete.png");
        } finally {
            if (client.isAlive()) {
                client.destroyForcibly();
                client.waitFor();
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
