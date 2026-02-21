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
    void alphaPlacementAtMaxHeightSucceeds() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaHt");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 90, z = 90;
            session.sendBlockPlace(x, 62, z, 1, 4);
            int blockType = session.waitForBlockChange(x, 63, z, 3000);
            assertTrue(blockType > 0, "Alpha placement at Y=63 (max valid) should succeed");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void alphaPlacementAboveMaxHeightFails() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaHtF");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 91, z = 91;
            session.sendBlockPlace(x, 62, z, 1, 4);
            assertTrue(session.waitForBlockChange(x, 63, z, 3000) > 0, "Setup block at Y=63");

            session.sendBlockPlace(x, 63, z, 1, 4);
            int result = session.waitForBlockChange(x, 64, z, 1500);
            assertTrue(result <= 0,
                    "Alpha placement at Y=64 (out of bounds) should fail, got blockType=" + result);
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void v47PlacementAtMaxHeightSucceeds() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "V47Ht");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 92, z = 92;
            session.sendBlockPlace(x, 62, z, 1, 4);
            int blockType = session.waitForBlockChange(x, 63, z, 3000);
            assertTrue(blockType > 0, "V47 placement at Y=63 (max valid) should succeed");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void v47PlacementAboveMaxHeightFails() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "V47HtF");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 93, z = 93;
            session.sendBlockPlace(x, 62, z, 1, 4);
            assertTrue(session.waitForBlockChange(x, 63, z, 3000) > 0, "Setup block at Y=63");

            session.sendBlockPlace(x, 63, z, 1, 4);
            int result = session.waitForBlockChange(x, 64, z, 1500);
            assertTrue(result <= 0,
                    "V47 placement at Y=64 (out of bounds) should fail, got blockType=" + result);
        } finally {
            bot.disconnect();
        }
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

    @Test
    void v477PlacementAtMaxHeightSucceeds() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_14, "HtV477");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 240, z = 240;
            session.sendBlockPlace(x, 62, z, 1, 4);
            int blockType = session.waitForBlockChange(x, 63, z, 3000);
            assertTrue(blockType > 0, "V477 placement at Y=63 (max valid) should succeed");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void v477PlacementAboveMaxHeightFails() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_14, "HtV477F");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 241, z = 241;
            session.sendBlockPlace(x, 62, z, 1, 4);
            assertTrue(session.waitForBlockChange(x, 63, z, 3000) > 0, "Setup block at Y=63");

            session.sendBlockPlace(x, 63, z, 1, 4);
            int result = session.waitForBlockChange(x, 64, z, 1500);
            assertTrue(result <= 0,
                    "V477 placement at Y=64 should fail, got blockType=" + result);
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void v764PlacementAtMaxHeightSucceeds() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "HtV764");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 242, z = 242;
            session.sendBlockPlace(x, 62, z, 1, 4);
            int blockType = session.waitForBlockChange(x, 63, z, 3000);
            assertTrue(blockType > 0, "V764 placement at Y=63 (max valid) should succeed");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void v764PlacementAboveMaxHeightFails() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "HtV764F");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 243, z = 243;
            session.sendBlockPlace(x, 62, z, 1, 4);
            assertTrue(session.waitForBlockChange(x, 63, z, 3000) > 0, "Setup block at Y=63");

            session.sendBlockPlace(x, 63, z, 1, 4);
            int result = session.waitForBlockChange(x, 64, z, 1500);
            assertTrue(result <= 0,
                    "V764 placement at Y=64 should fail, got blockType=" + result);
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void v774PlacementAtMaxHeightSucceeds() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "HtV774");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 244, z = 244;
            session.sendBlockPlace(x, 62, z, 1, 4);
            int blockType = session.waitForBlockChange(x, 63, z, 3000);
            assertTrue(blockType > 0, "V774 placement at Y=63 (max valid) should succeed");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void v774PlacementAboveMaxHeightFails() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "HtV774F");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 245, z = 245;
            session.sendBlockPlace(x, 62, z, 1, 4);
            assertTrue(session.waitForBlockChange(x, 63, z, 3000) > 0, "Setup block at Y=63");

            session.sendBlockPlace(x, 63, z, 1, 4);
            int result = session.waitForBlockChange(x, 64, z, 1500);
            assertTrue(result <= 0,
                    "V774 placement at Y=64 should fail, got blockType=" + result);
        } finally {
            bot.disconnect();
        }
    }
}
