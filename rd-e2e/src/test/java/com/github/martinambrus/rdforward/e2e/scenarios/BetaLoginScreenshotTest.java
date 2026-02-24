package com.github.martinambrus.rdforward.e2e.scenarios;

import com.github.martinambrus.rdforward.e2e.BaselineManager;
import com.github.martinambrus.rdforward.e2e.ClientLauncher;
import com.github.martinambrus.rdforward.e2e.E2ETestServer;
import com.github.martinambrus.rdforward.e2e.HeadlessDisplay;
import com.github.martinambrus.rdforward.e2e.ImageComparator;
import com.github.martinambrus.rdforward.e2e.StatusMonitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Beta 1.8.1 client logs into RDForward, loads the world (creative mode),
 * and captures a screenshot for baseline comparison.
 *
 * Run with: ./gradlew :rd-e2e:test -Pe2e
 */
class BetaLoginScreenshotTest {

    private static final String VERSION = "beta181";
    private static final String CHECKPOINT = "world_loaded";
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

        statusDir = Files.createTempDirectory("e2e-beta181-").toFile();
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
    void betaClientLoadsWorldAndScreenshotMatchesBaseline() throws Exception {
        String agentJar = findAgentJar();
        assertNotNull(agentJar, "rd-e2e-agent fat JAR not found. Run ./gradlew :rd-e2e-agent:fatJar first.");

        Process client = launcher.launchBeta181(agentJar, server.getPort(),
                statusDir, display.getDisplay());

        try {
            StatusMonitor monitor = new StatusMonitor(statusDir);
            String finalStatus = monitor.waitForTerminal(TIMEOUT_MS);

            assertNotNull(finalStatus, "Agent did not reach terminal state within timeout. "
                    + "Last status: " + monitor.readStatus());

            String state = StatusMonitor.extractJsonField(finalStatus, "state");
            String error = StatusMonitor.extractJsonField(finalStatus, "error");

            assertEquals("COMPLETE", state,
                    "Agent ended in " + state + " state. Error: " + error);

            File screenshot = new File(statusDir, "world_loaded.png");
            assertTrue(screenshot.exists(), "Screenshot file not produced by agent");
            assertTrue(screenshot.length() > 0, "Screenshot file is empty");

            BaselineManager baselines = new BaselineManager();
            if (!baselines.hasBaseline(VERSION, CHECKPOINT)) {
                baselines.recordBaseline(screenshot, VERSION, CHECKPOINT);
                System.out.println("[E2E] First run — baseline recorded. "
                        + "Re-run test to compare against baseline.");
            } else {
                ImageComparator comparator = new ImageComparator(0.90);
                File baseline = baselines.getBaselinePath(VERSION, CHECKPOINT);
                File diff = baselines.getDiffPath(VERSION, CHECKPOINT);

                ImageComparator.ComparisonResult result =
                        comparator.compare(baseline, screenshot, diff);

                assertTrue(result.passed,
                        "Screenshot differs from baseline by " + result.differencePercent
                                + "% (threshold: 10%). Diff saved to: " + diff.getAbsolutePath());
            }
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
