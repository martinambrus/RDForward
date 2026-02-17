package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotBedrockClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that a Bedrock bot can send and receive chat messages.
 */
class BedrockChatTest {

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
    void bedrockSendAndReceiveChat() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedChat1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Bedrock login should complete");

            session.sendChat("Hello from Bedrock");

            // Server echoes back as "<username>: message"
            String received = session.waitForChat("Hello from Bedrock", 3000);
            assertNotNull(received, "Bedrock bot should receive own chat echo");
        } finally {
            bot.disconnect();
        }
    }
}
