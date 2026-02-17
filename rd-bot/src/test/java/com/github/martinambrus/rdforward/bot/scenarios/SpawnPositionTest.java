package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the player spawns at the correct position:
 * feet on solid ground, not inside a block, at the expected Y.
 */
class SpawnPositionTest {

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
    void alphaV6SpawnEyeYIsCorrect() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "SpawnY");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Alpha S2C Y = eye level. Flat world surface at Y=42 (grass),
            // spawn feet at Y=43, eyes at 43 + (double) 1.62f
            double expectedEyeY = 43.0 + (double) 1.62f;
            assertEquals(expectedEyeY, session.getSpawnY(), 0.001,
                    "Spawn eye-level Y should be 43 + 1.62f");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void alphaV6BotIsOnGroundAfterSpawn() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "SpawnGround");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Wait for spawn chunk to arrive
            int blockX = (int) Math.floor(session.getX());
            int blockZ = (int) Math.floor(session.getZ());
            assertTrue(session.waitForChunkAt(blockX, blockZ, 5000),
                    "Spawn chunk should be received");

            assertTrue(session.isOnGround(),
                    "Bot should be on ground after spawn");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void alphaV6FeetBlockIsAirAndBelowIsGrass() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "SpawnBlock");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Compute feet position from Alpha eye-level Y
            double feetY = session.getY() - (double) 1.62f;
            int blockX = (int) Math.floor(session.getX());
            int feetBlockY = (int) Math.floor(feetY);
            int blockZ = (int) Math.floor(session.getZ());

            // Wait for spawn chunk to arrive
            assertTrue(session.waitForChunkAt(blockX, blockZ, 5000),
                    "Spawn chunk should be received");

            assertEquals(0, session.getBlockAt(blockX, feetBlockY, blockZ),
                    "Block at feet should be air (0)");
            assertEquals(2, session.getBlockAt(blockX, feetBlockY - 1, blockZ),
                    "Block below feet should be grass (2)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nettyV47SpawnFeetYIsCorrect() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "SpawnV47Y");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // V47 sends feet-level Y (server subtracts eye height for 1.8+ clients).
            // Flat world surface at Y=42 (grass), spawn feet at Y=43.
            assertEquals(43.0, session.getSpawnY(), 0.001,
                    "V47 spawn Y should be feet-level 43.0");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nettyV47BotIsOnGroundAfterSpawn() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "SpawnV47Gnd");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int blockX = (int) Math.floor(session.getX());
            int blockZ = (int) Math.floor(session.getZ());
            assertTrue(session.waitForChunkAt(blockX, blockZ, 5000),
                    "Spawn chunk should be received");

            assertTrue(session.isOnGround(),
                    "V47 bot should be on ground after spawn");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void nettyV47FeetBlockIsAirAndBelowIsGrass() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "SpawnV47Blk");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // V47 Y is already feet-level
            int blockX = (int) Math.floor(session.getX());
            int feetBlockY = (int) Math.floor(session.getY());
            int blockZ = (int) Math.floor(session.getZ());

            assertTrue(session.waitForChunkAt(blockX, blockZ, 5000),
                    "Spawn chunk should be received");

            assertEquals(0, session.getBlockAt(blockX, feetBlockY, blockZ),
                    "Block at feet should be air (0)");
            assertEquals(2, session.getBlockAt(blockX, feetBlockY - 1, blockZ),
                    "Block below feet should be grass (2)");
        } finally {
            bot.disconnect();
        }
    }
}
