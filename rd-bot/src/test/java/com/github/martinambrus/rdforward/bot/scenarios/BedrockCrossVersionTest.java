package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotBedrockClient;
import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests cross-version interactions between Bedrock bots and TCP bots
 * (Alpha, Netty). Covers chat, block placement, visibility, and despawn.
 */
class BedrockCrossVersionTest {

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
    void bedrockAndAlphaCanChat() throws Exception {
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedXChat1");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaXChat1");
        try {
            BotSession bedrockSession = bedrockBot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");

            // Bedrock sends, Alpha receives
            bedrockSession.sendChat("Hello from Bedrock");
            String received = alphaSession.waitForChat("Hello from Bedrock", 3000);
            assertNotNull(received, "Alpha should receive Bedrock's chat");

            // Alpha sends, Bedrock receives
            alphaSession.sendChat("Hello from Alpha");
            received = bedrockSession.waitForChat("Hello from Alpha", 3000);
            assertNotNull(received, "Bedrock should receive Alpha's chat");
        } finally {
            bedrockBot.disconnect();
            alphaBot.disconnect();
        }
    }

    @Test
    void bedrockAndNettyCanChat() throws Exception {
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedXChat2");
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "NettyXChat2");
        try {
            BotSession bedrockSession = bedrockBot.getSession();
            BotSession nettySession = nettyBot.getSession();
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");
            assertTrue(nettySession.isLoginComplete(), "Netty login should complete");

            // Bedrock sends, Netty receives
            bedrockSession.sendChat("Hi from Bedrock");
            String received = nettySession.waitForChat("Hi from Bedrock", 3000);
            assertNotNull(received, "Netty should receive Bedrock's chat");

            // Netty sends, Bedrock receives
            nettySession.sendChat("Hi from Netty");
            received = bedrockSession.waitForChat("Hi from Netty", 3000);
            assertNotNull(received, "Bedrock should receive Netty's chat");
        } finally {
            bedrockBot.disconnect();
            nettyBot.disconnect();
        }
    }

    @Test
    void bedrockPlacementVisibleToAlpha() throws Exception {
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedXPlace1");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaXWatch1");
        try {
            BotSession bedrockSession = bedrockBot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");

            Thread.sleep(500);

            int px = 80, py = 42, pz = 80;
            bedrockSession.sendBlockPlace(px, py, pz, 1, 4);

            int blockType = alphaSession.waitForBlockChange(px, py + 1, pz, 3000);
            assertTrue(blockType > 0, "Alpha should see Bedrock's block placement");
        } finally {
            bedrockBot.disconnect();
            alphaBot.disconnect();
        }
    }

    @Test
    void alphaPlacementVisibleToBedrock() throws Exception {
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaXPlace2");
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedXWatch2");
        try {
            BotSession alphaSession = alphaBot.getSession();
            BotSession bedrockSession = bedrockBot.getSession();
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");

            Thread.sleep(500);

            int px = 82, py = 42, pz = 82;
            alphaSession.sendBlockPlace(px, py, pz, 1, 4);

            int blockType = bedrockSession.waitForBlockChange(px, py + 1, pz, 3000);
            assertTrue(blockType > 0, "Bedrock should see Alpha's block placement");
        } finally {
            alphaBot.disconnect();
            bedrockBot.disconnect();
        }
    }

    @Test
    void bedrockAndAlphaCanSeeEachOther() throws Exception {
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedXVis1");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaXVis1");
        try {
            BotSession bedrockSession = bedrockBot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");

            // Both should see each other via spawnedPlayers
            Integer bedrockSawAlpha = bedrockSession.waitForSpawnedPlayer("AlphaXVis1", 3000);
            assertNotNull(bedrockSawAlpha, "Bedrock should see Alpha in spawnedPlayers");

            Integer alphaSawBedrock = alphaSession.waitForSpawnedPlayer("BedXVis1", 3000);
            assertNotNull(alphaSawBedrock, "Alpha should see Bedrock in spawnedPlayers");
        } finally {
            bedrockBot.disconnect();
            alphaBot.disconnect();
        }
    }

    @Test
    void bedrockDespawnNotifiesAlpha() throws Exception {
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaXDesp1");
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedXDesp1");
        try {
            BotSession alphaSession = alphaBot.getSession();
            BotSession bedrockSession = bedrockBot.getSession();
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");

            // Alpha should see Bedrock spawn — get entity ID as seen by Alpha
            Integer bedrockEntityId = alphaSession.waitForSpawnedPlayer("BedXDesp1", 3000);
            assertNotNull(bedrockEntityId, "Alpha should see Bedrock spawn");

            // Bedrock disconnects
            bedrockBot.disconnect();
            bedrockBot = null;
            Thread.sleep(200);

            // Alpha should receive DestroyEntity for the Bedrock player
            assertTrue(alphaSession.waitForDespawn(bedrockEntityId, 5000),
                    "Alpha should receive DestroyEntity for disconnected Bedrock player");
        } finally {
            alphaBot.disconnect();
            if (bedrockBot != null) bedrockBot.disconnect();
        }
    }
}
