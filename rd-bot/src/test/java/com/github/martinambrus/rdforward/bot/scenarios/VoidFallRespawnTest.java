package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that falling below Y = -10 triggers a server teleport back to spawn.
 */
class VoidFallRespawnTest {

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
    void voidFallTeleportsBackToSpawn() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "VoidBot");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Record current position update count (1 from login S2C position)
            int prevCount = session.getPositionUpdateCount();

            // Send position below the void threshold (Y < -10)
            session.sendPosition(128, -20, 128, 0, 0);

            // Wait for server to teleport us back
            assertTrue(session.waitForPositionUpdate(prevCount, 3000),
                    "Server should send position update after void fall");

            // Should be back near spawn (above ground)
            assertTrue(session.getY() > 0,
                    "Player should be teleported back above ground, got Y=" + session.getY());
        } finally {
            bot.disconnect();
        }
    }
}
