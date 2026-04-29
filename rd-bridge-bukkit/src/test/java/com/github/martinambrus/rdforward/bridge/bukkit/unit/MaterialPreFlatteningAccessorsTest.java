package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pins the pre-Flattening {@link Material} accessors Essentials
 * Pre-2.14 still calls. Each method, pre-fix, did not exist and threw
 * {@link NoSuchMethodError} when its dispatcher fired:
 *
 * <ul>
 *   <li>{@code getMaxStackSize()} — Essentials's {@code ItemDb.get}
 *       clamps {@code /give} amounts. Returning 64 matches a single
 *       vanilla stack.</li>
 *   <li>{@code getMaxDurability()} — Essentials's {@code Commandhat}
 *       gates the held item: 0 ⇒ block-like ⇒ acceptable hat. 0 across
 *       the board folds tools into the block-like path.</li>
 *   <li>{@code getData()} — Essentials's {@code Worth.setPrice}
 *       branches on null to pick the durability-aware key path —
 *       matches what we want for damage-variant items.</li>
 * </ul>
 */
class MaterialPreFlatteningAccessorsTest {

    @Test
    void getMaxStackSizeIsSingleStack() {
        assertEquals(64, Material.STONE.getMaxStackSize());
        assertEquals(64, Material.COBBLESTONE.getMaxStackSize());
    }

    @Test
    void getMaxDurabilityIsZeroAcrossTheBoard() {
        // Real Bukkit: tools have non-zero values, blocks have 0.
        // RDForward folds everything to 0 — Essentials /hat treats them
        // all as block-like, which matches legacy server behaviour.
        assertEquals((short) 0, Material.STONE.getMaxDurability());
        assertEquals((short) 0, Material.SKULL_ITEM.getMaxDurability());
    }

    @Test
    void getDataReturnsNull() {
        // Pre-Flattening returned a MaterialData subclass (Sign.class
        // for SIGN, etc.); modern API dropped the per-material data
        // class hierarchy. Essentials reads null as "use the durability-
        // keyed worth entry path", which is what we want.
        assertNull(Material.STONE.getData());
        assertNull(Material.SKULL_ITEM.getData());
    }
}
