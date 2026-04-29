package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.meta.BridgeItemMeta;
import com.github.martinambrus.rdforward.bridge.bukkit.meta.BridgeSkullMeta;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the {@link ItemStack} meta backing introduced for Essentials's
 * {@code /skull}, {@code /book}, and {@code /lore} commands. The prior
 * stub returned {@code null} from {@link ItemStack#getItemMeta()};
 * Essentials's {@code Commandskull} casts the result to
 * {@link SkullMeta} and immediately calls {@link SkullMeta#hasOwner()},
 * so a null reference NPE'd before any block placement.
 *
 * <p>The contract validated here:
 * <ul>
 *   <li>SKULL_ITEM / PLAYER_HEAD return a non-null {@link SkullMeta};
 *       other materials return {@link ItemMeta}.</li>
 *   <li>{@code setItemMeta} replaces the backing; {@code hasItemMeta}
 *       reflects the live state.</li>
 *   <li>The copy constructor deep-clones the meta so mutating the
 *       clone does not bleed into the original.</li>
 *   <li>Skull owner round-trips via {@code setOwner / hasOwner / getOwner}.</li>
 * </ul>
 */
class BridgeItemMetaWiringTest {

    @Test
    void getItemMetaForSkullItemReturnsSkullMeta() {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta meta = skull.getItemMeta();
        assertNotNull(meta, "skull item must surface a non-null meta backing");
        assertInstanceOf(SkullMeta.class, meta,
                "SKULL_ITEM must back its meta with SkullMeta so plugin casts succeed");
    }

    @Test
    void getItemMetaForPlayerHeadReturnsSkullMeta() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        assertInstanceOf(SkullMeta.class, head.getItemMeta(),
                "PLAYER_HEAD (modern flattened id) must also back as SkullMeta");
    }

    @Test
    void getItemMetaForGenericMaterialReturnsItemMeta() {
        ItemStack stone = new ItemStack(Material.STONE);
        ItemMeta meta = stone.getItemMeta();
        assertNotNull(meta);
        assertFalse(meta instanceof SkullMeta,
                "non-skull materials must back with the generic ItemMeta, not SkullMeta");
        assertInstanceOf(BridgeItemMeta.class, meta);
    }

    @Test
    void getItemMetaIsStableAcrossCalls() {
        // Same instance on repeat reads — the Essentials /lore mutate-
        // without-setItemMeta pattern relies on this so changes stick.
        ItemStack stack = new ItemStack(Material.STONE);
        ItemMeta first = stack.getItemMeta();
        ItemMeta second = stack.getItemMeta();
        assertSame(first, second);
    }

    @Test
    void hasItemMetaTracksLazyInitialization() {
        ItemStack stack = new ItemStack(Material.STONE);
        // hasItemMeta is intentionally false until something reads or sets meta.
        assertFalse(stack.hasItemMeta());
        stack.getItemMeta();
        assertTrue(stack.hasItemMeta());
    }

    @Test
    void setItemMetaReplacesBackingAndReturnsTrue() {
        ItemStack stack = new ItemStack(Material.STONE);
        BridgeItemMeta replacement = new BridgeItemMeta();
        replacement.setDisplayName("custom");
        assertTrue(stack.setItemMeta(replacement));
        assertSame(replacement, stack.getItemMeta());
        assertEquals("custom", stack.getItemMeta().getDisplayName());
    }

    @Test
    void setItemMetaNullClearsBacking() {
        ItemStack stack = new ItemStack(Material.STONE);
        stack.getItemMeta();          // force lazy init
        assertTrue(stack.hasItemMeta());
        assertTrue(stack.setItemMeta(null));
        assertFalse(stack.hasItemMeta());
    }

    @Test
    void copyConstructorClonesMetaSoMutationsDoNotBleed() {
        ItemStack original = new ItemStack(Material.STONE);
        original.getItemMeta().setDisplayName("origname");

        ItemStack copy = new ItemStack(original);
        assertNotSame(original.getItemMeta(), copy.getItemMeta(),
                "copy ctor must clone meta so the two stacks are independent");
        copy.getItemMeta().setDisplayName("copied");
        assertEquals("origname", original.getItemMeta().getDisplayName());
        assertEquals("copied", copy.getItemMeta().getDisplayName());
    }

    @Test
    void skullOwnerRoundTrips() {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        assertFalse(meta.hasOwner());
        assertNull(meta.getOwner());
        assertTrue(meta.setOwner("Notch"),
                "setOwner returns true on legacy Bukkit; the bridge mirrors that contract");
        assertTrue(meta.hasOwner());
        assertEquals("Notch", meta.getOwner());
    }

    @Test
    void bridgeSkullMetaCloneCarriesOwner() {
        BridgeSkullMeta meta = new BridgeSkullMeta();
        meta.setOwner("Steve");
        meta.setDisplayName("§fSkull of Steve");

        SkullMeta cloned = meta.clone();
        assertNotSame(meta, cloned);
        assertEquals("Steve", cloned.getOwner());
        assertEquals("§fSkull of Steve", cloned.getDisplayName());
    }
}
