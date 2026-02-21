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

    @Test
    void v340AndBetaCanChat() throws Exception {
        BotClient v340Bot = testServer.createBot(ProtocolVersion.RELEASE_1_12_2, "V340Chatter");
        BotClient betaBot = testServer.createBot(ProtocolVersion.BETA_1_8, "BetaChat2");
        try {
            BotSession v340Session = v340Bot.getSession();
            BotSession betaSession = betaBot.getSession();
            assertTrue(v340Session.isLoginComplete(), "V340 login should complete");
            assertTrue(betaSession.isLoginComplete(), "Beta login should complete");

            // V340 sends chat, Beta should receive it
            v340Session.sendChat("Hello from V340");
            String received = betaSession.waitForChat("Hello from V340", 3000);
            assertNotNull(received, "Beta bot should receive V340's chat message");

            // Beta sends chat, V340 should receive it
            betaSession.sendChat("Hello from Beta2");
            received = v340Session.waitForChat("Hello from Beta2", 3000);
            assertNotNull(received, "V340 bot should receive Beta's chat message");
        } finally {
            v340Bot.disconnect();
            betaBot.disconnect();
        }
    }

    @Test
    void v393AndAlphaCanChat() throws Exception {
        BotClient v393Bot = testServer.createBot(ProtocolVersion.RELEASE_1_13, "V393Chat");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaChat3");
        try {
            BotSession v393Session = v393Bot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            assertTrue(v393Session.isLoginComplete(), "1.13 login should complete");
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");

            v393Session.sendChat("Hello from 1.13");
            String received = alphaSession.waitForChat("Hello from 1.13", 3000);
            assertNotNull(received, "Alpha bot should receive 1.13's chat message");

            alphaSession.sendChat("Alpha reply to 1.13");
            received = v393Session.waitForChat("Alpha reply to 1.13", 3000);
            assertNotNull(received, "1.13 bot should receive Alpha's chat message");
        } finally {
            v393Bot.disconnect();
            alphaBot.disconnect();
        }
    }

    @Test
    void v764AndV340CanChat() throws Exception {
        BotClient v764Bot = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "V764Chat");
        BotClient v340Bot = testServer.createBot(ProtocolVersion.RELEASE_1_12_2, "V340Chat2");
        try {
            BotSession v764Session = v764Bot.getSession();
            BotSession v340Session = v340Bot.getSession();
            assertTrue(v764Session.isLoginComplete(), "1.20.2 login should complete");
            assertTrue(v340Session.isLoginComplete(), "1.12.2 login should complete");

            v764Session.sendChat("Hello from 1.20.2");
            String received = v340Session.waitForChat("Hello from 1.20.2", 3000);
            assertNotNull(received, "1.12.2 bot should receive 1.20.2's chat message");

            v340Session.sendChat("Hello from 1.12.2");
            received = v764Session.waitForChat("Hello from 1.12.2", 3000);
            assertNotNull(received, "1.20.2 bot should receive 1.12.2's chat message");
        } finally {
            v764Bot.disconnect();
            v340Bot.disconnect();
        }
    }

    @Test
    void v735AndV47CanChat() throws Exception {
        BotClient v735Bot = testServer.createBot(ProtocolVersion.RELEASE_1_16, "V735Chat");
        BotClient v47Bot = testServer.createBot(ProtocolVersion.RELEASE_1_8, "V47Chat");
        try {
            BotSession v735Session = v735Bot.getSession();
            BotSession v47Session = v47Bot.getSession();
            assertTrue(v735Session.isLoginComplete(), "1.16 login should complete");
            assertTrue(v47Session.isLoginComplete(), "1.8 login should complete");

            v735Session.sendChat("Hello from 1.16");
            String received = v47Session.waitForChat("Hello from 1.16", 3000);
            assertNotNull(received, "V47 bot should receive 1.16's chat message");

            v47Session.sendChat("Hello from 1.8");
            received = v735Session.waitForChat("Hello from 1.8", 3000);
            assertNotNull(received, "1.16 bot should receive V47's chat message");
        } finally {
            v735Bot.disconnect();
            v47Bot.disconnect();
        }
    }

    @Test
    void v774AndAlphaCanChat() throws Exception {
        BotClient v774Bot = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "V774Chat");
        BotClient alphaBot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaChat4");
        try {
            BotSession v774Session = v774Bot.getSession();
            BotSession alphaSession = alphaBot.getSession();
            assertTrue(v774Session.isLoginComplete(), "1.21.11 login should complete");
            assertTrue(alphaSession.isLoginComplete(), "Alpha login should complete");

            v774Session.sendChat("Hello from 1.21.11");
            String received = alphaSession.waitForChat("Hello from 1.21.11", 3000);
            assertNotNull(received, "Alpha bot should receive 1.21.11's chat message");

            alphaSession.sendChat("Alpha reply to 1.21.11");
            received = v774Session.waitForChat("Alpha reply to 1.21.11", 3000);
            assertNotNull(received, "1.21.11 bot should receive Alpha's chat message");
        } finally {
            v774Bot.disconnect();
            alphaBot.disconnect();
        }
    }
}
