package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.avaje.ebean.NoOpEbeanServer;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies JavaPlugin.getDatabase() returns a NoOpEbeanServer.
 * HomeSpawnPlus extends JavaPlugin and calls getDatabase() during
 * Ebean storage initialization.
 */
class JavaPluginGetDatabaseTest {

    @Test
    void getDatabaseReturnsNoOpEbeanServer() {
        JavaPlugin plugin = new JavaPlugin() {};
        var db = plugin.getDatabase();
        assertInstanceOf(NoOpEbeanServer.class, db);
    }

    @Test
    void getDatabaseReturnsNonNull() {
        JavaPlugin plugin = new JavaPlugin() {};
        assertNotNull(plugin.getDatabase(),
                "getDatabase must never return null — plugins do not null-check it");
    }
}
