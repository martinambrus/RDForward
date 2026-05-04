package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import java.io.File;

/**
 * Runtime bridge for the {@link NullFileParentTransformer} bytecode rewrite.
 * When a plugin creates {@code new File(null, child)}, Java resolves the path
 * relative to the JVM working directory (server root). Bukkit plugins that do
 * this — notably HomeSpawnPlus's YAML storage — end up scattering data files
 * across the server root instead of keeping them in {@code plugins/<name>/}.
 *
 * <p>The transformer rewrites every {@code new File(File, String)} and
 * {@code new File(String, String)} call in plugin classes to route through
 * {@link #resolve(String, File)}. This method checks whether the resulting
 * File has a null parent (i.e. a bare filename with no directory component)
 * and redirects it under the plugin's data directory.
 *
 * <p>Files that already have a directory component ({@code "subdir/file"},
 * absolute paths, or non-null parents) pass through unchanged — the overhead
 * is a single {@link File#getParentFile()} null check.
 */
public final class BridgeFiles {

    private BridgeFiles() {}

    /**
     * Post-construction fixup for {@code new File(parent, child)}.
     *
     * @param pluginDir the plugin's data directory path (e.g. {@code "plugins/HomeSpawnPlus"})
     * @param file      the File object just constructed by the plugin
     * @return {@code file} unchanged if it already has a parent directory,
     *         otherwise a new File under {@code pluginDir}
     */
    public static File resolve(String pluginDir, File file) {
        if (file.getParentFile() != null) return file;
        return new File(new File(pluginDir), file.getPath());
    }
}
