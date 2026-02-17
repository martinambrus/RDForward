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
 * Tests that ALL Beta (v7-v17) and Release pre-encryption (v21-v29) clients
 * can successfully log in to the server in offline mode.
 *
 * Verifies login completion, entity ID assignment, position reception
 * (confirming the player is "in the world" with chunks loaded).
 */
class BetaLoginTest {

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
            "BETA_1_0",     // v7
            "BETA_1_2",     // v8
            "BETA_1_3",     // v9
            "BETA_1_4",     // v10
            "BETA_1_5",     // v11 (String16 transition)
            "BETA_1_6",     // v12
            "BETA_1_7",     // v13
            "BETA_1_7_3",   // v14
            "BETA_1_8",     // v17 (KeepAlive int, creative mode)
            "BETA_1_9_PRE5", // v21 (item NBT)
            "RELEASE_1_0",  // v22
            "RELEASE_1_1",  // v23 (levelType in Login)
            "RELEASE_1_2_1", // v28 (section-based chunks)
            "RELEASE_1_2_4"  // v29
    })
    void loginSucceeds(ProtocolVersion version) throws Exception {
        BotClient bot = testServer.createBot(version, "Beta_" + version.name());
        try {
            BotSession session = bot.getSession();

            assertTrue(session.isLoginComplete(),
                    version + ": Login should complete (offline mode)");
            assertTrue(session.getEntityId() > 0,
                    version + ": Entity ID should be assigned");

            // Position should be set (confirms we received chunks and are "in the world")
            assertTrue(session.getY() != 0,
                    version + ": Y position should be set (in the world)");
        } finally {
            bot.disconnect();
        }
    }
}
