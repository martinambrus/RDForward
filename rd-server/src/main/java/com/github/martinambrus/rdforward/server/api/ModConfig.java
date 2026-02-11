package com.github.martinambrus.rdforward.server.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Per-mod configuration API using Java Properties files.
 *
 * Each mod gets its own config file in the "config/" directory,
 * named after the mod ID (e.g., "config/mymod.properties").
 *
 * Example:
 * <pre>
 *   ModConfig config = new ModConfig("mymod");
 *   config.setDefault("maxPlayers", "10");
 *   config.setDefault("enableFeature", "true");
 *   config.load();
 *
 *   int maxPlayers = config.getInt("maxPlayers", 10);
 *   boolean enabled = config.getBoolean("enableFeature", true);
 * </pre>
 */
public class ModConfig {

    private static final File CONFIG_DIR = new File("config");

    private final String modId;
    private final File configFile;
    private final Properties properties = new Properties();
    private final Properties defaults = new Properties();

    public ModConfig(String modId) {
        this.modId = modId;
        this.configFile = new File(CONFIG_DIR, modId + ".properties");
    }

    /**
     * Set a default value. If the key doesn't exist in the file, this value is used.
     */
    public void setDefault(String key, String value) {
        defaults.setProperty(key, value);
    }

    /**
     * Load the config file. Missing keys are populated from defaults and saved.
     */
    public void load() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }

        // Start with defaults
        properties.putAll(defaults);

        // Override with saved values
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Failed to load config for " + modId + ": " + e.getMessage());
            }
        }

        // Save to ensure all defaults are written
        save();
    }

    /**
     * Save the current configuration to disk.
     */
    public void save() {
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, modId + " configuration");
        } catch (IOException e) {
            System.err.println("Failed to save config for " + modId + ": " + e.getMessage());
        }
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public String getModId() {
        return modId;
    }
}
