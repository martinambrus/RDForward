package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pinning regression test for LogBlock 1.41, whose {@code Config}
 * iterates {@code fileConfiguration.getConfigurationSection("tools").getKeys(false)}
 * to enumerate per-tool subtrees. The previous stub returned {@code
 * null} so the iteration NPE'd at {@code SEVERE} severity.
 *
 * <p>Also pins prefix-aware reads on the subsection — every
 * {@code getString}/{@code getList}/{@code getInt} on the returned
 * section must resolve back through the root flat-key map with the
 * subsection prefix prepended.
 */
class MemorySectionConfigSectionTest {

    private static YamlConfiguration sampleToolsConfig() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("tools.tool.aliases", java.util.Arrays.asList("t"));
        cfg.set("tools.tool.leftClickBehavior", "NONE");
        cfg.set("tools.tool.item", 270);
        cfg.set("tools.toolblock.aliases", java.util.Arrays.asList("tb"));
        cfg.set("tools.toolblock.leftClickBehavior", "TOOL");
        cfg.set("tools.toolblock.item", 7);
        cfg.set("unrelated.key", "ignored");
        return cfg;
    }

    @Test
    void getConfigurationSectionReturnsViewWhenChildrenExist() {
        YamlConfiguration cfg = sampleToolsConfig();
        ConfigurationSection tools = cfg.getConfigurationSection("tools");
        assertNotNull(tools, "subsection must be non-null when keys exist under prefix");
    }

    @Test
    void getConfigurationSectionReturnsNullForMissingPath() {
        YamlConfiguration cfg = sampleToolsConfig();
        assertNull(cfg.getConfigurationSection("nope"));
    }

    @Test
    void getKeysFalseReturnsImmediateChildren() {
        YamlConfiguration cfg = sampleToolsConfig();
        ConfigurationSection tools = cfg.getConfigurationSection("tools");
        @SuppressWarnings("unchecked") Set<String> keys = (Set<String>) tools.getKeys(false);
        assertEquals(2, keys.size(), "tools has two immediate children: tool, toolblock");
        assertTrue(keys.contains("tool"));
        assertTrue(keys.contains("toolblock"));
    }

    @Test
    void getKeysTrueReturnsAllDescendants() {
        YamlConfiguration cfg = sampleToolsConfig();
        ConfigurationSection tools = cfg.getConfigurationSection("tools");
        @SuppressWarnings("unchecked") Set<String> keys = (Set<String>) tools.getKeys(true);
        assertTrue(keys.contains("tool.aliases"));
        assertTrue(keys.contains("tool.item"));
        assertTrue(keys.contains("toolblock.item"));
        assertFalse(keys.contains("unrelated.key"),
                "deep keys must be scoped to the subsection prefix");
    }

    @Test
    void nestedSubsectionResolvesAgainstRoot() {
        YamlConfiguration cfg = sampleToolsConfig();
        ConfigurationSection tools = cfg.getConfigurationSection("tools");
        ConfigurationSection tool = tools.getConfigurationSection("tool");
        assertNotNull(tool);
        assertEquals("NONE", tool.getString("leftClickBehavior"));
        assertEquals(270, tool.getInt("item"));
        assertEquals(java.util.Arrays.asList("t"), tool.getList("aliases"));
    }

    @Test
    void writesThroughSubsectionRoundTripToRoot() {
        YamlConfiguration cfg = sampleToolsConfig();
        ConfigurationSection tools = cfg.getConfigurationSection("tools");
        ConfigurationSection tool = tools.getConfigurationSection("tool");
        tool.set("newField", "hello");
        assertEquals("hello", cfg.getString("tools.tool.newField"),
                "subsection writes must surface in the root config");
    }

    @Test
    void rootGetKeysFalseReturnsTopLevelOnly() {
        YamlConfiguration cfg = sampleToolsConfig();
        @SuppressWarnings("unchecked") Set<String> keys = (Set<String>) cfg.getKeys(false);
        assertTrue(keys.contains("tools"));
        assertTrue(keys.contains("unrelated"));
        assertFalse(keys.contains("tools.tool"),
                "root getKeys(false) must collapse to top-level segments");
    }

    @Test
    void isConfigurationSectionTracksGetConfigurationSection() {
        YamlConfiguration cfg = sampleToolsConfig();
        assertTrue(cfg.isConfigurationSection("tools"));
        assertFalse(cfg.isConfigurationSection("nope"));
    }

    @Test
    void getCurrentPathReflectsPrefix() {
        YamlConfiguration cfg = sampleToolsConfig();
        ConfigurationSection tools = cfg.getConfigurationSection("tools");
        ConfigurationSection tool = tools.getConfigurationSection("tool");
        assertEquals("tools", tools.getCurrentPath());
        assertEquals("tools.tool", tool.getCurrentPath());
        assertEquals("tool", tool.getName());
    }

    @Test
    void containsHonorsPrefix() {
        YamlConfiguration cfg = sampleToolsConfig();
        ConfigurationSection tool = cfg.getConfigurationSection("tools").getConfigurationSection("tool");
        assertTrue(tool.contains("aliases"));
        assertFalse(tool.contains("leftClickBehavior_missing"));
    }
}
