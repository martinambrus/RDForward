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
 * Release 1.8.2: Tests walking to map edge, falling into void,
 * and respawn teleport.
 *
 * Run with: ./gradlew :rd-e2e:test -Pe2e
 */
class Release182VoidFallTest {

    private static final long TIMEOUT_MS = 180_000;

    private static E2ETestServer server;
    private static HeadlessDisplay display;
    private static ClientLauncher launcher;
    private static File statusDir;

    @BeforeAll
    static void setUp() throws Exception {
        server = new E2ETestServer();
        server.start();

        display = HeadlessDisplay.forFork();
        display.start();

        statusDir = Files.createTempDirectory("e2e-release182-void-fall-").toFile();
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
    void release182VoidFallPasses() throws Exception {
        String agentJar = findAgentJar();
        assertNotNull(agentJar, "rd-e2e-agent fat JAR not found. Run ./gradlew :rd-e2e-agent:fatJar first.");

        Process client = launcher.launchRelease182(agentJar, server.getPort(),
                statusDir, display.getDisplay(), "void_fall");

        try {
            StatusMonitor monitor = new StatusMonitor(statusDir);
            String finalStatus = monitor.waitForTerminal(TIMEOUT_MS);

            assertNotNull(finalStatus, "Agent did not reach terminal state within timeout. "
                    + "Last status: " + monitor.readStatus());

            String state = StatusMonitor.extractJsonField(finalStatus, "state");
            String error = StatusMonitor.extractJsonField(finalStatus, "error");

            assertEquals("COMPLETE", state,
                    "Agent ended in " + state + " state. Error: " + error);

            // Higher tolerance (15%) for sky/cloud movement between runs
            new ScreenshotBaselineVerifier("release182", "void_fall", 0.85)
                    .verifyAll(statusDir, "void_fall_complete.png");
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
