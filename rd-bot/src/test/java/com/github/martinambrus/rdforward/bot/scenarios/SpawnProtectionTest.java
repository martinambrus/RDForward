package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.api.PermissionManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for spawn protection:
 * - Non-op player cannot place blocks within spawn protection radius
 * - Non-op player cannot break blocks within spawn protection radius
 * - Operator (level 1+) can bypass spawn protection
 * - Blocks outside spawn protection radius can be modified by anyone
 *
 * Default spawn protection radius is 16 blocks.
 * Default world is 256x256 with spawn at center (128, 128).
 */
class SpawnProtectionTest {

    private static TestServer testServer;

    @BeforeAll
    static void startServer() throws InterruptedException {
        testServer = new TestServer();
        // Start with spawn protection enabled (radius 16) since this class
        // specifically tests spawn protection behavior.
        testServer.start(16);
    }

    @AfterAll
    static void stopServer() {
        testServer.stop();
    }

    @Test
    void nonOpCannotPlaceBlockAtSpawn() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "SpawnNoPlace");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());
            Thread.sleep(500);

            int bx = 128, by = 42, bz = 128;
            session.sendBlockPlace(bx, by, bz, 1, 4);

            String msg = session.waitForChat("cannot modify blocks", 3000);
            assertNotNull(msg, "Non-op should receive spawn protection message");

            int blockType = session.waitForBlockChange(bx, by + 1, bz, 1000);
            assertTrue(blockType <= 0,
                    "Block should not be placed within spawn protection for non-op");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nonOpCannotBreakBlockAtSpawn() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_3_1, "SpawnNoBreak");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());
            Thread.sleep(500);

            int bx = 128, by = 42, bz = 128;
            session.sendDigging(0, bx, by, bz, 1);

            String msg = session.waitForChat("cannot modify blocks", 3000);
            assertNotNull(msg, "Non-op should receive spawn protection message for breaking");

            int blockType = session.waitForBlockChangeValue(bx, by, bz, 0, 1000);
            assertNotEquals(0, blockType,
                    "Block should not be broken within spawn protection for non-op");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void opCanPlaceBlockAtSpawn() throws Exception {
        PermissionManager.addOp("SpawnOpPlace");
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "SpawnOpPlace");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());
            Thread.sleep(500);

            int bx = 130, by = 42, bz = 130;
            session.sendBlockPlace(bx, by, bz, 1, 4);

            int blockType = session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(blockType > 0,
                    "Op should be able to place blocks within spawn protection");
        } finally {
            bot.disconnect();
            PermissionManager.removeOp("SpawnOpPlace");
        }
    }

    @Test
    void opCanBreakBlockAtSpawn() throws Exception {
        PermissionManager.addOp("SpawnOpBreak");
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_3_1, "SpawnOpBreak");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());
            Thread.sleep(500);

            int bx = 131, by = 42, bz = 131;
            session.sendDigging(0, bx, by, bz, 1);

            int blockType = session.waitForBlockChangeValue(bx, by, bz, 0, 3000);
            assertEquals(0, blockType,
                    "Op should be able to break blocks within spawn protection");
        } finally {
            bot.disconnect();
            PermissionManager.removeOp("SpawnOpBreak");
        }
    }

    @Test
    void nonOpCanPlaceBlockOutsideSpawnRadius() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "SpawnFarPlace");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());
            Thread.sleep(500);

            int bx = 35, by = 42, bz = 35;
            session.sendBlockPlace(bx, by, bz, 1, 4);

            int blockType = session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(blockType > 0,
                    "Non-op should be able to place blocks outside spawn protection radius");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void level1OpBypassesSpawnProtection() throws Exception {
        PermissionManager.addOp("SpawnLv1", PermissionManager.OP_BYPASS_SPAWN);
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "SpawnLv1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());
            Thread.sleep(500);

            int bx = 132, by = 42, bz = 132;
            session.sendBlockPlace(bx, by, bz, 1, 4);

            int blockType = session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(blockType > 0,
                    "Level 1 op should bypass spawn protection");
        } finally {
            bot.disconnect();
            PermissionManager.removeOp("SpawnLv1");
        }
    }
}
