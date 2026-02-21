package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotBedrockClient;
import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.alpha.PickupSpawnPacket;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests cross-version inventory interactions between Bedrock and TCP clients.
 *
 * Bedrock clients run in creative mode (INSTABUILD) with infinite items —
 * no inventory slots are tracked or replenished. These tests verify that:
 * - TCP clients' inventory management works correctly when Bedrock is present
 * - Bedrock block operations don't corrupt TCP inventory state
 * - Both clients remain connected during concurrent operations
 */
class BedrockInventoryInteractionTest {

    private static final int COBBLESTONE = 4;

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

    // ---- TCP inventory unaffected by Bedrock operations ----

    @Test
    void tcpInventoryIntactAfterBedrockPlacement() throws Exception {
        BotClient tcpBot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TcpInv1");
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedInv1");
        try {
            BotSession tcpSession = tcpBot.getSession();
            BotSession bedrockSession = bedrockBot.getSession();
            assertTrue(tcpSession.isLoginComplete(), "TCP login should complete");
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");

            // TCP should have cobblestone in slot 36
            assertTrue(tcpSession.waitForSlotItem(36, COBBLESTONE, 5000),
                    "TCP should receive cobblestone in slot 36");
            int countBefore = tcpSession.getSlotCount(36);

            // Bedrock places a block
            bedrockSession.sendBlockPlace(170, 42, 170, 1, 4);
            Thread.sleep(1000);

            // TCP inventory should be unaffected
            assertEquals(COBBLESTONE, tcpSession.getSlotItemId(36),
                    "TCP slot 36 should still have cobblestone after Bedrock placement");
            assertEquals(countBefore, tcpSession.getSlotCount(36),
                    "TCP slot 36 count should be unchanged by Bedrock placement");
        } finally {
            tcpBot.disconnect();
            bedrockBot.disconnect();
        }
    }

