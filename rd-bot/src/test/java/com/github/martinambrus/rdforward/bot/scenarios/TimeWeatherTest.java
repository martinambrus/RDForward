package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.api.PermissionManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for time/weather system:
 * - Time advances automatically via tick loop
 * - /time set changes time and broadcasts update
 * - /time freeze stops time advancement
 * - /weather rain sends weather change packets
 * - /weather clear stops rain
 */
class TimeWeatherTest {

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
    void timeAdvancesAutomatically() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TimeBot1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Wait for at least one time update to arrive
            boolean received = session.waitForTimeUpdate(0, 3000);
            assertTrue(received, "Should receive time updates");

            long firstTime = session.getLastTimeOfDay();
            int firstCount = session.getTimeUpdateCount();

            // Wait for another time update
            received = session.waitForTimeUpdate(firstCount, 3000);
            assertTrue(received, "Should receive a second time update");

            long secondTime = session.getLastTimeOfDay();
            // Time should advance (both values should be positive since time is not frozen)
            assertTrue(secondTime >= firstTime,
                    "Time should advance: first=" + firstTime + " second=" + secondTime);
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void timeSetChangesTime() throws Exception {
        PermissionManager.addOp("TimeSetOp");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TimeSetOp");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete());

            // Wait for initial time update
            session.waitForTimeUpdate(0, 3000);
            int countBefore = session.getTimeUpdateCount();

            // Set time to 18000 (night)
            session.sendChat("/time set 18000");

            // Wait for the time update broadcast
            boolean received = session.waitForTimeUpdate(countBefore, 3000);
            assertTrue(received, "Should receive time update after /time set");

            long time = session.getLastTimeOfDay();
            // Time should be at or near 18000 (may have advanced slightly)
            assertTrue(time >= 18000 && time < 18100,
                    "Time should be near 18000 but was " + time);

            // Also verify chat confirmation
            String msg = session.waitForChat("Set time to 18000", 3000);
            assertNotNull(msg, "Should receive confirmation message");
        } finally {
            op.disconnect();
            PermissionManager.removeOp("TimeSetOp");
        }
    }

    @Test
    void timeFreezeStopsAdvancement() throws Exception {
        PermissionManager.addOp("FreezeOp");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "FreezeOp");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete());

            // Set a known time first
            session.sendChat("/time set 6000");
            session.waitForChat("Set time to 6000", 3000);

            // Wait for time update with the new time
            int countBefore = session.getTimeUpdateCount();
            session.waitForTimeUpdate(countBefore, 3000);

            // Freeze time
            countBefore = session.getTimeUpdateCount();
            session.sendChat("/time freeze");
            session.waitForChat("Time frozen", 3000);

            // Wait for the freeze broadcast
            boolean received = session.waitForTimeUpdate(countBefore, 3000);
            assertTrue(received, "Should receive time update after freeze");

            // Frozen time is sent as negative value in protocol
            long frozenTime = session.getLastTimeOfDay();
            assertTrue(frozenTime < 0,
                    "Frozen time should be negative but was " + frozenTime);

            // The absolute value should be near 6000
            long absTime = Math.abs(frozenTime);
            assertTrue(absTime >= 6000 && absTime < 6100,
                    "Frozen time absolute should be near 6000 but was " + absTime);

            // Unfreeze for cleanup
            session.sendChat("/time unfreeze");
            session.waitForChat("Time unfrozen", 3000);
        } finally {
            op.disconnect();
            PermissionManager.removeOp("FreezeOp");
        }
    }

    @Test
    void weatherRainSendsPackets() throws Exception {
        PermissionManager.addOp("RainOp");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "RainOp");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete());

            int weatherCountBefore = session.getWeatherChangeCount();

            // Start rain
            session.sendChat("/weather rain");

            // Wait for weather change packet
            boolean received = session.waitForWeatherChange(weatherCountBefore, 3000);
            assertTrue(received, "Should receive weather change packet for rain");

            // Pre-Netty: reason 1 = begin rain
            assertEquals(1, session.getLastWeatherReason(),
                    "Weather reason should be BEGIN_RAIN (1)");

            // Verify chat confirmation
            String msg = session.waitForChat("Set weather to rain", 3000);
            assertNotNull(msg, "Should receive rain confirmation");

            // Clean up: clear weather
            session.sendChat("/weather clear");
            session.waitForChat("Set weather to clear", 3000);
        } finally {
            op.disconnect();
            PermissionManager.removeOp("RainOp");
        }
    }

    @Test
    void weatherClearStopsRain() throws Exception {
        PermissionManager.addOp("ClearOp");
        BotClient op = testServer.createBot(ProtocolVersion.BETA_1_7_3, "ClearOp");
        try {
            BotSession session = op.getSession();
            assertTrue(session.isLoginComplete());

            // Start rain first
            session.sendChat("/weather rain");
            session.waitForChat("Set weather to rain", 3000);

            int weatherCountBefore = session.getWeatherChangeCount();

            // Clear weather
            session.sendChat("/weather clear");

            // Wait for weather change packet
            boolean received = session.waitForWeatherChange(weatherCountBefore, 3000);
            assertTrue(received, "Should receive weather change packet for clear");

            // Pre-Netty: reason 2 = end rain
            assertEquals(2, session.getLastWeatherReason(),
                    "Weather reason should be END_RAIN (2)");

            String msg = session.waitForChat("Set weather to clear", 3000);
            assertNotNull(msg, "Should receive clear confirmation");
        } finally {
            op.disconnect();
            PermissionManager.removeOp("ClearOp");
        }
    }
}
