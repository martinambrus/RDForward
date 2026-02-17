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
}
