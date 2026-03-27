package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.api.PermissionManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for /op and /deop commands:
 * - Op grants operator status with specified level
 * - Op defaults to level 1 for player-issued commands
 * - Cannot grant higher level than own
 * - Deop removes operator status
 * - Cannot deop yourself
 * - Cannot deop player with higher level
 * - Non-op cannot use /op or /deop
 * - Op command works for offline players (message confirmation)
 */
class OpDeopTest {

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

    @AfterEach
    void cleanUp() {
        // Clean up any ops created during tests
        PermissionManager.removeOp("OpAdmin");
        PermissionManager.removeOp("OpTarget");
        PermissionManager.removeOp("OpLow");
        PermissionManager.removeOp("OpHigh");
        PermissionManager.removeOp("DeopSelf");
        PermissionManager.removeOp("DeopTarget");
        PermissionManager.removeOp("NonOp");
    }

    @Test
    void opGrantsOperatorViaCommand() throws Exception {
        PermissionManager.addOp("OpAdmin", PermissionManager.OP_MANAGE);
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "OpAdmin");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "OpTarget");
        try {
            BotSession opSession = op.getSession();
            assertTrue(opSession.isLoginComplete());
            assertTrue(target.getSession().isLoginComplete());

            assertFalse(PermissionManager.isOp("OpTarget"), "Target should not be op initially");

            opSession.sendChat("/op OpTarget");
            String msg = opSession.waitForChat("Made OpTarget an operator", 3000);
            assertNotNull(msg, "Should receive op confirmation");
            assertTrue(PermissionManager.isOp("OpTarget"), "Target should be op after /op");
        } finally {
            op.disconnect();
            target.disconnect();
        }
    }

    @Test
    void opDefaultsToLevel1ForPlayer() throws Exception {
        PermissionManager.addOp("OpAdmin", PermissionManager.OP_MANAGE);
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "OpAdmin");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "OpTarget");
        try {
            assertTrue(op.getSession().isLoginComplete());
            assertTrue(target.getSession().isLoginComplete());

            op.getSession().sendChat("/op OpTarget");
            op.getSession().waitForChat("operator", 3000);

            assertEquals(PermissionManager.OP_BYPASS_SPAWN, PermissionManager.getOpLevel("OpTarget"),
                    "Player-issued /op without level should default to level 1");
        } finally {
            op.disconnect();
            target.disconnect();
        }
    }

    @Test
    void opGrantsSpecificLevel() throws Exception {
        PermissionManager.addOp("OpAdmin", PermissionManager.OP_MANAGE);
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "OpAdmin");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "OpTarget");
        try {
            assertTrue(op.getSession().isLoginComplete());
            assertTrue(target.getSession().isLoginComplete());

            op.getSession().sendChat("/op OpTarget 2");
            op.getSession().waitForChat("operator", 3000);

            assertEquals(PermissionManager.OP_CHEAT, PermissionManager.getOpLevel("OpTarget"),
                    "Should grant the specified op level");
        } finally {
            op.disconnect();
            target.disconnect();
        }
    }

    @Test
    void cannotGrantHigherLevelThanOwn() throws Exception {
        // /op requires OP_MANAGE (3), so use level 3 and try to grant level 4
        PermissionManager.addOp("OpLow", PermissionManager.OP_MANAGE); // level 3
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "OpLow");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "OpTarget");
        try {
            BotSession opSession = op.getSession();
            assertTrue(opSession.isLoginComplete());
            assertTrue(target.getSession().isLoginComplete());

            opSession.sendChat("/op OpTarget 4");
            String msg = opSession.waitForChat("cannot grant a higher op level", 3000);
            assertNotNull(msg, "Should receive escalation prevention message");
            assertFalse(PermissionManager.isOp("OpTarget"),
                    "Target should not become op with escalated level");
        } finally {
            op.disconnect();
            target.disconnect();
        }
    }

    @Test
    void deopRemovesOperator() throws Exception {
        PermissionManager.addOp("OpAdmin", PermissionManager.OP_MANAGE);
        PermissionManager.addOp("DeopTarget", PermissionManager.OP_BYPASS_SPAWN);

        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "OpAdmin");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "DeopTarget");
        try {
            BotSession opSession = op.getSession();
            assertTrue(opSession.isLoginComplete());
            assertTrue(target.getSession().isLoginComplete());

            assertTrue(PermissionManager.isOp("DeopTarget"), "Target should be op before deop");

            opSession.sendChat("/deop DeopTarget");
            String msg = opSession.waitForChat("Removed DeopTarget from operators", 3000);
            assertNotNull(msg, "Should receive deop confirmation");
            assertFalse(PermissionManager.isOp("DeopTarget"),
                    "Target should not be op after deop");
        } finally {
            op.disconnect();
            target.disconnect();
        }
    }

    @Test
    void cannotDeopSelf() throws Exception {
        PermissionManager.addOp("DeopSelf", PermissionManager.OP_MANAGE);
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "DeopSelf");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete());

            session.sendChat("/deop DeopSelf");
            String msg = session.waitForChat("cannot deop yourself", 3000);
            assertNotNull(msg, "Should receive self-deop prevention message");
            assertTrue(PermissionManager.isOp("DeopSelf"),
                    "Player should still be op after failed self-deop");
        } finally {
            op.disconnect();
        }
    }

    @Test
    void cannotDeopHigherLevelOp() throws Exception {
        PermissionManager.addOp("OpLow", PermissionManager.OP_MANAGE); // level 3
        PermissionManager.addOp("OpHigh", PermissionManager.OP_ADMIN); // level 4

        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "OpLow");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete());

            session.sendChat("/deop OpHigh");
            String msg = session.waitForChat("cannot deop a player with a higher op level", 3000);
            assertNotNull(msg, "Should receive deop prevention message for higher level op");
            assertTrue(PermissionManager.isOp("OpHigh"),
                    "Higher level op should still be op");
        } finally {
            op.disconnect();
        }
    }

    @Test
    void nonOpCannotUseOpCommand() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "NonOp");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());

            session.sendChat("/op NonOp");
            String msg = session.waitForChat("permission", 3000);
            assertNotNull(msg, "Non-op should receive permission denied for /op");
            assertFalse(PermissionManager.isOp("NonOp"),
                    "Non-op should not be able to op themselves");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nonOpCannotUseDeopCommand() throws Exception {
        PermissionManager.addOp("DeopTarget", PermissionManager.OP_BYPASS_SPAWN);
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "NonOp");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());

            session.sendChat("/deop DeopTarget");
            String msg = session.waitForChat("permission", 3000);
            assertNotNull(msg, "Non-op should receive permission denied for /deop");
            assertTrue(PermissionManager.isOp("DeopTarget"),
                    "Target should still be op after non-op deop attempt");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void opForOfflinePlayerShowsNote() throws Exception {
        PermissionManager.addOp("OpAdmin", PermissionManager.OP_MANAGE);
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "OpAdmin");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete());

            session.sendChat("/op OfflineGuy");
            String msg = session.waitForChat("not online", 3000);
            assertNotNull(msg, "Should note that target is offline");

            // Op should still be granted
            assertTrue(PermissionManager.isOp("OfflineGuy"),
                    "Offline player should still receive op");
        } finally {
            op.disconnect();
            PermissionManager.removeOp("OfflineGuy");
        }
    }
}
