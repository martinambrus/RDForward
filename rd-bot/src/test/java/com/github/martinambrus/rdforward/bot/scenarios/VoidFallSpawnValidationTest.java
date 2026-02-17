package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that void fall respawn produces a valid spawn position:
 * correct Y, on ground, and correct block types.
 */
class VoidFallSpawnValidationTest {

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
    void voidFallRespawnLandsOnGround() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "VFGnd");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int prevCount = session.getPositionUpdateCount();

            // Fall into the void
            session.sendPosition(session.getX(), -20, session.getZ(),
                    session.getYaw(), session.getPitch());

            // Wait for server to teleport back
            assertTrue(session.waitForPositionUpdate(prevCount, 5000),
                    "Server should send position update after void fall");

            // Wait for chunk at respawn position
            int blockX = (int) Math.floor(session.getX());
            int blockZ = (int) Math.floor(session.getZ());
            assertTrue(session.waitForChunkAt(blockX, blockZ, 5000),
                    "Respawn chunk should be received");

            assertTrue(session.isOnGround(),
                    "Bot should be on ground after void fall respawn");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void voidFallRespawnYMatchesSpawnY() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "VFSpnY");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            double originalSpawnY = session.getSpawnY();
            int prevCount = session.getPositionUpdateCount();

            // Fall into the void
            session.sendPosition(session.getX(), -20, session.getZ(),
                    session.getYaw(), session.getPitch());

            // Wait for server to teleport back
            assertTrue(session.waitForPositionUpdate(prevCount, 5000),
                    "Server should send position update after void fall");

            assertEquals(originalSpawnY, session.getY(), 0.001,
                    "Respawn Y should match original spawn Y");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void voidFallRespawnBlockBelowIsSolid() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "VFBlk");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Login should complete");

            int prevCount = session.getPositionUpdateCount();

            // Fall into the void
            session.sendPosition(session.getX(), -20, session.getZ(),
                    session.getYaw(), session.getPitch());

            // Wait for server to teleport back
            assertTrue(session.waitForPositionUpdate(prevCount, 5000),
                    "Server should send position update after void fall");

            // Compute feet position from Alpha eye-level Y
            double feetY = session.getY() - (double) 1.62f;
            int blockX = (int) Math.floor(session.getX());
            int feetBlockY = (int) Math.floor(feetY);
            int blockZ = (int) Math.floor(session.getZ());

            // Wait for chunk at respawn position
            assertTrue(session.waitForChunkAt(blockX, blockZ, 5000),
                    "Respawn chunk should be received");

            assertEquals(0, session.getBlockAt(blockX, feetBlockY, blockZ),
                    "Block at feet after respawn should be air (0)");
            assertNotEquals(0, session.getBlockAt(blockX, feetBlockY - 1, blockZ),
                    "Block below feet after respawn should be solid (non-zero)");
        } finally {
            bot.disconnect();
        }
    }
}
