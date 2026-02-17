package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests creative-mode block placement sweep: various item IDs placed
 * by a 1.8 bot all convert to cobblestone (below surface) or grass
 * (at surface). Non-block items (ID > 255) are silently rejected.
 */
class CreativeBlockPlacementTest {

    private static final int GRASS = 2;
    private static final int COBBLESTONE = 4;
    private static final int SURFACE_Y = 42;

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
    void allBlockIdsConvertToCobblestoneOrGrass() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "SweepBot");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int baseX = 140, z = 140;
            assertTrue(session.waitForChunkAt(baseX, z, 5000), "Chunk data should arrive");

            // Representative block IDs to test
            int[] blockIds = {1, 2, 3, 5, 7, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                    44, 45, 46, 48, 49};

            for (int i = 0; i < blockIds.length; i++) {
                int x = baseX + i;

                // Break block at Y=41 to make air
                session.sendDigging(0, x, 41, z, 1);
                assertEquals(0, session.waitForBlockChangeValue(x, 41, z, 0, 3000),
                        "Block at (" + x + ",41," + z + ") should break to air");

                // Place with this block ID (below surface -> cobblestone)
                session.sendBlockPlace(x, 40, z, 1, blockIds[i]);
                int blockType = session.waitForBlockChangeValue(x, 41, z, COBBLESTONE, 3000);
                assertEquals(COBBLESTONE, blockType,
                        "Block ID " + blockIds[i] + " should convert to cobblestone below surface");
            }
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nonBlockItemsAreRejected() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "RejectBot");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int baseX = 160, z = 160;
            assertTrue(session.waitForChunkAt(baseX, z, 5000), "Chunk data should arrive");

            // Non-block item IDs (> 255) should be rejected by the server.
            // The server sends a BlockChange with air (0) as a correction, but
            // no solid block is placed in the world.
            int[] nonBlockIds = {256, 320, 400};

            for (int i = 0; i < nonBlockIds.length; i++) {
                int x = baseX + i;

                // Y=43 is already air in the flat world
                session.sendBlockPlace(x, SURFACE_Y, z, 1, nonBlockIds[i]);

                // Server sends air correction — verify no solid block was placed
                int blockType = session.waitForBlockChange(x, SURFACE_Y + 1, z, 2000);
                assertTrue(blockType <= 0,
                        "Non-block item ID " + nonBlockIds[i]
                                + " should not place a solid block (got " + blockType + ")");
            }
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void placementAtSurfaceYConvertsToGrass() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "GrassSweep");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int baseX = 170, z = 170;
            assertTrue(session.waitForChunkAt(baseX, z, 5000), "Chunk data should arrive");

            // Place 5 different block IDs at surface Y
            int[] blockIds = {1, 3, 5, 12, 49}; // stone, dirt, wood, sand, obsidian

            for (int i = 0; i < blockIds.length; i++) {
                int x = baseX + i;

                // Break existing grass at surface Y
                session.sendDigging(0, x, SURFACE_Y, z, 1);
                assertEquals(0, session.waitForBlockChangeValue(x, SURFACE_Y, z, 0, 3000),
                        "Block at (" + x + "," + SURFACE_Y + "," + z + ") should break to air");

                // Place block at surface Y (on top of Y=41, direction=1)
                session.sendBlockPlace(x, SURFACE_Y - 1, z, 1, blockIds[i]);
                int blockType = session.waitForBlockChangeValue(x, SURFACE_Y, z, GRASS, 3000);
                assertEquals(GRASS, blockType,
                        "Block ID " + blockIds[i] + " at surface Y should convert to grass");
            }
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nettyV109BlockIdsConvertToCobblestone() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_9_4, "V109Sweep");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int baseX = 180, z = 180;
            assertTrue(session.waitForChunkAt(baseX, z, 5000), "Chunk data should arrive");

            int[] blockIds = {1, 2, 3, 5, 7, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                    44, 45, 46, 48, 49};

            for (int i = 0; i < blockIds.length; i++) {
                int x = baseX + i;

                session.sendDigging(0, x, 41, z, 1);
                assertEquals(0, session.waitForBlockChangeValue(x, 41, z, 0, 3000),
                        "Block at (" + x + ",41," + z + ") should break to air");

                session.sendBlockPlace(x, 40, z, 1, blockIds[i]);
                int blockType = session.waitForBlockChangeValue(x, 41, z, COBBLESTONE, 3000);
                assertEquals(COBBLESTONE, blockType,
                        "Block ID " + blockIds[i] + " should convert to cobblestone below surface (V109)");
            }
        } finally {
            bot.disconnect();
        }
    }
}
