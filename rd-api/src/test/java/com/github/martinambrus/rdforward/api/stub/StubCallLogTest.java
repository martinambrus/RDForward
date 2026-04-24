package com.github.martinambrus.rdforward.api.stub;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StubCallLogTest {

    private final List<LogRecord> captured = new ArrayList<>();
    private Handler handler;
    private Logger logger;

    @BeforeEach
    void setUp() {
        StubCallLog.resetForTests();
        logger = Logger.getLogger("RDForward/StubCall");
        handler = new Handler() {
            @Override public void publish(LogRecord record) { captured.add(record); }
            @Override public void flush() {}
            @Override public void close() throws SecurityException {}
        };
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
    }

    @AfterEach
    void tearDown() {
        StubCallLog.resetForTests();
        logger.removeHandler(handler);
        logger.setUseParentHandlers(true);
    }

    @Test
    void firstCallLogsWarning() {
        StubCallLog.logOnce("demo-plugin", "org.bukkit.World.spawnEntity(Location, EntityType)");
        assertEquals(1, captured.size());
        assertEquals(Level.WARNING, captured.get(0).getLevel());
        String msg = captured.get(0).getMessage();
        assertTrue(msg.contains("demo-plugin"), "message mentions plugin: " + msg);
        assertTrue(msg.contains("spawnEntity"), "message mentions signature: " + msg);
    }

    @Test
    void repeatedCallsWithSameKeyLogOnce() {
        for (int i = 0; i < 5; i++) {
            StubCallLog.logOnce("demo-plugin", "org.bukkit.World.spawnEntity(Location, EntityType)");
        }
        assertEquals(1, captured.size(), "same (plugin, signature) logs exactly once");
    }

    @Test
    void differentSignaturesSamePluginEachLog() {
        StubCallLog.logOnce("demo-plugin", "org.bukkit.World.spawnEntity(Location, EntityType)");
        StubCallLog.logOnce("demo-plugin", "org.bukkit.World.dropItem(Location, ItemStack)");
        assertEquals(2, captured.size());
    }

    @Test
    void sameSignatureDifferentPluginsEachLog() {
        StubCallLog.logOnce("plugin-a", "org.bukkit.World.spawnEntity(Location, EntityType)");
        StubCallLog.logOnce("plugin-b", "org.bukkit.World.spawnEntity(Location, EntityType)");
        assertEquals(2, captured.size(), "per-plugin dedup — same signature from two plugins logs twice");
    }

    @Test
    void nullPluginIdFallsBackToUnknown() {
        StubCallLog.logOnce(null, "org.bukkit.World.spawnEntity(Location, EntityType)");
        assertEquals(1, captured.size());
        assertTrue(captured.get(0).getMessage().contains("<unknown>"));
    }

    @Test
    void blankPluginIdFallsBackToUnknown() {
        StubCallLog.logOnce("   ", "org.bukkit.World.spawnEntity(Location, EntityType)");
        assertEquals(1, captured.size());
        assertTrue(captured.get(0).getMessage().contains("<unknown>"));
    }

    @Test
    void nullOrBlankSignatureIsIgnored() {
        StubCallLog.logOnce("demo", null);
        StubCallLog.logOnce("demo", "");
        assertEquals(0, captured.size(), "null/blank signature must not log");
    }

    @Test
    void hasLoggedReflectsState() {
        assertFalse(StubCallLog.hasLogged("demo", "sig.foo"));
        StubCallLog.logOnce("demo", "sig.foo");
        assertTrue(StubCallLog.hasLogged("demo", "sig.foo"));
        assertFalse(StubCallLog.hasLogged("demo", "sig.bar"));
    }

    @Test
    void resetForTestsClearsState() {
        StubCallLog.logOnce("demo", "sig.foo");
        StubCallLog.resetForTests();
        captured.clear();
        StubCallLog.logOnce("demo", "sig.foo");
        assertEquals(1, captured.size(), "after resetForTests, the first call re-logs");
    }
}
