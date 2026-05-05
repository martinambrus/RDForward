package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers World methods added for XcraftGate compatibility:
 * {@code getPlayers}, {@code setPVP}, {@code setSpawnFlags},
 * {@code setDifficulty}, {@code getDifficulty}.
 * Each method must resolve at the JVM level (no NoSuchMethodError)
 * and return sensible defaults or no-op.
 */
class WorldStubsTest {

    private StubRdServer rd;

    @BeforeEach void setUp() {
        rd = new StubRdServer();
        BukkitBridge.install(rd);
    }

    @AfterEach void tearDown() {
        BukkitBridge.uninstall();
    }

    private World world() {
        return Bukkit.getServer().getWorlds().get(0);
    }

    @Test
    void getPlayersReturnsNonNullList() {
        // No PlayerManager in test context -> empty list.
        // In production BukkitWorldAdapter returns live players.
        var players = world().getPlayers();
        assertNotNull(players);
        assertTrue(players.isEmpty());
    }

    @Test
    void setPVPDoesNotThrow() {
        world().setPVP(true);
        world().setPVP(false);
    }

    @Test
    void setSpawnFlagsDoesNotThrow() {
        world().setSpawnFlags(true, true);
        world().setSpawnFlags(false, false);
    }

    @Test
    void setDifficultyDoesNotThrow() {
        for (Difficulty d : Difficulty.values()) {
            world().setDifficulty(d);
        }
    }

    @Test
    void getDifficultyReturnsNormal() {
        assertEquals(Difficulty.NORMAL, world().getDifficulty());
    }
}
