package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that bots from different protocol families can exchange chat messages.
 * Verifies the server's cross-protocol chat translation works correctly.
 */
class CrossVersionChatTest {

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
    void alphaAndNettyCanChat() throws Exception {
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaChatter");
        BotClient nettyBot = testServer.createBot(ProtocolVersion.RELEASE_1_7_6, "NettyChatter");
        try {
            BotSession alphaSession = alphaBot.getSession();
            BotSession nettySession = nettyBot.getSession();
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");
            assertTrue(nettySession.isLoginComplete(), "Netty login should complete");

            // Alpha sends chat, Netty should receive it
            alphaSession.sendChat("Hello from Alpha");
            String received = nettySession.waitForChat("Hello from Alpha", 3000);
            assertNotNull(received, "Netty bot should receive Alpha's chat message");

            // Netty sends chat, Alpha should receive it
            nettySession.sendChat("Hello from Netty");
            received = alphaSession.waitForChat("Hello from Netty", 3000);
            assertNotNull(received, "Alpha bot should receive Netty's chat message");
        } finally {
            alphaBot.disconnect();
            nettyBot.disconnect();
        }
    }

    @Test
    void betaAndEncryptedReleaseCanChat() throws Exception {
        BotClient betaBot = testServer.createBot(ProtocolVersion.BETA_1_8, "BetaChatter");
        BotClient releaseBot = testServer.createBot(ProtocolVersion.RELEASE_1_5, "ReleaseChatter");
        try {
            BotSession betaSession = betaBot.getSession();
            BotSession releaseSession = releaseBot.getSession();
            assertTrue(betaSession.isLoginComplete(), "Beta login should complete");
            assertTrue(releaseSession.isLoginComplete(), "Release login should complete");

            // Beta sends, Release receives
            betaSession.sendChat("Beta says hi");
            String received = releaseSession.waitForChat("Beta says hi", 3000);
            assertNotNull(received, "Release bot should receive Beta's message");

            // Release sends, Beta receives
            releaseSession.sendChat("Release replies");
            received = betaSession.waitForChat("Release replies", 3000);
            assertNotNull(received, "Beta bot should receive Release's message");
        } finally {
            betaBot.disconnect();
            releaseBot.disconnect();
        }
    }

    @Test
    void nettyV109AndAlphaCanChat() throws Exception {
        BotClient v109Bot = testServer.createBot(ProtocolVersion.RELEASE_1_9_4, "V109Chatter");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaChat2");
        try {
            BotSession v109Session = v109Bot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            assertTrue(v109Session.isLoginComplete(), "V109 login should complete");
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");

            // V109 sends chat, Alpha should receive it
            v109Session.sendChat("Hello from V109");
            String received = alphaSession.waitForChat("Hello from V109", 3000);
            assertNotNull(received, "Alpha bot should receive V109's chat message");

            // Alpha sends chat, V109 should receive it
            alphaSession.sendChat("Hello from Alpha2");
            received = v109Session.waitForChat("Hello from Alpha2", 3000);
            assertNotNull(received, "V109 bot should receive Alpha's chat message");
        } finally {
            v109Bot.disconnect();
            alphaBot.disconnect();
        }
    }
}
