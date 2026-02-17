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
 * Tests that Release v39-v78 clients (which require encryption) can
 * successfully log in to the server in offline mode.
 *
 * Verifies: encryption handshake completes, login completes,
 * entity ID assigned, position received (player is "in the world").
 */
class ReleaseEncryptionLoginTest {

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
            "RELEASE_1_3_1", // v39 (first encrypted version)
            "RELEASE_1_4_2", // v47
            "RELEASE_1_4_4", // v49
            "RELEASE_1_4_6", // v51
            "RELEASE_1_5",   // v60
            "RELEASE_1_5_2", // v61
            "RELEASE_1_6_1", // v73 (float abilities, Entity Properties)
            "RELEASE_1_6_2", // v74
            "RELEASE_1_6_4"  // v78
    })
    void encryptedLoginSucceeds(ProtocolVersion version) throws Exception {
        BotClient bot = testServer.createBot(version, "Enc_" + version.name());
        try {
            BotSession session = bot.getSession();

            assertTrue(session.isLoginComplete(),
                    version + ": Encrypted login should complete (offline mode)");
            assertTrue(session.getEntityId() > 0,
                    version + ": Entity ID should be assigned");
            assertTrue(session.getY() != 0,
                    version + ": Y position should be set (in the world)");
        } finally {
            bot.disconnect();
        }
    }
}
