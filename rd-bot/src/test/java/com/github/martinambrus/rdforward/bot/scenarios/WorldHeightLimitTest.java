package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the server rejects placements at world height boundaries.
 * Y=63 is the max valid block (height=64), Y=64 is out of bounds.
 */
class WorldHeightLimitTest {

    private static TestServer testServer;

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
    void placementAtMaxHeightSucceeds() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_8, "HeightBot");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            Thread.sleep(500);

            // Valid: place on top face of Y=62 -> target Y=63 (max valid, height=64)
            int x = 40, z = 40;
            session.sendBlockPlace(x, 62, z, 1, 4);

            int blockType = session.waitForBlockChange(x, 63, z, 3000);
            assertTrue(blockType > 0, "Placement at Y=63 (max valid) should succeed");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void placementAboveMaxHeightFails() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_8, "HeightBot2");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            Thread.sleep(500);

            // First place a valid block at Y=63 to have a surface to click on
            int x = 45, z = 45;
            session.sendBlockPlace(x, 62, z, 1, 4);
            int placed = session.waitForBlockChange(x, 63, z, 3000);
            assertTrue(placed > 0, "Setup: block at Y=63 should be placed");

            // Invalid: place on top face of Y=63 -> target Y=64 (out of bounds)
            session.sendBlockPlace(x, 63, z, 1, 4);

            // Server should reject: either sends BlockChange(0) cancellation or nothing
            int result = session.waitForBlockChange(x, 64, z, 1500);
            assertTrue(result <= 0,
                    "Placement at Y=64 (out of bounds) should fail, got blockType=" + result);
        } finally {
            bot.disconnect();
        }
    }
}
