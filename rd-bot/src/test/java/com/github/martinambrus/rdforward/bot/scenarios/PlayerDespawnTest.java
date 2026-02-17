package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that when a player disconnects, other players receive
 * a DestroyEntity packet for that player's entity ID.
 */
class PlayerDespawnTest {

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
    void disconnectedPlayerEntityIsDestroyed() throws Exception {
        BotClient observer = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "WatchBot");
        BotClient leaver = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "LeaveBot");
        try {
            BotSession observerSession = observer.getSession();
            BotSession leaverSession = leaver.getSession();
            assertTrue(observerSession.isLoginComplete(), "Observer login should complete");
            assertTrue(leaverSession.isLoginComplete(), "Leaver login should complete");

            int leaverEntityId = leaverSession.getEntityId();

            // Observer should see leaver spawn
            assertNotNull(observerSession.waitForPlayerSpawn(3000),
                    "Observer should see leaver spawn");

            // Leaver disconnects
            leaver.disconnect();
            leaver = null;

            // Observer should receive DestroyEntity for the leaver
            assertTrue(observerSession.waitForDespawn(leaverEntityId, 3000),
                    "Observer should receive DestroyEntity for disconnected player");
        } finally {
            observer.disconnect();
            if (leaver != null) leaver.disconnect();
        }
    }
}
