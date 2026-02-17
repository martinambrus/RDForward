package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that pre-1.0.17 Alpha clients (v13/v14) receive 35 cooked porkchops
 * on login, while post-1.0.17 clients do not. The server sends porkchops
 * to pre-rewrite clients since they lack UpdateHealthPacket and handle
 * fall damage client-side.
 */
class PorkchopInventoryTest {

    private static final int COOKED_PORKCHOP = 320;
    private static final int COBBLESTONE = 4;

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
    void alphaV14ReceivesPorkchopsOnLogin() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_0_16, "PorkV14");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForReceivedItemTotal(COOKED_PORKCHOP, 35, 5000),
                    "Should receive at least 35 porkchops");
            assertEquals(35, session.getReceivedItemTotal(COOKED_PORKCHOP),
                    "Should receive exactly 35 porkchops");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void alphaV14ReceivesCobblestoneAndPorkchops() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_0_16, "PorkCobV14");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            assertTrue(session.waitForReceivedItemTotal(COBBLESTONE, 64, 5000),
                    "Should receive 64 cobblestone");
            assertTrue(session.waitForReceivedItemTotal(COOKED_PORKCHOP, 35, 5000),
                    "Should receive 35 porkchops");
            assertEquals(64, session.getReceivedItemTotal(COBBLESTONE),
                    "Cobblestone total should be 64");
            assertEquals(35, session.getReceivedItemTotal(COOKED_PORKCHOP),
                    "Porkchop total should be 35");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void alphaV6DoesNotReceivePorkchops() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "NoPorkV6");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Wait for cobblestone to ensure login inventory has been sent
            assertTrue(session.waitForReceivedItemTotal(COBBLESTONE, 64, 5000),
                    "Should receive cobblestone");

            // Brief extra wait to ensure no late porkchop packets
            Thread.sleep(500);
            assertEquals(0, session.getReceivedItemTotal(COOKED_PORKCHOP),
                    "v6 client should NOT receive porkchops");
        } finally {
            bot.disconnect();
        }
    }
}
