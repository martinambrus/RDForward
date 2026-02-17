package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that block placement broadcasts across protocol versions.
 * One bot places a block, the other (using a different protocol) sees the change.
 */
class CrossVersionBlockTest {

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
    void alphaPlacementVisibleToRelease() throws Exception {
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaPlacer");
        BotClient releaseBot = testServer.createBot(ProtocolVersion.RELEASE_1_3_1, "ReleaseWatcher");
        try {
            BotSession alphaSession = alphaBot.getSession();
            BotSession releaseSession = releaseBot.getSession();
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            assertTrue(releaseSession.isLoginComplete(), "Release login should complete");

            Thread.sleep(500);

            // Alpha places a block on the surface top face (y=42 → target y=43)
            int placeX = 30;
            int placeY = 42;
            int placeZ = 30;
            alphaSession.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            // Release bot should see the block change at y=43
            int blockType = releaseSession.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "Release bot should see Alpha's block placement");
        } finally {
            alphaBot.disconnect();
            releaseBot.disconnect();
        }
    }

    @Test
    void nettyPlacementVisibleToBeta() throws Exception {
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_7_6, "NettyPlacer");
        BotClient betaBot = testServer.createBot(ProtocolVersion.BETA_1_8, "BetaWatcher");
        try {
            BotSession nettySession = nettyBot.getSession();
            BotSession betaSession = betaBot.getSession();
            assertTrue(nettySession.isLoginComplete(), "Netty login should complete");
            assertTrue(betaSession.isLoginComplete(), "Beta login should complete");

            Thread.sleep(500);

            // Netty places a block on the surface top face
            int placeX = 40;
            int placeY = 42;
            int placeZ = 40;
            nettySession.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            // Beta bot should see the block change at y=43
            int blockType = betaSession.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "Beta bot should see Netty's block placement");
        } finally {
            nettyBot.disconnect();
            betaBot.disconnect();
        }
    }

    @Test
    void nettyV109PlacementVisibleToAlpha() throws Exception {
        BotClient v109Bot = testServer.createBot(ProtocolVersion.RELEASE_1_9_4, "V109Placer");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaWatch2");
        try {
            BotSession v109Session = v109Bot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            assertTrue(v109Session.isLoginComplete(), "V109 login should complete");
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");

            Thread.sleep(500);

            int placeX = 50;
            int placeY = 42;
            int placeZ = 50;
            v109Session.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            int blockType = alphaSession.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "Alpha bot should see V109's block placement");
        } finally {
            v109Bot.disconnect();
            alphaBot.disconnect();
        }
    }

    @Test
    void alphaPlacementVisibleToNettyV109() throws Exception {
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaPlacer2");
        BotClient v109Bot = testServer.createBot(ProtocolVersion.RELEASE_1_9_4, "V109Watch");
        try {
            BotSession alphaSession = alphaBot.getSession();
            BotSession v109Session = v109Bot.getSession();
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            assertTrue(v109Session.isLoginComplete(), "V109 login should complete");

            Thread.sleep(500);

            int placeX = 55;
            int placeY = 42;
            int placeZ = 55;
            alphaSession.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            int blockType = v109Session.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "V109 bot should see Alpha's block placement");
        } finally {
            alphaBot.disconnect();
            v109Bot.disconnect();
        }
    }
}
