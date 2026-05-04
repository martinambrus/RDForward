package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ConfigurationSerialization.deserializeObject() — resolves a
 * Map with a "==" key back to the registered class via static
 * deserialize method or (Map) constructor fallback.
 */
class ConfigurationSerializationDeserializeTest {

    @AfterEach
    void cleanup() {
        ConfigurationSerialization.unregisterClass("StaticSer");
        ConfigurationSerialization.unregisterClass("CtorSer");
        ConfigurationSerialization.unregisterClass("BothSer");
    }

    @SerializableAs("StaticSer")
    public static class StaticSer implements ConfigurationSerializable {
        final String value;
        StaticSer(String value) { this.value = value; }
        @Override public Map<String, Object> serialize() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("value", value);
            return m;
        }
        public static ConfigurationSerializable deserialize(Map map) {
            return new StaticSer(String.valueOf(map.getOrDefault("value", "")));
        }
    }

    @SerializableAs("CtorSer")
    public static class CtorSer implements ConfigurationSerializable {
        final String value;
        @SuppressWarnings("unchecked")
        public CtorSer(Map map) { this.value = String.valueOf(map.getOrDefault("value", "")); }
        @Override public Map<String, Object> serialize() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("value", value);
            return m;
        }
    }

    @SerializableAs("BothSer")
    public static class BothSer implements ConfigurationSerializable {
        final String source;
        @SuppressWarnings("unchecked")
        public BothSer(Map map) { this.source = "constructor"; }
        private BothSer(String source) { this.source = source; }
        @Override public Map<String, Object> serialize() { return new LinkedHashMap<>(); }
        public static ConfigurationSerializable deserialize(Map<String, ?> map) {
            return new BothSer("static");
        }
    }

    @Test
    void deserializeObjectResolvesStaticDeserializeMethod() {
        ConfigurationSerialization.registerClass(StaticSer.class);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("==", "StaticSer");
        map.put("value", "hello");
        Object result = ConfigurationSerialization.deserializeObject(map);
        assertInstanceOf(StaticSer.class, result);
        assertEquals("hello", ((StaticSer) result).value);
    }

    @Test
    void deserializeObjectFallsBackToMapConstructor() {
        ConfigurationSerialization.registerClass(CtorSer.class);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("==", "CtorSer");
        map.put("value", "world");
        Object result = ConfigurationSerialization.deserializeObject(map);
        assertInstanceOf(CtorSer.class, result);
        assertEquals("world", ((CtorSer) result).value);
    }

    @Test
    void deserializeObjectReturnsNullForUnregisteredAlias() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("==", "Unknown");
        assertNull(ConfigurationSerialization.deserializeObject(map));
    }

    @Test
    void deserializeObjectReturnsNullForMissingTypeKey() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("value", "x");
        assertNull(ConfigurationSerialization.deserializeObject(map));
    }

    @Test
    void deserializeObjectReturnsNullForNullMap() {
        assertNull(ConfigurationSerialization.deserializeObject(null));
    }

    @Test
    void deserializeObjectPrefersStaticMethodOverConstructor() {
        ConfigurationSerialization.registerClass(BothSer.class);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("==", "BothSer");
        Object result = ConfigurationSerialization.deserializeObject(map);
        assertInstanceOf(BothSer.class, result);
        assertEquals("static", ((BothSer) result).source,
                "static deserialize method must win over Map constructor");
    }
}
