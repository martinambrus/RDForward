package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldGameRuleStubsTest {

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
    void setGameRuleValueReturnsTrue() {
        assertTrue(world().setGameRuleValue("commandBlockOutput", "false"));
    }

    @Test
    void getGameRuleValueReturnsEmptyString() {
        assertEquals("", world().getGameRuleValue("commandBlockOutput"));
    }

    @Test
    void isGameRuleReturnsFalse() {
        assertFalse(world().isGameRule("commandBlockOutput"));
    }

    @Test
    void getGameRulesReturnsEmptyArray() {
        assertArrayEquals(new String[0], world().getGameRules());
    }
}
