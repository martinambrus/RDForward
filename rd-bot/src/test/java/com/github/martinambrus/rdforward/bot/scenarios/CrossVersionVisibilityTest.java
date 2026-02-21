package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that bots from six different protocol families can all see
 * each other via SpawnPlayerPacket broadcasts.
 */
class CrossVersionVisibilityTest {

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
    void sixProtocolsCanSeeEachOther() throws Exception {
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaVis");
        BotClient betaBot = testServer.createBot(ProtocolVersion.BETA_1_8, "BetaVis");
        BotClient releaseBot = testServer.createBot(ProtocolVersion.RELEASE_1_5, "ReleaseVis");
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_7_6, "NettyVis");
        BotClient v109Bot = testServer.createBot(ProtocolVersion.RELEASE_1_9_4, "V109Vis");
        BotClient v340Bot = testServer.createBot(ProtocolVersion.RELEASE_1_12_2, "V340Vis");
        try {
            BotSession alphaSession = alphaBot.getSession();
            BotSession betaSession = betaBot.getSession();
            BotSession releaseSession = releaseBot.getSession();
            BotSession nettySession = nettyBot.getSession();
            BotSession v109Session = v109Bot.getSession();
            BotSession v340Session = v340Bot.getSession();

            // All should be logged in
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            assertTrue(betaSession.isLoginComplete(), "Beta login should complete");
            assertTrue(releaseSession.isLoginComplete(), "Release login should complete");
            assertTrue(nettySession.isLoginComplete(), "Netty login should complete");
            assertTrue(v109Session.isLoginComplete(), "V109 login should complete");
            assertTrue(v340Session.isLoginComplete(), "V340 login should complete");

            // Each should see at least one other player (via SpawnPlayerPacket)
            // The last bot to join should have seen all 5 others during login.
            // Earlier bots see later bots via broadcast after join.
            Thread.sleep(500);

            assertTrue(v340Session.getSpawnedPlayers().size() >= 5,
                    "V340 should see all 5 other players, saw: " + v340Session.getSpawnedPlayers());

            // Alpha joined first, so it sees the 5 subsequent joins
            assertTrue(alphaSession.getSpawnedPlayers().size() >= 5,
                    "Alpha should see 5 other players, saw: " + alphaSession.getSpawnedPlayers());
        } finally {
            alphaBot.disconnect();
            betaBot.disconnect();
            releaseBot.disconnect();
            nettyBot.disconnect();
            v109Bot.disconnect();
            v340Bot.disconnect();
        }
    }

    @Test
    void modernNettyProtocolsCanSeeEachOther() throws Exception {
        // Test visibility across 1.13+ protocol boundaries including CONFIGURATION state
        BotClient v393Bot = testServer.createBot(ProtocolVersion.RELEASE_1_13, "V393Vis");
        BotClient v735Bot = testServer.createBot(ProtocolVersion.RELEASE_1_16, "V735Vis");
        BotClient v764Bot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "V764Vis");
        BotClient v774Bot = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "V774Vis");
        try {
            BotSession v393Session = v393Bot.getSession();
            BotSession v735Session = v735Bot.getSession();
            BotSession v764Session = v764Bot.getSession();
            BotSession v774Session = v774Bot.getSession();

            assertTrue(v393Session.isLoginComplete(), "1.13 login should complete");
            assertTrue(v735Session.isLoginComplete(), "1.16 login should complete");
            assertTrue(v764Session.isLoginComplete(), "1.20.2 login should complete");
            assertTrue(v774Session.isLoginComplete(), "1.21.11 login should complete");

            Thread.sleep(500);

            // Last bot (1.21.11) should see all 3 others
            assertTrue(v774Session.getSpawnedPlayers().size() >= 3,
                    "1.21.11 should see 3 other players, saw: " + v774Session.getSpawnedPlayers());

            // First bot (1.13) should see 3 subsequent joins
            assertTrue(v393Session.getSpawnedPlayers().size() >= 3,
                    "1.13 should see 3 other players, saw: " + v393Session.getSpawnedPlayers());
        } finally {
            v393Bot.disconnect();
            v735Bot.disconnect();
            v764Bot.disconnect();
            v774Bot.disconnect();
        }
    }

    @Test
    void modernAndLegacyProtocolsCanSeeEachOther() throws Exception {
        // Test that a 1.21.11 bot and an Alpha bot can see each other
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaVisX");
        BotClient v774Bot = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "V774VisX");
        try {
            BotSession alphaSession = alphaBot.getSession();
            BotSession v774Session = v774Bot.getSession();
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            assertTrue(v774Session.isLoginComplete(), "1.21.11 login should complete");

            Thread.sleep(500);

            assertTrue(alphaSession.getSpawnedPlayers().size() >= 1,
                    "Alpha should see 1.21.11 player, saw: " + alphaSession.getSpawnedPlayers());
            assertTrue(v774Session.getSpawnedPlayers().size() >= 1,
                    "1.21.11 should see Alpha player, saw: " + v774Session.getSpawnedPlayers());
        } finally {
            alphaBot.disconnect();
            v774Bot.disconnect();
        }
    }
}
