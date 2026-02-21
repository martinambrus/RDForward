package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotBedrockClient;
import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests multiplayer visibility and entity despawn involving Bedrock players.
 *
 * Extends coverage from MultiPlayerTest and PlayerDespawnTest to include:
 * - Two Bedrock bots seeing each other (AddPlayerPacket)
 * - Entity despawn in all directions: TCP->Bedrock, Bedrock->Bedrock, Bedrock->modern Netty
 * - Bedrock in multi-protocol visibility (Bedrock + Alpha + Netty all see each other)
 */
class BedrockMultiPlayerDespawnTest {

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

    // ---- Multiplayer Visibility ----

    @Test
    void twoBedrockBotsCanSeeEachOther() throws Exception {
        BotBedrockClient bot1 = testServer.createBedrockBot("BedMP1");
        BotBedrockClient bot2 = testServer.createBedrockBot("BedMP2");
        try {
            BotSession session1 = bot1.getSession();
            BotSession session2 = bot2.getSession();
            assertTrue(session1.isLoginComplete(), "Bot1 login should complete");
            assertTrue(session2.isLoginComplete(), "Bot2 login should complete");

            // Each should see the other via AddPlayerPacket
            Integer eid1 = session2.waitForSpawnedPlayer("BedMP1", 5000);
            Integer eid2 = session1.waitForSpawnedPlayer("BedMP2", 5000);
            assertNotNull(eid1, "Bot2 should see Bot1 via AddPlayerPacket");
            assertNotNull(eid2, "Bot1 should see Bot2 via AddPlayerPacket");
        } finally {
            bot1.disconnect();
            bot2.disconnect();
        }
    }

    @Test
    void bedrockInMultiProtocolVisibility() throws Exception {
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedVis1");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AVis1");
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "NVis1");
        try {
            BotSession bedrockSession = bedrockBot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            BotSession nettySession = nettyBot.getSession();
            assertTrue(bedrockSession.isLoginComplete());
            assertTrue(alphaSession.isLoginComplete());
            assertTrue(nettySession.isLoginComplete());

            // Bedrock sees both TCP bots (AddPlayerPacket includes username)
            assertNotNull(bedrockSession.waitForSpawnedPlayer("AVis1", 5000),
                    "Bedrock should see Alpha player");
            assertNotNull(bedrockSession.waitForSpawnedPlayer("NVis1", 5000),
                    "Bedrock should see Netty player");

            // Alpha sees Bedrock (SpawnPlayerPacket includes name)
            assertNotNull(alphaSession.waitForSpawnedPlayer("BedVis1", 5000),
                    "Alpha should see Bedrock player");

            // Netty 1.8 SpawnPlayerPacket doesn't include player name — check size instead
            Thread.sleep(500);
            assertTrue(nettySession.getSpawnedPlayers().size() >= 2,
                    "Netty should see both other players, saw: " + nettySession.getSpawnedPlayers());
        } finally {
            bedrockBot.disconnect();
            alphaBot.disconnect();
            nettyBot.disconnect();
        }
    }

    // ---- Despawn ----

    @Test
    void bedrockDespawnNotifiesBedrock() throws Exception {
        BotBedrockClient observer = testServer.createBedrockBot("BedDO1");
        BotBedrockClient leaver = testServer.createBedrockBot("BedDL1");
        try {
            BotSession observerSession = observer.getSession();
            BotSession leaverSession = leaver.getSession();
            assertTrue(observerSession.isLoginComplete());
            assertTrue(leaverSession.isLoginComplete());

            Integer eid = observerSession.waitForSpawnedPlayer("BedDL1", 5000);
            assertNotNull(eid, "Observer should see leaver");

            leaver.disconnect();

            assertTrue(observerSession.waitForDespawn(eid, 5000),
                    "Bedrock observer should see Bedrock leaver despawn via RemoveEntityPacket");
        } finally {
            observer.disconnect();
            leaver.disconnect();
        }
    }

    @Test
    void tcpDespawnNotifiesBedrock() throws Exception {
        BotBedrockClient observer = testServer.createBedrockBot("BedDO2");
        BotClient leaver = testServer.createBot(ProtocolVersion.RELEASE_1_8, "TcpDL1");
        try {
            BotSession observerSession = observer.getSession();
            BotSession leaverSession = leaver.getSession();
            assertTrue(observerSession.isLoginComplete());
            assertTrue(leaverSession.isLoginComplete());

            Integer eid = observerSession.waitForSpawnedPlayer("TcpDL1", 5000);
            assertNotNull(eid, "Bedrock observer should see TCP player");

            leaver.disconnect();

            assertTrue(observerSession.waitForDespawn(eid, 5000),
                    "Bedrock observer should see TCP leaver despawn via RemoveEntityPacket");
        } finally {
            observer.disconnect();
            leaver.disconnect();
        }
    }

    @Test
    void bedrockDespawnNotifiesModernNetty() throws Exception {
        BotClient observer = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "NtyDO1");
        BotBedrockClient leaver = testServer.createBedrockBot("BedDL2");
        try {
            BotSession observerSession = observer.getSession();
            BotSession leaverSession = leaver.getSession();
            assertTrue(observerSession.isLoginComplete());
            assertTrue(leaverSession.isLoginComplete());

            // v764 SpawnEntity doesn't include player name — wait for any spawn
            Thread.sleep(500);
            assertTrue(observerSession.getSpawnedPlayers().size() >= 1,
                    "Modern Netty observer should see Bedrock player");
            int eid = observerSession.getSpawnedPlayers().keySet().iterator().next();

            leaver.disconnect();

            assertTrue(observerSession.waitForDespawn(eid, 5000),
                    "Modern Netty observer should see Bedrock leaver despawn via DestroyEntitiesPacket");
        } finally {
            observer.disconnect();
            leaver.disconnect();
        }
    }
}
