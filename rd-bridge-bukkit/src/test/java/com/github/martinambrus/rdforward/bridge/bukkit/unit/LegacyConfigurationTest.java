package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pinning regression test for OpenWarp v1.1, which boots through the
 * pre-Bukkit-1.x configuration API:
 * {@code new Configuration(file).load()} for read, {@code
 * setProperty}/{@code save} for write, and {@code getNode}/{@code
 * getNodes}/{@code getNodeList} to walk hierarchical YAML. The class
 * was removed from Bukkit years ago in favour of {@code
 * org.bukkit.configuration.file.YamlConfiguration}; legacy plugins
 * still ship bytecode pinned against the old type and {@code
 * NoClassDefFoundError} without a stub.
 */
class LegacyConfigurationTest {

    @Test
    void loadReadsBackPreviousSave(@TempDir Path dir) throws Exception {
        File file = dir.resolve("openwarp.yml").toFile();
        Configuration first = new Configuration(file);
        first.setProperty("debug", true);
        first.setProperty("quotas.public", 5);
        first.setProperty("home", Map.of("x", 1.0, "y", 64.0, "z", 2.0, "world", "main"));
        assertTrue(first.save(), "save() must report success on a writable temp dir");
        assertTrue(Files.exists(file.toPath()));

        Configuration second = new Configuration(file);
        second.load();
        assertEquals(true, second.getBoolean("debug", false));
        assertEquals(5, second.getInt("quotas.public", -1));
        assertEquals("main", second.getString("home.world"));
    }

    @Test
    void getNodeReturnsNullForMissingPath(@TempDir Path dir) {
        Configuration cfg = new Configuration(dir.resolve("missing.yml").toFile());
        cfg.load();
        assertNull(cfg.getNode("nope"), "missing path must return null so OpenWarp's null-checks work");
    }

    @Test
    void getNodeWalksNestedSection(@TempDir Path dir) {
        Configuration cfg = new Configuration(dir.resolve("nested.yml").toFile());
        cfg.setProperty("home.x", 10.0);
        cfg.setProperty("home.world", "main");
        ConfigurationNode home = cfg.getNode("home");
        assertNotNull(home);
        assertEquals(10.0, home.getDouble("x", 0.0));
        assertEquals("main", home.getString("world"));
    }

    @Test
    void getKeysReturnsNullForAbsentSubsection(@TempDir Path dir) {
        // OpenWarp.OWConfigurationManager.loadWarps does
        //   List keys = config.getKeys("warps");
        //   if (keys != null) { ... }
        // so absent paths must return null, not an empty list.
        Configuration cfg = new Configuration(dir.resolve("empty.yml").toFile());
        assertNull(cfg.getKeys("warps"));
    }

    @Test
    void getKeysReturnsImmediateChildren(@TempDir Path dir) {
        Configuration cfg = new Configuration(dir.resolve("k.yml").toFile());
        cfg.setProperty("warps.alpha.world", "main");
        cfg.setProperty("warps.beta.world", "main");
        java.util.List<String> keys = cfg.getKeys("warps");
        assertNotNull(keys);
        assertEquals(2, keys.size());
        assertTrue(keys.contains("alpha"));
        assertTrue(keys.contains("beta"));
    }

    @Test
    void getStringListFallsBackToDefault(@TempDir Path dir) {
        Configuration cfg = new Configuration(dir.resolve("missing-list.yml").toFile());
        java.util.List<String> def = Arrays.asList("a", "b");
        assertEquals(def, cfg.getStringList("nope", def));
    }

    @Test
    void getNodesEnumeratesChildren(@TempDir Path dir) {
        Configuration cfg = new Configuration(dir.resolve("nodes.yml").toFile());
        cfg.setProperty("warps.alpha.world", "main");
        cfg.setProperty("warps.beta.world", "main");
        Map<String, ConfigurationNode> nodes = cfg.getNodes("warps");
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertEquals("main", nodes.get("alpha").getString("world"));
    }

    @Test
    void removePropertyClearsValue(@TempDir Path dir) {
        Configuration cfg = new Configuration(dir.resolve("rm.yml").toFile());
        cfg.setProperty("debug", true);
        cfg.removeProperty("debug");
        assertEquals(false, cfg.getBoolean("debug", false));
    }
}
