package com.github.martinambrus.rdforward.bot.scenarios;

import com.github.martinambrus.rdforward.bot.BotClient;
import com.github.martinambrus.rdforward.bot.BotSession;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Semi-automated "Bedrock visibility parade" test.
 *
 * Connects one bot per supported Java protocol version (plus RubyDung and
 * Classic) sequentially. Each bot:
 * 1. Logs in with a version-specific username
 * 2. Sends a chat message: "Hi from &lt;version&gt;"
 * 3. Places a block in front of itself
 * 4. Waits 500ms
 * 5. Breaks that block
 * 6. Disconnects
 *
 * Between each bot, there is a 1-second pause so a human watching on
 * a Bedrock client can observe each version appearing and disappearing.
 *
 * To use an external server, set system properties:
 *   -Dtest.host=localhost -Dtest.port=25565
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BedrockVisibilityParadeTest {

    // Pre-Classic + Classic
    private static final Object[][] PRECL_VERSIONS = {
            { ProtocolVersion.RUBYDUNG, "RD" },
            { ProtocolVersion.CLASSIC, "Classic" },
    };

    // Alpha
    private static final Object[][] ALPHA_VERSIONS = {
            { ProtocolVersion.ALPHA_1_0_15, "A1015" },
            { ProtocolVersion.ALPHA_1_0_16, "A1016" },
            { ProtocolVersion.ALPHA_1_0_17, "A1017" },
            { ProtocolVersion.ALPHA_1_1_0, "A110" },
            { ProtocolVersion.ALPHA_1_2_0, "A120" },
            { ProtocolVersion.ALPHA_1_2_2, "A122" },
            { ProtocolVersion.ALPHA_1_2_3, "A123" },
            { ProtocolVersion.ALPHA_1_2_5, "A125" },
    };

    // Beta
    private static final Object[][] BETA_VERSIONS = {
            { ProtocolVersion.BETA_1_0, "B10" },
            { ProtocolVersion.BETA_1_2, "B12" },
            { ProtocolVersion.BETA_1_3, "B13" },
            { ProtocolVersion.BETA_1_4, "B14" },
            { ProtocolVersion.BETA_1_5, "B15" },
            { ProtocolVersion.BETA_1_6, "B16" },
            { ProtocolVersion.BETA_1_7, "B17" },
            { ProtocolVersion.BETA_1_7_3, "B173" },
            { ProtocolVersion.BETA_1_8, "B18" },
            { ProtocolVersion.BETA_1_9_PRE5, "B19p5" },
    };

    // Release (pre-Netty: 1.0 - 1.6.4)
    private static final Object[][] PRE_NETTY_VERSIONS = {
            { ProtocolVersion.RELEASE_1_0, "R100" },
            { ProtocolVersion.RELEASE_1_1, "R110" },
            { ProtocolVersion.RELEASE_1_2_1, "R121" },
            { ProtocolVersion.RELEASE_1_2_4, "R124" },
            { ProtocolVersion.RELEASE_1_3_1, "R131" },
            { ProtocolVersion.RELEASE_1_4_2, "R142" },
            { ProtocolVersion.RELEASE_1_4_4, "R144" },
            { ProtocolVersion.RELEASE_1_4_6, "R146" },
            { ProtocolVersion.RELEASE_1_5, "R150" },
            { ProtocolVersion.RELEASE_1_5_2, "R152" },
            { ProtocolVersion.RELEASE_1_6_1, "R161" },
            { ProtocolVersion.RELEASE_1_6_2, "R162" },
            { ProtocolVersion.RELEASE_1_6_4, "R164" },
    };

    // Netty early (1.7.2 - 1.12.2)
    private static final Object[][] NETTY_EARLY_VERSIONS = {
            { ProtocolVersion.RELEASE_1_7_2, "R172" },
            { ProtocolVersion.RELEASE_1_7_6, "R176" },
            { ProtocolVersion.RELEASE_1_8, "R180" },
            { ProtocolVersion.RELEASE_1_9, "R190" },
            { ProtocolVersion.RELEASE_1_9_1, "R191" },
            { ProtocolVersion.RELEASE_1_9_2, "R192" },
            { ProtocolVersion.RELEASE_1_9_4, "R194" },
            { ProtocolVersion.RELEASE_1_10, "R1100" },
            { ProtocolVersion.RELEASE_1_11, "R1110" },
            { ProtocolVersion.RELEASE_1_11_2, "R1112" },
            { ProtocolVersion.RELEASE_1_12, "R1120" },
            { ProtocolVersion.RELEASE_1_12_1, "R1121" },
            { ProtocolVersion.RELEASE_1_12_2, "R1122" },
    };

    // Netty mid (1.13 - 1.16.4)
    private static final Object[][] NETTY_MID_VERSIONS = {
            { ProtocolVersion.RELEASE_1_13, "R1130" },
            { ProtocolVersion.RELEASE_1_13_1, "R1131" },
            { ProtocolVersion.RELEASE_1_13_2, "R1132" },
            { ProtocolVersion.RELEASE_1_14, "R1140" },
            { ProtocolVersion.RELEASE_1_14_1, "R1141" },
            { ProtocolVersion.RELEASE_1_14_2, "R1142" },
            { ProtocolVersion.RELEASE_1_14_3, "R1143" },
            { ProtocolVersion.RELEASE_1_14_4, "R1144" },
            { ProtocolVersion.RELEASE_1_15, "R1150" },
            { ProtocolVersion.RELEASE_1_15_1, "R1151" },
            { ProtocolVersion.RELEASE_1_15_2, "R1152" },
            { ProtocolVersion.RELEASE_1_16, "R1160" },
            { ProtocolVersion.RELEASE_1_16_1, "R1161" },
            { ProtocolVersion.RELEASE_1_16_2, "R1162" },
            { ProtocolVersion.RELEASE_1_16_3, "R1163" },
            { ProtocolVersion.RELEASE_1_16_4, "R1164" },
    };

    // Netty modern (1.17 - 1.21.11)
    private static final Object[][] NETTY_MODERN_VERSIONS = {
            { ProtocolVersion.RELEASE_1_17, "R1170" },
            { ProtocolVersion.RELEASE_1_17_1, "R1171" },
            { ProtocolVersion.RELEASE_1_18, "R1180" },
            { ProtocolVersion.RELEASE_1_18_2, "R1182" },
            { ProtocolVersion.RELEASE_1_19, "R1190" },
            { ProtocolVersion.RELEASE_1_19_1, "R1191" },
            { ProtocolVersion.RELEASE_1_19_3, "R1193" },
            { ProtocolVersion.RELEASE_1_19_4, "R1194" },
            { ProtocolVersion.RELEASE_1_20, "R1200" },
            { ProtocolVersion.RELEASE_1_20_2, "R1202" },
            { ProtocolVersion.RELEASE_1_20_3, "R1203" },
            { ProtocolVersion.RELEASE_1_20_5, "R1205" },
            { ProtocolVersion.RELEASE_1_21, "R1210" },
            { ProtocolVersion.RELEASE_1_21_2, "R1212" },
            { ProtocolVersion.RELEASE_1_21_4, "R1214" },
            { ProtocolVersion.RELEASE_1_21_5, "R1215" },
            { ProtocolVersion.RELEASE_1_21_6, "R1216" },
            { ProtocolVersion.RELEASE_1_21_7, "R1217" },
            { ProtocolVersion.RELEASE_1_21_9, "R1219" },
            { ProtocolVersion.RELEASE_1_21_11, "R12111" },
    };

    @BeforeAll
    static void setup() {
        assumeTrue(System.getProperty("test.host") != null,
                "Skipped: requires external server (-Dtest.host=localhost -Dtest.port=25565)");
    }

    @Test @Order(1)
    void paradePreClassic() throws Exception {
        runParade(PRECL_VERSIONS);
    }

    @Test @Order(2)
    void paradeAlpha() throws Exception {
        runParade(ALPHA_VERSIONS);
    }

    @Test @Order(3)
    void paradeBeta() throws Exception {
        runParade(BETA_VERSIONS);
    }

    @Test @Order(4)
    void paradePreNetty() throws Exception {
        runParade(PRE_NETTY_VERSIONS);
    }

    @Test @Order(5)
    void paradeNettyEarly() throws Exception {
        runParade(NETTY_EARLY_VERSIONS);
    }

    @Test @Order(6)
    void paradeNettyMid() throws Exception {
        runParade(NETTY_MID_VERSIONS);
    }

    @Test @Order(7)
    void paradeNettyModern() throws Exception {
        runParade(NETTY_MODERN_VERSIONS);
    }

    private void runParade(Object[][] versions) throws Exception {
        int total = versions.length;
        int passed = 0;
        int failed = 0;

        for (int i = 0; i < total; i++) {
            ProtocolVersion version = (ProtocolVersion) versions[i][0];
            String name = (String) versions[i][1];

            System.out.println("[Parade " + (i + 1) + "/" + total + "] Connecting "
                    + name + " (" + version + ")...");

            BotClient bot = null;
            try {
                String host = System.getProperty("test.host", "localhost");
                int port = Integer.parseInt(System.getProperty("test.port", "25565"));
                bot = new BotClient(host, port, version, name,
                        new io.netty.channel.nio.NioEventLoopGroup(1));
                bot.connectSync(5000);
                BotSession session = bot.getSession();
                assertNotNull(session, name + " session should not be null");
                assertTrue(session.isLoginComplete(), name + " login should complete");

                // Send greeting chat
                session.sendChat("Hi from " + name);
                Thread.sleep(300);

                // Place a block in front of spawn (+Z direction)
                int bx = (int) Math.floor(session.getX());
                int bz = (int) Math.floor(session.getZ()) + 2;
                double feetY;
                if (version.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
                    feetY = session.getY();
                } else {
                    feetY = session.getY() - (double) 1.62f;
                }
                int by = (int) Math.floor(feetY);

                // Place block on top of ground (face 1 = top)
                session.sendBlockPlace(bx, by - 1, bz, 1, 4);
                Thread.sleep(500);

                // Break the block
                session.sendDigging(0, bx, by, bz, 1);
                session.sendDigging(2, bx, by, bz, 1);
                Thread.sleep(200);

                passed++;
                System.out.println("[Parade " + (i + 1) + "/" + total + "] "
                        + name + " OK");
            } catch (Exception e) {
                failed++;
                System.err.println("[Parade " + (i + 1) + "/" + total + "] "
                        + name + " FAILED: " + e.getMessage());
            } finally {
                if (bot != null) {
                    bot.disconnect();
                }
            }

            // Pause between bots so the Bedrock observer can see each one
            Thread.sleep(1000);
        }

        System.out.println("[Parade] Complete: " + passed + " passed, "
                + failed + " failed out of " + total);
        assertEquals(0, failed, failed + " version(s) failed to connect");
    }
}
