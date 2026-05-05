package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/** Pins Vault item lookup behaviour for the bridge stub. */
class VaultItemsTest {

    @Test
    void itemInfoHoldsFields() {
        ItemInfo info = new ItemInfo(Material.COBBLESTONE, (short) 0, "Cobblestone", new String[][]{{"cobblestone"}});
        assertSame(Material.COBBLESTONE, info.getType());
        assertEquals(0, info.getSubTypeId());
        assertEquals("Cobblestone", info.getName());
        assertEquals(Material.COBBLESTONE.getId(), info.getId());
        assertFalse(info.isDurable());
    }

    @Test
    void itemInfoToStackReturnsValidItemStack() {
        ItemInfo info = new ItemInfo(Material.STONE, (short) 2, "Polished Granite", null);
        ItemStack stack = info.toStack();
        assertNotNull(stack);
        assertSame(Material.STONE, stack.getType());
        assertEquals(1, stack.getAmount());
        assertEquals(2, stack.getDurability());
    }

    @Test
    void itemInfoToStackNullMaterialReturnsNull() {
        ItemInfo info = new ItemInfo(null, (short) 0, "Nothing", null);
        assertNull(info.toStack());
    }

    @Test
    void itemByStringNumericId() {
        ItemInfo info = Items.itemByString("4");
        assertNotNull(info, "numeric id 4 (cobblestone) must resolve");
        assertEquals(Material.COBBLESTONE, info.getType());
    }

    @Test
    void itemByStringNumericIdWithSubtype() {
        ItemInfo info = Items.itemByString("4:1");
        assertNotNull(info);
        assertEquals(Material.COBBLESTONE, info.getType());
        assertEquals(1, info.getSubTypeId());
    }

    @Test
    void itemByStringMaterialName() {
        ItemInfo info = Items.itemByString("cobblestone");
        assertNotNull(info, "'cobblestone' must resolve via name lookup");
        assertEquals(Material.COBBLESTONE, info.getType());
    }

    @Test
    void itemByStringPartialName() {
        ItemInfo info = Items.itemByString("cobble");
        assertNotNull(info, "partial name 'cobble' must resolve");
        assertEquals(Material.COBBLESTONE, info.getType());
    }

    @Test
    void itemByStringNullReturnsNull() {
        assertNull(Items.itemByString(null));
        assertNull(Items.itemByString(""));
    }

    @Test
    void itemByStringUnknownReturnsNull() {
        assertNull(Items.itemByString("zzzznonexistent"));
    }

    @Test
    void itemById() {
        ItemInfo info = Items.itemById(Material.STONE.getId());
        assertNotNull(info);
        assertEquals(Material.STONE, info.getType());
    }

    @Test
    void itemByIdWithSubtype() {
        ItemInfo info = Items.itemById(Material.STONE.getId(), (short) 1);
        assertNotNull(info);
        assertEquals(Material.STONE, info.getType());
        assertEquals(1, info.getSubTypeId());
    }

    @Test
    void itemByIdInvalidReturnsNull() {
        assertNull(Items.itemById(99999));
    }

    @Test
    void itemByStack() {
        ItemStack stack = new ItemStack(Material.DIRT, 3);
        ItemInfo info = Items.itemByStack(stack);
        assertNotNull(info);
        assertEquals(Material.DIRT, info.getType());
    }

    @Test
    void itemByStackNullReturnsNull() {
        assertNull(Items.itemByStack(null));
    }

    @Test
    void itemByStackWithDurability() {
        ItemStack stack = new ItemStack(Material.STONE, 1, (short) 3);
        ItemInfo info = Items.itemByStack(stack);
        assertNotNull(info);
        assertEquals(Material.STONE, info.getType());
        assertEquals(3, info.getSubTypeId());
    }

    @Test
    void itemByType() {
        ItemInfo info = Items.itemByType(Material.COBBLESTONE);
        assertNotNull(info);
        assertEquals(Material.COBBLESTONE, info.getType());
    }

    @Test
    void itemByTypeNullReturnsNull() {
        assertNull(Items.itemByType(null));
    }

    @Test
    void itemByNameList() {
        ArrayList<String> names = new ArrayList<>(Arrays.asList("cobblestone"));
        ItemInfo info = Items.itemByName(names);
        assertNotNull(info);
        assertEquals(Material.COBBLESTONE, info.getType());
    }

    @Test
    void getItemListIsNotEmpty() {
        assertFalse(Items.getItemList().isEmpty(), "item list must be populated from Material enum");
    }

    @Test
    void joinStringArray() {
        assertEquals("a, b, c", Items.join(new String[]{"a", "b", "c"}, ", "));
    }

    @Test
    void joinList() {
        assertEquals("x-y", Items.join(Arrays.asList("x", "y"), "-"));
    }

    @Test
    void joinEmptyReturnsEmpty() {
        assertEquals("", Items.join(new ArrayList<>(), ","));
    }

    @Test
    void itemByNamesArray() {
        ArrayList<String> names = new ArrayList<>(Arrays.asList("stone", "dirt"));
        ItemInfo[] results = Items.itemByNames(names, false);
        assertTrue(results.length >= 1);
    }

    @Test
    void itemsByName() {
        ItemInfo[] results = Items.itemsByName("stone", false);
        assertNotNull(results);
    }
}
