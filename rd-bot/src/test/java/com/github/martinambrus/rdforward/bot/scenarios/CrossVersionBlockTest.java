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

    @Test
    void v340PlacementVisibleToV109() throws Exception {
        BotClient v340Bot = testServer.createBot(ProtocolVersion.RELEASE_1_12_2, "V340Placer");
        BotClient v109Bot = testServer.createBot(ProtocolVersion.RELEASE_1_9_4, "V109Watch2");
        try {
            BotSession v340Session = v340Bot.getSession();
            BotSession v109Session = v109Bot.getSession();
            assertTrue(v340Session.isLoginComplete(), "V340 login should complete");
            assertTrue(v109Session.isLoginComplete(), "V109 login should complete");

            Thread.sleep(500);

            int placeX = 60;
            int placeY = 42;
            int placeZ = 60;
            v340Session.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            int blockType = v109Session.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "V109 bot should see V340's block placement");
        } finally {
            v340Bot.disconnect();
            v109Bot.disconnect();
        }
    }

    @Test
    void v477PlacementVisibleToAlpha() throws Exception {
        BotClient v477Bot = testServer.createBot(ProtocolVersion.RELEASE_1_14, "V477Placer");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaWatch3");
        try {
            BotSession v477Session = v477Bot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            assertTrue(v477Session.isLoginComplete(), "1.14 login should complete");
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");

            Thread.sleep(500);

            int placeX = 65;
            int placeY = 42;
            int placeZ = 65;
            v477Session.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            int blockType = alphaSession.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "Alpha bot should see 1.14's block placement");
        } finally {
            v477Bot.disconnect();
            alphaBot.disconnect();
        }
    }

    @Test
    void alphaPlacementVisibleToV764() throws Exception {
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaPlacer3");
        BotClient v764Bot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "V764Watch");
        try {
            BotSession alphaSession = alphaBot.getSession();
            BotSession v764Session = v764Bot.getSession();
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            assertTrue(v764Session.isLoginComplete(), "1.20.2 login should complete");

            Thread.sleep(500);

            int placeX = 70;
            int placeY = 42;
            int placeZ = 70;
            alphaSession.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            // 1.20.2 uses block state IDs; non-zero means non-air
            int blockType = v764Session.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "1.20.2 bot should see Alpha's block placement");
        } finally {
            alphaBot.disconnect();
            v764Bot.disconnect();
        }
    }

    @Test
    void v393PlacementVisibleToV47() throws Exception {
        BotClient v393Bot = testServer.createBot(ProtocolVersion.RELEASE_1_13, "V393Placer");
        BotClient v47Bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "V47Watch");
        try {
            BotSession v393Session = v393Bot.getSession();
            BotSession v47Session = v47Bot.getSession();
            assertTrue(v393Session.isLoginComplete(), "1.13 login should complete");
            assertTrue(v47Session.isLoginComplete(), "1.8 login should complete");

            Thread.sleep(500);

            int placeX = 110, placeY = 42, placeZ = 110;
            v393Session.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            int blockType = v47Session.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "V47 bot should see 1.13's block placement");
        } finally {
            v393Bot.disconnect();
            v47Bot.disconnect();
        }
    }

    @Test
    void v735PlacementVisibleToAlpha() throws Exception {
        BotClient v735Bot = testServer.createBot(ProtocolVersion.RELEASE_1_16, "V735Placer");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaWatch4");
        try {
            BotSession v735Session = v735Bot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            assertTrue(v735Session.isLoginComplete(), "1.16 login should complete");
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");

            Thread.sleep(500);

            int placeX = 112, placeY = 42, placeZ = 112;
            v735Session.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            int blockType = alphaSession.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "Alpha bot should see 1.16's block placement");
        } finally {
            v735Bot.disconnect();
            alphaBot.disconnect();
        }
    }

    @Test
    void v774PlacementVisibleToV340() throws Exception {
        BotClient v774Bot = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "V774Placer");
        BotClient v340Bot = testServer.createBot(ProtocolVersion.RELEASE_1_12_2, "V340Watch2");
        try {
            BotSession v774Session = v774Bot.getSession();
            BotSession v340Session = v340Bot.getSession();
            assertTrue(v774Session.isLoginComplete(), "1.21.11 login should complete");
            assertTrue(v340Session.isLoginComplete(), "1.12.2 login should complete");

            Thread.sleep(500);

            int placeX = 75;
            int placeY = 42;
            int placeZ = 75;
            v774Session.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            int blockType = v340Session.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "1.12.2 bot should see 1.21.11's block placement");
        } finally {
            v774Bot.disconnect();
            v340Bot.disconnect();
        }
    }
}
