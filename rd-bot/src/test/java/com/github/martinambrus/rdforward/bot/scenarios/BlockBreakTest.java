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
}
