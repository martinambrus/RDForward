package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests bidirectional block placement AND breaking sync between different
 * protocol families. Alpha v6 and Netty v5 alternate placing and breaking,
 * each verifying the other's actions.
 */
class BlockSyncTest {

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
    void bidirectionalPlaceAndBreakSync() throws Exception {
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaSync");
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_7_6, "NettySync");
        try {
            BotSession alphaSession = alphaBot.getSession();
            BotSession nettySession = nettyBot.getSession();
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            assertTrue(nettySession.isLoginComplete(), "Netty login should complete");

            Thread.sleep(500);

            // --- Alpha places at (50, 42, 50) top face -> target (50, 43, 50) ---
            alphaSession.sendBlockPlace(50, 42, 50, 1, 4);
            int nettySeesPlace1 = nettySession.waitForBlockChange(50, 43, 50, 3000);
            assertTrue(nettySeesPlace1 > 0, "Netty should see Alpha's placement");

            // --- Netty places at (55, 42, 55) top face -> target (55, 43, 55) ---
            nettySession.sendBlockPlace(55, 42, 55, 1, 4);
            int alphaSeesPlace2 = alphaSession.waitForBlockChange(55, 43, 55, 3000);
            assertTrue(alphaSeesPlace2 > 0, "Alpha should see Netty's placement");

            // --- Alpha breaks the block it placed at (50, 43, 50) ---
            alphaSession.sendDigging(0, 50, 43, 50, 1);
            int nettySeesBreak1 = nettySession.waitForBlockChangeValue(50, 43, 50, 0, 3000);
            assertEquals(0, nettySeesBreak1, "Netty should see Alpha's block broken to air");

            // --- Netty breaks the block it placed at (55, 43, 55) ---
            nettySession.sendDigging(0, 55, 43, 55, 1);
            int alphaSeesBreak2 = alphaSession.waitForBlockChangeValue(55, 43, 55, 0, 3000);
            assertEquals(0, alphaSeesBreak2, "Alpha should see Netty's block broken to air");
        } finally {
            alphaBot.disconnect();
            nettyBot.disconnect();
        }
    }
}
