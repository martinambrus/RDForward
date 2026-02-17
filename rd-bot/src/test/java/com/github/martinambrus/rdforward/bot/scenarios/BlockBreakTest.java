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
}
