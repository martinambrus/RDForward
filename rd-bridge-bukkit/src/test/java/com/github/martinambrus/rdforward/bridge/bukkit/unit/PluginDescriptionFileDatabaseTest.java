package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies PluginDescriptionFile.database field and isDatabaseEnabled().
 * HomeSpawnPlus has {@code database: true} in its plugin.yml; the parser
 * passes it through to PDF.
 */
class PluginDescriptionFileDatabaseTest {

    @Test
    void isDatabaseEnabledReturnsFalseByDefault() {
        PluginDescriptionFile pdf = new PluginDescriptionFile("Test", "1.0", "com.test.Main");
        assertFalse(pdf.isDatabaseEnabled());
    }

    @Test
    void isDatabaseEnabledReturnsTrueWhenSet() {
        PluginDescriptionFile pdf = new PluginDescriptionFile("Test", "1.0", "com.test.Main",
                "", java.util.Collections.emptyList(), java.util.Collections.emptyList(),
                java.util.Collections.emptyMap(), true);
        assertTrue(pdf.isDatabaseEnabled());
    }

    @Test
    void sixArgConstructorDefaultsDatabaseFalse() {
        PluginDescriptionFile pdf = new PluginDescriptionFile("Test", "1.0", "com.test.Main",
                "", java.util.Collections.emptyList(), java.util.Collections.emptyList());
        assertFalse(pdf.isDatabaseEnabled());
    }

    @Test
    void sevenArgConstructorDefaultsDatabaseFalse() {
        PluginDescriptionFile pdf = new PluginDescriptionFile("Test", "1.0", "com.test.Main",
                "", java.util.Collections.emptyList(), java.util.Collections.emptyList(),
                java.util.Collections.emptyMap());
        assertFalse(pdf.isDatabaseEnabled());
    }
}
