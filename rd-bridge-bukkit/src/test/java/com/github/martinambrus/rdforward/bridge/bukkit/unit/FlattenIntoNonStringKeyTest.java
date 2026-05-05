package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.configuration.MemorySection;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MemorySection#flattenInto} handling of non-String map keys.
 * SnakeYAML parses bare boolean literals (true/false/on/off) and integers as
 * non-String keys. TimeShift's config triggers this when keys are boolean values.
 */
class FlattenIntoNonStringKeyTest {

    @Test
    void booleanKeyFlattenedAsString() {
        Map<String, Object> source = new LinkedHashMap<>();
        // SnakeYAML produces Map<Object,Object>; simulate via raw put
        @SuppressWarnings("unchecked")
        Map<Object, Object> raw = (Map<Object, Object>) (Map<?, ?>) source;
        raw.put(true, "yes");
        raw.put(false, "no");

        Map<String, Object> dest = new HashMap<>();
        // Cast back to Map<String,Object> — same erasure trick as YamlConfiguration
        @SuppressWarnings("unchecked")
        Map<String, Object> typed = (Map<String, Object>) (Map<?, ?>) raw;
        MemorySection.flattenInto(typed, "", dest);

        assertEquals("yes", dest.get("true"));
        assertEquals("no", dest.get("false"));
    }

    @Test
    void integerKeyFlattenedAsString() {
        Map<String, Object> source = new LinkedHashMap<>();
        @SuppressWarnings("unchecked")
        Map<Object, Object> raw = (Map<Object, Object>) (Map<?, ?>) source;
        raw.put(42, "answer");

        Map<String, Object> dest = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> typed = (Map<String, Object>) (Map<?, ?>) raw;
        MemorySection.flattenInto(typed, "", dest);

        assertEquals("answer", dest.get("42"));
    }

    @Test
    void nestedMapWithBooleanKeyFlattened() {
        Map<String, Object> inner = new LinkedHashMap<>();
        @SuppressWarnings("unchecked")
        Map<Object, Object> rawInner = (Map<Object, Object>) (Map<?, ?>) inner;
        rawInner.put(true, "enabled");

        Map<String, Object> source = new LinkedHashMap<>();
        source.put("section", inner);

        Map<String, Object> dest = new HashMap<>();
        MemorySection.flattenInto(source, "", dest);

        assertEquals("enabled", dest.get("section.true"));
    }

    @Test
    void prefixedNonStringKeyFlattened() {
        Map<String, Object> source = new LinkedHashMap<>();
        @SuppressWarnings("unchecked")
        Map<Object, Object> raw = (Map<Object, Object>) (Map<?, ?>) source;
        raw.put(true, "val");

        Map<String, Object> dest = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> typed = (Map<String, Object>) (Map<?, ?>) raw;
        MemorySection.flattenInto(typed, "parent", dest);

        assertEquals("val", dest.get("parent.true"));
    }
}
