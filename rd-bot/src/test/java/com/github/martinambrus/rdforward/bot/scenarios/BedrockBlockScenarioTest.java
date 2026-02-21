package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotBedrockClient;
import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bedrock-specific block scenario tests:
 * - Grass conversion (surface vs below-surface placement)
 * - Column build (5 vertical placements)
 * - World height limit (max height accepted, above rejected)
 * - Bidirectional block sync with TCP bots (place + break)
 *
 * Note: Bedrock uses runtime IDs from the block palette, not classic block IDs.
 * Tests compare runtime IDs for inequality rather than checking exact values.
 */
class BedrockBlockScenarioTest {

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

    // ---- Grass Conversion ----

    @Test
    void bedrockPlacementAtSurfaceConvertsToGrass() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedGrs1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            // Break existing grass at surface Y=42, then re-place on Y=41 face=1
            int x = 140, z = 140;
            session.sendDigging(0, x, 42, z, 1);
            int broken = session.waitForBlockChange(x, 42, z, 3000);
            assertTrue(broken >= 0, "Break should produce UpdateBlockPacket");

            session.getBlockChanges().remove(packCoords(x, 42, z));
            session.sendBlockPlace(x, 41, z, 1, 4);

            int surfaceRtId = session.waitForBlockChange(x, 42, z, 3000);
            assertTrue(surfaceRtId > 0,
                    "Surface placement should produce non-zero runtime ID (grass)");

            // Now place below surface: break Y=41, re-place on Y=40 face=1
            session.sendDigging(0, x, 41, z, 1);
            int brokenBelow = session.waitForBlockChange(x, 41, z, 3000);
            assertTrue(brokenBelow >= 0, "Break below surface should succeed");

            session.getBlockChanges().remove(packCoords(x, 41, z));
            session.sendBlockPlace(x, 40, z, 1, 4);

            int belowRtId = session.waitForBlockChange(x, 41, z, 3000);
            assertTrue(belowRtId > 0,
                    "Below-surface placement should produce non-zero runtime ID (cobblestone)");

