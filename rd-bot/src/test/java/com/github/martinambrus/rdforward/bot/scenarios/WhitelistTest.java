package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.api.PermissionManager;
import com.github.martinambrus.rdforward.server.api.ServerProperties;
import com.github.martinambrus.rdforward.server.api.WhitelistManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for whitelist enforcement and /whitelist command:
 * - Non-whitelisted player rejected when whitelist enabled
 * - Whitelisted player allowed when whitelist enabled
 * - Operator bypasses whitelist
 * - /whitelist on/off/add/remove/list via chat commands
 * - Whitelist disabled allows everyone
 */
class WhitelistTest {

    private static TestServer testServer;

    @BeforeAll
    static void startServer() throws InterruptedException {
        testServer = new TestServer();
        testServer.start();
    }

    @AfterAll
    static void stopServer() {
        // Ensure whitelist is disabled after tests
        ServerProperties.setWhiteList(false);
        testServer.stop();
    }

    @AfterEach
    void cleanUp() {
        ServerProperties.setWhiteList(false);
        WhitelistManager.removePlayer("WlAllow");
        WhitelistManager.removePlayer("WlDeny");
        WhitelistManager.removePlayer("WlAdd");
        WhitelistManager.removePlayer("WlRemove");
        PermissionManager.removeOp("WlOp");
        PermissionManager.removeOp("WlBypass");
    }

    @Test
    void whitelistDisabledAllowsEveryone() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "WlDeny");
        try {
            assertTrue(bot.getSession().isLoginComplete(),
                    "Player should connect when whitelist is disabled");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void whitelistEnabledRejectsNonWhitelisted() throws Exception {
        ServerProperties.setWhiteList(true);

        boolean connectFailed = false;
        try {
            BotClient bot = new BotClient("localhost", testServer.getPort(),
                    ProtocolVersion.BETA_1_7_3, "WlDeny", testServer.getBotGroup());
            bot.connectSync(3000);
            bot.disconnect();
        } catch (Exception e) {
            connectFailed = true;
        }
        assertTrue(connectFailed,
                "Non-whitelisted player should be rejected when whitelist is enabled");
    }

    @Test
    void whitelistEnabledAllowsWhitelisted() throws Exception {
        WhitelistManager.addPlayer("WlAllow");
        ServerProperties.setWhiteList(true);

        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "WlAllow");
        try {
            assertTrue(bot.getSession().isLoginComplete(),
                    "Whitelisted player should connect when whitelist is enabled");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void operatorBypassesWhitelist() throws Exception {
        PermissionManager.addOp("WlBypass");
        ServerProperties.setWhiteList(true);

        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "WlBypass");
        try {
            assertTrue(bot.getSession().isLoginComplete(),
                    "Operator should bypass whitelist and connect");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void whitelistCommandOnOff() throws Exception {
        PermissionManager.addOp("WlOp", PermissionManager.OP_MANAGE);
        ServerProperties.setWhiteList(false);

        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "WlOp");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete());

            // Enable whitelist via command
            session.sendChat("/whitelist on");
            String msg = session.waitForChat("Whitelist is now enabled", 3000);
            assertNotNull(msg, "Should receive whitelist enabled confirmation");
            assertTrue(ServerProperties.isWhiteList(), "Whitelist should be enabled");

            // Disable whitelist via command
            session.sendChat("/whitelist off");
            msg = session.waitForChat("Whitelist is now disabled", 3000);
            assertNotNull(msg, "Should receive whitelist disabled confirmation");
            assertFalse(ServerProperties.isWhiteList(), "Whitelist should be disabled");
        } finally {
            op.disconnect();
        }
    }

    @Test
    void whitelistCommandAddRemove() throws Exception {
        PermissionManager.addOp("WlOp", PermissionManager.OP_MANAGE);

        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "WlOp");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete());

            // Add player to whitelist
            session.sendChat("/whitelist add WlAdd");
            String msg = session.waitForChat("Added WlAdd to the whitelist", 3000);
            assertNotNull(msg, "Should confirm whitelist add");
            assertTrue(WhitelistManager.isWhitelisted("WlAdd"),
                    "Player should be whitelisted after add");

            // Remove player from whitelist
            session.sendChat("/whitelist remove WlAdd");
            msg = session.waitForChat("Removed WlAdd from the whitelist", 3000);
            assertNotNull(msg, "Should confirm whitelist remove");
            assertFalse(WhitelistManager.isWhitelisted("WlAdd"),
                    "Player should not be whitelisted after remove");
        } finally {
            op.disconnect();
        }
    }

    @Test
    void whitelistCommandListShowsPlayers() throws Exception {
        PermissionManager.addOp("WlOp", PermissionManager.OP_MANAGE);
        WhitelistManager.addPlayer("WlAllow");

        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "WlOp");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete());

            session.sendChat("/whitelist list");
            // Should see "wlallow" in the list (stored lowercase)
            String msg = session.waitForChat("wlallow", 3000);
            assertNotNull(msg, "Whitelist list should contain added player");
        } finally {
            op.disconnect();
        }
    }

    @Test
    void whitelistCommandRequiresOp() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "WlDeny");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());

            session.sendChat("/whitelist on");
            String msg = session.waitForChat("permission", 3000);
            assertNotNull(msg, "Non-op should receive permission denied for /whitelist");
            assertFalse(ServerProperties.isWhiteList(),
                    "Whitelist should not be enabled by non-op");
        } finally {
            bot.disconnect();
        }
    }
}
