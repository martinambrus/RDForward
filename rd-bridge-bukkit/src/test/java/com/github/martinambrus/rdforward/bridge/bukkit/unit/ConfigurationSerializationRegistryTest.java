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
 * Tests for the real ConfigurationSerialization registry (replaces the
 * previous no-op stub). HomeSpawnPlus calls registerClass() to make its
 * SerializableHome/Spawn/Player types discoverable during YAML loading.
 */
class ConfigurationSerializationRegistryTest {

    @AfterEach
    void cleanup() {
        ConfigurationSerialization.unregisterClass("TestAlias");
        ConfigurationSerialization.unregisterClass("NoAnnotationSer");
        ConfigurationSerialization.unregisterClass(AnnotatedSer.class);
        ConfigurationSerialization.unregisterClass(NoAnnotationSer.class);
    }

    @SerializableAs("TestAlias")
    public static class AnnotatedSer implements ConfigurationSerializable {
        @Override public Map<String, Object> serialize() { return new LinkedHashMap<>(); }
    }

    public static class NoAnnotationSer implements ConfigurationSerializable {
        @Override public Map<String, Object> serialize() { return new LinkedHashMap<>(); }
    }

    @Test
    void registerClassWithAnnotationStoresAlias() {
        ConfigurationSerialization.registerClass(AnnotatedSer.class);
        assertEquals(AnnotatedSer.class, ConfigurationSerialization.getClassByAlias("TestAlias"));
    }

    @Test
    void registerClassWithoutAnnotationUsesSimpleName() {
        ConfigurationSerialization.registerClass(NoAnnotationSer.class);
        assertEquals(NoAnnotationSer.class, ConfigurationSerialization.getClassByAlias("NoAnnotationSer"));
    }

    @Test
    void registerClassWithExplicitAlias() {
        ConfigurationSerialization.registerClass(AnnotatedSer.class, "CustomKey");
        assertEquals(AnnotatedSer.class, ConfigurationSerialization.getClassByAlias("CustomKey"));
        ConfigurationSerialization.unregisterClass("CustomKey");
    }

    @Test
    void getClassByAliasReturnsNullForUnknown() {
        assertNull(ConfigurationSerialization.getClassByAlias("nonexistent"));
    }

    @Test
    void getClassByAliasReturnsNullForNull() {
        assertNull(ConfigurationSerialization.getClassByAlias(null));
    }

    @Test
    void getAliasReturnsAnnotationValue() {
        assertEquals("TestAlias", ConfigurationSerialization.getAlias(AnnotatedSer.class));
    }

    @Test
    void getAliasReturnsSimpleNameWithoutAnnotation() {
        assertEquals("NoAnnotationSer", ConfigurationSerialization.getAlias(NoAnnotationSer.class));
    }

    @Test
    void getAliasReturnsNullForNull() {
        assertNull(ConfigurationSerialization.getAlias(null));
    }

    @Test
    void unregisterClassRemovesByAlias() {
        ConfigurationSerialization.registerClass(AnnotatedSer.class);
        ConfigurationSerialization.unregisterClass("TestAlias");
        assertNull(ConfigurationSerialization.getClassByAlias("TestAlias"));
    }

    @Test
    void unregisterClassRemovesByClass() {
        ConfigurationSerialization.registerClass(AnnotatedSer.class);
        ConfigurationSerialization.unregisterClass(AnnotatedSer.class);
        assertNull(ConfigurationSerialization.getClassByAlias("TestAlias"));
    }

    @Test
    void registerClassIdempotent() {
        ConfigurationSerialization.registerClass(AnnotatedSer.class);
        ConfigurationSerialization.registerClass(AnnotatedSer.class);
        assertEquals(AnnotatedSer.class, ConfigurationSerialization.getClassByAlias("TestAlias"));
    }
}
