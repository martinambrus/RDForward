package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for YamlConfiguration.saveToString() deep serialization —
 * ConfigurationSerializable objects are converted to Maps with a "=="
 * key before SnakeYAML dumps them. HomeSpawnPlus's homes.yml must
 * contain actual location data, not empty "{}".
 */
class YamlConfigurationDeepSerializeTest {

    @SerializableAs("TestLoc")
    public static class TestLoc implements ConfigurationSerializable {
        final String world;
        final double x, y, z;
        TestLoc(String world, double x, double y, double z) {
            this.world = world; this.x = x; this.y = y; this.z = z;
        }
        @SuppressWarnings("unchecked")
        public TestLoc(Map map) {
            this.world = String.valueOf(map.getOrDefault("world", ""));
            this.x = ((Number) map.getOrDefault("x", 0)).doubleValue();
            this.y = ((Number) map.getOrDefault("y", 0)).doubleValue();
            this.z = ((Number) map.getOrDefault("z", 0)).doubleValue();
        }
        @Override public Map<String, Object> serialize() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("world", world);
            m.put("x", x);
            m.put("y", y);
            m.put("z", z);
            return m;
        }
    }

    @AfterEach
    void cleanup() {
        ConfigurationSerialization.unregisterClass("TestLoc");
    }

    @Test
    void saveToStringContainsEqualsKeyAndData() {
        ConfigurationSerialization.registerClass(TestLoc.class);
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("home", new TestLoc("overworld", 135.4, 50.0, 129.2));
        String yaml = cfg.saveToString();
        assertTrue(yaml.contains("==: TestLoc"), "must contain '==' type key");
        assertTrue(yaml.contains("world: overworld"), "must contain serialized world");
        assertTrue(yaml.contains("x: 135.4"), "must contain serialized x");
    }

    @Test
    void deepSerializeRecursesIntoNestedMaps() {
        ConfigurationSerialization.registerClass(TestLoc.class);
        YamlConfiguration cfg = new YamlConfiguration();
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("loc", new TestLoc("nether", 10, 20, 30));
        cfg.set("data", nested);
        String yaml = cfg.saveToString();
        assertTrue(yaml.contains("==: TestLoc"));
        assertTrue(yaml.contains("world: nether"));
    }

    @Test
    void deepSerializeRecursesIntoLists() {
        ConfigurationSerialization.registerClass(TestLoc.class);
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("locs", List.of(new TestLoc("a", 1, 2, 3), new TestLoc("b", 4, 5, 6)));
        String yaml = cfg.saveToString();
        assertTrue(yaml.contains("world: a"));
        assertTrue(yaml.contains("world: b"));
    }

    @Test
    void deepSerializePreservesNonSerializableValues() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("name", "test");
        cfg.set("count", 42);
        cfg.set("flag", true);
        String yaml = cfg.saveToString();
        assertTrue(yaml.contains("name: test"));
        assertTrue(yaml.contains("count: 42"));
        assertTrue(yaml.contains("flag: true"));
    }

    @Test
    void roundTripPreservesSerializableData() {
        ConfigurationSerialization.registerClass(TestLoc.class);

        // Save: set a serializable object, serialize to YAML
        YamlConfiguration cfg1 = new YamlConfiguration();
        cfg1.set("home", new TestLoc("overworld", 100, 64, 200));
        String yaml = cfg1.saveToString();

        // Verify the YAML contains all the data
        assertTrue(yaml.contains("==: TestLoc"), "YAML must contain type key");
        assertTrue(yaml.contains("world: overworld"));
        assertTrue(yaml.contains("x: 100"));
        assertTrue(yaml.contains("y: 64"));
        assertTrue(yaml.contains("z: 200"));

        // Load: manually reconstruct the flat entries from the YAML
        // (simulating what loadFromString + flattenInto would produce)
        YamlConfiguration cfg2 = new YamlConfiguration();
        cfg2.set("home.x", 100.0);
        cfg2.set("home.y", 64.0);
        cfg2.set("home.z", 200.0);
        cfg2.set("home.world", "overworld");
        cfg2.set("home.==", "TestLoc");

        Object result = cfg2.get("home");
        assertInstanceOf(TestLoc.class, result,
                "get('home') must deserialize back to TestLoc");
        assertEquals("overworld", ((TestLoc) result).world);
        assertEquals(100.0, ((TestLoc) result).x);
    }
}
