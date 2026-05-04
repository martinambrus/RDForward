package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginDescriptor;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies BukkitPluginDescriptor.database field and BukkitPluginParser
 * reads {@code database: true/false} from plugin YAML.
 */
class BukkitPluginDescriptorDatabaseTest {

    @Test
    void descriptorDatabaseDefaultsFalse() {
        BukkitPluginDescriptor desc = new BukkitPluginDescriptor("Test", "1.0", "com.test.Main",
                java.util.Collections.emptyList());
        assertFalse(desc.database());
    }

    @Test
    void descriptorDatabaseTrueWhenProvided() {
        BukkitPluginDescriptor desc = new BukkitPluginDescriptor("Test", "1.0", "com.test.Main",
                java.util.Collections.emptyList(), java.util.Collections.emptyList(),
                Map.of(), true);
        assertTrue(desc.database());
    }

    @Test
    void parserReadsDatabaseTrueFromYaml() throws Exception {
        String yaml = "name: Test\nversion: '1.0'\nmain: com.test.Main\ndatabase: true\n";
        BukkitPluginDescriptor desc = BukkitPluginParser.parse(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        assertTrue(desc.database());
    }

    @Test
    void parserReadsDatabaseFalseFromYaml() throws Exception {
        String yaml = "name: Test\nversion: '1.0'\nmain: com.test.Main\ndatabase: false\n";
        BukkitPluginDescriptor desc = BukkitPluginParser.parse(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        assertFalse(desc.database());
    }

    @Test
    void parserDefaultsDatabaseFalseWhenMissing() throws Exception {
        String yaml = "name: Test\nversion: '1.0'\nmain: com.test.Main\n";
        BukkitPluginDescriptor desc = BukkitPluginParser.parse(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        assertFalse(desc.database());
    }
}
