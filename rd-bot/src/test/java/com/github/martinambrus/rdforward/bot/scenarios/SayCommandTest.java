package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.api.PermissionManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for /say command:
 * - Broadcasts "[SenderName] message" to all players
 * - Multi-word messages preserved
 * - All connected players receive the broadcast
 * - Non-op players cannot use /say
 */
class SayCommandTest {

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
        PermissionManager.removeOp("SayOp");
    }

    @Test
    void sayBroadcastsToAllPlayers() throws Exception {
        PermissionManager.addOp("SayOp", PermissionManager.OP_CHEAT);
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "SayOp");
        BotClient listener = testServer.createBot(ProtocolVersion.BETA_1_7_3, "SayListener");
        try {
            BotSession opSession = op.getSession();
            BotSession listenerSession = listener.getSession();
            assertTrue(opSession.isLoginComplete());
            assertTrue(listenerSession.isLoginComplete());

            opSession.sendChat("/say Hello everyone!");

            // Both the sender and listener should see the broadcast
            String opMsg = opSession.waitForChat("\\[SayOp\\] Hello everyone!", 3000);
            assertNotNull(opMsg, "Sender should see the broadcast");

            String listenerMsg = listenerSession.waitForChat("\\[SayOp\\] Hello everyone!", 3000);
            assertNotNull(listenerMsg, "Other players should see the broadcast");
        } finally {
            op.disconnect();
            listener.disconnect();
        }
    }

    @Test
    void sayPreservesMultiWordMessage() throws Exception {
        PermissionManager.addOp("SayOp", PermissionManager.OP_CHEAT);
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "SayOp");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete());

            session.sendChat("/say this is a multi word message");

            String msg = session.waitForChat("this is a multi word message", 3000);
            assertNotNull(msg, "Multi-word message should be preserved");
        } finally {
            op.disconnect();
        }
    }

    @Test
    void sayRequiresOpPermission() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "SayNoOp");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());

            session.sendChat("/say test");

            String msg = session.waitForChat("permission", 3000);
            assertNotNull(msg, "Non-op should receive permission denied for /say");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void sayWithNoArgsShowsUsage() throws Exception {
        PermissionManager.addOp("SayOp", PermissionManager.OP_CHEAT);
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "SayOp");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete());

            session.sendChat("/say");

            String msg = session.waitForChat("Usage: say", 3000);
            assertNotNull(msg, "Should show usage when no message provided");
        } finally {
            op.disconnect();
        }
    }

    @Test
    void sayCrossVersionBroadcast() throws Exception {
        PermissionManager.addOp("SayOp", PermissionManager.OP_CHEAT);
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "SayOp");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "SayAlpha");
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "SayNetty");
        try {
            assertTrue(op.getSession().isLoginComplete());
            assertTrue(alphaBot.getSession().isLoginComplete());
            assertTrue(nettyBot.getSession().isLoginComplete());

            op.getSession().sendChat("/say cross version test");

            String alphaMsg = alphaBot.getSession().waitForChat("cross version test", 3000);
            assertNotNull(alphaMsg, "Alpha client should receive /say broadcast");

            String nettyMsg = nettyBot.getSession().waitForChat("cross version test", 3000);
            assertNotNull(nettyMsg, "Netty client should receive /say broadcast");
        } finally {
            op.disconnect();
            alphaBot.disconnect();
            nettyBot.disconnect();
        }
    }
}
