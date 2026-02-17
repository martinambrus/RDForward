package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that spawn position is consistent across all protocol families:
 * Alpha, Beta, pre-Netty Release, Netty 1.7.x, and Netty 1.8.
 */
class CrossVersionSpawnTest {

    private static TestServer testServer;

    /** Versions to test, one from each protocol family. */
    private static final ProtocolVersion[] VERSIONS = {
            ProtocolVersion.ALPHA_1_2_5,    // Alpha (eye-level Y)
            ProtocolVersion.BETA_1_8,       // Beta (eye-level Y)
            ProtocolVersion.RELEASE_1_5,    // Pre-Netty Release (eye-level Y)
            ProtocolVersion.RELEASE_1_7_6,  // Netty 1.7.x (eye-level Y)
            ProtocolVersion.RELEASE_1_8,    // Netty 1.8 (feet-level Y)
    };

    @BeforeAll
    static void startServer() throws InterruptedException {
        testServer = new TestServer();
        testServer.start();
    }

    @AfterAll
    static void stopServer() {
        testServer.stop();
    }

    @Test
    void allVersionsSpawnOnGround() throws Exception {
        for (ProtocolVersion version : VERSIONS) {
            withBot(version, "CvGnd" + version.ordinal(), session -> {
                int blockX = (int) Math.floor(session.getX());
                int blockZ = (int) Math.floor(session.getZ());
                assertTrue(session.waitForChunkAt(blockX, blockZ, 5000),
                        version + ": spawn chunk should be received");
                assertTrue(session.isOnGround(),
                        version + ": bot should be on ground after spawn");
            });
        }
    }

    @Test
    void allVersionsAgreeOnFeetY() throws Exception {
        for (ProtocolVersion version : VERSIONS) {
            withBot(version, "CvFtY" + version.ordinal(), session -> {
                double feetY;
                if (version.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
                    feetY = session.getSpawnY();
                } else {
                    feetY = session.getSpawnY() - (double) 1.62f;
                }
                assertEquals(43.0, feetY, 0.001,
                        version + ": feet Y should be 43.0");
            });
        }
    }

    @Test
    void allVersionsHaveGrassBelow() throws Exception {
        for (ProtocolVersion version : VERSIONS) {
            withBot(version, "CvBlk" + version.ordinal(), session -> {
                int blockX = (int) Math.floor(session.getX());
                int blockZ = (int) Math.floor(session.getZ());
                assertTrue(session.waitForChunkAt(blockX, blockZ, 5000),
                        version + ": spawn chunk should be received");
                assertEquals(0, session.getBlockAt(blockX, 43, blockZ),
                        version + ": block at Y=43 should be air (0)");
                assertEquals(2, session.getBlockAt(blockX, 42, blockZ),
                        version + ": block at Y=42 should be grass (2)");
            });
        }
    }

    /**
     * Connects a bot, runs an assertion, and disconnects.
     * Sequential per-version to avoid port/player-name collisions.
     */
    private void withBot(ProtocolVersion version, String name, BotAssertion assertion) throws Exception {
        BotClient bot = testServer.createBot(version, name);
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), version + ": login should complete");
            assertion.run(session);
        } finally {
            bot.disconnect();
        }
    }

    @FunctionalInterface
    private interface BotAssertion {
        void run(BotSession session) throws Exception;
    }
}
