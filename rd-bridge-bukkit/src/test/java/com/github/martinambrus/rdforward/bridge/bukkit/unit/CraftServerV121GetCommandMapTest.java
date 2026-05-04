package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies CraftServer v1_21_R1 exposes getCommandMap() so plugins that
 * cast Bukkit.getServer() to CraftServer can register commands.
 * HomeSpawnPlus's CommandRegister does this.
 */
class CraftServerV121GetCommandMapTest {

    @BeforeEach @AfterEach
    void clean() { BukkitBridge.uninstall(); }

    @Test
    void getCommandMapReturnsNonNull() {
        BukkitBridge.install(new StubRdServer());
        CraftServer craft = (CraftServer) Bukkit.getServer();
        assertNotNull(craft.getCommandMap());
    }

    @Test
    void getCommandMapReturnsSimpleCommandMap() {
        BukkitBridge.install(new StubRdServer());
        CraftServer craft = (CraftServer) Bukkit.getServer();
        assertInstanceOf(SimpleCommandMap.class, craft.getCommandMap());
    }

    @Test
    void getCommandMapReturnsSameInstance() {
        BukkitBridge.install(new StubRdServer());
        CraftServer craft = (CraftServer) Bukkit.getServer();
        assertSame(craft.getCommandMap(), craft.getCommandMap(),
                "commandMap must be a singleton field");
    }
}
