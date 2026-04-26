package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pinning regression for LoginSecurity's bundled
 * {@code CommentConfiguration}, which extends {@link YamlConfiguration}
 * and dereferences {@code options().indent()} during
 * {@code onEnable}. The previous stub returned {@code null} and produced
 * {@link NullPointerException}; this test guards the lazy non-null
 * contract on both {@link YamlConfiguration#options()} and the parent
 * {@code FileConfiguration.options()} signature.
 */
class FileConfigurationOptionsWiringTest {

    @Test
    void yamlOptionsAreNonNullAndCovariant() {
        YamlConfiguration cfg = new YamlConfiguration();
        YamlConfigurationOptions opts = cfg.options();
        assertNotNull(opts, "YamlConfiguration.options() must not return null");
        assertSame(opts, cfg.options(),
                "options() must be cached so chained .indent(2).pathSeparator('.') configures one instance");
    }

    @Test
    void optionsConfigurationRoundTripsToOwner() {
        YamlConfiguration cfg = new YamlConfiguration();
        YamlConfigurationOptions opts = cfg.options();
        assertSame(cfg, opts.configuration(),
                "options.configuration() must echo the YamlConfiguration that produced it");
    }

    @Test
    void parentFileConfigurationOptionsAccessorIsNonNull() {
        // Plugins that hold the abstract type still call options(); the
        // covariant override flows the same instance up.
        org.bukkit.configuration.file.FileConfiguration cfg = new YamlConfiguration();
        FileConfigurationOptions opts = cfg.options();
        assertNotNull(opts);
        assertTrue(opts instanceof YamlConfigurationOptions,
                "FileConfiguration.options() returns the YamlConfigurationOptions instance");
    }

    @Test
    void indentReturnsSensibleDefault() {
        YamlConfigurationOptions opts = new YamlConfiguration().options();
        assertEquals(2, opts.indent(),
                "indent default must be 2 to match real Bukkit; LoginSecurity asserts on this");
    }
}
