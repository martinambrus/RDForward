package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests grass conversion for creative-mode block placement.
 * The server converts any placed block to grass (2) at surface Y (42)
 * and cobblestone (4) below the surface for Beta 1.8+ (creative) clients.
 */
class GrassConversionTest {

    private static final int GRASS = 2;
    private static final int COBBLESTONE = 4;
    private static final int SURFACE_Y = 42; // world.getHeight() * 2 / 3 = 64 * 2 / 3 = 42

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
    void creativePlacementAtSurfaceConvertsToGrass() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "GrassBot");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int x = 130, z = 130;

            // Wait for chunk data at the target position
            assertTrue(session.waitForChunkAt(x, z, 5000), "Chunk data should arrive");

            // Break the existing grass at surface Y to make room
            session.sendDigging(0, x, SURFACE_Y, z, 1);
            assertEquals(0, session.waitForBlockChangeValue(x, SURFACE_Y, z, 0, 3000),
                    "Block should be broken to air");

            // Place cobblestone at surface Y (on top of Y=41, direction=1)
            session.sendBlockPlace(x, SURFACE_Y - 1, z, 1, COBBLESTONE);

            // Server should convert to grass at surface Y
            int blockType = session.waitForBlockChangeValue(x, SURFACE_Y, z, GRASS, 3000);
            assertEquals(GRASS, blockType,
                    "Cobblestone placed at surface Y should convert to grass");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void creativePlacementBelowSurfaceStaysCobblestone() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "CobbleBot18");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int x = 131, z = 131;

            // Wait for chunk data
            assertTrue(session.waitForChunkAt(x, z, 5000), "Chunk data should arrive");

            // Break block at Y=41 (below surface) to create air
            session.sendDigging(0, x, 41, z, 1);
            assertEquals(0, session.waitForBlockChangeValue(x, 41, z, 0, 3000),
                    "Block at Y=41 should be broken to air");

            // Place cobblestone at Y=41 (below surface) — on top of Y=40, direction=1
            session.sendBlockPlace(x, 40, z, 1, COBBLESTONE);

            // Server should keep it as cobblestone (below surface)
            int blockType = session.waitForBlockChangeValue(x, 41, z, COBBLESTONE, 3000);
            assertEquals(COBBLESTONE, blockType,
                    "Cobblestone placed below surface Y should stay cobblestone");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void creativeMultipleItemIdsAllConvert() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "MultiBot");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int baseX = 132, z = 132;

            // Wait for chunk data
            assertTrue(session.waitForChunkAt(baseX, z, 5000), "Chunk data should arrive");

            // Place different block IDs below surface (on top of Y=40 -> Y=41)
            // First break Y=41 at several X offsets
            int[] itemIds = {1, 3, 12, 5}; // stone, dirt, sand, wood
            for (int i = 0; i < itemIds.length; i++) {
                int x = baseX + i * 2;
                session.sendDigging(0, x, 41, z, 1);
                assertEquals(0, session.waitForBlockChangeValue(x, 41, z, 0, 3000),
                        "Block at (" + x + ",41," + z + ") should be broken to air");
            }

            // Place each item ID at Y=41 (below surface -> cobblestone)
            for (int i = 0; i < itemIds.length; i++) {
                int x = baseX + i * 2;
                session.sendBlockPlace(x, 40, z, 1, itemIds[i]);
                int blockType = session.waitForBlockChangeValue(x, 41, z, COBBLESTONE, 3000);
                assertEquals(COBBLESTONE, blockType,
                        "Item ID " + itemIds[i] + " placed below surface should become cobblestone");
            }
        } finally {
            bot.disconnect();
        }
    }

    // 1.13+ block state IDs: grass block = 9, cobblestone = 14
    private static final int GRASS_STATE = 9;
    private static final int COBBLESTONE_STATE = 14;

    @Test
    void v477PlacementAtSurfaceConvertsToGrass() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_14, "GrsV477");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int x = 250, z = 250;
            Thread.sleep(500);

            session.sendDigging(0, x, SURFACE_Y, z, 1);
            assertEquals(0, session.waitForBlockChangeValue(x, SURFACE_Y, z, 0, 3000),
                    "Block should be broken to air");

            session.sendBlockPlace(x, SURFACE_Y - 1, z, 1, 4);
            int blockType = session.waitForBlockChangeValue(x, SURFACE_Y, z, GRASS_STATE, 3000);
            assertEquals(GRASS_STATE, blockType,
                    "V477 placement at surface Y should convert to grass (state 9)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void v477PlacementBelowSurfaceStaysCobblestone() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_14, "CobV477");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int x = 251, z = 251;
            Thread.sleep(500);

            session.sendDigging(0, x, 41, z, 1);
            assertEquals(0, session.waitForBlockChangeValue(x, 41, z, 0, 3000),
                    "Block at Y=41 should be broken to air");

            session.sendBlockPlace(x, 40, z, 1, 4);
            int blockType = session.waitForBlockChangeValue(x, 41, z, COBBLESTONE_STATE, 3000);
            assertEquals(COBBLESTONE_STATE, blockType,
                    "V477 placement below surface should stay cobblestone (state 14)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void v764PlacementAtSurfaceConvertsToGrass() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "GrsV764");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int x = 252, z = 252;
            Thread.sleep(500);

            session.sendDigging(0, x, SURFACE_Y, z, 1);
            assertEquals(0, session.waitForBlockChangeValue(x, SURFACE_Y, z, 0, 3000),
                    "Block should be broken to air");

            session.sendBlockPlace(x, SURFACE_Y - 1, z, 1, 4);
            int blockType = session.waitForBlockChangeValue(x, SURFACE_Y, z, GRASS_STATE, 3000);
            assertEquals(GRASS_STATE, blockType,
                    "V764 placement at surface Y should convert to grass (state 9)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void v774PlacementAtSurfaceConvertsToGrass() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "GrsV774");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int x = 240, z = 240;
            Thread.sleep(500);

            session.sendDigging(0, x, SURFACE_Y, z, 1);
            assertEquals(0, session.waitForBlockChangeValue(x, SURFACE_Y, z, 0, 3000),
                    "Block should be broken to air");

            session.sendBlockPlace(x, SURFACE_Y - 1, z, 1, 4);
            int blockType = session.waitForBlockChangeValue(x, SURFACE_Y, z, GRASS_STATE, 3000);
            assertEquals(GRASS_STATE, blockType,
                    "V774 placement at surface Y should convert to grass (state 9)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void v774PlacementBelowSurfaceStaysCobblestone() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "CobV774");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int x = 241, z = 241;
            Thread.sleep(500);

            session.sendDigging(0, x, 41, z, 1);
            assertEquals(0, session.waitForBlockChangeValue(x, 41, z, 0, 3000),
                    "Block at Y=41 should be broken to air");

            session.sendBlockPlace(x, 40, z, 1, 4);
            int blockType = session.waitForBlockChangeValue(x, 41, z, COBBLESTONE_STATE, 3000);
            assertEquals(COBBLESTONE_STATE, blockType,
                    "V774 placement below surface should stay cobblestone (state 14)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void v764PlacementBelowSurfaceStaysCobblestone() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "CobV764");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int x = 253, z = 253;
            Thread.sleep(500);

            session.sendDigging(0, x, 41, z, 1);
            assertEquals(0, session.waitForBlockChangeValue(x, 41, z, 0, 3000),
                    "Block at Y=41 should be broken to air");

            session.sendBlockPlace(x, 40, z, 1, 4);
            int blockType = session.waitForBlockChangeValue(x, 41, z, COBBLESTONE_STATE, 3000);
            assertEquals(COBBLESTONE_STATE, blockType,
                    "V764 placement below surface should stay cobblestone (state 14)");
        } finally {
            bot.disconnect();
        }
    }
}
