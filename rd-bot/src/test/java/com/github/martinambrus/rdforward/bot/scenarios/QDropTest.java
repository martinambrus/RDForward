package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.alpha.PickupSpawnPacket;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Q-drop behavior (pressing Q to drop an item) across all protocol versions.
 *
 * Survival clients (Alpha, Beta pre-1.8): Q-dropping cobblestone triggers
 * server-side replenishment, keeping the stack at 64.
 *
 * Creative clients (Beta 1.8+, Release, Netty): Q-drop is accepted without
 * error and the inventory slot remains intact (creative mode has infinite items).
 */
class QDropTest {

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

    // ---- Survival clients: Q-drop triggers replenishment ----

    @Test
    void alphaV6QDropReplenishesCobblestone() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "QDropV6");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForReceivedItemTotal(COBBLESTONE, 64, 5000),
                    "Should receive initial 64 cobblestone");

            // Alpha Q-drop: PickupSpawnPacket with cobblestone
            session.sendPacket(new PickupSpawnPacket(9999, COBBLESTONE, 1, 0, 0, 0));

            assertTrue(session.waitForReceivedItemTotal(COBBLESTONE, 65, 5000),
                    "Server should replenish after Q-drop (total > 64)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void alphaV14QDropReplenishesCobblestone() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_0_16, "QDropV14");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForReceivedItemTotal(COBBLESTONE, 64, 5000),
                    "Should receive initial 64 cobblestone");

            session.sendPacket(new PickupSpawnPacket(9998, COBBLESTONE, 1, 0, 0, 0));

            assertTrue(session.waitForReceivedItemTotal(COBBLESTONE, 65, 5000),
                    "V14 server should replenish after Q-drop (total > 64)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void alphaV1QDropReplenishesCobblestone() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_0_17, "QDropV1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForReceivedItemTotal(COBBLESTONE, 64, 5000),
                    "Should receive initial 64 cobblestone");

            session.sendPacket(new PickupSpawnPacket(9997, COBBLESTONE, 1, 0, 0, 0));

            assertTrue(session.waitForReceivedItemTotal(COBBLESTONE, 65, 5000),
                    "V1 server should replenish after Q-drop (total > 64)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void betaV14QDropReplenishesCobblestone() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "QDropBV14");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");
            assertEquals(64, session.getSlotCount(36),
                    "Initial cobblestone count should be 64");

            // Beta Q-drop: PlayerDiggingPacket with status 4 (DROP_ITEM)
            session.sendDigging(4, 0, 0, 0, 0);

            // Server replenishes via SetSlot for survival Beta clients
            Thread.sleep(2000);
            assertEquals(COBBLESTONE, session.getSlotItemId(36),
                    "Slot 36 should still have cobblestone after Q-drop");
            assertTrue(session.getSlotCount(36) > 0,
                    "Slot 36 count should be positive after replenishment");
        } finally {
            bot.disconnect();
        }
    }

    // ---- Creative clients: Q-drop accepted, slot unchanged ----

    @Test
    void betaV17QDropDoesNotReduceInventory() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_8, "QDropBV17");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");
            int countBefore = session.getSlotCount(36);

            session.sendDigging(4, 0, 0, 0, 0);

            Thread.sleep(1000);
            assertEquals(COBBLESTONE, session.getSlotItemId(36),
                    "V17 slot 36 should still have cobblestone after Q-drop");
            assertEquals(countBefore, session.getSlotCount(36),
                    "V17 slot 36 count should be unchanged after Q-drop");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void releaseV39QDropDoesNotReduceInventory() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_3_1, "QDropV39");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");
            int countBefore = session.getSlotCount(36);

            session.sendDigging(4, 0, 0, 0, 0);

            Thread.sleep(1000);
            assertEquals(COBBLESTONE, session.getSlotItemId(36),
                    "V39 slot 36 should still have cobblestone after Q-drop");
            assertEquals(countBefore, session.getSlotCount(36),
                    "V39 slot 36 count should be unchanged after Q-drop");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nettyV47QDropDoesNotReduceInventory() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "QDropNV47");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");
            int countBefore = session.getSlotCount(36);

            session.sendDigging(4, 0, 0, 0, 0);

            Thread.sleep(1000);
            assertEquals(COBBLESTONE, session.getSlotItemId(36),
                    "V47 slot 36 should still have cobblestone after Q-drop");
            assertEquals(countBefore, session.getSlotCount(36),
                    "V47 slot 36 count should be unchanged after Q-drop");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nettyV340QDropDoesNotReduceInventory() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_12_2, "QDropN340");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");
            int countBefore = session.getSlotCount(36);

            session.sendDigging(4, 0, 0, 0, 0);

            Thread.sleep(1000);
            assertEquals(COBBLESTONE, session.getSlotItemId(36),
                    "V340 slot 36 should still have cobblestone after Q-drop");
            assertEquals(countBefore, session.getSlotCount(36),
                    "V340 slot 36 count should be unchanged after Q-drop");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nettyV477QDropDoesNotReduceInventory() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_14, "QDropN477");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForNonEmptySlot(36, 5000),
                    "Should receive cobblestone in slot 36");
            int countBefore = session.getSlotCount(36);

            session.sendDigging(4, 0, 0, 0, 0);

            Thread.sleep(1000);
            assertTrue(session.getSlotItemId(36) > 0,
                    "V477 slot 36 should still have cobblestone after Q-drop");
            assertEquals(countBefore, session.getSlotCount(36),
                    "V477 slot 36 count should be unchanged after Q-drop");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nettyV764QDropDoesNotReduceInventory() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "QDropN764");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForNonEmptySlot(36, 5000),
                    "Should receive cobblestone in slot 36");
            int countBefore = session.getSlotCount(36);

            session.sendDigging(4, 0, 0, 0, 0);

            Thread.sleep(1000);
            assertTrue(session.getSlotItemId(36) > 0,
                    "V764 slot 36 should still have cobblestone after Q-drop");
            assertEquals(countBefore, session.getSlotCount(36),
                    "V764 slot 36 count should be unchanged after Q-drop");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nettyV774QDropDoesNotReduceInventory() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "QDropN774");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForNonEmptySlot(36, 5000),
                    "Should receive cobblestone in slot 36");
            int countBefore = session.getSlotCount(36);

            session.sendDigging(4, 0, 0, 0, 0);

            Thread.sleep(1000);
            assertTrue(session.getSlotItemId(36) > 0,
                    "V774 slot 36 should still have cobblestone after Q-drop");
            assertEquals(countBefore, session.getSlotCount(36),
                    "V774 slot 36 count should be unchanged after Q-drop");
        } finally {
            bot.disconnect();
        }
    }
}
