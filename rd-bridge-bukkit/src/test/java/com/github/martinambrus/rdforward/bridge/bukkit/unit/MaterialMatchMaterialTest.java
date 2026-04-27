package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pinning regression test for LogBlock 1.41, whose {@code Config}
 * boots through {@code Material.matchMaterial(id)} on every entry of
 * the bundled {@code materials.yml} (default values include numeric
 * ids like {@code "0"}). The previous stub only had
 * {@link Material#getMaterial(String)} which throws on numeric input.
 *
 * <p>Also covers WorldEdit 5.6.1's
 * {@code BukkitWorld.matchMaterial("MINECRAFT:STONE")} call site,
 * which trims the {@code minecraft:} prefix before resolving the enum.
 */
class MaterialMatchMaterialTest {

    @Test
    void numericStringFallsBackToLegacyId() {
        assertEquals(Material.AIR, Material.matchMaterial("0"));
        assertEquals(Material.STONE, Material.matchMaterial("1"));
        assertEquals(Material.COBBLESTONE, Material.matchMaterial("4"));
    }

    @Test
    void nameLookupIsCaseInsensitive() {
        assertEquals(Material.STONE, Material.matchMaterial("stone"));
        assertEquals(Material.STONE, Material.matchMaterial("STONE"));
        assertEquals(Material.STONE, Material.matchMaterial("Stone"));
    }

    @Test
    void minecraftPrefixIsStripped() {
        assertEquals(Material.STONE, Material.matchMaterial("minecraft:stone"));
        assertEquals(Material.STONE, Material.matchMaterial("MINECRAFT:STONE"));
    }

    @Test
    void spacesAndHyphensCoerceToUnderscore() {
        assertEquals(Material.OAK_PLANKS, Material.matchMaterial("oak planks"));
        assertEquals(Material.OAK_PLANKS, Material.matchMaterial("oak-planks"));
    }

    @Test
    void inputIsTrimmed() {
        assertEquals(Material.STONE, Material.matchMaterial("  stone  "));
    }

    @Test
    void unknownNameReturnsNull() {
        assertNull(Material.matchMaterial("nonsense_block_42"));
    }

    @Test
    void unknownNumericIdReturnsNull() {
        assertNull(Material.matchMaterial("9999"));
    }

    @Test
    void nullInputReturnsNull() {
        assertNull(Material.matchMaterial(null));
    }

    @Test
    void blankInputReturnsNull() {
        assertNull(Material.matchMaterial(""));
        assertNull(Material.matchMaterial("   "));
    }

    @Test
    void legacyFlagOverloadIgnoresFlag() {
        assertEquals(Material.STONE, Material.matchMaterial("stone", true));
        assertEquals(Material.STONE, Material.matchMaterial("stone", false));
        assertEquals(Material.AIR, Material.matchMaterial("0", true));
    }
}
