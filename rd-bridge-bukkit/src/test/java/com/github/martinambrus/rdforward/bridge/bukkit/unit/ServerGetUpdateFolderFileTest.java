package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Server;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies Server.getUpdateFolderFile() resolves to the plugins/update
 * directory. FarmProtect 1.9.0's Updater calls this to stage hot-swap jars,
 * and Paper-derived plugins NoSuchMethodError without it.
 */
class ServerGetUpdateFolderFileTest {

    @Test
    void defaultGetUpdateFolderFileResolvesUnderPluginsDir() {
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
        File f = server.getUpdateFolderFile();
        assertNotNull(f);
        assertEquals("plugins" + File.separator + "update", f.getPath());
        assertEquals("update", server.getUpdateFolder(),
                "default getUpdateFolder() must remain 'update' so the File path matches the String accessor");
    }

    @Test
    void overrideOfGetUpdateFolderPropagatesIntoFile() {
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
            @Override public String getUpdateFolder() { return "staging"; }
        };
        assertEquals("plugins" + File.separator + "staging", server.getUpdateFolderFile().getPath());
    }
}
