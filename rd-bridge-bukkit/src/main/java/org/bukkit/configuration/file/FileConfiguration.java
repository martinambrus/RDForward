package org.bukkit.configuration.file;

import org.bukkit.configuration.Configuration;

import java.io.File;
import java.io.IOException;

/**
 * Bukkit-shaped file-backed configuration. Real implementation lives in
 * {@link YamlConfiguration}. Kept as an abstract class to match upstream
 * inheritance so plugins that declare {@code FileConfiguration} references
 * compile.
 */
public abstract class FileConfiguration implements Configuration {

    public abstract void load(File file) throws IOException;

    public abstract void save(File file) throws IOException;
}
