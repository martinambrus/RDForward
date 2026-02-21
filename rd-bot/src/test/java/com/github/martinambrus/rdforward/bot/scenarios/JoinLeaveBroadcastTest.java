package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that "X joined the game" and "X left the game" chat broadcasts
 * are sent to other connected players.
 */
class JoinLeaveBroadcastTest {

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
    void joinAndLeaveBroadcasts() throws Exception {
        BotClient observer = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "Observer");
        try {
            BotSession observerSession = observer.getSession();
            assertTrue(observerSession.isLoginComplete(), "Observer login should complete");

            // Second bot joins — observer should see join message
            BotClient joiner = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "JoinBot");
            BotSession joinerSession = joiner.getSession();
            assertTrue(joinerSession.isLoginComplete(), "JoinBot login should complete");

            String joinMsg = observerSession.waitForChat("JoinBot joined the game", 3000);
            assertNotNull(joinMsg, "Observer should receive join broadcast");

            // Second bot leaves — observer should see leave message.
            // channel.close() is async; give the server time to fire channelInactive.
            joiner.disconnect();
            Thread.sleep(200);

            String leaveMsg = observerSession.waitForChat("JoinBot left the game", 5000);
            assertNotNull(leaveMsg, "Observer should receive leave broadcast");
        } finally {
            observer.disconnect();
        }
    }

    @Test
    void modernNettyJoinAndLeaveBroadcasts() throws Exception {
        BotClient observer = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "Observer2");
        try {
            BotSession observerSession = observer.getSession();
            assertTrue(observerSession.isLoginComplete(), "Observer login should complete");

            BotClient joiner = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "JoinBot2");
            BotSession joinerSession = joiner.getSession();
            assertTrue(joinerSession.isLoginComplete(), "JoinBot2 login should complete");

            String joinMsg = observerSession.waitForChat("JoinBot2 joined the game", 3000);
            assertNotNull(joinMsg, "Observer should receive join broadcast");

            joiner.disconnect();
            Thread.sleep(200);

            String leaveMsg = observerSession.waitForChat("JoinBot2 left the game", 5000);
            assertNotNull(leaveMsg, "Observer should receive leave broadcast");
        } finally {
            observer.disconnect();
        }
    }

    @Test
    void crossVersionJoinBroadcast() throws Exception {
        // Alpha observer sees 1.21.11 player join/leave
        BotClient observer = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "Observer3");
        try {
            BotSession observerSession = observer.getSession();
            assertTrue(observerSession.isLoginComplete(), "Observer login should complete");

            BotClient joiner = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "JoinBot3");
            BotSession joinerSession = joiner.getSession();
            assertTrue(joinerSession.isLoginComplete(), "JoinBot3 login should complete");

            String joinMsg = observerSession.waitForChat("JoinBot3 joined the game", 3000);
            assertNotNull(joinMsg, "Alpha observer should see 1.21.11 player join");

            joiner.disconnect();
            Thread.sleep(200);

            String leaveMsg = observerSession.waitForChat("JoinBot3 left the game", 5000);
            assertNotNull(leaveMsg, "Alpha observer should see 1.21.11 player leave");
        } finally {
            observer.disconnect();
        }
    }
}
