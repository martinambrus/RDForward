package com.github.martinambrus.rdforward.modloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure unit coverage for {@link LegacyGoogleRemapper} — exercises both
 * the prefix mapping table and the byte-level fast-path probe used by
 * {@link ModClassLoader} to skip the full ASM read/write cycle on
 * classes that don't reference the legacy package.
 */
class LegacyGoogleRemapperTest {

    private final LegacyGoogleRemapper remapper = new LegacyGoogleRemapper();

    @Test
    void mapsLegacyCommonPrefixToCanonical() {
        assertEquals("com/google/common/collect/ImmutableList",
                remapper.map("net/minecraft/util/com/google/common/collect/ImmutableList"));
        assertEquals("com/google/common/base/Preconditions",
                remapper.map("net/minecraft/util/com/google/common/base/Preconditions"));
    }

    @Test
    void mapsLegacyGsonPrefixToCanonical() {
        assertEquals("com/google/gson/Gson",
                remapper.map("net/minecraft/util/com/google/gson/Gson"));
        assertEquals("com/google/gson/JsonElement",
                remapper.map("net/minecraft/util/com/google/gson/JsonElement"));
    }

    @Test
    void leavesCanonicalNamesUntouched() {
        String canonical = "com/google/common/collect/ImmutableList";
        assertSame(canonical, remapper.map(canonical),
                "canonical names must round-trip without allocation");
    }

    @Test
    void leavesUnrelatedNamesUntouched() {
        assertEquals("java/lang/String", remapper.map("java/lang/String"));
        assertEquals("org/bukkit/Bukkit", remapper.map("org/bukkit/Bukkit"));
        // Similar prefix but not under com/google — no rewrite.
        assertEquals("net/minecraft/util/com/google/protobuf/Message",
                remapper.map("net/minecraft/util/com/google/protobuf/Message"));
    }

    @Test
    void mapHandlesNullSafely() {
        assertNull(remapper.map(null));
    }

    @Test
    void byteScanFindsLegacyPrefix() {
        byte[] bytes = ("garbage net/minecraft/util/com/google/common/collect/ImmutableList more")
                .getBytes();
        assertTrue(LegacyGoogleRemapper.classBytesContainLegacy(bytes));
    }

    @Test
    void byteScanRejectsCleanBytes() {
        byte[] bytes = "java/lang/String com/google/common/collect/ImmutableList".getBytes();
        assertFalse(LegacyGoogleRemapper.classBytesContainLegacy(bytes),
                "canonical references must not trip the legacy fast-path");
    }

    @Test
    void byteScanHandlesShortAndEmptyInputs() {
        assertFalse(LegacyGoogleRemapper.classBytesContainLegacy(null));
        assertFalse(LegacyGoogleRemapper.classBytesContainLegacy(new byte[0]));
        assertFalse(LegacyGoogleRemapper.classBytesContainLegacy("net".getBytes()));
    }

    @Test
    void byteScanLocatesPrefixAtBoundaries() {
        byte[] start = "net/minecraft/util/com/google_etc".getBytes();
        assertTrue(LegacyGoogleRemapper.classBytesContainLegacy(start),
                "prefix at offset 0 must be detected");

        byte[] end = ("padding " + "net/minecraft/util/com/google").getBytes();
        assertTrue(LegacyGoogleRemapper.classBytesContainLegacy(end),
                "prefix at end-of-buffer must be detected");
    }

    @Test
    void legacyBytesConstantMatchesPrefix() {
        assertArrayEquals("net/minecraft/util/com/google".getBytes(),
                LegacyGoogleRemapper.LEGACY_BYTES);
    }
}