    @Test
    void tcpInventoryIntactAfterBedrockBreak() throws Exception {
        BotClient tcpBot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "TcpInv2");
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedInv2");
        try {
            BotSession tcpSession = tcpBot.getSession();
            BotSession bedrockSession = bedrockBot.getSession();
            assertTrue(tcpSession.isLoginComplete(), "TCP login should complete");
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");
            Thread.sleep(500);

            assertTrue(tcpSession.waitForSlotItem(36, COBBLESTONE, 5000),
                    "TCP should receive cobblestone in slot 36");
            int countBefore = tcpSession.getSlotCount(36);

            // Bedrock breaks a block
            bedrockSession.sendDigging(0, 172, 42, 172, 1);
            Thread.sleep(1000);

            // TCP inventory should be unaffected
            assertEquals(COBBLESTONE, tcpSession.getSlotItemId(36),
                    "TCP slot 36 should still have cobblestone after Bedrock break");
            assertEquals(countBefore, tcpSession.getSlotCount(36),
                    "TCP slot 36 count should be unchanged by Bedrock break");
        } finally {
            tcpBot.disconnect();
            bedrockBot.disconnect();
        }
    }

    // ---- TCP Q-drop replenishment works with Bedrock present ----

    @Test
    void alphaQDropReplenishesWithBedrockPresent() throws Exception {
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AQDrp1");
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedQD1");
        try {
            BotSession alphaSession = alphaBot.getSession();
            BotSession bedrockSession = bedrockBot.getSession();
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");

            assertTrue(alphaSession.waitForReceivedItemTotal(COBBLESTONE, 64, 5000),
                    "Alpha should receive initial 64 cobblestone");

            // Alpha Q-drop while Bedrock is connected
            alphaSession.sendPacket(new PickupSpawnPacket(9990, COBBLESTONE, 1, 0, 0, 0));

            assertTrue(alphaSession.waitForReceivedItemTotal(COBBLESTONE, 65, 5000),
                    "Alpha cobblestone should be replenished even with Bedrock present");

            // Both should stay connected
            assertTrue(bedrockSession.isConnected(), "Bedrock should stay connected");
        } finally {
            alphaBot.disconnect();
            bedrockBot.disconnect();
        }
    }

    @Test
    void betaQDropReplenishesWithBedrockPresent() throws Exception {
        BotClient betaBot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "BQDrp1");
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedQD2");
        try {
            BotSession betaSession = betaBot.getSession();
            BotSession bedrockSession = bedrockBot.getSession();
            assertTrue(betaSession.isLoginComplete(), "Beta login should complete");
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");

            assertTrue(betaSession.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Beta should receive cobblestone in slot 36");

            // Beta Q-drop (PlayerDiggingPacket status=4) while Bedrock is connected
            betaSession.sendDigging(4, 0, 0, 0, 0);

            Thread.sleep(2000);
            assertEquals(COBBLESTONE, betaSession.getSlotItemId(36),
                    "Beta slot 36 should still have cobblestone after Q-drop");
            assertTrue(betaSession.getSlotCount(36) > 0,
                    "Beta slot 36 count should be positive after replenishment");

            assertTrue(bedrockSession.isConnected(), "Bedrock should stay connected");
        } finally {
            betaBot.disconnect();
            bedrockBot.disconnect();
        }
    }

    @Test
    void nettyQDropUnchangedWithBedrockPresent() throws Exception {
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "NQDrp1");
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedQD3");
        try {
            BotSession nettySession = nettyBot.getSession();
            BotSession bedrockSession = bedrockBot.getSession();
            assertTrue(nettySession.isLoginComplete(), "Netty login should complete");
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");

            assertTrue(nettySession.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Netty should receive cobblestone in slot 36");
            int countBefore = nettySession.getSlotCount(36);

            // Creative Q-drop while Bedrock is connected
            nettySession.sendDigging(4, 0, 0, 0, 0);

            Thread.sleep(1000);
            assertEquals(COBBLESTONE, nettySession.getSlotItemId(36),
                    "Netty slot 36 should still have cobblestone after Q-drop");
            assertEquals(countBefore, nettySession.getSlotCount(36),
                    "Netty slot 36 count should be unchanged (creative mode)");

            assertTrue(bedrockSession.isConnected(), "Bedrock should stay connected");
        } finally {
            nettyBot.disconnect();
            bedrockBot.disconnect();
        }
    }

    // ---- Concurrent building: TCP inventory survives ----

    @Test
    void tcpReplenishmentWorksDuringConcurrentBedrockBuilding() throws Exception {
        BotClient betaBot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "BConc1");
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedConc1");
        try {
            BotSession betaSession = betaBot.getSession();
            BotSession bedrockSession = bedrockBot.getSession();
            assertTrue(betaSession.isLoginComplete(), "Beta login should complete");
            assertTrue(bedrockSession.isLoginComplete(), "Bedrock login should complete");

            assertTrue(betaSession.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Beta should receive cobblestone in slot 36");
            Thread.sleep(500);

            // Both place blocks at the same time
            betaSession.sendBlockPlace(174, 42, 174, 1, COBBLESTONE);
            bedrockSession.sendBlockPlace(176, 42, 176, 1, 4);

            // Beta's inventory should be replenished
            Thread.sleep(2000);
            assertEquals(COBBLESTONE, betaSession.getSlotItemId(36),
                    "Beta slot 36 should still have cobblestone after concurrent building");
            assertTrue(betaSession.getSlotCount(36) > 0,
                    "Beta slot 36 count should be positive");

            // Both should stay connected
            assertTrue(betaSession.isLoginComplete(), "Beta should stay connected");
            assertTrue(bedrockSession.isConnected(), "Bedrock should stay connected");
        } finally {
            betaBot.disconnect();
            bedrockBot.disconnect();
        }
    }

    // ---- Bedrock rapid placement stability ----

    @Test
    void bedrockRapidPlacementStaysConnected() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedRap1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            // Place 10 blocks rapidly (no inventory to deplete in creative)
            for (int i = 0; i < 10; i++) {
                session.sendBlockPlace(180 + i, 42, 180, 1, 4);
            }

            // Wait for all block changes to be confirmed
            for (int i = 0; i < 10; i++) {
                int blockType = session.waitForBlockChange(180 + i, 43, 180, 3000);
                assertTrue(blockType > 0,
                        "Block " + (i + 1) + " at X=" + (180 + i) + " should be placed");
            }

            // Bot should remain connected after rapid placement
            assertTrue(session.isConnected(), "Bedrock should stay connected after rapid placement");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void bedrockRapidBreakAndPlaceStaysConnected() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedRap2");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            Thread.sleep(500);

            // Place and then break 5 blocks
            for (int i = 0; i < 5; i++) {
                int x = 190 + i;
                session.sendBlockPlace(x, 42, 190, 1, 4);
                int placed = session.waitForBlockChange(x, 43, 190, 3000);
                assertTrue(placed > 0, "Block " + (i + 1) + " should be placed");

                session.getBlockChanges().remove(packCoords(x, 43, 190));
                session.sendDigging(0, x, 43, 190, 1);
                int broken = session.waitForBlockChange(x, 43, 190, 3000);
                assertTrue(broken >= 0, "Block " + (i + 1) + " should be broken");
                assertNotEquals(placed, broken,
                        "Broken runtime ID should differ from placed runtime ID");
            }

            assertTrue(session.isConnected(), "Bedrock should stay connected after rapid place+break");
        } finally {
            bot.disconnect();
        }
    }

    private static long packCoords(int x, int y, int z) {
        return ((long) x & 0xFFFFFFFFL) << 32 | ((long) y & 0xFFL) << 24 | ((long) z & 0xFFFFFFL);
    }
}
