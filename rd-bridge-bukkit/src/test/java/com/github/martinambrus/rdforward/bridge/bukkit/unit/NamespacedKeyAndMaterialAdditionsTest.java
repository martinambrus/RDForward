package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link NamespacedKey} round-trips real values (the previous
 * stub returned null from every getter, corrupting CoreProtect's audit
 * DB) and the {@link Material} accessors CoreProtect drives during
 * audit/rollback (isAir, getKey, createBlockData, hasGravity, and the
 * defensive guard on {@link Material#getMaterial(int)}).
 */
class NamespacedKeyAndMaterialAdditionsTest {

    /* ---- NamespacedKey ---- */

    @Test
    void namespacedKeyRoundTripsCtorArgs() {
        NamespacedKey k = new NamespacedKey("mc", "stone");
        assertEquals("mc", k.getNamespace());
        assertEquals("stone", k.getKey());
        assertEquals("mc:stone", k.toString());
    }

    @Test
    void namespacedKeyNullNamespaceFallsBackToMinecraft() {
        // Defensive — null namespace becomes "minecraft" so DB-persisted
        // keys never carry "null:" prefixes.
        NamespacedKey k = new NamespacedKey((String) null, "stone");
        assertEquals(NamespacedKey.MINECRAFT, k.getNamespace());
    }

    @Test
    void namespacedKeyNullKeyFallsBackToEmpty() {
        NamespacedKey k = new NamespacedKey("mc", null);
        assertEquals("", k.getKey());
        assertEquals("mc:", k.toString());
    }

    @Test
    void namespacedKeyEqualsAndHashCodeUseBothFields() {
        NamespacedKey a = new NamespacedKey("minecraft", "stone");
        NamespacedKey b = new NamespacedKey("minecraft", "stone");
        NamespacedKey c = new NamespacedKey("minecraft", "dirt");
        NamespacedKey d = new NamespacedKey("custom", "stone");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, d);
    }

    @Test
    void minecraftFactoryUsesMinecraftNamespace() {
        NamespacedKey k = NamespacedKey.minecraft("stone");
        assertEquals("minecraft", k.getNamespace());
        assertEquals("stone", k.getKey());
    }

    @Test
    void fromStringSplitsOnFirstColon() {
        NamespacedKey k = NamespacedKey.fromString("mc:stone");
        assertNotNull(k);
        assertEquals("mc", k.getNamespace());
        assertEquals("stone", k.getKey());
    }

    @Test
    void fromStringWithoutColonUsesMinecraftDefault() {
        NamespacedKey k = NamespacedKey.fromString("stone");
        assertNotNull(k);
        assertEquals(NamespacedKey.MINECRAFT, k.getNamespace());
        assertEquals("stone", k.getKey());
    }

    @Test
    void fromStringNullOrEmptyReturnsNull() {
        // Real Bukkit returns null for unparseable input — must not NPE.
        assertNull(NamespacedKey.fromString(null));
        assertNull(NamespacedKey.fromString(""));
    }

    /* ---- Material accessors used by CoreProtect audit/rollback ---- */

    @Test
    void getKeyReturnsLowercaseMinecraftNamespacedKey() {
        // CoreProtect persists this exact value in its audit DB. Format
        // must match upstream: minecraft:<lowercase_name>.
        NamespacedKey k = Material.STONE.getKey();
        assertEquals(NamespacedKey.MINECRAFT, k.getNamespace());
        assertEquals("stone", k.getKey());
        assertEquals("minecraft:stone", k.toString());
    }

    @Test
    void isAirOnlyTrueForAir() {
        assertTrue(Material.AIR.isAir());
        assertFalse(Material.STONE.isAir());
        assertFalse(Material.DIRT.isAir());
    }

    @Test
    void createBlockDataReturnsBlockDataMatchingMaterial() {
        BlockData d = Material.STONE.createBlockData();
        assertNotNull(d);
        assertSame(Material.STONE, d.getMaterial());
    }

    @Test
    void createBlockDataWithSuffixIgnoresSuffix() {
        // RDForward has no per-block state model, so the property suffix
        // is dropped — the result still represents the bare material.
        BlockData d = Material.STONE.createBlockData("[facing=north]");
        assertSame(Material.STONE, d.getMaterial());
    }

    @Test
    void hasGravityCoversFallingBlocks() {
        // CoreProtect walks above a placement and gravityScans this set.
        assertTrue(Material.SAND.hasGravity());
        assertTrue(Material.GRAVEL.hasGravity());
        assertTrue(Material.ANVIL.hasGravity());
        assertFalse(Material.STONE.hasGravity());
        assertFalse(Material.DIRT.hasGravity());
        assertFalse(Material.AIR.hasGravity());
    }

    @Test
    void getMaterialIntNegativeReturnsNullWithoutScanning() {
        // Many synthesised Material entries (BOW, CROSSBOW, all
        // CoreProtect refs) carry legacyId=-1. A naive linear scan
        // would match the first such entry — guard returns null instead
        // so legacy /give 0 / WorldEdit /set -1 fail cleanly.
        assertNull(Material.getMaterial(-1));
        assertNull(Material.getMaterial(-9999));
    }

    @Test
    void getMaterialIntZeroResolvesToAir() {
        assertSame(Material.AIR, Material.getMaterial(0));
    }
}
