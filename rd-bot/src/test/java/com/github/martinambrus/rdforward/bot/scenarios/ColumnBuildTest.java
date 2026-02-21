package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests 5 consecutive vertical block placements all succeed.
 * Verifies: validation -> world.setBlock() -> BlockChange confirmation.
 */
class ColumnBuildTest {

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
    void fiveBlockColumnPlacement() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "ColumnBot");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            Thread.sleep(500);

            // Place 5 blocks in a column at x=30, z=30.
            // Surface is at Y=42. Placing on top face (dir=1) of Y=42+i
            // puts block at Y=43+i. First target at Y=43, last at Y=47.
            int x = 30;
            int z = 30;
            for (int i = 0; i < 5; i++) {
                int targetY = 42 + i;
                session.sendBlockPlace(x, targetY, z, 1, 4);

                int blockType = session.waitForBlockChange(x, targetY + 1, z, 3000);
                assertTrue(blockType > 0,
                        "Block " + (i + 1) + " at Y=" + (targetY + 1) + " should be placed");
            }
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void fiveBlockColumnPlacementBeta() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_8, "ColBeta");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 80, z = 80;
            for (int i = 0; i < 5; i++) {
                int targetY = 42 + i;
                session.sendBlockPlace(x, targetY, z, 1, 4);
                int blockType = session.waitForBlockChange(x, targetY + 1, z, 3000);
                assertTrue(blockType > 0,
                        "Beta block " + (i + 1) + " at Y=" + (targetY + 1) + " should be placed");
            }
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void fiveBlockColumnPlacementV47() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "ColV47");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 82, z = 82;
            for (int i = 0; i < 5; i++) {
                int targetY = 42 + i;
                session.sendBlockPlace(x, targetY, z, 1, 4);
                int blockType = session.waitForBlockChange(x, targetY + 1, z, 3000);
                assertTrue(blockType > 0,
                        "V47 block " + (i + 1) + " at Y=" + (targetY + 1) + " should be placed");
            }
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void fiveBlockColumnPlacementV340() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_12_2, "ColV340");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 84, z = 84;
            for (int i = 0; i < 5; i++) {
                int targetY = 42 + i;
                session.sendBlockPlace(x, targetY, z, 1, 4);
                int blockType = session.waitForBlockChange(x, targetY + 1, z, 3000);
                assertTrue(blockType > 0,
                        "V340 block " + (i + 1) + " at Y=" + (targetY + 1) + " should be placed");
            }
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void fiveBlockColumnPlacementV477() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_14, "ColV477");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 230, z = 230;
            for (int i = 0; i < 5; i++) {
                int targetY = 42 + i;
                session.sendBlockPlace(x, targetY, z, 1, 4);
                int blockType = session.waitForBlockChange(x, targetY + 1, z, 3000);
                assertTrue(blockType > 0,
                        "V477 block " + (i + 1) + " at Y=" + (targetY + 1) + " should be placed");
            }
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void fiveBlockColumnPlacementV764() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "ColV764");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 231, z = 231;
            for (int i = 0; i < 5; i++) {
                int targetY = 42 + i;
                session.sendBlockPlace(x, targetY, z, 1, 4);
                int blockType = session.waitForBlockChange(x, targetY + 1, z, 3000);
                assertTrue(blockType > 0,
                        "V764 block " + (i + 1) + " at Y=" + (targetY + 1) + " should be placed");
            }
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void fiveBlockColumnPlacementV774() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "ColV774");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 232, z = 232;
            for (int i = 0; i < 5; i++) {
                int targetY = 42 + i;
                session.sendBlockPlace(x, targetY, z, 1, 4);
                int blockType = session.waitForBlockChange(x, targetY + 1, z, 3000);
                assertTrue(blockType > 0,
                        "V774 block " + (i + 1) + " at Y=" + (targetY + 1) + " should be placed");
            }
        } finally {
            bot.disconnect();
        }
    }
}
