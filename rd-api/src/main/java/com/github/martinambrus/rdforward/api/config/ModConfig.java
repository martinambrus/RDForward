package com.github.martinambrus.rdforward.api.config;

/**
 * Per-mod configuration file. Each mod gets its own namespace; keys are
 * isolated between mods.
 */
public interface ModConfig {

    String getModId();

    /** Set a default value applied when the key is absent. */
    void setDefault(String key, String value);

    void load();

    void save();

    String getString(String key, String defaultValue);

    int getInt(String key, int defaultValue);

    long getLong(String key, long defaultValue);

    boolean getBoolean(String key, boolean defaultValue);

    void set(String key, String value);
}
