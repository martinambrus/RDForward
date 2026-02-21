package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotBedrockClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that a Bedrock bot spawns at the correct position.
 * Bedrock StartGamePacket sends eye-level Y (same convention as MovePlayerPacket).
 * Flat world surface at Y=42 (grass), feet at Y=43, eyes at ~44.62.
 *
 * Note: getBlockAt() returns -1 for Bedrock (chunks not parsed), so
 * block-level assertions (isOnGround, block type checks) are not possible.
 */
class BedrockSpawnPositionTest {

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
    void bedrockSpawnEyeYIsCorrect() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedSpnY");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // StartGamePacket sends eye-level Y: 43 + 1.62 = 44.62
            // Float precision: (float) 44.62 → ~44.62
            double expectedEyeY = 43.0 + 1.62;
            assertEquals(expectedEyeY, session.getSpawnY(), 0.01,
                    "Bedrock spawn Y should be eye-level ~44.62");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void bedrockSpawnFeetYIsAboveGround() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedSpnFt");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Eye-level Y ~44.62, feet = eye - 1.62 ≈ 43.0
            // Float precision: (float) 44.62 → 44.619998..., so feet ≈ 42.999998
            double feetY = session.getY() - 1.62;
            assertEquals(43.0, feetY, 0.01,
                    "Bedrock feet Y should be ~43.0 (above grass at 42)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void bedrockSpawnReceivesChunkAndStaysConnected() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedSpnCk");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int blockX = (int) Math.floor(session.getX());
            int blockZ = (int) Math.floor(session.getZ());
            assertTrue(session.waitForChunkAt(blockX, blockZ, 5000),
                    "Spawn chunk should be received");

            // Verify bot stays connected
            Thread.sleep(500);
            assertTrue(session.isLoginComplete(),
                    "Bedrock bot should remain connected after spawn");
        } finally {
            bot.disconnect();
        }
    }
}
