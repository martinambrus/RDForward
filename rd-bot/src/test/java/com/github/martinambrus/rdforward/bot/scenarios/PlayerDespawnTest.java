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

            // Leaver disconnects — channel.close() is async; give the server
            // time to fire channelInactive and broadcast the despawn.
            leaver.disconnect();
            leaver = null;
            Thread.sleep(200);

            // Observer should receive DestroyEntity for the leaver
            assertTrue(observerSession.waitForDespawn(leaverEntityId, 5000),
                    "Observer should receive DestroyEntity for disconnected player");
        } finally {
            observer.disconnect();
            if (leaver != null) leaver.disconnect();
        }
    }

    @Test
    void betaDisconnectTriggersDestroy() throws Exception {
        BotClient observer = testServer.createBot(ProtocolVersion.BETA_1_8, "BetaWatch");
        BotClient leaver = testServer.createBot(ProtocolVersion.BETA_1_8, "BetaLeave");
        try {
            BotSession observerSession = observer.getSession();
            BotSession leaverSession = leaver.getSession();
            assertTrue(observerSession.isLoginComplete(), "Observer login should complete");
            assertTrue(leaverSession.isLoginComplete(), "Leaver login should complete");

            int leaverEntityId = leaverSession.getEntityId();

            Thread.sleep(500);
            assertTrue(observerSession.getSpawnedPlayers().size() >= 1,
                    "Observer should see leaver spawn");

            leaver.disconnect();
            leaver = null;
            Thread.sleep(200);

            assertTrue(observerSession.waitForDespawn(leaverEntityId, 5000),
                    "Observer should receive DestroyEntity for disconnected Beta player");
        } finally {
            observer.disconnect();
            if (leaver != null) leaver.disconnect();
        }
    }

    @Test
    void nettyV47DisconnectTriggersDestroy() throws Exception {
        BotClient observer = testServer.createBot(ProtocolVersion.RELEASE_1_8, "V47Watch");
        BotClient leaver = testServer.createBot(ProtocolVersion.RELEASE_1_8, "V47Leave");
        try {
            BotSession observerSession = observer.getSession();
            BotSession leaverSession = leaver.getSession();
            assertTrue(observerSession.isLoginComplete(), "Observer login should complete");
            assertTrue(leaverSession.isLoginComplete(), "Leaver login should complete");

            int leaverEntityId = leaverSession.getEntityId();

            Thread.sleep(500);
            assertTrue(observerSession.getSpawnedPlayers().size() >= 1,
                    "Observer should see leaver spawn");

            leaver.disconnect();
            leaver = null;
            Thread.sleep(200);

            assertTrue(observerSession.waitForDespawn(leaverEntityId, 5000),
                    "Observer should receive DestroyEntity for disconnected V47 player");
        } finally {
            observer.disconnect();
            if (leaver != null) leaver.disconnect();
        }
    }

    @Test
    void v393DisconnectTriggersDestroy() throws Exception {
        BotClient observer = testServer.createBot(ProtocolVersion.RELEASE_1_13, "V393Watch");
        BotClient leaver = testServer.createBot(ProtocolVersion.RELEASE_1_13, "V393Leave");
        try {
            BotSession observerSession = observer.getSession();
            BotSession leaverSession = leaver.getSession();
            assertTrue(observerSession.isLoginComplete(), "Observer login should complete");
            assertTrue(leaverSession.isLoginComplete(), "Leaver login should complete");

            int leaverEntityId = leaverSession.getEntityId();

            Thread.sleep(500);
            assertTrue(observerSession.getSpawnedPlayers().size() >= 1,
                    "Observer should see leaver spawn");

            leaver.disconnect();
            leaver = null;
            Thread.sleep(200);

            assertTrue(observerSession.waitForDespawn(leaverEntityId, 5000),
                    "Observer should receive DestroyEntity for disconnected V393 player");
        } finally {
            observer.disconnect();
            if (leaver != null) leaver.disconnect();
        }
    }

    @Test
    void modernNettyDisconnectTriggersDestroy() throws Exception {
        BotClient observer = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "WatchBot2");
        BotClient leaver = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "LeaveBot2");
        try {
            BotSession observerSession = observer.getSession();
            BotSession leaverSession = leaver.getSession();
            assertTrue(observerSession.isLoginComplete(), "Observer login should complete");
            assertTrue(leaverSession.isLoginComplete(), "Leaver login should complete");

            int leaverEntityId = leaverSession.getEntityId();

            // Observer should see leaver via SpawnEntity
            Thread.sleep(500);
            assertTrue(observerSession.getSpawnedPlayers().size() >= 1,
                    "Observer should see leaver spawn");

            leaver.disconnect();
            leaver = null;
            Thread.sleep(200);

            assertTrue(observerSession.waitForDespawn(leaverEntityId, 5000),
                    "Observer should receive DestroyEntity for disconnected 1.20.2 player");
        } finally {
            observer.disconnect();
            if (leaver != null) leaver.disconnect();
        }
    }

    @Test
    void latestProtocolDisconnectTriggersDestroy() throws Exception {
        BotClient observer = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "WatchV774");
        BotClient leaver = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "LeaveV774");
        try {
            BotSession observerSession = observer.getSession();
            BotSession leaverSession = leaver.getSession();
            assertTrue(observerSession.isLoginComplete(), "Observer login should complete");
            assertTrue(leaverSession.isLoginComplete(), "Leaver login should complete");

            int leaverEntityId = leaverSession.getEntityId();

            Thread.sleep(500);
            assertTrue(observerSession.getSpawnedPlayers().size() >= 1,
                    "Observer should see leaver spawn");

            leaver.disconnect();
            leaver = null;
            Thread.sleep(200);

            assertTrue(observerSession.waitForDespawn(leaverEntityId, 5000),
                    "Observer should receive DestroyEntity for disconnected 1.21.11 player");
        } finally {
            observer.disconnect();
            if (leaver != null) leaver.disconnect();
        }
    }
}
