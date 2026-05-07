package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Locks in safe defaults for the metadata accessors that legacy
 * plugins call directly on {@link PluginDescriptionFile}. Citizens
 * 2.0.13 prints {@code getWebsite()} in its info banner and threw
 * {@code NoSuchMethodError} until the stub gained the method;
 * EssentialsX-era plugins reach for {@code getPrefix()},
 * {@code getSoftDepend()} and {@code getLoadBefore()} during reload
 * paths. Each accessor must return a non-null safe default so a
 * caller can chain on it without NPE.
 */
class PluginDescriptionFileMetadataTest {

    @Test
    void getWebsiteReturnsEmptyString() {
        PluginDescriptionFile pdf = new PluginDescriptionFile("Test", "1.0", "com.test.Main");
        assertEquals("", pdf.getWebsite(),
                "must be non-null empty string — Citizens prints this directly");
    }

    @Test
    void getPrefixReturnsEmptyString() {
        PluginDescriptionFile pdf = new PluginDescriptionFile("Test", "1.0", "com.test.Main");
        assertEquals("", pdf.getPrefix());
    }

    @Test
    void getSoftDependReturnsEmptyList() {
        PluginDescriptionFile pdf = new PluginDescriptionFile("Test", "1.0", "com.test.Main");
        assertNotNull(pdf.getSoftDepend());
        assertTrue(pdf.getSoftDepend().isEmpty());
    }

    @Test
    void getLoadBeforeReturnsEmptyList() {
        PluginDescriptionFile pdf = new PluginDescriptionFile("Test", "1.0", "com.test.Main");
        assertNotNull(pdf.getLoadBefore());
        assertTrue(pdf.getLoadBefore().isEmpty());
    }
}
