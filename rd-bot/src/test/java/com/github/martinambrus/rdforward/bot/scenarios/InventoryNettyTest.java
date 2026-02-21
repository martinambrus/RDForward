package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests server-side WindowClick processing using a Netty 1.8 (v47) bot.
 * Creative mode: 1 cobblestone in slot 36, no resetInventory on CloseWindow.
 */
class InventoryNettyTest {

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
    void nettyLeftClickSwap() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "NInvSwap");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");

            // Left-click slot 36: pick up cobble into cursor
            int action1 = session.sendWindowClick(36, 0, 0);
            Boolean accepted1 = session.waitForConfirmTransaction(action1, 3000);
            assertNotNull(accepted1, "Should receive ConfirmTransaction for action " + action1);
            assertTrue(accepted1, "Action should be accepted");

            // Left-click slot 37: put cobble down
            int action2 = session.sendWindowClick(37, 0, 0);
            Boolean accepted2 = session.waitForConfirmTransaction(action2, 3000);
            assertNotNull(accepted2, "Should receive ConfirmTransaction for action " + action2);
            assertTrue(accepted2, "Action should be accepted");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nettyRightClickOnSingleItem() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "NInvRight");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");

            // Right-click slot 36 (1 cobble): picks up ceil(1/2)=1, slot becomes empty
            int action1 = session.sendWindowClick(36, 1, 0);
            Boolean accepted1 = session.waitForConfirmTransaction(action1, 3000);
            assertNotNull(accepted1, "Should receive ConfirmTransaction for right-click");
            assertTrue(accepted1, "Right-click should be accepted");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nettyCloseWindowNoCrash() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "NInvClose");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");
            assertTrue(session.waitForSlotItem(36, COBBLESTONE, 5000),
                    "Should receive cobblestone in slot 36");

            // Move cobble to slot 37
            int action1 = session.sendWindowClick(36, 0, 0);
            assertNotNull(session.waitForConfirmTransaction(action1, 3000));
            int action2 = session.sendWindowClick(37, 0, 0);
            assertNotNull(session.waitForConfirmTransaction(action2, 3000));

            // Send CloseWindow — creative mode, no reset
            session.sendCloseWindow(0);

            // Verify bot stays connected (brief pause to detect crashes)
            Thread.sleep(500);
            assertTrue(session.getChannel().isActive(),
                    "Bot should remain connected after CloseWindow");
        } finally {
            bot.disconnect();
        }
    }

    // V393 (1.13) InventoryNettyTest skipped: bot's C2S reverse mapping sends
    // WindowClick at the wrong packet ID for V393 (0x0E instead of 0x08).
    // ConfirmTransaction was removed in 1.17, limiting expansion to pre-1.13 only.
}
