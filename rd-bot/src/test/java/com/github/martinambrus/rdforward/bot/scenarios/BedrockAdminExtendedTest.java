package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotBedrockClient;
import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.api.BanManager;
import com.github.martinambrus.rdforward.server.api.PermissionManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended admin command tests involving Bedrock players.
 *
 * Covers scenarios not in BedrockAdminTimeWeatherTest:
 * - Ban/unban with Bedrock as op and as target
 * - tp-self-to-player and tp-player-to-player with Bedrock op
 * - Kick reason broadcast visible to Bedrock observer
 */
class BedrockAdminExtendedTest {

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

    // ---- Ban/Unban ----

    @Test
    void bedrockOpBansTcpPlayer() throws Exception {
        PermissionManager.addOp("BedBanOp");
        BotBedrockClient op = testServer.createBedrockBot("BedBanOp");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TcpBanTgt");
        try {
            assertTrue(op.getSession().isLoginComplete(), "Bedrock op login should complete");
            assertTrue(target.getSession().isLoginComplete(), "TCP target login should complete");

            op.getSession().sendChat("/ban TcpBanTgt");

            // Wait for target to disconnect
            long deadline = System.currentTimeMillis() + 5000;
            while (target.getChannel().isActive() && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            assertFalse(target.getChannel().isActive(), "TCP target should be disconnected by ban");

            // Reconnect should fail
            boolean reconnectFailed = false;
            try {
                BotClient reconnect = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TcpBanTgt");
                reconnect.disconnect();
            } catch (Exception e) {
                reconnectFailed = true;
            }
            assertTrue(reconnectFailed, "Banned TCP player should not reconnect");
        } finally {
            op.disconnect();
            target.disconnect();
            BanManager.unbanPlayer("TcpBanTgt");
            PermissionManager.removeOp("BedBanOp");
        }
    }

    @Test
    void tcpOpBansBedrockPlayer() throws Exception {
        PermissionManager.addOp("TcpBanOp");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TcpBanOp");
        BotBedrockClient target = testServer.createBedrockBot("BedBanTgt");
        try {
            BotSession opSession = op.getSession();
            BotSession targetSession = target.getSession();
            assertTrue(opSession.isLoginComplete(), "TCP op login should complete");
            assertTrue(targetSession.isLoginComplete(), "Bedrock target login should complete");

            opSession.sendChat("/ban BedBanTgt");

            // Wait for Bedrock target to disconnect
            long deadline = System.currentTimeMillis() + 5000;
            while (targetSession.isConnected() && System.currentTimeMillis() < deadline) {
                Thread.sleep(100);
            }
            assertFalse(targetSession.isConnected(),
                    "Bedrock target should be disconnected by ban");

            // Bedrock reconnect should fail
            boolean reconnectFailed = false;
            try {
                BotBedrockClient reconnect = testServer.createBedrockBot("BedBanTgt");
                reconnect.disconnect();
            } catch (Exception e) {
                reconnectFailed = true;
            }
            assertTrue(reconnectFailed, "Banned Bedrock player should not reconnect");
        } finally {
            op.disconnect();
            target.disconnect();
            BanManager.unbanPlayer("BedBanTgt");
            PermissionManager.removeOp("TcpBanOp");
        }
    }

    @Test
    void bedrockOpUnbansTcpPlayer() throws Exception {
        PermissionManager.addOp("BedUnOp");
        BanManager.banPlayer("TcpUnTgt");

        BotBedrockClient op = testServer.createBedrockBot("BedUnOp");
        try {
            assertTrue(op.getSession().isLoginComplete(), "Bedrock op login should complete");

            // Verify TCP player can't connect while banned
            boolean connectFailed = false;
            try {
                BotClient banned = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TcpUnTgt");
                banned.disconnect();
            } catch (Exception e) {
                connectFailed = true;
            }
            assertTrue(connectFailed, "TCP player should not connect while banned");

            // Unban via Bedrock op
            op.getSession().sendChat("/unban TcpUnTgt");
            Thread.sleep(200);

            // Now TCP player should connect successfully
            BotClient unbanned = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TcpUnTgt");
            try {
                assertTrue(unbanned.getSession().isLoginComplete(),
                        "TCP player should connect after Bedrock op unban");
            } finally {
                unbanned.disconnect();
            }
        } finally {
            op.disconnect();
            BanManager.unbanPlayer("TcpUnTgt");
            PermissionManager.removeOp("BedUnOp");
        }
    }

    @Test
    void tcpOpUnbansBedrockPlayer() throws Exception {
        PermissionManager.addOp("TcpUnOp");
        BanManager.banPlayer("BedUnTgt");

        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TcpUnOp");
        try {
            assertTrue(op.getSession().isLoginComplete(), "TCP op login should complete");

            // Verify Bedrock player can't connect while banned
            boolean connectFailed = false;
            try {
                BotBedrockClient banned = testServer.createBedrockBot("BedUnTgt");
                banned.disconnect();
            } catch (Exception e) {
                connectFailed = true;
            }
            assertTrue(connectFailed, "Bedrock player should not connect while banned");

            // Unban via TCP op
            op.getSession().sendChat("/unban BedUnTgt");
            Thread.sleep(200);

            // Now Bedrock player should connect successfully
            BotBedrockClient unbanned = testServer.createBedrockBot("BedUnTgt");
            try {
                assertTrue(unbanned.getSession().isLoginComplete(),
                        "Bedrock player should connect after TCP op unban");
            } finally {
                unbanned.disconnect();
            }
        } finally {
            op.disconnect();
            BanManager.unbanPlayer("BedUnTgt");
            PermissionManager.removeOp("TcpUnOp");
        }
    }

    // ---- Teleport variants ----

    @Test
    void bedrockOpTpSelfToTcpPlayer() throws Exception {
        PermissionManager.addOp("BedTpS");
        BotBedrockClient op = testServer.createBedrockBot("BedTpS");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TcpTpDst");
        try {
            BotSession opSession = op.getSession();
            BotSession targetSession = target.getSession();
            assertTrue(opSession.isLoginComplete());
            assertTrue(targetSession.isLoginComplete());

            double targetX = targetSession.getX();
            double targetZ = targetSession.getZ();

            int prevCount = opSession.getPositionUpdateCount();
            opSession.sendChat("/tp TcpTpDst");

            boolean updated = opSession.waitForPositionUpdate(prevCount, 3000);
            assertTrue(updated, "Bedrock op should receive position update");

            assertEquals(targetX, opSession.getX(), 1.0, "X should match target");
            assertEquals(targetZ, opSession.getZ(), 1.0, "Z should match target");
        } finally {
            op.disconnect();
            target.disconnect();
            PermissionManager.removeOp("BedTpS");
        }
    }

    @Test
    void bedrockOpTpTcpPlayerToTcpPlayer() throws Exception {
        PermissionManager.addOp("BedTpOp3");
        BotBedrockClient op = testServer.createBedrockBot("BedTpOp3");
        BotClient playerA = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TpA2");
        BotClient playerB = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TpB2");
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
            opSession.sendChat("/tp TpA2 TpB2");

            boolean updated = sessionA.waitForPositionUpdate(prevCount, 3000);
            assertTrue(updated, "TpA2 should receive position update from Bedrock op's /tp");

            assertEquals(bX, sessionA.getX(), 1.0, "A's X should match B");
            assertEquals(bY, sessionA.getY(), 1.0, "A's Y should match B");
            assertEquals(bZ, sessionA.getZ(), 1.0, "A's Z should match B");
        } finally {
            op.disconnect();
            playerA.disconnect();
            playerB.disconnect();
            PermissionManager.removeOp("BedTpOp3");
        }
    }

    // ---- Kick reason broadcast ----

    @Test
    void bedrockOpKickReasonVisibleToObserver() throws Exception {
        PermissionManager.addOp("BedKROp");
        BotBedrockClient op = testServer.createBedrockBot("BedKROp");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "KRTarget");
        BotBedrockClient observer = testServer.createBedrockBot("BedKRObs");
        try {
            BotSession opSession = op.getSession();
            BotSession observerSession = observer.getSession();
            assertTrue(opSession.isLoginComplete());
            assertTrue(target.getSession().isLoginComplete());
            assertTrue(observerSession.isLoginComplete());

            opSession.sendChat("/kick KRTarget griefing");

            String msg = observerSession.waitForChat("was kicked: griefing", 3000);
            assertNotNull(msg, "Bedrock observer should see kick reason broadcast");
        } finally {
            op.disconnect();
            target.disconnect();
            observer.disconnect();
            PermissionManager.removeOp("BedKROp");
        }
    }
}
