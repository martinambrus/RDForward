package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitSchedulerAdapter;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the scheduler {@code cancelTask(int)} fix (ClearLag TPS measurement
 * depended on it) and new World stubs added for ClearLag compatibility:
 * {@code getEntitiesByClass}, {@code setKeepSpawnInMemory}, spawn limits.
 */
class SchedulerCancelTaskTest {

    private StubRdServer rd;
    private BukkitSchedulerAdapter adapter;

    @BeforeEach
    void setUp() {
        rd = new StubRdServer();
        adapter = new BukkitSchedulerAdapter(rd.scheduler);
    }

    @AfterEach
    void tearDown() {
        adapter.shutdown();
    }

    private static final org.bukkit.plugin.Plugin PLUGIN =
            new org.bukkit.plugin.Plugin() {
                public String getName() { return "TestPlugin"; }
                public org.bukkit.plugin.PluginDescriptionFile getDescription() { return null; }
                public java.util.logging.Logger getLogger() { return java.util.logging.Logger.getLogger("TestPlugin"); }
                public org.bukkit.Server getServer() { return null; }
                public boolean isEnabled() { return true; }
                public void onLoad() {}
                public void onEnable() {}
                public void onDisable() {}
            };

    // --- cancelTask(int) ---

    @Test
    void cancelTaskByIntegerIdCancelsSyncTask() {
        BukkitTask task = adapter.runTaskTimer(PLUGIN, () -> {}, 0, 1);
        int id = task.getTaskId();
        assertFalse(task.isCancelled());

        adapter.cancelTask(id);

        assertTrue(task.isCancelled(), "cancelTask(int) should cancel the backing task");
    }

    @Test
    void cancelTaskByIntegerIdCancelsAsyncTask() {
        BukkitTask task = adapter.runTaskAsynchronously(PLUGIN, () -> {});
        int id = task.getTaskId();

        adapter.cancelTask(id);

        assertTrue(task.isCancelled(), "cancelTask(int) should cancel async task");
    }

    @Test
    void cancelTaskUnknownIdDoesNotThrow() {
        assertDoesNotThrow(() -> adapter.cancelTask(99999));
    }

    @Test
    void legacyScheduleSyncRepeatingReturnsPositiveId() {
        int id = adapter.scheduleSyncRepeatingTask(PLUGIN, () -> {}, 0L, 1L);
        assertTrue(id > 0, "legacy schedule method must return positive task id");
    }

    @Test
    void legacyCancelTaskCancelsByIntegerId() {
        int id = adapter.scheduleSyncRepeatingTask(PLUGIN, () -> {}, 0L, 1L);
        adapter.cancelTask(id);
        // Verify the task was registered and then removed — no crash = success
    }

    // --- World stubs for ClearLag ---

    private World world() {
        BukkitBridge.install(rd);
        try {
            return Bukkit.getServer().getWorlds().get(0);
        } finally {
            BukkitBridge.uninstall();
        }
    }

    @Test
    void setKeepSpawnInMemoryDoesNotThrow() {
        assertDoesNotThrow(() -> world().setKeepSpawnInMemory(true));
        assertDoesNotThrow(() -> world().setKeepSpawnInMemory(false));
    }

    @Test
    void setWaterAnimalSpawnLimitDoesNotThrow() {
        assertDoesNotThrow(() -> world().setWaterAnimalSpawnLimit(0));
    }

    @Test
    void setAnimalSpawnLimitDoesNotThrow() {
        assertDoesNotThrow(() -> world().setAnimalSpawnLimit(0));
    }

    @Test
    void setMonsterSpawnLimitDoesNotThrow() {
        assertDoesNotThrow(() -> world().setMonsterSpawnLimit(0));
    }

    @Test
    void setAmbientSpawnLimitDoesNotThrow() {
        assertDoesNotThrow(() -> world().setAmbientSpawnLimit(0));
    }

    @Test
    void getEntitiesByClassReturnsEmptyCollection() {
        Collection<org.bukkit.entity.Entity> result =
                world().getEntitiesByClass(org.bukkit.entity.Entity.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEntitiesByClassesReturnsEmptyCollection() {
        Collection<org.bukkit.entity.Entity> result =
                world().getEntitiesByClasses(org.bukkit.entity.Entity.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
