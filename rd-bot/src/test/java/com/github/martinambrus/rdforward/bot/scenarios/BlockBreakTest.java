package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests creative-mode instant block breaking and cross-version broadcast.
 * Alpha v6 places a block, Release v39 breaks it, both see the changes.
 */
class BlockBreakTest {

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
    void crossVersionBlockBreaking() throws Exception {
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaBreak");
        BotClient releaseBot = testServer.createBot(ProtocolVersion.RELEASE_1_3_1, "ReleaseBreak");
        try {
            BotSession alphaSession = alphaBot.getSession();
            BotSession releaseSession = releaseBot.getSession();
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            assertTrue(releaseSession.isLoginComplete(), "Release login should complete");

            Thread.sleep(500);

            // Alpha places block at (35, 42, 35) top face -> target (35, 43, 35)
            int bx = 35, by = 42, bz = 35;
            alphaSession.sendBlockPlace(bx, by, bz, 1, 4);

            // Both should see the placement
            int alphaPlaced = alphaSession.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(alphaPlaced > 0, "Alpha should see own placement");

            int releasePlaced = releaseSession.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(releasePlaced > 0, "Release should see Alpha's placement");

            // Release breaks the block (STATUS_STARTED=0, creative instant break)
            releaseSession.sendDigging(0, bx, by + 1, bz, 1);

            // Both should see it become air (0)
            int alphaBroken = alphaSession.waitForBlockChangeValue(bx, by + 1, bz, 0, 3000);
            assertEquals(0, alphaBroken, "Alpha should see block broken to air");

            int releaseBroken = releaseSession.waitForBlockChangeValue(bx, by + 1, bz, 0, 3000);
            assertEquals(0, releaseBroken, "Release should see block broken to air");
        } finally {
            alphaBot.disconnect();
            releaseBot.disconnect();
        }
    }

