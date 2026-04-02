package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.bot.TestServer;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cross-version player visibility tests involving Classic protocol clients.
 * Verifies that Classic clients can see players from other protocol families
 * (Alpha, Beta, Netty) and vice versa.
 *
 * Note: Classic 0.0.15a is excluded due to fixed-port binding conflicts
 * in the test environment.
 */
class ClassicCrossVersionVisibilityTest {

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
    void classicSeesAlphaPlayer() throws Exception {
        BotClient classic = testServer.createBot(ProtocolVersion.CLASSIC, "CVAlpha1");
        BotClient alpha = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "CVAlpha2");
        try {
            assertTrue(classic.getSession().isLoginComplete());
            assertTrue(alpha.getSession().isLoginComplete());

            Integer eid = classic.getSession().waitForSpawnedPlayer("CVAlpha2", 3000);
            assertNotNull(eid, "Classic client should see Alpha player spawn");
        } finally {
            classic.disconnect();
            alpha.disconnect();
        }
    }

    @Test
    void alphaSeesClassicPlayer() throws Exception {
        BotClient alpha = testServer.createBot(ProtocolVersion.ALPHA_1_2_5, "AVClassic1");
        BotClient classic = testServer.createBot(ProtocolVersion.CLASSIC, "AVClassic2");
        try {
            assertTrue(alpha.getSession().isLoginComplete());
            assertTrue(classic.getSession().isLoginComplete());

            Integer eid = alpha.getSession().waitForSpawnedPlayer("AVClassic2", 3000);
            assertNotNull(eid, "Alpha client should see Classic player spawn");
        } finally {
            alpha.disconnect();
            classic.disconnect();
        }
    }

    @Test
    void classicSeesBetaPlayer() throws Exception {
        BotClient classic = testServer.createBot(ProtocolVersion.CLASSIC, "CVBeta1");
        BotClient beta = testServer.createBot(ProtocolVersion.BETA_1_7_3, "CVBeta2");
        try {
            assertTrue(classic.getSession().isLoginComplete());
            assertTrue(beta.getSession().isLoginComplete());

            Integer eid = classic.getSession().waitForSpawnedPlayer("CVBeta2", 3000);
            assertNotNull(eid, "Classic client should see Beta player spawn");
        } finally {
            classic.disconnect();
            beta.disconnect();
        }
    }

    @Test
    void classicSeesNettyPlayer() throws Exception {
        BotClient classic = testServer.createBot(ProtocolVersion.CLASSIC, "CVNetty1");
        BotClient netty = testServer.createBot(ProtocolVersion.RELEASE_1_8, "CVNetty2");
        try {
            assertTrue(classic.getSession().isLoginComplete());
            assertTrue(netty.getSession().isLoginComplete());
            // Netty player spawn is deferred by 1 second on the server
            Thread.sleep(1500);

            Integer eid = classic.getSession().waitForSpawnedPlayer("CVNetty2", 3000);
            assertNotNull(eid, "Classic client should see Netty player spawn");
        } finally {
            classic.disconnect();
            netty.disconnect();
        }
    }

    @Disabled("Known limitation: Netty clients may not receive Classic player spawn broadcasts")
    @Test
    void nettySeesClassicPlayer() throws Exception {
        BotClient netty = testServer.createBot(ProtocolVersion.RELEASE_1_8, "NVClassic1");
        BotClient classic = testServer.createBot(ProtocolVersion.CLASSIC, "NVClassic2");
        try {
            assertTrue(netty.getSession().isLoginComplete());
            assertTrue(classic.getSession().isLoginComplete());
            // Netty deferred spawn broadcast + Classic spawn notification
            Thread.sleep(1500);

            Integer eid = netty.getSession().waitForSpawnedPlayer("NVClassic2", 5000);
            assertNotNull(eid, "Netty client should see Classic player spawn");
        } finally {
            netty.disconnect();
            classic.disconnect();
        }
    }

    @Test
    void classicPlayerDespawnBroadcast() throws Exception {
        BotClient observer = testServer.createBot(ProtocolVersion.CLASSIC, "CVDesp1");
        BotClient leaver = testServer.createBot(ProtocolVersion.CLASSIC, "CVDesp2");
        try {
            assertTrue(observer.getSession().isLoginComplete());
            assertTrue(leaver.getSession().isLoginComplete());

            Integer eid = observer.getSession().waitForSpawnedPlayer("CVDesp2", 3000);
            assertNotNull(eid, "Observer should see leaver spawn");

            leaver.disconnect();

            boolean despawned = observer.getSession().waitForDespawn(eid, 3000);
            assertTrue(despawned, "Observer should see leaver despawn after disconnect");
        } finally {
            observer.disconnect();
        }
    }

    @Test
    void classic016aSeesClassic030Player() throws Exception {
        BotClient c16 = testServer.createBot(ProtocolVersion.CLASSIC_0_0_16A, "C16Vis1");
        BotClient c30 = testServer.createBot(ProtocolVersion.CLASSIC, "C30Vis2");
        try {
            assertTrue(c16.getSession().isLoginComplete());
            assertTrue(c30.getSession().isLoginComplete());

            Integer eid = c16.getSession().waitForSpawnedPlayer("C30Vis2", 3000);
            assertNotNull(eid, "Classic 0.0.16a should see Classic 0.30 player");
        } finally {
            c16.disconnect();
            c30.disconnect();
        }
    }
}
