package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that multiple bots connecting simultaneously can see each other.
 * Bot1 connects first, then Bot2. Both should receive SpawnPlayerPacket
 * for the other player.
 */
class MultiPlayerTest {

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
    void twoBotsCanSeeEachOther() throws Exception {
        BotClient bot1 = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "Player1");
        BotClient bot2 = null;
        try {
            BotSession session1 = bot1.getSession();
            assertTrue(session1.isLoginComplete(), "Bot1 login should complete");

            bot2 = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "Player2");
            BotSession session2 = bot2.getSession();
            assertTrue(session2.isLoginComplete(), "Bot2 login should complete");

            // Bot2 should have received a SpawnPlayerPacket for Bot1 during login
            // (server sends existing players to new joiners)
            Packet spawnInBot2 = session2.waitForPlayerSpawn(3000);
            assertNotNull(spawnInBot2, "Bot2 should see Bot1 spawn");

            // Bot1 should receive a SpawnPlayerPacket for Bot2
            // (server broadcasts new player join to existing players)
            Packet spawnInBot1 = null;
            long deadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < deadline) {
                if (!session1.getSpawnedPlayers().isEmpty()) {
                    spawnInBot1 = session1.waitForPlayerSpawn(100);
                    break;
                }
                Thread.sleep(50);
            }
            assertNotNull(spawnInBot1, "Bot1 should see Bot2 spawn");
        } finally {
            bot1.disconnect();
            if (bot2 != null) bot2.disconnect();
        }
    }
}
