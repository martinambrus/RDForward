package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pre-Flattening plugins (WorldEdit 5.6.1's BukkitPlayer.giveItem,
 * EssentialsX pre-2.x) construct ItemStacks via numeric type ids:
 * {@code new ItemStack(int typeId, int amount)}. The auto-generated
 * stub omitted this signature — //wand crashed with NoSuchMethodError.
 */
class ItemStackLegacyCtorTest {

    @Test
    void intIntCtorResolvesMaterialByLegacyId() {
        // Material.STONE is legacy id 1.
        ItemStack s = new ItemStack(1, 5);
        assertSame(Material.STONE, s.getType());
        assertEquals(5, s.getAmount());
        assertEquals(1, s.getTypeId());
    }

    @Test
    void intIntCtorWithUnknownIdLeavesMaterialNull() {
        // 9999 sits beyond the highest id in our Material catalog
        // (records cap at 2267) so {@code getMaterial(9999)} returns
        // null. The numeric id survives via the {@code legacyId}
        // fallback so {@code getTypeId()} reports the original wire id
        // — InventoryPresets save/restore depends on this round-trip
        // for items outside the Material set.
        ItemStack wand = new ItemStack(9999, 1);
        assertNull(wand.getType());
        assertEquals(1, wand.getAmount());
        assertEquals(9999, wand.getTypeId(),
                "legacyId fallback preserves the wire id when Material is null");
    }

    @Test
    void intIntShortCtorCarriesDurability() {
        ItemStack s = new ItemStack(1, 1, (short) 7);
        assertEquals(1, s.getAmount());
        assertEquals((short) 7, s.getDurability());
    }

    @Test
    void setTypeIdResolvesAndUpdatesMaterial() {
        ItemStack s = new ItemStack(0, 1);
        assertSame(Material.AIR, s.getType());
        s.setTypeId(Material.COBBLESTONE.getId());
        assertSame(Material.COBBLESTONE, s.getType());
    }

    @Test
    void copyCtorClonesFields() {
        ItemStack a = new ItemStack(1, 3, (short) 9);
        ItemStack b = new ItemStack(a);
        assertNotNull(b);
        assertSame(a.getType(), b.getType());
        assertEquals(a.getAmount(), b.getAmount());
        assertEquals(a.getDurability(), b.getDurability());
        // Mutating the copy must not propagate back.
        b.setAmount(99);
        assertEquals(3, a.getAmount());
    }

    @Test
    void materialCtorSetsAmountToOne() {
        ItemStack s = new ItemStack(Material.STONE);
        assertSame(Material.STONE, s.getType());
        assertEquals(1, s.getAmount());
    }

    @Test
    void setTypeAndSetAmountReadBack() {
        ItemStack s = new ItemStack(Material.AIR);
        s.setType(Material.OAK_PLANKS);
        s.setAmount(64);
        s.setDurability((short) 13);
        assertSame(Material.OAK_PLANKS, s.getType());
        assertEquals(64, s.getAmount());
        assertEquals((short) 13, s.getDurability());
    }
}
