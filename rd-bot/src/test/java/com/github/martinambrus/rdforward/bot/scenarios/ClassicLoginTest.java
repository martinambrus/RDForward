package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Login tests for Classic protocol versions:
 * - Classic 0.0.16a (protocol 3)
 * - Classic 0.0.20a (protocol 6)
 * - Classic 0.30 (protocol 7, standard Classic)
 *
 * Note: Classic 0.0.15a (protocol -1) is excluded because it binds a
 * fixed port (5565) which conflicts in the test environment.
 */
class ClassicLoginTest {

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

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = ProtocolVersion.class, names = {
            "CLASSIC_0_0_16A",
            "CLASSIC_0_0_20A",
            "CLASSIC"
    })
    void classicLoginSucceeds(ProtocolVersion version) throws Exception {
        BotClient bot = testServer.createBot(version, "CLogin_" + version.name());
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(),
                    version + ": Login should complete (level data received)");
            assertTrue(session.getY() != 0,
                    version + ": Y position should be set after spawn");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void twoClassicClientsCanConnectSimultaneously() throws Exception {
        BotClient bot1 = testServer.createBot(ProtocolVersion.CLASSIC, "ClassicA");
        BotClient bot2 = testServer.createBot(ProtocolVersion.CLASSIC, "ClassicB");
        try {
            assertTrue(bot1.getSession().isLoginComplete(), "First client login");
            assertTrue(bot2.getSession().isLoginComplete(), "Second client login");
            assertTrue(bot1.getSession().isConnected(), "First still connected");
            assertTrue(bot2.getSession().isConnected(), "Second still connected");
        } finally {
            bot1.disconnect();
            bot2.disconnect();
        }
    }

    @Test
    void classicAndAlphaCanConnectSimultaneously() throws Exception {
        BotClient classic = testServer.createBot(ProtocolVersion.CLASSIC, "ClassicC");
        BotClient alpha = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AlphaC");
        try {
            assertTrue(classic.getSession().isLoginComplete(),
                    "Classic login should complete");
            assertTrue(alpha.getSession().isLoginComplete(),
                    "Alpha login should complete alongside Classic");
        } finally {
            classic.disconnect();
            alpha.disconnect();
        }
    }
}
