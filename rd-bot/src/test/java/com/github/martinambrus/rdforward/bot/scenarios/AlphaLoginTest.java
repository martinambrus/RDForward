package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.AddToInventoryPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.PlayerPositionAndLookS2CPacket;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that ALL Alpha protocol clients can successfully log in to the server.
 * Covers pre-rewrite (v13, v14), post-rewrite pre-1.2.0 (v1, v2),
 * and post-rewrite 1.2.0+ (v3-v6).
 *
 * Pre-1.2.0 versions (v1, v2, v13, v14) are kicked on first connect with a
 * TimSort JVM flag warning; TestServer handles this warmup automatically.
 */
class AlphaLoginTest {

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
            "ALPHA_1_0_15", // v13 (pre-rewrite SMP)
            "ALPHA_1_0_16", // v14 (pre-rewrite SMP)
            "ALPHA_1_0_17", // v1 (post-rewrite, no mapSeed)
            "ALPHA_1_1_0",  // v2 (post-rewrite, no mapSeed)
            "ALPHA_1_2_0",  // v3 (post-rewrite, with mapSeed)
            "ALPHA_1_2_2",  // v4
            "ALPHA_1_2_3",  // v5
            "ALPHA_1_2_5"   // v6
    })
    void alphaLoginSucceeds(ProtocolVersion version) throws Exception {
        BotClient bot = testServer.createBot(version, "Alpha_" + version.name());
        try {
            BotSession session = bot.getSession();

            assertTrue(session.isLoginComplete(),
                    version + ": Login should complete (offline mode)");
            assertTrue(session.getEntityId() > 0,
                    version + ": Entity ID should be assigned");
            assertTrue(session.getY() != 0,
                    version + ": Y position should be set (in the world)");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void alphaV6ReceivesCobblestone() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "CobbleBot");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            // Poll for AddToInventory packet with cobblestone (ID 4, count 64)
            boolean receivedCobble = false;
            long deadline = System.currentTimeMillis() + 5000;
            while (!receivedCobble && System.currentTimeMillis() < deadline) {
                for (Packet p : session.getReceivedPackets()) {
                    if (p instanceof AddToInventoryPacket inv) {
                        if (inv.getItemId() == 4 && inv.getCount() == 64) {
                            receivedCobble = true;
                            break;
                        }
                    }
                }
                if (!receivedCobble) Thread.sleep(50);
            }
            assertTrue(receivedCobble, "Should receive 64 cobblestone via AddToInventory");
        } finally {
            bot.disconnect();
        }
    }
}
