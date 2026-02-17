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
}
