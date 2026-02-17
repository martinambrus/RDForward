package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotBedrockClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Bedrock block placement and breaking via InventoryTransactionPacket
 * and PlayerActionPacket.
 */
class BedrockBlockTest {

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
    void bedrockBlockPlacement() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedPlace1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Bedrock login should complete");

            Thread.sleep(500);

            // Place on surface top face (y=42, face=1) -> block at y=43
            int px = 70, py = 42, pz = 70;
            session.sendBlockPlace(px, py, pz, 1, 4);

            int blockType = session.waitForBlockChange(px, py + 1, pz, 3000);
            assertTrue(blockType > 0,
                    "Should receive UpdateBlockPacket with non-zero runtime ID (got " + blockType + ")");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void bedrockBlockBreaking() throws Exception {
        BotBedrockClient bot = testServer.createBedrockBot("BedBreak1");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete(), "Bedrock login should complete");

            Thread.sleep(500);

            // Place a block first, then break it
            int bx = 72, by = 42, bz = 72;
            session.sendBlockPlace(bx, by, bz, 1, 4);

            int placed = session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(placed > 0, "Block should be placed first");

            // Clear the recorded block change so we can detect the break's UpdateBlockPacket
            session.getBlockChanges().remove(
                    packCoords(bx, by + 1, bz));

            // Break the placed block
            session.sendDigging(0, bx, by + 1, bz, 1);

            // Wait for the break confirmation — Bedrock uses palette runtime IDs,
            // so air's runtime ID may not be 0. Just verify we get a new UpdateBlockPacket
            // with a value different from the placed block.
            int broken = session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(broken >= 0, "Should receive UpdateBlockPacket for break");
            assertNotEquals(placed, broken,
                    "Break runtime ID should differ from placement runtime ID");
        } finally {
            bot.disconnect();
        }
    }

    private static long packCoords(int x, int y, int z) {
        return ((long) x & 0xFFFFFFFFL) << 32 | ((long) y & 0xFFL) << 24 | ((long) z & 0xFFFFFFL);
    }
}
