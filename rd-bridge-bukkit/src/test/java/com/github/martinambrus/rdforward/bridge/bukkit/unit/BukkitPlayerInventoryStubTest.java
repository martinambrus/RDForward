package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Real Bukkit's Player.getInventory() never returns null. WE 5.6.1's
 * BukkitPlayer.giveItem assumes that contract — without a non-null
 * stub the //wand command NPE'd before reaching addItem. RDForward
 * has no item-inventory model; the proxy returns safe defaults and
 * accepts every mutator silently.
 */
class BukkitPlayerInventoryStubTest {

    @AfterEach
    void wipeCache() {
        BukkitPlayer.evict("inv-test");
    }

    @Test
    void getInventoryIsNonNull() {
        Player p = newPlayer();
        assertNotNull(p.getInventory());
    }

    @Test
    void addItemReturnsEmptyMapNoLeftovers() {
        Player p = newPlayer();
        PlayerInventory inv = p.getInventory();
        // WE giveItem's call shape: pass an array of one stack.
        ItemStack[] toAdd = new ItemStack[] { new ItemStack(Material.STONE, 1) };
        java.util.HashMap leftovers = inv.addItem(toAdd);
        assertNotNull(leftovers);
        assertTrue(leftovers.isEmpty(), "no-op inventory must report no leftovers");
    }

    @Test
    void contentsArrayQueriesReturnRealisticallySized() {
        // Pre-API: the proxy returned length-0 arrays (no inventory
        // model). With the BukkitPlayerInventory bridge in place, sizes
        // match real Bukkit (41 total / 36 storage / 4 armor / 1 extra)
        // even when the backing rd-api Player has no inventory view —
        // entries are simply null. WE 5.6.1's giveItem-fallthrough path
        // walks {@code getContents()} so the array must be non-zero
        // length to avoid IndexOutOfBoundsException.
        Player p = newPlayer();
        PlayerInventory inv = p.getInventory();
        assertEquals(41, inv.getContents().length);
        assertEquals(36, inv.getStorageContents().length);
        assertEquals(4, inv.getArmorContents().length);
        assertEquals(1, inv.getExtraContents().length);
    }

    @Test
    void getViewersReturnsEmptyList() {
        Player p = newPlayer();
        assertNotNull(p.getInventory().getViewers());
        assertEquals(0, p.getInventory().getViewers().size());
    }

    @Test
    void mutatorsDoNotThrow() {
        // Plugins that hand items to the player (LP rewards, EssentialsX
        // /give, WE /wand fall-through) must succeed without exception.
        Player p = newPlayer();
        PlayerInventory inv = p.getInventory();
        inv.setItemInHand(new ItemStack(Material.OAK_PLANKS, 1));
        inv.setHelmet(null);
        inv.clear();
        inv.setHeldItemSlot(0);
    }

    @Test
    void inventoryReferenceIsStableAcrossCalls() {
        Player p = newPlayer();
        assertSame(p.getInventory(), p.getInventory(),
                "getInventory should return the same stub instance");
    }

    private static Player newPlayer() {
        com.github.martinambrus.rdforward.api.world.Location loc =
                new com.github.martinambrus.rdforward.api.world.Location("stub", 0, 0, 0, 0f, 0f);
        StubRdServer.StubRdPlayer backing = new StubRdServer.StubRdPlayer("inv-test", loc);
        return BukkitPlayer.create("inv-test", backing, null);
    }
}
