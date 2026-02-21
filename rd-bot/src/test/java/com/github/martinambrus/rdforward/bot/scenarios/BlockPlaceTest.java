package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests block placement: bot places a block and receives a BlockChange
 * confirmation from the server. Also tests that a second bot receives
 * the broadcast.
 */
class BlockPlaceTest {

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
    void blockPlacementReceivesConfirmation() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "PlaceBot");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Wait for chunks and inventory
            Thread.sleep(500);

            // Place cobblestone (ID 4) on top face of the surface.
            // Flat world: surfaceY = height*2/3 = 42 (grass layer).
            // Placing on top face (dir=1) of y=42 puts block at y=43 (air → cobblestone).
            int placeX = 10;
            int placeY = 42;
            int placeZ = 10;
            session.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            // Should receive a BlockChange confirmation for the block above (y+1)
            // Direction 1 = top face, so block is placed at y+1
            int blockType = session.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "Should receive BlockChange confirmation with non-air block type");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void blockPlacementBroadcastsToOtherPlayers() throws Exception {
        BotClient bot1 = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "Placer");
        BotClient bot2 = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "Observer");
        try {
            BotSession session1 = bot1.getSession();
            BotSession session2 = bot2.getSession();
            assertTrue(session1.isLoginComplete(), "Bot1 login should complete");
            assertTrue(session2.isLoginComplete(), "Bot2 login should complete");

            Thread.sleep(500);

            // Bot1 places a block on top of the surface (y=42 grass → y=43 air)
            int placeX = 20;
            int placeY = 42;
            int placeZ = 20;
            session1.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            // Bot2 should also receive the BlockChange broadcast
            int blockType = session2.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "Observer should receive block change broadcast");
        } finally {
            bot1.disconnect();
            bot2.disconnect();
        }
    }

    @Test
    void modernNettyBlockPlacementReceivesConfirmation() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "PlaceBot2");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            Thread.sleep(500);

            int placeX = 12;
            int placeY = 42;
            int placeZ = 12;
            session.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            // Block state ID should be non-zero (block placed)
            int blockType = session.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "Should receive BlockChange confirmation with non-air block state");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void latestProtocolBlockPlacementBroadcasts() throws Exception {
        BotClient bot1 = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "Placer2");
        BotClient bot2 = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "Observer2");
        try {
            BotSession session1 = bot1.getSession();
            BotSession session2 = bot2.getSession();
            assertTrue(session1.isLoginComplete(), "Bot1 login should complete");
            assertTrue(session2.isLoginComplete(), "Bot2 login should complete");

            Thread.sleep(500);

            int placeX = 22;
            int placeY = 42;
            int placeZ = 22;
            session1.sendBlockPlace(placeX, placeY, placeZ, 1, 4);

            int blockType = session2.waitForBlockChange(placeX, placeY + 1, placeZ, 3000);
            assertTrue(blockType > 0, "Observer should receive block change broadcast on latest protocol");
        } finally {
            bot1.disconnect();
            bot2.disconnect();
        }
    }
}
