package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.compat.BridgeFiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class BridgeFilesTest {

    @Test
    void resolveReturnsOriginalFileWhenParentIsNonNull() {
        File parent = new File("/some/dir");
        File file = new File(parent, "test.yml");
        File result = BridgeFiles.resolve("plugins/MyPlugin", file);
        assertSame(file, result, "file with non-null parent must be returned unchanged");
    }

    @Test
    void resolveRedirectsNullParentToPluginDir() {
        File file = new File("spawns.yml");
        // On most JVMs new File("spawns.yml").getParentFile() returns null
        // because there's no directory component.
        File result = BridgeFiles.resolve("plugins/HomeSpawnPlus", file);
        assertNotSame(file, result, "file with null parent must be redirected");
        assertEquals(new File(new File("plugins/HomeSpawnPlus"), "spawns.yml"), result);
    }

    @Test
    void resolveDoesNotRedirectPathWithDirectoryComponent() {
        // "data/homes.yml" has parent "data", not null — must pass through unchanged
        File file = new File("data/homes.yml");
        File result = BridgeFiles.resolve("plugins/MyPlugin", file);
        assertSame(file, result, "file with directory component has non-null parent");
    }

    @Test
    void resolvePreservesAbsolutePath() {
        File file = new File("/absolute/path/config.yml");
        // Absolute paths have a parent, so they pass through unchanged
        File result = BridgeFiles.resolve("plugins/MyPlugin", file);
        assertSame(file, result);
    }

    @Test
    void resolveWithTempDirParent(@TempDir File tempDir) {
        File file = new File(tempDir, "test.yml");
        File result = BridgeFiles.resolve("plugins/MyPlugin", file);
        assertSame(file, result, "file with temp dir parent must be returned unchanged");
        assertEquals(tempDir, file.getParentFile());
    }
}
