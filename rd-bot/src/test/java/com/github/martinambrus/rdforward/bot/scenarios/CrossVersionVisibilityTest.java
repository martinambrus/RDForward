package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that bots from four different protocol families can all see
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
    void fourProtocolsCanSeeEachOther() throws Exception {
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaVis");
        BotClient betaBot = testServer.createBot(ProtocolVersion.BETA_1_8, "BetaVis");
        BotClient releaseBot = testServer.createBot(ProtocolVersion.RELEASE_1_5, "ReleaseVis");
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_7_6, "NettyVis");
        try {
            BotSession alphaSession = alphaBot.getSession();
            BotSession betaSession = betaBot.getSession();
            BotSession releaseSession = releaseBot.getSession();
            BotSession nettySession = nettyBot.getSession();

            // All should be logged in
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            assertTrue(betaSession.isLoginComplete(), "Beta login should complete");
            assertTrue(releaseSession.isLoginComplete(), "Release login should complete");
            assertTrue(nettySession.isLoginComplete(), "Netty login should complete");

            // Each should see at least one other player (via SpawnPlayerPacket)
            // The last bot to join should have seen all 3 others during login.
            // Earlier bots see later bots via broadcast after join.
            Thread.sleep(500);

            assertTrue(nettySession.getSpawnedPlayers().size() >= 3,
                    "Netty should see all 3 other players, saw: " + nettySession.getSpawnedPlayers());

            // Alpha joined first, so it sees the 3 subsequent joins
            assertTrue(alphaSession.getSpawnedPlayers().size() >= 3,
                    "Alpha should see 3 other players, saw: " + alphaSession.getSpawnedPlayers());
        } finally {
            alphaBot.disconnect();
            betaBot.disconnect();
            releaseBot.disconnect();
            nettyBot.disconnect();
        }
    }
}
