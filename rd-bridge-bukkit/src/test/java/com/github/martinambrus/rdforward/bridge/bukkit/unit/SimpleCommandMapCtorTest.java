package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pinning regression test for WorldEdit's {@code CommandRegistration}
 * fallback path, which constructs {@code new SimpleCommandMap(server)}
 * after failing to retrieve the live command map. Without the single-arg
 * constructor the call resolved to {@code NoSuchMethodError} at runtime.
 */
class SimpleCommandMapCtorTest {

    @BeforeEach @AfterEach
    void clean() { BukkitBridge.uninstall(); }

    @Test
    void singleArgServerConstructorIsCallable() {
        BukkitBridge.install(new StubRdServer());
        SimpleCommandMap map = new SimpleCommandMap(Bukkit.getServer());
        assertNotNull(map, "SimpleCommandMap(Server) must be a callable constructor");
    }
}