            assertNotEquals(surfaceRtId, belowRtId,
                    "Surface (grass) and below-surface (cobblestone) should have different runtime IDs");
        } finally {
            bot.disconnect();
        }
    }

    // ---- Column Build ----

    @Test
    void bedrockFiveBlockColumnPlacement() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedCol1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            int x = 142, z = 142;
            for (int i = 0; i < 5; i++) {
                int targetY = 42 + i;
                session.sendBlockPlace(x, targetY, z, 1, 4);
                int blockType = session.waitForBlockChange(x, targetY + 1, z, 3000);
                assertTrue(blockType > 0,
                        "Bedrock block " + (i + 1) + " at Y=" + (targetY + 1) + " should be placed");
            }
        } finally {
            bot.disconnect();
        }
    }

    // ---- World Height Limit ----

    @Test
    void bedrockPlacementAtMaxHeightSucceeds() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedHt1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            // Place on Y=62 face=1 -> target Y=63 (max valid)
            int x = 144, z = 144;
            session.sendBlockPlace(x, 62, z, 1, 4);
            int blockType = session.waitForBlockChange(x, 63, z, 3000);
            assertTrue(blockType > 0, "Bedrock placement at Y=63 (max valid) should succeed");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void bedrockPlacementAboveMaxHeightFails() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedHt2");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            // Place on Y=63 face=1 -> target Y=64 (out of bounds)
            int x = 146, z = 146;
            session.sendBlockPlace(x, 63, z, 1, 4);

            // Server should reject — no UpdateBlockPacket at Y=64
            int blockType = session.waitForBlockChange(x, 64, z, 1000);
            assertTrue(blockType <= 0,
                    "Bedrock placement at Y=64 (above max) should be rejected");
        } finally {
            bot.disconnect();
        }
    }

    // ---- Block Sync (Bedrock <-> TCP) ----

    @Test
    void bedrockAndNettyBidirectionalBlockSync() throws Exception {
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedSync1");
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "NetSync1");
        try {
            BotSession bedrockSession = bedrockBot.getSession();
            BotSession nettySession = nettyBot.getSession();
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");
            assertTrue(nettySession.isLoginComplete(), "Netty login should complete");
            Thread.sleep(500);

            // Bedrock places at (150, 42, 150) -> target (150, 43, 150)
            bedrockSession.sendBlockPlace(150, 42, 150, 1, 4);
            int nettySees = nettySession.waitForBlockChange(150, 43, 150, 3000);
            assertTrue(nettySees > 0, "Netty should see Bedrock's placement");

            // Netty places at (155, 42, 155) -> target (155, 43, 155)
            nettySession.sendBlockPlace(155, 42, 155, 1, 4);
            int bedrockSees = bedrockSession.waitForBlockChange(155, 43, 155, 3000);
            assertTrue(bedrockSees > 0, "Bedrock should see Netty's placement");

            // Bedrock breaks at (150, 43, 150)
            bedrockSession.getBlockChanges().remove(packCoords(150, 43, 150));
            bedrockSession.sendDigging(0, 150, 43, 150, 1);
            int nettySeesBreak = nettySession.waitForBlockChangeValue(150, 43, 150, 0, 3000);
            assertEquals(0, nettySeesBreak, "Netty should see Bedrock's block broken to air");

            // Netty breaks at (155, 43, 155)
            bedrockSession.getBlockChanges().remove(packCoords(155, 43, 155));
            nettySession.sendDigging(0, 155, 43, 155, 1);
            int bedrockSeesBreak = bedrockSession.waitForBlockChange(155, 43, 155, 3000);
            // Bedrock break runtime ID should differ from the placement ID
            assertNotEquals(bedrockSees, bedrockSeesBreak,
                    "Bedrock should see Netty's block broken (different runtime ID)");
        } finally {
            bedrockBot.disconnect();
            nettyBot.disconnect();
        }
    }

    @Test
    void bedrockAndAlphaBidirectionalBlockSync() throws Exception {
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedSync2");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "ASync2");
        try {
            BotSession bedrockSession = bedrockBot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            Thread.sleep(500);

            // Bedrock places at (160, 42, 160) -> target (160, 43, 160)
            bedrockSession.sendBlockPlace(160, 42, 160, 1, 4);
            int alphaSees = alphaSession.waitForBlockChange(160, 43, 160, 3000);
            assertTrue(alphaSees > 0, "Alpha should see Bedrock's placement");

            // Alpha places at (165, 42, 165) -> target (165, 43, 165)
            alphaSession.sendBlockPlace(165, 42, 165, 1, 4);
            int bedrockSees = bedrockSession.waitForBlockChange(165, 43, 165, 3000);
            assertTrue(bedrockSees > 0, "Bedrock should see Alpha's placement");

            // Bedrock breaks at (160, 43, 160)
            bedrockSession.getBlockChanges().remove(packCoords(160, 43, 160));
            bedrockSession.sendDigging(0, 160, 43, 160, 1);
            int alphaSeesBreak = alphaSession.waitForBlockChangeValue(160, 43, 160, 0, 3000);
            assertEquals(0, alphaSeesBreak, "Alpha should see Bedrock's block broken to air");

            // Alpha breaks at (165, 43, 165)
            bedrockSession.getBlockChanges().remove(packCoords(165, 43, 165));
            alphaSession.sendDigging(0, 165, 43, 165, 1);
            int bedrockSeesBreak = bedrockSession.waitForBlockChange(165, 43, 165, 3000);
            assertNotEquals(bedrockSees, bedrockSeesBreak,
                    "Bedrock should see Alpha's block broken (different runtime ID)");
        } finally {
            bedrockBot.disconnect();
            alphaBot.disconnect();
        }
    }

    private static long packCoords(int x, int y, int z) {
        return ((long) x & 0xFFFFFFFFL) << 32 | ((long) y & 0xFFL) << 24 | ((long) z & 0xFFFFFFL);
    }
}
