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
 * Tests that Netty-framed clients (1.7.2+ / v4-v5) can successfully
 * log in to the server in offline mode.
 *
 * Verifies: VarInt framing detection, Netty state machine
 * (HANDSHAKING -> LOGIN -> PLAY), encryption handshake,
 * login completion, entity ID assignment, position reception.
 */
class NettyLoginTest {

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
            "RELEASE_1_7_2",  // v4 (Netty rewrite)
            "RELEASE_1_7_6",  // v5 (property list in SpawnPlayer)
            "RELEASE_1_8",    // v47 (packed positions, VarInt entity IDs)
            "RELEASE_1_9",    // v107 (paletted chunks, teleportId)
            "RELEASE_1_9_1",  // v108 (JoinGame int dimension)
            "RELEASE_1_9_2",  // v109
            "RELEASE_1_9_4",  // v110 (blockEntityCount in chunks)
            "RELEASE_1_10",   // v210
            "RELEASE_1_11",   // v315 (block placement float cursors)
            "RELEASE_1_11_2", // v316
            "RELEASE_1_12",   // v335 (C2S reshuffled)
            "RELEASE_1_12_1", // v338 (PlaceGhostRecipe shifts S2C)
            "RELEASE_1_12_2", // v340 (Long keepalive)
            "RELEASE_1_13",   // v393 (The Flattening, new packet IDs)
            "RELEASE_1_14",   // v477 (position encoding change, new JoinGame)
            "RELEASE_1_15",   // v573 (SpawnPlayer metadata removed)
            "RELEASE_1_16",   // v735 (binary UUID, JoinGame NBT codec)
            "RELEASE_1_16_2", // v751 (JoinGame rewrite, isHardcore)
            "RELEASE_1_18",   // v757 (chunk+light merged, simulationDistance)
            "RELEASE_1_19_4", // v762 (PlayerPosition no dismount, damage_type)
            "RELEASE_1_20_2", // v764 (CONFIGURATION state, SpawnPlayer removed)
            "RELEASE_1_20_3", // v765 (NBT text components)
            "RELEASE_1_20_5", // v766 (per-registry, SelectKnownPacks)
            "RELEASE_1_21",   // v767 (new registries: painting, enchantment, jukebox)
            "RELEASE_1_21_2", // v768 (PlayerPosition rewrite, JoinGame seaLevel)
            "RELEASE_1_21_5", // v770 (animal variant registries)
            "RELEASE_1_21_9", // v773 (major S2C ID shifts)
            "RELEASE_1_21_11" // v774 (latest supported)
    })
    void nettyLoginSucceeds(ProtocolVersion version) throws Exception {
        BotClient bot = testServer.createBot(version, "Netty_" + version.name());
        try {
            BotSession session = bot.getSession();

            assertTrue(session.isLoginComplete(),
                    version + ": Netty login should complete (offline mode)");
            assertTrue(session.getEntityId() > 0,
                    version + ": Entity ID should be assigned");
            assertTrue(session.getY() != 0,
                    version + ": Y position should be set (in the world)");
        } finally {
            bot.disconnect();
        }
    }
}
