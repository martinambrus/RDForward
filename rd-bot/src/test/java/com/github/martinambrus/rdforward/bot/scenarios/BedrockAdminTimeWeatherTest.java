package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotBedrockClient;
import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.api.PermissionManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Bedrock-specific admin commands, time/weather reception,
 * and join/leave broadcast scenarios.
 */
class BedrockAdminTimeWeatherTest {

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

    // ---- Admin: Bedrock as op ----

    @Test
    void bedrockOpCanKickTcpPlayer() throws Exception {
        PermissionManager.addOp("BedOp1");
        BotBedrockClient op = testServer.createBedrockBot("BedOp1");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "BedKickTgt");
        try {
            BotSession opSession = op.getSession();
            BotSession targetSession = target.getSession();
            assertTrue(opSession.isLoginComplete(), "Bedrock op login should complete");
            assertTrue(targetSession.isLoginComplete(), "Target login should complete");

            opSession.sendChat("/kick BedKickTgt");

            long deadline = System.currentTimeMillis() + 3000;
            while (target.getChannel().isActive() && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            assertFalse(target.getChannel().isActive(), "Target should be disconnected by Bedrock op");
        } finally {
            op.disconnect();
            target.disconnect();
            PermissionManager.removeOp("BedOp1");
        }
    }

    @Test
    void bedrockOpCanTeleportTcpPlayer() throws Exception {
        PermissionManager.addOp("BedOp2");
        BotBedrockClient op = testServer.createBedrockBot("BedOp2");
        BotClient target = testServer.createBot(ProtocolVersion.BETA_1_7_3, "BedTpTgt");
        try {
            BotSession opSession = op.getSession();
            BotSession targetSession = target.getSession();
            assertTrue(opSession.isLoginComplete(), "Bedrock op login should complete");
            assertTrue(targetSession.isLoginComplete(), "Target login should complete");

            int prevCount = targetSession.getPositionUpdateCount();
            opSession.sendChat("/tp BedTpTgt 20 50 20");

            boolean updated = targetSession.waitForPositionUpdate(prevCount, 3000);
            assertTrue(updated, "Target should receive position update from Bedrock op's /tp");

            assertEquals(20.0, targetSession.getX(), 1.0, "X should be ~20");
            assertEquals(20.0, targetSession.getZ(), 1.0, "Z should be ~20");
        } finally {
            op.disconnect();
            target.disconnect();
            PermissionManager.removeOp("BedOp2");
        }
    }

    // ---- Admin: Bedrock as target ----

    @Test
    void tcpOpCanKickBedrockPlayer() throws Exception {
        PermissionManager.addOp("TcpKickOp");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TcpKickOp");
        BotBedrockClient target = testServer.createBedrockBot("BedKickMe");
        try {
            BotSession opSession = op.getSession();
            BotSession targetSession = target.getSession();
            assertTrue(opSession.isLoginComplete(), "TCP op login should complete");
            assertTrue(targetSession.isLoginComplete(), "Bedrock target login should complete");

            opSession.sendChat("/kick BedKickMe");

            long deadline = System.currentTimeMillis() + 5000;
            while (targetSession.isConnected() && System.currentTimeMillis() < deadline) {
                Thread.sleep(100);
            }
            assertFalse(targetSession.isConnected(),
                    "Bedrock player should be disconnected by TCP op's kick");
        } finally {
            op.disconnect();
            target.disconnect();
            PermissionManager.removeOp("TcpKickOp");
        }
    }

    @Test
    void tcpOpCanTeleportBedrockPlayer() throws Exception {
        PermissionManager.addOp("TcpTpOp");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TcpTpOp");
        BotBedrockClient target = testServer.createBedrockBot("BedTpMe");
        try {
            BotSession opSession = op.getSession();
            BotSession targetSession = target.getSession();
            assertTrue(opSession.isLoginComplete(), "TCP op login should complete");
            assertTrue(targetSession.isLoginComplete(), "Bedrock target login should complete");

            int prevCount = targetSession.getPositionUpdateCount();
            opSession.sendChat("/tp BedTpMe 30 50 30");

            boolean updated = targetSession.waitForPositionUpdate(prevCount, 3000);
            assertTrue(updated, "Bedrock target should receive position update from /tp");

            // Bedrock Y is eye-level
            double expectedEyeY = 50 + 1.62;
            assertEquals(30.0, targetSession.getX(), 1.0, "X should be ~30");
            assertEquals(expectedEyeY, targetSession.getY(), 1.0, "Y should be ~51.62 (eye-level)");
            assertEquals(30.0, targetSession.getZ(), 1.0, "Z should be ~30");
        } finally {
            op.disconnect();
            target.disconnect();
            PermissionManager.removeOp("TcpTpOp");
        }
    }

    // ---- Time ----

