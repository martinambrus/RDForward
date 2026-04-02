package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Block placement and breaking tests for Classic protocol versions.
 * Each test uses unique coordinates to avoid conflicts when sharing
 * the same server instance.
 */
class ClassicBlockPlaceTest {

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

    // === Per-version placement tests (unique coordinates per test) ===

    @Test
    void classic016aCanPlaceBlock() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.CLASSIC_0_0_16A, "CPl16a");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());
            Thread.sleep(500);

            int bx = 30, by = 42, bz = 30;
            session.sendBlockPlace(bx, by, bz, 1, 4);

            int blockType = session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(blockType > 0, "Classic 0.0.16a should place block");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void classic020aCanPlaceBlock() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.CLASSIC_0_0_20A, "CPl20a");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());
            Thread.sleep(500);

            int bx = 31, by = 42, bz = 31;
            session.sendBlockPlace(bx, by, bz, 1, 4);

            int blockType = session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(blockType > 0, "Classic 0.0.20a should place block");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void classic030CanPlaceBlock() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.CLASSIC, "CPl30");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());
            Thread.sleep(500);

            int bx = 32, by = 42, bz = 32;
            session.sendBlockPlace(bx, by, bz, 1, 4);

            int blockType = session.waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(blockType > 0, "Classic 0.30 should place block");
        } finally {
            bot.disconnect();
        }
    }

    // === Per-version break tests (unique coordinates) ===

    @Test
    void classic016aCanBreakBlock() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.CLASSIC_0_0_16A, "CBr16a");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());
            Thread.sleep(500);

            int bx = 33, by = 42, bz = 33;
            session.sendDigging(0, bx, by, bz, 1);

            int blockType = session.waitForBlockChangeValue(bx, by, bz, 0, 3000);
            assertEquals(0, blockType, "Classic 0.0.16a broken block should become air");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void classic020aCanBreakBlock() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.CLASSIC_0_0_20A, "CBr20a");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());
            Thread.sleep(500);

            int bx = 34, by = 42, bz = 34;
            session.sendDigging(0, bx, by, bz, 1);

            int blockType = session.waitForBlockChangeValue(bx, by, bz, 0, 3000);
            assertEquals(0, blockType, "Classic 0.0.20a broken block should become air");
        } finally {
            bot.disconnect();
        }
    }

    @Test
    void classic030CanBreakBlock() throws Exception {
        BotClient bot = testServer.createBot(ProtocolVersion.CLASSIC, "CBr30");
        try {
            BotSession session = bot.getSession();
            assertTrue(session.isLoginComplete());
            Thread.sleep(500);

            int bx = 35, by = 42, bz = 35;
            session.sendDigging(0, bx, by, bz, 1);

            int blockType = session.waitForBlockChangeValue(bx, by, bz, 0, 3000);
            assertEquals(0, blockType, "Classic 0.30 broken block should become air");
        } finally {
            bot.disconnect();
        }
    }

    // === Cross-version block broadcast tests ===

    @Test
    void classicBlockPlaceBroadcastToOtherClassic() throws Exception {
        BotClient placer = testServer.createBot(ProtocolVersion.CLASSIC, "CPlBrd1");
        BotClient observer = testServer.createBot(ProtocolVersion.CLASSIC, "CObBrd1");
        try {
            assertTrue(placer.getSession().isLoginComplete());
            assertTrue(observer.getSession().isLoginComplete());
            Thread.sleep(500);

            int bx = 36, by = 42, bz = 36;
            placer.getSession().sendBlockPlace(bx, by, bz, 1, 4);

            int blockType = observer.getSession().waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(blockType > 0, "Observer should see placer's block change");
        } finally {
            placer.disconnect();
            observer.disconnect();
        }
    }

    @Test
    void classicBlockPlaceVisibleToAlpha() throws Exception {
        BotClient classic = testServer.createBot(ProtocolVersion.CLASSIC, "CPlAlp1");
        BotClient alpha = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AObAlp1");
        try {
            assertTrue(classic.getSession().isLoginComplete());
            assertTrue(alpha.getSession().isLoginComplete());
            Thread.sleep(500);

            int bx = 37, by = 42, bz = 37;
            classic.getSession().sendBlockPlace(bx, by, bz, 1, 4);

            int blockType = alpha.getSession().waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(blockType > 0, "Alpha should see Classic block placement");
        } finally {
            classic.disconnect();
            alpha.disconnect();
        }
    }

    @Test
    void alphaBlockPlaceVisibleToClassic() throws Exception {
        BotClient alpha = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "APlCls1");
        BotClient classic = testServer.createBot(ProtocolVersion.CLASSIC, "CObCls1");
        try {
            assertTrue(alpha.getSession().isLoginComplete());
            assertTrue(classic.getSession().isLoginComplete());
            Thread.sleep(500);

            int bx = 38, by = 42, bz = 38;
            alpha.getSession().sendBlockPlace(bx, by, bz, 1, 4);

            int blockType = classic.getSession().waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(blockType > 0, "Classic should see Alpha block placement");
        } finally {
            alpha.disconnect();
            classic.disconnect();
        }
    }

    @Test
    void classicBlockPlaceVisibleToNetty() throws Exception {
        BotClient classic = testServer.createBot(ProtocolVersion.CLASSIC, "CPlNet1");
        BotClient netty = testServer.createBot(ProtocolVersion.RELEASE_1_8, "NObNet1");
        try {
            assertTrue(classic.getSession().isLoginComplete());
            assertTrue(netty.getSession().isLoginComplete());
            Thread.sleep(500);

            int bx = 39, by = 42, bz = 39;
            classic.getSession().sendBlockPlace(bx, by, bz, 1, 4);

            int blockType = netty.getSession().waitForBlockChange(bx, by + 1, bz, 3000);
            assertTrue(blockType > 0, "Netty 1.8 should see Classic block placement");
        } finally {
            classic.disconnect();
            netty.disconnect();
        }
    }
}
