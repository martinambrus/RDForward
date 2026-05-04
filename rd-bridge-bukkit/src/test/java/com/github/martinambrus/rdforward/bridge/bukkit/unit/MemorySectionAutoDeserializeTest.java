package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MemorySection.get() auto-deserialization of sub-sections
 * that contain a "==" key. HomeSpawnPlus's YAML storage reads homes
 * back as SerializableHome objects, not raw Maps or MemorySections.
 */
class MemorySectionAutoDeserializeTest {

    @SerializableAs("TestPoint")
    public static class TestPoint implements ConfigurationSerializable {
        final double x, y;
        @SuppressWarnings("unchecked")
        public TestPoint(Map map) {
            this.x = ((Number) map.getOrDefault("x", 0)).doubleValue();
            this.y = ((Number) map.getOrDefault("y", 0)).doubleValue();
        }
        @Override public Map<String, Object> serialize() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("x", x);
            m.put("y", y);
            return m;
        }
    }

    @AfterEach
    void cleanup() {
        ConfigurationSerialization.unregisterClass("TestPoint");
    }

    @Test
    void getAutoDeserializesSubsectionWithEqualsKey() {
        ConfigurationSerialization.registerClass(TestPoint.class);
        // Simulate what flattenInto produces after loading YAML with:
        //   point:
        //     ==: TestPoint
        //     x: 1.5
        //     y: 2.5
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("point.x", 1.5);
        cfg.set("point.y", 2.5);
        cfg.set("point.==", "TestPoint");

        Object result = cfg.get("point");
        assertInstanceOf(TestPoint.class, result,
                "get('point') must auto-deserialize when '==' key is present");
        assertEquals(1.5, ((TestPoint) result).x);
        assertEquals(2.5, ((TestPoint) result).y);
    }

    @Test
    void getReturnsSubsectionWhenNoTypeKey() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("section.x", 1);
        cfg.set("section.y", 2);
        // No "==" key — should return a MemorySection view
        Object result = cfg.get("section");
        assertInstanceOf(MemorySection.class, result,
                "without '==' key, get must return MemorySection view");
    }

    @Test
    void getReturnsSubsectionWhenAliasNotRegistered() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("obj.x", 1);
        cfg.set("obj.==", "UnregisteredType");
        // "==" key present but alias not registered — falls through to MemorySection
        Object result = cfg.get("obj");
        assertInstanceOf(MemorySection.class, result,
                "unregistered alias must fall back to MemorySection view");
    }

    @Test
    void getDeserializesDirectMapValueWithEqualsKey() {
        ConfigurationSerialization.registerClass(TestPoint.class);
        YamlConfiguration cfg = new YamlConfiguration();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("==", "TestPoint");
        map.put("x", 3.0);
        map.put("y", 4.0);
        cfg.set("direct", map);

        Object result = cfg.get("direct");
        assertInstanceOf(TestPoint.class, result,
                "direct Map value with '==' key must be auto-deserialized");
        assertEquals(3.0, ((TestPoint) result).x);
    }
}
