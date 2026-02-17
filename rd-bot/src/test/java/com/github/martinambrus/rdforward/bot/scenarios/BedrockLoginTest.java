package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotBedrockClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that a Bedrock bot can connect, complete the login handshake,
 * and receive valid spawn data from the server.
 */
class BedrockLoginTest {

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
    void bedrockLoginSucceeds() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedBot1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Bedrock login should complete");
            assertTrue(session.getEntityId() > 0, "Entity ID should be positive");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void bedrockSpawnPositionIsValid() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedBot2");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            double y = session.getY();
            // Spawn Y should be eye-level above the flat world surface
            // Flat world surface is at height*2/3 = 42, so feet = 43, eyes ~= 44.62
            assertTrue(y > 40 && y < 80,
                    "Spawn Y should be reasonable (got " + y + ")");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void bedrockReceivesChunkData() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedBot3");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // The server sends an empty chunk at spawn during the login sequence.
            // The spawn position is at world center (128, 128).
            int spawnX = (int) Math.floor(session.getX());
            int spawnZ = (int) Math.floor(session.getZ());
            assertTrue(session.waitForChunkAt(spawnX, spawnZ, 5000),
                    "Should receive chunk data at spawn position");
        } finally {
            bot.disconnect();
        }
    }
}
