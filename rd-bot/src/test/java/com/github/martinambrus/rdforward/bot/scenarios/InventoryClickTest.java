package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests server-side WindowClick processing using a Beta 1.7.3 (v14) bot.
 * Beta survival: 64 cobblestone in slot 36, resetInventory on CloseWindow.
 */
class InventoryClickTest {

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
    void leftClickSwapSlots() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "InvSwap");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");

            // Left-click slot 36: pick up 64 cobble into cursor
            int action1 = session.sendWindowClick(36, 0, 0);
            Boolean accepted1 = session.waitForConfirmTransaction(action1, 3000);
            assertNotNull(accepted1, "Should receive ConfirmTransaction for action " + action1);
            assertTrue(accepted1, "Action should be accepted");

            // Left-click slot 37: put 64 cobble down
            int action2 = session.sendWindowClick(37, 0, 0);
            Boolean accepted2 = session.waitForConfirmTransaction(action2, 3000);
            assertNotNull(accepted2, "Should receive ConfirmTransaction for action " + action2);
            assertTrue(accepted2, "Action should be accepted");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void rightClickSplitStack() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "InvSplit");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");

            // Right-click slot 36: pick up ceil(64/2)=32, leave 32
            int action1 = session.sendWindowClick(36, 1, 0);
            Boolean accepted1 = session.waitForConfirmTransaction(action1, 3000);
            assertNotNull(accepted1, "Should receive ConfirmTransaction for split");
            assertTrue(accepted1, "Split should be accepted");

            // Left-click slot 37: put 32 down
            int action2 = session.sendWindowClick(37, 0, 0);
            Boolean accepted2 = session.waitForConfirmTransaction(action2, 3000);
            assertNotNull(accepted2, "Should receive ConfirmTransaction for place");
            assertTrue(accepted2, "Place should be accepted");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void dropFullStack() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "InvDrop");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");

            // Left-click slot 36: pick up 64 cobble into cursor
            int action1 = session.sendWindowClick(36, 0, 0);
            Boolean accepted1 = session.waitForConfirmTransaction(action1, 3000);
            assertNotNull(accepted1, "Should receive ConfirmTransaction for pickup");
            assertTrue(accepted1, "Pickup should be accepted");

            // Left-click slot -999: drop entire cursor
            int action2 = session.sendWindowClick(-999, 0, 0);
            Boolean accepted2 = session.waitForConfirmTransaction(action2, 3000);
            assertNotNull(accepted2, "Should receive ConfirmTransaction for drop");
            assertTrue(accepted2, "Drop should be accepted");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void dropOneItem() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "InvDrop1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");

            // Left-click slot 36: pick up all into cursor
            int action1 = session.sendWindowClick(36, 0, 0);
            assertNotNull(session.waitForConfirmTransaction(action1, 3000),
                    "Should receive ConfirmTransaction for pickup");

            // Right-click slot -999: drop one from cursor
            int action2 = session.sendWindowClick(-999, 1, 0);
            assertNotNull(session.waitForConfirmTransaction(action2, 3000),
                    "Should receive ConfirmTransaction for drop-one");

            // Left-click slot 36: put remaining back
            int action3 = session.sendWindowClick(36, 0, 0);
            assertNotNull(session.waitForConfirmTransaction(action3, 3000),
                    "Should receive ConfirmTransaction for put-back");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void emptySlotClick() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "InvEmpty");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");

            // Left-click empty slot 37: should not crash, server accepts
            int action1 = session.sendWindowClick(37, 0, 0);
            Boolean accepted = session.waitForConfirmTransaction(action1, 3000);
            assertNotNull(accepted, "Should receive ConfirmTransaction for empty-slot click");
            assertTrue(accepted, "Empty-slot click should be accepted");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void closeWindowResetsInventory() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "InvClose");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");

            // Move cobble from slot 36 to slot 37
            int action1 = session.sendWindowClick(36, 0, 0);
            assertNotNull(session.waitForConfirmTransaction(action1, 3000));
            int action2 = session.sendWindowClick(37, 0, 0);
            assertNotNull(session.waitForConfirmTransaction(action2, 3000));

            // Send CloseWindow — Beta survival resets inventory with 64 cobble in slot 36
            session.sendCloseWindow(0);

            // Wait for the WindowItems reset to arrive
            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 3000),
                    "After CloseWindow, slot 36 should have cobblestone again (reset)");
            assertEquals(64, session.getSlotCount(36),
                    "After CloseWindow reset, slot 36 should have 64 cobblestone");
        } finally {
            bot.disconnect();
        }
    }
}