    @Test
    void bedrockReceivesTimeUpdates() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedTime1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            boolean received = session.waitForTimeUpdate(0, 3000);
            assertTrue(received, "Bedrock should receive time updates via SetTimePacket");

            long time = session.getLastTimeOfDay();
            assertTrue(time >= 0, "Time of day should be non-negative (got " + time + ")");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void bedrockReceivesTimeSetCommand() throws Exception {
        PermissionManager.addOp("BedTimeOp");
        BotBedrockClient op = testServer.createBedrockBot("BedTimeOp");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            session.waitForTimeUpdate(0, 3000);
            int countBefore = session.getTimeUpdateCount();

            session.sendChat("/time set 18000");

            boolean received = session.waitForTimeUpdate(countBefore, 3000);
            assertTrue(received, "Bedrock should receive time update after /time set");

            long time = session.getLastTimeOfDay();
            assertTrue(time >= 18000 && time < 18100,
                    "Time should be near 18000 but was " + time);
        } finally {
            op.disconnect();
            PermissionManager.removeOp("BedTimeOp");
        }
    }

    // ---- Weather ----

    @Test
    void bedrockReceivesWeatherRain() throws Exception {
        PermissionManager.addOp("BedRainOp");
        BotBedrockClient op = testServer.createBedrockBot("BedRainOp");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int weatherCount = session.getWeatherChangeCount();
            session.sendChat("/weather rain");

            boolean received = session.waitForWeatherChange(weatherCount, 3000);
            assertTrue(received, "Bedrock should receive weather change for rain");

            // Bedrock LevelEvent: START_RAINING = 1
            assertEquals(1, session.getLastWeatherReason(),
                    "Weather reason should be START_RAINING (1)");

            // Clean up
            session.sendChat("/weather clear");
        } finally {
            op.disconnect();
            PermissionManager.removeOp("BedRainOp");
        }
    }

    @Test
    void bedrockReceivesWeatherClear() throws Exception {
        PermissionManager.addOp("BedClrOp");
        BotBedrockClient op = testServer.createBedrockBot("BedClrOp");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Start rain first
            session.sendChat("/weather rain");
            session.waitForWeatherChange(0, 3000);

            int weatherCount = session.getWeatherChangeCount();
            session.sendChat("/weather clear");

            boolean received = session.waitForWeatherChange(weatherCount, 3000);
            assertTrue(received, "Bedrock should receive weather change for clear");

            // Bedrock LevelEvent: STOP_RAINING = 2
            assertEquals(2, session.getLastWeatherReason(),
                    "Weather reason should be STOP_RAINING (2)");
        } finally {
            op.disconnect();
            PermissionManager.removeOp("BedClrOp");
        }
    }

    // ---- Join/Leave Broadcasts ----

    @Test
    void bedrockSeesJoinAndLeaveBroadcasts() throws Exception {
        BotBedrockClient observer = testServer.createBedrockBot("BedObs1");
        try {
            BotSession observerSession = observer.getSession();
            assertTrue(observerSession.isLoginComplete(), "Observer login should complete");

            BotClient joiner = testServer.createBot(ProtocolVersion.BETA_1_7_3, "BedJoinee");
            BotSession joinerSession = joiner.getSession();
            assertTrue(joinerSession.isLoginComplete(), "Joiner login should complete");

            String joinMsg = observerSession.waitForChat("BedJoinee joined the game", 3000);
            assertNotNull(joinMsg, "Bedrock observer should see TCP player join");

            joiner.disconnect();
            Thread.sleep(200);

            String leaveMsg = observerSession.waitForChat("BedJoinee left the game", 5000);
            assertNotNull(leaveMsg, "Bedrock observer should see TCP player leave");
        } finally {
            observer.disconnect();
        }
    }

    @Test
    void tcpSeesBedrockJoinAndLeave() throws Exception {
        BotClient observer = testServer.createBot(ProtocolVersion.RELEASE_1_8, "TcpObs1");
        try {
            BotSession observerSession = observer.getSession();
            assertTrue(observerSession.isLoginComplete(), "TCP observer login should complete");

            BotBedrockClient joiner = testServer.createBedrockBot("BedJoiner");
            BotSession joinerSession = joiner.getSession();
            assertTrue(joinerSession.isLoginComplete(), "Bedrock joiner login should complete");

            String joinMsg = observerSession.waitForChat("BedJoiner joined the game", 3000);
            assertNotNull(joinMsg, "TCP observer should see Bedrock player join");

            joiner.disconnect();
            Thread.sleep(200);

            String leaveMsg = observerSession.waitForChat("BedJoiner left the game", 5000);
            assertNotNull(leaveMsg, "TCP observer should see Bedrock player leave");
        } finally {
            observer.disconnect();
        }
    }
}
