package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that two bots can exchange chat messages.
 * Both bots connect, one sends a message, the other should receive it.
 */
class ChatTest {

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
    void botsCanExchangeChat() throws Exception {
        BotClient bot1 = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "ChatBot1");
        BotClient bot2 = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "ChatBot2");
        try {
            BotSession session1 = bot1.getSession();
            BotSession session2 = bot2.getSession();
            assertTrue(session1.isLoginComplete(), "Bot1 login should complete");
            assertTrue(session2.isLoginComplete(), "Bot2 login should complete");

            // Bot1 sends a chat message
            session1.sendChat("Hello from Bot1");

            // Bot2 should receive it (server prefixes with "<username> ")
            String received = session2.waitForChat("Hello from Bot1", 3000);
            assertNotNull(received, "Bot2 should receive chat from Bot1");

            // Bot2 sends a reply
            session2.sendChat("Reply from Bot2");

            // Bot1 should receive it
            String reply = session1.waitForChat("Reply from Bot2", 3000);
            assertNotNull(reply, "Bot1 should receive reply from Bot2");
        } finally {
            bot1.disconnect();
            bot2.disconnect();
        }
    }

    @Test
    void nettyBotsCanExchangeChat() throws Exception {
        BotClient bot1 = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "NettyChatA");
        BotClient bot2 = testServer.createBot(ProtocolVersion.RELEASE_1_20_2, "NettyChatB");
        try {
            BotSession session1 = bot1.getSession();
            BotSession session2 = bot2.getSession();
            assertTrue(session1.isLoginComplete(), "Bot1 login should complete");
            assertTrue(session2.isLoginComplete(), "Bot2 login should complete");

            session1.sendChat("Hello from NettyChatA");
            String received = session2.waitForChat("Hello from NettyChatA", 3000);
            assertNotNull(received, "Bot2 should receive chat from Bot1");

            session2.sendChat("Reply from NettyChatB");
            String reply = session1.waitForChat("Reply from NettyChatB", 3000);
            assertNotNull(reply, "Bot1 should receive reply from Bot2");
        } finally {
            bot1.disconnect();
            bot2.disconnect();
        }
    }

    @Test
    void betaBotsCanExchangeChat() throws Exception {
        BotClient bot1 = testServer.createBot(ProtocolVersion.BETA_1_7_3, "BetaChatA");
        BotClient bot2 = testServer.createBot(ProtocolVersion.BETA_1_7_3, "BetaChatB");
        try {
            BotSession session1 = bot1.getSession();
            BotSession session2 = bot2.getSession();
            assertTrue(session1.isLoginComplete(), "Bot1 login should complete");
            assertTrue(session2.isLoginComplete(), "Bot2 login should complete");

            session1.sendChat("Hello from BetaChatA");
            String received = session2.waitForChat("Hello from BetaChatA", 3000);
            assertNotNull(received, "Bot2 should receive chat from Bot1");

            session2.sendChat("Reply from BetaChatB");
            String reply = session1.waitForChat("Reply from BetaChatB", 3000);
            assertNotNull(reply, "Bot1 should receive reply from Bot2");
        } finally {
            bot1.disconnect();
            bot2.disconnect();
        }
    }

    @Test
    void preNettyReleaseBotsCanExchangeChat() throws Exception {
        BotClient bot1 = testServer.createBot(ProtocolVersion.RELEASE_1_5, "PreNtChtA");
        BotClient bot2 = testServer.createBot(ProtocolVersion.RELEASE_1_5, "PreNtChtB");
        try {
            BotSession session1 = bot1.getSession();
            BotSession session2 = bot2.getSession();
            assertTrue(session1.isLoginComplete(), "Bot1 login should complete");
            assertTrue(session2.isLoginComplete(), "Bot2 login should complete");

            session1.sendChat("Hello from PreNtChtA");
            String received = session2.waitForChat("Hello from PreNtChtA", 3000);
            assertNotNull(received, "Bot2 should receive chat from Bot1");

            session2.sendChat("Reply from PreNtChtB");
            String reply = session1.waitForChat("Reply from PreNtChtB", 3000);
            assertNotNull(reply, "Bot1 should receive reply from Bot2");
        } finally {
            bot1.disconnect();
            bot2.disconnect();
        }
    }

    @Test
    void nettyV47BotsCanExchangeChat() throws Exception {
        BotClient bot1 = testServer.createBot(ProtocolVersion.RELEASE_1_8, "V47ChatA");
        BotClient bot2 = testServer.createBot(ProtocolVersion.RELEASE_1_8, "V47ChatB");
        try {
            BotSession session1 = bot1.getSession();
            BotSession session2 = bot2.getSession();
            assertTrue(session1.isLoginComplete(), "Bot1 login should complete");
            assertTrue(session2.isLoginComplete(), "Bot2 login should complete");

            session1.sendChat("Hello from V47ChatA");
            String received = session2.waitForChat("Hello from V47ChatA", 3000);
            assertNotNull(received, "Bot2 should receive chat from Bot1");

            session2.sendChat("Reply from V47ChatB");
            String reply = session1.waitForChat("Reply from V47ChatB", 3000);
            assertNotNull(reply, "Bot1 should receive reply from Bot2");
        } finally {
            bot1.disconnect();
            bot2.disconnect();
        }
    }

    @Test
    void v393BotsCanExchangeChat() throws Exception {
        BotClient bot1 = testServer.createBot(ProtocolVersion.RELEASE_1_13, "V393ChtA");
        BotClient bot2 = testServer.createBot(ProtocolVersion.RELEASE_1_13, "V393ChtB");
        try {
            BotSession session1 = bot1.getSession();
            BotSession session2 = bot2.getSession();
            assertTrue(session1.isLoginComplete(), "Bot1 login should complete");
            assertTrue(session2.isLoginComplete(), "Bot2 login should complete");

            session1.sendChat("Hello from V393ChtA");
            String received = session2.waitForChat("Hello from V393ChtA", 3000);
            assertNotNull(received, "Bot2 should receive chat from Bot1");

            session2.sendChat("Reply from V393ChtB");
            String reply = session1.waitForChat("Reply from V393ChtB", 3000);
            assertNotNull(reply, "Bot1 should receive reply from Bot2");
        } finally {
            bot1.disconnect();
            bot2.disconnect();
        }
    }

    @Test
    void v735BotsCanExchangeChat() throws Exception {
        BotClient bot1 = testServer.createBot(ProtocolVersion.RELEASE_1_16, "V735ChtA");
        BotClient bot2 = testServer.createBot(ProtocolVersion.RELEASE_1_16, "V735ChtB");
        try {
            BotSession session1 = bot1.getSession();
            BotSession session2 = bot2.getSession();
            assertTrue(session1.isLoginComplete(), "Bot1 login should complete");
            assertTrue(session2.isLoginComplete(), "Bot2 login should complete");

            session1.sendChat("Hello from V735ChtA");
            String received = session2.waitForChat("Hello from V735ChtA", 3000);
            assertNotNull(received, "Bot2 should receive chat from Bot1");

            session2.sendChat("Reply from V735ChtB");
            String reply = session1.waitForChat("Reply from V735ChtB", 3000);
            assertNotNull(reply, "Bot1 should receive reply from Bot2");
        } finally {
            bot1.disconnect();
            bot2.disconnect();
        }
    }

    @Test
    void latestProtocolBotsCanExchangeChat() throws Exception {
        BotClient bot1 = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "LateChatA");
        BotClient bot2 = testServer.createBot(ProtocolVersion.RELEASE_1_21_11, "LateChatB");
        try {
            BotSession session1 = bot1.getSession();
            BotSession session2 = bot2.getSession();
            assertTrue(session1.isLoginComplete(), "Bot1 login should complete");
            assertTrue(session2.isLoginComplete(), "Bot2 login should complete");

            session1.sendChat("Latest protocol chat");
            String received = session2.waitForChat("Latest protocol chat", 3000);
            assertNotNull(received, "Bot2 should receive chat from Bot1 on latest protocol");

            session2.sendChat("Latest reply");
            String reply = session1.waitForChat("Latest reply", 3000);
            assertNotNull(reply, "Bot1 should receive reply from Bot2 on latest protocol");
        } finally {
            bot1.disconnect();
            bot2.disconnect();
        }
    }
}
