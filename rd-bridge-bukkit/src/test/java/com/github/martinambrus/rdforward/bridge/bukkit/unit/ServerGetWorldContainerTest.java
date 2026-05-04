package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Server;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies Server.getWorldContainer() returns a non-null File.
 * HomeSpawnPlus's player listener probes getWorldContainer() to find
 * player.dat files.
 */
class ServerGetWorldContainerTest {

    @Test
    void defaultGetWorldContainerReturnsCurrentDir() {
        Server server = new Server() {
            @Override public String getName() { return "test"; }
            @Override public String getVersion() { return "1"; }
            @Override public String getBukkitVersion() { return "1"; }
            @Override public java.util.logging.Logger getLogger() { return null; }
            @Override public int broadcastMessage(String msg) { return 0; }
            @Override public org.bukkit.plugin.PluginManager getPluginManager() { return null; }
            @Override public org.bukkit.scheduler.BukkitScheduler getScheduler() { return null; }
            @Override public org.bukkit.command.ConsoleCommandSender getConsoleSender() { return null; }
            @Override public org.bukkit.entity.Player getPlayer(String name) { return null; }
            @Override public java.util.List<org.bukkit.World> getWorlds() { return java.util.List.of(); }
            @Override public org.bukkit.World getWorld(String name) { return null; }
            @Override public java.util.Collection<org.bukkit.entity.Player> getOnlinePlayers() { return java.util.List.of(); }
        };
        File dir = server.getWorldContainer();
        assertNotNull(dir);
        assertEquals(".", dir.getPath());
    }
}
