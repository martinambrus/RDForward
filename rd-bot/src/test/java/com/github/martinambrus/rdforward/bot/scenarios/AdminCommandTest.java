package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.api.BanManager;
import com.github.martinambrus.rdforward.server.api.PermissionManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for admin commands: kick, tp, ban, unban.
 * Uses Beta 1.7.3 (v14) for the op bot since it supports chat.
 */
class AdminCommandTest {

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
    void kickDisconnectsPlayer() throws Exception {
        PermissionManager.addOp("KickOp");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "KickOp");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "KickTarget");
        try {
            BotSession opSession = op.getSession();
            BotSession targetSession = target.getSession();
            assertTrue(opSession.isLoginComplete(), "Op login should complete");
            assertTrue(targetSession.isLoginComplete(), "Target login should complete");

            // Op kicks the target
            opSession.sendChat("/kick KickTarget");

            // Wait for target to disconnect
            long deadline = System.currentTimeMillis() + 3000;
            while (target.getChannel().isActive() && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            assertFalse(target.getChannel().isActive(), "KickTarget should be disconnected");
        } finally {
            op.disconnect();
            target.disconnect();
            PermissionManager.removeOp("KickOp");
        }
    }

    @Test
    void kickReasonBroadcasts() throws Exception {
        PermissionManager.addOp("KickOp2");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "KickOp2");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "KickTarget2");
        try {
            BotSession opSession = op.getSession();
            assertTrue(opSession.isLoginComplete());
            assertTrue(target.getSession().isLoginComplete());

            opSession.sendChat("/kick KickTarget2 griefing");

            // Op should receive the kick broadcast message
            String msg = opSession.waitForChat("was kicked: griefing", 3000);
            assertNotNull(msg, "Op should see kick reason broadcast");
        } finally {
            op.disconnect();
            target.disconnect();
            PermissionManager.removeOp("KickOp2");
        }
    }

    @Test
    void tpToCoordinates() throws Exception {
        PermissionManager.addOp("TpOp");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TpOp");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TpTarget");
        try {
            BotSession opSession = op.getSession();
            BotSession targetSession = target.getSession();
            assertTrue(opSession.isLoginComplete());
            assertTrue(targetSession.isLoginComplete());

            int prevCount = targetSession.getPositionUpdateCount();
            opSession.sendChat("/tp TpTarget 20 50 20");

            // Target should receive a position update near (20, 51.62, 20)
            boolean updated = targetSession.waitForPositionUpdate(prevCount, 3000);
            assertTrue(updated, "TpTarget should receive position update");

            // Check coordinates. Y should be eyes = 50 + 1.62f
            double expectedEyeY = 50 + (double) 1.62f;
            assertEquals(20.0, targetSession.getX(), 1.0, "X should be ~20");
            assertEquals(expectedEyeY, targetSession.getY(), 1.0, "Y should be ~51.62");
            assertEquals(20.0, targetSession.getZ(), 1.0, "Z should be ~20");
        } finally {
            op.disconnect();
            target.disconnect();
            PermissionManager.removeOp("TpOp");
        }
    }

    @Test
    void tpSelfToPlayer() throws Exception {
        PermissionManager.addOp("TpSelf");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TpSelf");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TpDest");
        try {
            BotSession opSession = op.getSession();
            BotSession targetSession = target.getSession();
            assertTrue(opSession.isLoginComplete());
            assertTrue(targetSession.isLoginComplete());

            double targetX = targetSession.getX();
            double targetY = targetSession.getY();
            double targetZ = targetSession.getZ();

            int prevCount = opSession.getPositionUpdateCount();
            opSession.sendChat("/tp TpDest");

            boolean updated = opSession.waitForPositionUpdate(prevCount, 3000);
            assertTrue(updated, "Op should receive position update");

            // Op position should be near target's position
            assertEquals(targetX, opSession.getX(), 1.0, "X should match target");
            assertEquals(targetY, opSession.getY(), 1.0, "Y should match target");
            assertEquals(targetZ, opSession.getZ(), 1.0, "Z should match target");
        } finally {
            op.disconnect();
            target.disconnect();
            PermissionManager.removeOp("TpSelf");
        }
    }

    @Test
    void tpPlayerToPlayer() throws Exception {
        PermissionManager.addOp("TpOp3");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TpOp3");
        BotClient playerA = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TpA");
        BotClient playerB = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TpB");
        try {
            BotSession opSession = op.getSession();
            BotSession sessionA = playerA.getSession();
            BotSession sessionB = playerB.getSession();
            assertTrue(opSession.isLoginComplete());
            assertTrue(sessionA.isLoginComplete());
            assertTrue(sessionB.isLoginComplete());

            double bX = sessionB.getX();
            double bY = sessionB.getY();
            double bZ = sessionB.getZ();

            int prevCount = sessionA.getPositionUpdateCount();
            opSession.sendChat("/tp TpA TpB");

            boolean updated = sessionA.waitForPositionUpdate(prevCount, 3000);
            assertTrue(updated, "TpA should receive position update");

            assertEquals(bX, sessionA.getX(), 1.0, "A's X should match B");
            assertEquals(bY, sessionA.getY(), 1.0, "A's Y should match B");
            assertEquals(bZ, sessionA.getZ(), 1.0, "A's Z should match B");
        } finally {
            op.disconnect();
            playerA.disconnect();
            playerB.disconnect();
            PermissionManager.removeOp("TpOp3");
        }
    }

    @Test
    void banPreventsLogin() throws Exception {
        PermissionManager.addOp("BanOp");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "BanOp");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "BanBot");
        try {
            assertTrue(op.getSession().isLoginComplete());
            assertTrue(target.getSession().isLoginComplete());

            // Ban the target
            op.getSession().sendChat("/ban BanBot");

            // Wait for target to disconnect
            long deadline = System.currentTimeMillis() + 3000;
            while (target.getChannel().isActive() && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            assertFalse(target.getChannel().isActive(), "BanBot should be disconnected");

            // Try reconnecting — should fail
            boolean reconnectFailed = false;
            try {
                BotClient reconnect = new BotClient("localhost", testServer.getPort(),
                        ProtocolVersion.BETA_1_7_3, "BanBot", testServer.getBotGroup());
                reconnect.connectSync(3000);
                reconnect.disconnect();
            } catch (Exception e) {
                reconnectFailed = true;
            }
            assertTrue(reconnectFailed, "Banned player should not be able to reconnect");
        } finally {
            op.disconnect();
            target.disconnect();
            BanManager.unbanPlayer("BanBot");
            PermissionManager.removeOp("BanOp");
        }
    }

    @Test
    void unbanAllowsLogin() throws Exception {
        PermissionManager.addOp("UnbanOp");
        BanManager.banPlayer("UnbanBot");

        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "UnbanOp");
        try {
            assertTrue(op.getSession().isLoginComplete());

            // Verify UnbanBot can't connect while banned
            boolean connectFailed = false;
            try {
                BotClient banned = new BotClient("localhost", testServer.getPort(),
                        ProtocolVersion.BETA_1_7_3, "UnbanBot", testServer.getBotGroup());
                banned.connectSync(3000);
                banned.disconnect();
            } catch (Exception e) {
                connectFailed = true;
            }
            assertTrue(connectFailed, "UnbanBot should not connect while banned");

            // Unban via command
            op.getSession().sendChat("/unban UnbanBot");
            Thread.sleep(200); // Allow command to process

            // Now UnbanBot should connect successfully
            BotClient unbanBot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "UnbanBot");
            try {
                assertTrue(unbanBot.getSession().isLoginComplete(), "UnbanBot should connect after unban");
            } finally {
                unbanBot.disconnect();
            }
        } finally {
            op.disconnect();
            BanManager.unbanPlayer("UnbanBot");
            PermissionManager.removeOp("UnbanOp");
        }
    }
}