    @Test
    void crossVersionBlockBreakingWithV109() throws Exception {
        BotClient v109Bot = testServer.createBot(ProtocolVersion.RELEASE_1_9_4, "V109Break");
        BotClient v47Bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "V47Break");
        try {
            BotSession v109Session = v109Bot.getSession();
            BotSession v47Session = v47Bot.getSession();
            assertTrue(v109Session.isLoginComplete(), "V109 login should complete");
            assertTrue(v47Session.isLoginComplete(), "V47 login should complete");

            Thread.sleep(500);

            // V109 places block at (37, 42, 37) top face -> target (37, 43, 37)
            int bx = 37, by = 42, bz = 37;
            v109Session.sendBlockPlace(bx, by, bz, 1, 4);

            // Both should see the placement
            int v109Placed = v109Session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(v109Placed > 0, "V109 should see own placement");

            int v47Placed = v47Session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(v47Placed > 0, "V47 should see V109's placement");

            // V47 breaks the block (STATUS_STARTED=0, creative instant break)
            v47Session.sendDigging(0, bx, by + 1, bz, 1);

            // Both should see it become air (0)
            int v109Broken = v109Session.waitForBlockChangeValue(bx, by + 1, bz, 0, 3000);
            assertEquals(0, v109Broken, "V109 should see block broken to air");

            int v47Broken = v47Session.waitForBlockChangeValue(bx, by + 1, bz, 0, 3000);
            assertEquals(0, v47Broken, "V47 should see block broken to air");
        } finally {
            v109Bot.disconnect();
            v47Bot.disconnect();
        }
    }

    @Test
    void crossVersionBlockBreakingWithV340() throws Exception {
        BotClient v340Bot = testServer.createBot(ProtocolVersion.RELEASE_1_12_2, "V340Break");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaBreak2");
        try {
            BotSession v340Session = v340Bot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            assertTrue(v340Session.isLoginComplete(), "V340 login should complete");
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");

            Thread.sleep(500);

            // V340 places block at (39, 42, 39) top face -> target (39, 43, 39)
            int bx = 39, by = 42, bz = 39;
            v340Session.sendBlockPlace(bx, by, bz, 1, 4);

            int v340Placed = v340Session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(v340Placed > 0, "V340 should see own placement");

            int alphaPlaced = alphaSession.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(alphaPlaced > 0, "Alpha should see V340's placement");

            // Alpha breaks the block
            alphaSession.sendDigging(0, bx, by + 1, bz, 1);

            int v340Broken = v340Session.waitForBlockChangeValue(bx, by + 1, bz, 0, 3000);
            assertEquals(0, v340Broken, "V340 should see block broken to air");

            int alphaBroken = alphaSession.waitForBlockChangeValue(bx, by + 1, bz, 0, 3000);
            assertEquals(0, alphaBroken, "Alpha should see block broken to air");
        } finally {
            v340Bot.disconnect();
            alphaBot.disconnect();
        }
    }

    @Test
    void crossVersionBlockBreakingWithV477() throws Exception {
        BotClient v477Bot = testServer.createBot(ProtocolVersion.RELEASE_1_14, "V477Break");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaBreak3");
        try {
            BotSession v477Session = v477Bot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            assertTrue(v477Session.isLoginComplete(), "1.14 login should complete");
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");

            Thread.sleep(500);

            // Alpha places block
            int bx = 41, by = 42, bz = 41;
            alphaSession.sendBlockPlace(bx, by, bz, 1, 4);

            int alphaPlaced = alphaSession.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(alphaPlaced > 0, "Alpha should see own placement");

            int v477Placed = v477Session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(v477Placed > 0, "1.14 should see Alpha's placement");

            // 1.14 breaks the block (uses V477 position encoding)
            v477Session.sendDigging(0, bx, by + 1, bz, 1);

            int alphaBroken = alphaSession.waitForBlockChangeValue(bx, by + 1, bz, 0, 3000);
            assertEquals(0, alphaBroken, "Alpha should see block broken to air");

            int v477Broken = v477Session.waitForBlockChangeValue(bx, by + 1, bz, 0, 3000);
            assertEquals(0, v477Broken, "1.14 should see block broken to air");
        } finally {
            v477Bot.disconnect();
            alphaBot.disconnect();
        }
    }

    @Test
    void crossVersionBlockBreakingWithV764() throws Exception {
        BotClient v764Bot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "V764Break");
        BotClient v340Bot = testServer.createBot(ProtocolVersion.RELEASE_1_12_2, "V340Break2");
        try {
            BotSession v764Session = v764Bot.getSession();
            BotSession v340Session = v340Bot.getSession();
            assertTrue(v764Session.isLoginComplete(), "1.20.2 login should complete");
            assertTrue(v340Session.isLoginComplete(), "1.12.2 login should complete");

            Thread.sleep(500);

            // 1.20.2 places block
            int bx = 43, by = 42, bz = 43;
            v764Session.sendBlockPlace(bx, by, bz, 1, 4);

            int v764Placed = v764Session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(v764Placed > 0, "1.20.2 should see own placement");

            int v340Placed = v340Session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(v340Placed > 0, "1.12.2 should see 1.20.2's placement");

            // 1.12.2 breaks the block
            v340Session.sendDigging(0, bx, by + 1, bz, 1);

            int v764Broken = v764Session.waitForBlockChangeValue(bx, by + 1, bz, 0, 3000);
            assertEquals(0, v764Broken, "1.20.2 should see block broken to air");

            int v340Broken = v340Session.waitForBlockChangeValue(bx, by + 1, bz, 0, 3000);
            assertEquals(0, v340Broken, "1.12.2 should see block broken to air");
        } finally {
            v764Bot.disconnect();
            v340Bot.disconnect();
        }
    }

    @Test
    void crossVersionBlockBreakingWithV774() throws Exception {
        BotClient v774Bot = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "V774Break");
        BotClient v340Bot = testServer.createBot(ProtocolVersion.RELEASE_1_12_2, "V340Break3");
        try {
            BotSession v774Session = v774Bot.getSession();
            BotSession v340Session = v340Bot.getSession();
            assertTrue(v774Session.isLoginComplete(), "1.21.11 login should complete");
            assertTrue(v340Session.isLoginComplete(), "1.12.2 login should complete");

            Thread.sleep(500);

            // 1.21.11 places block
            int bx = 45, by = 42, bz = 45;
            v774Session.sendBlockPlace(bx, by, bz, 1, 4);

            int v774Placed = v774Session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(v774Placed > 0, "1.21.11 should see own placement");

            int v340Placed = v340Session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(v340Placed > 0, "1.12.2 should see 1.21.11's placement");

            // 1.12.2 breaks the block
            v340Session.sendDigging(0, bx, by + 1, bz, 1);

            int v774Broken = v774Session.waitForBlockChangeValue(bx, by + 1, bz, 0, 3000);
            assertEquals(0, v774Broken, "1.21.11 should see block broken to air");

            int v340Broken = v340Session.waitForBlockChangeValue(bx, by + 1, bz, 0, 3000);
            assertEquals(0, v340Broken, "1.12.2 should see block broken to air");
        } finally {
            v774Bot.disconnect();
            v340Bot.disconnect();
        }
    }
}
