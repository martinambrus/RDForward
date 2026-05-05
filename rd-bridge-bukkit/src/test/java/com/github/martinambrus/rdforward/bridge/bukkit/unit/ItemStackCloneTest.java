package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pins ItemStack.clone() — must return a distinct copy, not null. */
class ItemStackCloneTest {

    @Test
    void cloneReturnsNonNull() {
        ItemStack original = new ItemStack(Material.COBBLESTONE, 5);
        ItemStack copy = original.clone();
        assertNotNull(copy, "clone() must never return null");
    }

    @Test
    void clonePreservesType() {
        ItemStack original = new ItemStack(Material.STONE, 1);
        assertEquals(Material.STONE, original.clone().getType());
    }

    @Test
    void clonePreservesAmount() {
        ItemStack original = new ItemStack(Material.DIRT, 64);
        assertEquals(64, original.clone().getAmount());
    }

    @Test
    void clonePreservesDurability() {
        ItemStack original = new ItemStack(Material.STONE, 1, (short) 3);
        assertEquals(3, original.clone().getDurability());
    }

    @Test
    void cloneReturnsDistinctInstance() {
        ItemStack original = new ItemStack(Material.COBBLESTONE, 10);
        ItemStack copy = original.clone();
        assertNotSame(original, copy, "clone must return a new instance");
        copy.setAmount(99);
        assertEquals(10, original.getAmount(), "mutating the clone must not affect the original");
    }

    @Test
    void clonePreservesMeta() {
        ItemStack original = new ItemStack(Material.STONE, 1);
        ItemMeta meta = original.getItemMeta();
        meta.setDisplayName("TestName");
        original.setItemMeta(meta);

        ItemStack copy = original.clone();
        ItemMeta copyMeta = copy.getItemMeta();
        assertEquals("TestName", copyMeta.getDisplayName());
    }

    @Test
    void cloneMetaIsIndependent() {
        ItemStack original = new ItemStack(Material.COBBLESTONE, 1);
        ItemMeta meta = original.getItemMeta();
        meta.setDisplayName("Original");
        original.setItemMeta(meta);

        ItemStack copy = original.clone();
        copy.getItemMeta().setDisplayName("Clone");

        assertEquals("Original", original.getItemMeta().getDisplayName(),
                "mutating clone's meta must not affect original's meta");
    }

    @Test
    void cloneNullMetaIsSafe() {
        ItemStack original = new ItemStack(Material.DIRT, 1);
        // no meta set — internal meta field stays null
        ItemStack copy = original.clone();
        assertNotNull(copy);
        assertFalse(copy.hasItemMeta());
    }
}
