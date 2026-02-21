package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotBedrockClient;
import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended cross-version tests involving Bedrock players.
 *
 * BedrockCrossVersionTest covers Bedrock <-> Alpha and Bedrock <-> 1.8.
 * This class extends coverage to additional protocol pairs:
 * - Chat: Bedrock <-> Beta, 1.13, 1.20.2, 1.21.11
 * - Join/leave broadcasts: Bedrock with modern Netty observers
 */
class BedrockCrossVersionExtendedTest {

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

    // ---- Cross-version chat pairs ----

    @Test
    void bedrockAndBetaCanChat() throws Exception {
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedCB1");
        BotClient betaBot = testServer.createBot(ProtocolVersion.BETA_1_7_3, "BetaCB1");
        try {
            BotSession bedrockSession = bedrockBot.getSession();
            BotSession betaSession = betaBot.getSession();
            assertTrue(bedrockSession.isLoginComplete());
            assertTrue(betaSession.isLoginComplete());

            bedrockSession.sendChat("hello from bedrock");
            String betaReceived = betaSession.waitForChat("hello from bedrock", 3000);
            assertNotNull(betaReceived, "Beta should receive Bedrock's chat");

            betaSession.sendChat("hello from beta");
            String bedrockReceived = bedrockSession.waitForChat("hello from beta", 3000);
            assertNotNull(bedrockReceived, "Bedrock should receive Beta's chat");
        } finally {
            bedrockBot.disconnect();
            betaBot.disconnect();
        }
    }

    @Test
    void bedrockAndV393CanChat() throws Exception {
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedC393");
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_13, "Nty393");
        try {
            BotSession bedrockSession = bedrockBot.getSession();
            BotSession nettySession = nettyBot.getSession();
            assertTrue(bedrockSession.isLoginComplete());
            assertTrue(nettySession.isLoginComplete());

            bedrockSession.sendChat("msg to 1.13");
            String received = nettySession.waitForChat("msg to 1.13", 3000);
            assertNotNull(received, "1.13 should receive Bedrock's chat");

            nettySession.sendChat("msg from 1.13");
            String bedrockReceived = bedrockSession.waitForChat("msg from 1.13", 3000);
            assertNotNull(bedrockReceived, "Bedrock should receive 1.13's chat");
        } finally {
            bedrockBot.disconnect();
            nettyBot.disconnect();
        }
    }

    @Test
    void bedrockAndV764CanChat() throws Exception {
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedC764");
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "Nty764");
        try {
            BotSession bedrockSession = bedrockBot.getSession();
            BotSession nettySession = nettyBot.getSession();
            assertTrue(bedrockSession.isLoginComplete());
            assertTrue(nettySession.isLoginComplete());

            bedrockSession.sendChat("msg to 1.20.2");
            String received = nettySession.waitForChat("msg to 1.20.2", 3000);
            assertNotNull(received, "1.20.2 should receive Bedrock's chat");

            nettySession.sendChat("msg from 1.20.2");
            String bedrockReceived = bedrockSession.waitForChat("msg from 1.20.2", 3000);
            assertNotNull(bedrockReceived, "Bedrock should receive 1.20.2's chat");
        } finally {
            bedrockBot.disconnect();
            nettyBot.disconnect();
        }
    }

    @Test
    void bedrockAndV774CanChat() throws Exception {
        BotBedrockClient bedrockBot = testServer.createBedrockBot("BedC774");
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "Nty774");
        try {
            BotSession bedrockSession = bedrockBot.getSession();
            BotSession nettySession = nettyBot.getSession();
            assertTrue(bedrockSession.isLoginComplete());
            assertTrue(nettySession.isLoginComplete());

            bedrockSession.sendChat("msg to 1.21.11");
            String received = nettySession.waitForChat("msg to 1.21.11", 3000);
            assertNotNull(received, "1.21.11 should receive Bedrock's chat");

            nettySession.sendChat("msg from 1.21.11");
            String bedrockReceived = bedrockSession.waitForChat("msg from 1.21.11", 3000);
            assertNotNull(bedrockReceived, "Bedrock should receive 1.21.11's chat");
        } finally {
            bedrockBot.disconnect();
            nettyBot.disconnect();
        }
    }

    // ---- Join/leave broadcasts with modern Netty ----

    @Test
    void bedrockObservesModernNettyJoinLeave() throws Exception {
        BotBedrockClient observer = testServer.createBedrockBot("BedJLO1");
        try {
            BotSession observerSession = observer.getSession();
            assertTrue(observerSession.isLoginComplete());

            BotClient joiner = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "ModJnr1");
            assertTrue(joiner.getSession().isLoginComplete());

            String joinMsg = observerSession.waitForChat("ModJnr1 joined the game", 3000);
            assertNotNull(joinMsg, "Bedrock should see modern Netty player join");

            joiner.disconnect();
            Thread.sleep(200);

            String leaveMsg = observerSession.waitForChat("ModJnr1 left the game", 5000);
            assertNotNull(leaveMsg, "Bedrock should see modern Netty player leave");
        } finally {
            observer.disconnect();
        }
    }

    @Test
    void modernNettyObservesBedrockJoinLeave() throws Exception {
        BotClient observer = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "ModObs1");
        try {
            BotSession observerSession = observer.getSession();
            assertTrue(observerSession.isLoginComplete());

            BotBedrockClient joiner = testServer.createBedrockBot("BedJnr1");
            assertTrue(joiner.getSession().isLoginComplete());

            String joinMsg = observerSession.waitForChat("BedJnr1 joined the game", 3000);
            assertNotNull(joinMsg, "Modern Netty should see Bedrock player join");

            joiner.disconnect();
            Thread.sleep(200);

            String leaveMsg = observerSession.waitForChat("BedJnr1 left the game", 5000);
            assertNotNull(leaveMsg, "Modern Netty should see Bedrock player leave");
        } finally {
            observer.disconnect();
        }
    }
}
