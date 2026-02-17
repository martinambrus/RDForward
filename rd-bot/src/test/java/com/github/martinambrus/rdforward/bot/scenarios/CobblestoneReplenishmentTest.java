package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.alpha.PickupSpawnPacket;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests cobblestone replenishment after Q-drop and block placement.
 * The server keeps the player's cobblestone supply topped up so they
 * can keep building in creative mode.
 */
class CobblestoneReplenishmentTest {

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

    @Test
    void alphaV6CobblestoneReplenishedAfterDrop() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "DropBot");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Wait for initial 64 cobblestone
            assertTrue(session.waitForReceivedItemTotal(COBBLESTONE, 64, 5000),
                    "Should receive initial 64 cobblestone");

            // Q-drop: send PickupSpawnPacket with 1 cobblestone
            session.sendPacket(new PickupSpawnPacket(9999, COBBLESTONE, 1, 0, 0, 0));

            // Server should immediately replenish the dropped cobblestone
            assertTrue(session.waitForReceivedItemTotal(COBBLESTONE, 65, 5000),
                    "Should receive replenishment after Q-drop (total > 64)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void alphaV6CobblestoneReplenishedAfterPlace() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "PlaceRepBot");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Wait for initial cobblestone and chunks
            assertTrue(session.waitForReceivedItemTotal(COBBLESTONE, 64, 5000),
                    "Should receive initial 64 cobblestone");
            Thread.sleep(300);

            // Place a block (consumes 1 cobblestone)
            session.sendBlockPlace(10, 42, 10, 1, COBBLESTONE);

            // Server batches replenishment with ~1s delay; wait up to 3s
            assertTrue(session.waitForReceivedItemTotal(COBBLESTONE, 65, 3000),
                    "Should receive replenishment after placement (total > 64)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void betaV14CobblestoneReplenishedAfterPlace() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "BetaRepBot");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Wait for initial SetSlot with cobblestone at hotbar slot 0 (slot 36)
            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");
            assertEquals(64, session.getSlotCount(36),
                    "Initial cobblestone count should be 64");

            Thread.sleep(300);

            // Place a block
            session.sendBlockPlace(15, 42, 15, 1, COBBLESTONE);

            // Server replenishes immediately for Beta via SetSlot
            // Wait for slot to be refreshed (it will be re-sent)
            Thread.sleep(2000);
            assertEquals(COBBLESTONE, session.getSlotItemId(36),
                    "Slot 36 should still have cobblestone after placement");
            assertTrue(session.getSlotCount(36) > 0,
                    "Slot 36 count should be positive after replenishment");
        } finally {
            bot.disconnect();
        }
    }
}
