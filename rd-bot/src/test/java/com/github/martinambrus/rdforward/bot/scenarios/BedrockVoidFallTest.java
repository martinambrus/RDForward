package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotBedrockClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that a Bedrock bot falling below Y=-10 is teleported back to spawn.
 * The server checks MovePlayerPacket C2S: if (eyeY - 1.62) < -10 → teleportToSpawn().
 * sendPosition() takes feet-level Y and converts to eye-level for MovePlayerPacket.
 */
class BedrockVoidFallTest {

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
    void bedrockVoidFallTeleportsBackToSpawn() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedVoid1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int prevCount = session.getPositionUpdateCount();

            // Send position below the void threshold (feet Y = -20)
            session.sendPosition(session.getX(), -20, session.getZ(), 0, 0);

            // Wait for server to teleport us back via S2C MovePlayerPacket
            assertTrue(session.waitForPositionUpdate(prevCount, 5000),
                    "Server should send position update after void fall");

            // Should be back near spawn (eye-level Y should be above ground)
            assertTrue(session.getY() > 0,
                    "Player should be teleported back above ground, got Y=" + session.getY());
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void bedrockVoidFallRespawnYMatchesSpawnY() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedVoid2");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            double originalSpawnY = session.getSpawnY();
            int prevCount = session.getPositionUpdateCount();

            // Fall into the void
            session.sendPosition(session.getX(), -20, session.getZ(), 0, 0);

            // Wait for server to teleport back
            assertTrue(session.waitForPositionUpdate(prevCount, 5000),
                    "Server should send position update after void fall");

            // Respawn Y should match original spawn Y (both eye-level)
            assertEquals(originalSpawnY, session.getY(), 0.01,
                    "Respawn Y should match original spawn Y");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void bedrockVoidFallBotStaysConnected() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedVoid3");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int prevCount = session.getPositionUpdateCount();

            // Fall into the void
            session.sendPosition(session.getX(), -20, session.getZ(), 0, 0);

            // Wait for respawn
            assertTrue(session.waitForPositionUpdate(prevCount, 5000),
                    "Server should send position update after void fall");

            // Bot should remain connected after void fall + respawn
            Thread.sleep(500);
            assertTrue(session.isLoginComplete(),
                    "Bedrock bot should remain connected after void fall respawn");
        } finally {
            bot.disconnect();
        }
    }
}
