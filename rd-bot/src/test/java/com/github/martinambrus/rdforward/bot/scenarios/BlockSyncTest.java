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

    @Test
    void bidirectionalPlaceAndBreakSyncV47Beta() throws Exception {
        BotClient v47Bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "V47Sync");
        BotClient betaBot = testServer.createBot(ProtocolVersion.BETA_1_8, "BetaSync");
        try {
            BotSession v47Session = v47Bot.getSession();
            BotSession betaSession = betaBot.getSession();
            assertTrue(v47Session.isLoginComplete(), "V47 login should complete");
            assertTrue(betaSession.isLoginComplete(), "Beta login should complete");

            Thread.sleep(500);

            // V47 places at (100, 42, 100) top face -> target (100, 43, 100)
            v47Session.sendBlockPlace(100, 42, 100, 1, 4);
            int betaSeesPlace1 = betaSession.waitForBlockChange(100, 43, 100, 3000);
            assertTrue(betaSeesPlace1 > 0, "Beta should see V47's placement");

            // Beta places at (105, 42, 105) top face -> target (105, 43, 105)
            betaSession.sendBlockPlace(105, 42, 105, 1, 4);
            int v47SeesPlace2 = v47Session.waitForBlockChange(105, 43, 105, 3000);
            assertTrue(v47SeesPlace2 > 0, "V47 should see Beta's placement");

            // V47 breaks block at (100, 43, 100)
            v47Session.sendDigging(0, 100, 43, 100, 1);
            int betaSeesBreak1 = betaSession.waitForBlockChangeValue(100, 43, 100, 0, 3000);
            assertEquals(0, betaSeesBreak1, "Beta should see V47's block broken to air");

            // Beta breaks block at (105, 43, 105)
            betaSession.sendDigging(0, 105, 43, 105, 1);
            int v47SeesBreak2 = v47Session.waitForBlockChangeValue(105, 43, 105, 0, 3000);
            assertEquals(0, v47SeesBreak2, "V47 should see Beta's block broken to air");
        } finally {
            v47Bot.disconnect();
            betaBot.disconnect();
        }
    }

    @Test
    void bidirectionalPlaceAndBreakSyncV340V109() throws Exception {
        BotClient v340Bot = testServer.createBot(ProtocolVersion.RELEASE_1_12_2, "V340Sync");
        BotClient v109Bot = testServer.createBot(ProtocolVersion.RELEASE_1_9_4, "V109Sync");
        try {
            BotSession v340Session = v340Bot.getSession();
            BotSession v109Session = v109Bot.getSession();
            assertTrue(v340Session.isLoginComplete(), "V340 login should complete");
            assertTrue(v109Session.isLoginComplete(), "V109 login should complete");

            Thread.sleep(500);

            // V340 places at (102, 42, 102) top face -> target (102, 43, 102)
            v340Session.sendBlockPlace(102, 42, 102, 1, 4);
            int v109SeesPlace1 = v109Session.waitForBlockChange(102, 43, 102, 3000);
            assertTrue(v109SeesPlace1 > 0, "V109 should see V340's placement");

            // V109 places at (107, 42, 107) top face -> target (107, 43, 107)
            v109Session.sendBlockPlace(107, 42, 107, 1, 4);
            int v340SeesPlace2 = v340Session.waitForBlockChange(107, 43, 107, 3000);
            assertTrue(v340SeesPlace2 > 0, "V340 should see V109's placement");

            // V340 breaks block at (102, 43, 102)
            v340Session.sendDigging(0, 102, 43, 102, 1);
            int v109SeesBreak1 = v109Session.waitForBlockChangeValue(102, 43, 102, 0, 3000);
            assertEquals(0, v109SeesBreak1, "V109 should see V340's block broken to air");

            // V109 breaks block at (107, 43, 107)
            v109Session.sendDigging(0, 107, 43, 107, 1);
            int v340SeesBreak2 = v340Session.waitForBlockChangeValue(107, 43, 107, 0, 3000);
            assertEquals(0, v340SeesBreak2, "V340 should see V109's block broken to air");
        } finally {
            v340Bot.disconnect();
            v109Bot.disconnect();
        }
    }

    @Test
    void bidirectionalPlaceAndBreakSyncModernNetty() throws Exception {
        BotClient v764Bot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "V764Sync");
        BotClient v477Bot = testServer.createBot(ProtocolVersion.RELEASE_1_14, "V477Sync");
        try {
            BotSession v764Session = v764Bot.getSession();
            BotSession v477Session = v477Bot.getSession();
            assertTrue(v764Session.isLoginComplete(), "1.20.2 login should complete");
            assertTrue(v477Session.isLoginComplete(), "1.14 login should complete");

            Thread.sleep(500);

            // --- 1.20.2 places at (52, 42, 52) top face -> target (52, 43, 52) ---
            v764Session.sendBlockPlace(52, 42, 52, 1, 4);
            int v477SeesPlace1 = v477Session.waitForBlockChange(52, 43, 52, 3000);
            assertTrue(v477SeesPlace1 > 0, "1.14 should see 1.20.2's placement");

            // --- 1.14 places at (57, 42, 57) top face -> target (57, 43, 57) ---
            v477Session.sendBlockPlace(57, 42, 57, 1, 4);
            int v764SeesPlace2 = v764Session.waitForBlockChange(57, 43, 57, 3000);
            assertTrue(v764SeesPlace2 > 0, "1.20.2 should see 1.14's placement");

            // --- 1.20.2 breaks block at (52, 43, 52) ---
            v764Session.sendDigging(0, 52, 43, 52, 1);
            int v477SeesBreak1 = v477Session.waitForBlockChangeValue(52, 43, 52, 0, 3000);
            assertEquals(0, v477SeesBreak1, "1.14 should see 1.20.2's block broken to air");

            // --- 1.14 breaks block at (57, 43, 57) ---
            v477Session.sendDigging(0, 57, 43, 57, 1);
            int v764SeesBreak2 = v764Session.waitForBlockChangeValue(57, 43, 57, 0, 3000);
            assertEquals(0, v764SeesBreak2, "1.20.2 should see 1.14's block broken to air");
        } finally {
            v764Bot.disconnect();
            v477Bot.disconnect();
        }
    }
}
