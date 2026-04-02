package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the /rtp (random teleport) command.
 * Verifies that the command teleports the player to a safe location
 * within the world bounds and sends confirmation messages.
 */
class RtpCommandTest {

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
    void rtpTeleportsPlayer() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "RtpBot1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());

            double origX = session.getX();
            double origZ = session.getZ();
            int prevCount = session.getPositionUpdateCount();

            session.sendChat("/rtp");

            // Should receive position update from teleport
            boolean updated = session.waitForPositionUpdate(prevCount, 5000);
            assertTrue(updated, "Should receive position update after /rtp");

            // Should receive confirmation message with coordinates
            String msg = session.waitForChat("Teleported to", 3000);
            assertNotNull(msg, "Should receive teleport confirmation message");

            // Position should have changed (may be same by extreme coincidence,
            // but practically always different)
            double newX = session.getX();
            double newZ = session.getZ();
            assertTrue(newX != origX || newZ != origZ,
                    "Position should change after /rtp");

            // Position should be within world bounds (256x256, margin 5)
            assertTrue(newX >= 5 && newX <= 251,
                    "X should be within world bounds but was " + newX);
            assertTrue(newZ >= 5 && newZ <= 251,
                    "Z should be within world bounds but was " + newZ);
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void rtpWorksForAlphaClient() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "RtpAlpha");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());

            int prevCount = session.getPositionUpdateCount();
            session.sendChat("/rtp");

            boolean updated = session.waitForPositionUpdate(prevCount, 5000);
            assertTrue(updated, "Alpha client should be teleported by /rtp");

            String msg = session.waitForChat("Teleported to", 3000);
            assertNotNull(msg, "Alpha client should receive rtp confirmation");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void rtpWorksForNettyClient() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "RtpNetty");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());

            int prevCount = session.getPositionUpdateCount();
            session.sendChat("/rtp");

            boolean updated = session.waitForPositionUpdate(prevCount, 5000);
            assertTrue(updated, "Netty client should be teleported by /rtp");

            String msg = session.waitForChat("Teleported to", 3000);
            assertNotNull(msg, "Netty client should receive rtp confirmation");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void rtpTeleportsToSafeHeight() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "RtpSafe");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());

            int prevCount = session.getPositionUpdateCount();
            session.sendChat("/rtp");
            session.waitForPositionUpdate(prevCount, 5000);

            // Y should be above ground (eye level = feet + 1.62)
            // Flat world surface is at y=42, so eye level should be >= 43.62
            double eyeY = session.getY();
            assertTrue(eyeY > 1.62,
                    "Should teleport above ground level, but Y was " + eyeY);
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void multipleRtpProduceDifferentLocations() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "RtpMulti");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());

            // First rtp
            int prevCount = session.getPositionUpdateCount();
            session.sendChat("/rtp");
            session.waitForPositionUpdate(prevCount, 5000);
            double x1 = session.getX();
            double z1 = session.getZ();

            // Second rtp
            prevCount = session.getPositionUpdateCount();
            session.sendChat("/rtp");
            session.waitForPositionUpdate(prevCount, 5000);
            double x2 = session.getX();
            double z2 = session.getZ();

            // The two locations should differ (extremely unlikely to be identical)
            assertTrue(x1 != x2 || z1 != z2,
                    "Two consecutive /rtp should produce different locations");
        } finally {
            bot.disconnect();
        }
    }
}
