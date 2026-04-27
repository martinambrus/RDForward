package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.inventory.InventoryItem;
import com.github.martinambrus.rdforward.api.inventory.PlayerInventoryView;
import com.github.martinambrus.rdforward.api.player.Player;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayerInventory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the Bukkit inventory bridge translates between Bukkit's
 * 41-slot layout and rd-api's 45-slot wire layout, and that the
 * higher-level operations Bukkit plugins rely on (addItem merge-then-
 * fill, removeItem decrement, getArmorContents ordering) match real
 * Bukkit's contract closely enough to keep the surveyed plugins
 * (InventoryPresets, ChestShop, EssentialsX /give) working.
 */
class BukkitPlayerInventoryMappingTest {

    /**
     * Minimal in-memory {@link PlayerInventoryView} for the tests —
     * exercises the bridge in isolation without booting the server or
     * the InventoryAdapter dispatch chain.
     */
    private static final class FakeView implements PlayerInventoryView {
        final InventoryItem[] slots = new InventoryItem[45];
        int held = 36;

        FakeView() {
            for (int i = 0; i < 45; i++) slots[i] = InventoryItem.EMPTY;
        }

        @Override public int size() { return 45; }
        @Override public InventoryItem getSlot(int wireSlot) {
            if (wireSlot < 0 || wireSlot >= 45) return InventoryItem.EMPTY;
            InventoryItem it = slots[wireSlot];
            return it == null ? InventoryItem.EMPTY : it;
        }
        @Override public void setSlot(int wireSlot, InventoryItem item) {
            if (wireSlot < 0 || wireSlot >= 45) return;
            slots[wireSlot] = item == null ? InventoryItem.EMPTY : item;
        }
        @Override public InventoryItem[] getContents() {
            InventoryItem[] copy = new InventoryItem[45];
            System.arraycopy(slots, 0, copy, 0, 45);
            return copy;
        }
        @Override public void setContents(InventoryItem[] items) {
            int n = Math.min(items.length, 45);
            for (int i = 0; i < n; i++) {
                slots[i] = items[i] == null ? InventoryItem.EMPTY : items[i];
            }
            for (int i = n; i < 45; i++) slots[i] = InventoryItem.EMPTY;
        }
        @Override public void clear() {
            for (int i = 0; i < 45; i++) slots[i] = InventoryItem.EMPTY;
        }
        @Override public int getHeldSlot() { return held; }
        @Override public void setHeldSlot(int wireSlot) { held = wireSlot; }
    }

    private static final class FakePlayer implements Player {
        private final PlayerInventoryView view;
        FakePlayer(PlayerInventoryView v) { this.view = v; }
        @Override public String getName() { return "fake"; }
        @Override public com.github.martinambrus.rdforward.api.world.Location getLocation() { return null; }
        @Override public void teleport(com.github.martinambrus.rdforward.api.world.Location l) {}
        @Override public void sendMessage(String m) {}
        @Override public com.github.martinambrus.rdforward.api.version.ProtocolVersion getProtocolVersion() { return null; }
        @Override public boolean isOp() { return false; }
        @Override public void kick(String r) {}
        @Override public PlayerInventoryView getInventory() { return view; }
        @Override public InetSocketAddress getAddress() { return null; }
    }

    private static BukkitPlayerInventory inv(FakeView v) {
        FakePlayer player = new FakePlayer(v);
        return new BukkitPlayerInventory(() -> player);
    }

    // ---- Slot translation ----

    @Test
    void hotbarBukkitMapsToWire36To44() {
        // Bukkit slot 0 (hotbar pos 0) -> wire slot 36 (hotbar pos 0).
        FakeView v = new FakeView();
        BukkitPlayerInventory bukkit = inv(v);
        bukkit.setItem(0, new ItemStack(Material.STONE, 5));
        assertEquals(1, v.slots[36].itemId());
        assertEquals(5, v.slots[36].count());

        bukkit.setItem(8, new ItemStack(Material.STONE, 1));
        assertEquals(1, v.slots[44].itemId());
    }

    @Test
    void storageBukkit9To35MapsIdentity() {
        FakeView v = new FakeView();
        BukkitPlayerInventory bukkit = inv(v);
        bukkit.setItem(9, new ItemStack(Material.STONE, 3));
        bukkit.setItem(35, new ItemStack(Material.STONE, 7));
        assertEquals(3, v.slots[9].count());
        assertEquals(7, v.slots[35].count());
    }

    @Test
    void armorBukkit36To39MapsToWire8To5InReverse() {
        // Bukkit order is boots, leggings, chestplate, helmet — that's
        // why slot 36 is wire 8 (boots) and slot 39 is wire 5 (helmet).
        FakeView v = new FakeView();
        BukkitPlayerInventory bukkit = inv(v);
        bukkit.setItem(36, new ItemStack(Material.STONE, 1)); // boots-position
        bukkit.setItem(37, new ItemStack(Material.STONE, 2)); // legs-position
        bukkit.setItem(38, new ItemStack(Material.STONE, 3)); // chest-position
        bukkit.setItem(39, new ItemStack(Material.STONE, 4)); // helmet-position
        assertEquals(1, v.slots[8].count());
        assertEquals(2, v.slots[7].count());
        assertEquals(3, v.slots[6].count());
        assertEquals(4, v.slots[5].count());
    }

    @Test
    void offhandBukkit40DroppedSilently() {
        // Offhand is not modeled. setItem(40, ...) must not write any
        // wire slot and must not throw.
        FakeView v = new FakeView();
        BukkitPlayerInventory bukkit = inv(v);
        bukkit.setItem(40, new ItemStack(Material.STONE, 1));
        for (int i = 0; i < 45; i++) {
            assertTrue(v.slots[i].isEmpty(), "Wire slot " + i + " should remain empty");
        }
    }

    // ---- Round-trip ----

    @Test
    void getItemRoundTripsCountAndDamage() {
        FakeView v = new FakeView();
        BukkitPlayerInventory bukkit = inv(v);
        bukkit.setItem(0, new ItemStack(Material.STONE, 12));
        ItemStack got = bukkit.getItem(0);
        assertNotNull(got);
        assertEquals(1, got.getTypeId());
        assertEquals(12, got.getAmount());
    }

    @Test
    void setContentsRespectsBukkitLayout() {
        FakeView v = new FakeView();
        BukkitPlayerInventory bukkit = inv(v);
        ItemStack[] items = new ItemStack[41];
        items[0] = new ItemStack(Material.STONE, 5);  // hotbar 0 -> wire 36
        items[9] = new ItemStack(Material.STONE, 9);  // storage 9 -> wire 9
        items[39] = new ItemStack(Material.STONE, 1); // helmet -> wire 5
        bukkit.setContents(items);
        assertEquals(5, v.slots[36].count());
        assertEquals(9, v.slots[9].count());
        assertEquals(1, v.slots[5].count());
    }

    @Test
    void setContentsPreservesArmorWhenCallerSendsShorterArray() {
        // Older Bukkit code passes a 36-entry storage+hotbar slice.
        // setArmorContents pre-loaded armor must survive the partial
        // setContents call.
        FakeView v = new FakeView();
        BukkitPlayerInventory bukkit = inv(v);
        bukkit.setArmorContents(new ItemStack[] {
                new ItemStack(Material.STONE, 1), // boots -> wire 8
                null, null, null
        });
        ItemStack[] storageOnly = new ItemStack[36];
        storageOnly[0] = new ItemStack(Material.STONE, 5);
        bukkit.setContents(storageOnly);
        assertEquals(5, v.slots[36].count(), "hotbar must update");
        assertEquals(1, v.slots[8].count(), "boots must survive partial setContents");
    }

    @Test
    void getArmorContentsReturnsBootsLegsChestHelmInOrder() {
        FakeView v = new FakeView();
        BukkitPlayerInventory bukkit = inv(v);
        // Direct wire writes — slot 5=helm, 6=chest, 7=legs, 8=boots.
        v.slots[5] = new InventoryItem(298, 1, 0);
        v.slots[6] = new InventoryItem(299, 1, 0);
        v.slots[7] = new InventoryItem(300, 1, 0);
        v.slots[8] = new InventoryItem(301, 1, 0);
        ItemStack[] armor = bukkit.getArmorContents();
        assertEquals(4, armor.length);
        assertEquals(301, armor[0].getTypeId(), "armor[0] must be boots (wire 8)");
        assertEquals(300, armor[1].getTypeId(), "armor[1] must be leggings (wire 7)");
        assertEquals(299, armor[2].getTypeId(), "armor[2] must be chestplate (wire 6)");
        assertEquals(298, armor[3].getTypeId(), "armor[3] must be helmet (wire 5)");
    }

    // ---- addItem / removeItem semantics ----

    @Test
    void addItemFillsEmptySlot() {
        FakeView v = new FakeView();
        BukkitPlayerInventory bukkit = inv(v);
        HashMap leftover = bukkit.addItem(new ItemStack[] { new ItemStack(Material.STONE, 5) });
        assertTrue(leftover.isEmpty());
        // First scanned slot is Bukkit 0 -> wire 36.
        assertEquals(5, v.slots[36].count());
    }

    @Test
    void addItemMergesIntoExistingMatchingStack() {
        FakeView v = new FakeView();
        v.slots[36] = new InventoryItem(1, 10, 0); // pre-existing stone stack
        BukkitPlayerInventory bukkit = inv(v);
        HashMap leftover = bukkit.addItem(new ItemStack[] { new ItemStack(Material.STONE, 20) });
        assertTrue(leftover.isEmpty());
        // 10 already in slot 36 + 20 added = 30, no overflow.
        assertEquals(30, v.slots[36].count());
        // Nothing spilled into Bukkit slot 1 / wire 37.
        assertTrue(v.slots[37].isEmpty());
    }

    @Test
    void addItemReturnsOverflowWhenInventoryFull() {
        FakeView v = new FakeView();
        // Fill every storage+hotbar slot (Bukkit 0-35 = wire 36-44 +
        // wire 9-35) with a non-stackable load so addItem has nowhere
        // to go.
        for (int b = 0; b < 36; b++) {
            int wire = b <= 8 ? 36 + b : b;
            v.slots[wire] = new InventoryItem(264, 64, 0); // diamond, max stack
        }
        BukkitPlayerInventory bukkit = inv(v);
        HashMap leftover = bukkit.addItem(new ItemStack[] { new ItemStack(Material.STONE, 5) });
        assertEquals(1, leftover.size(), "overflow map must contain the rejected stack");
        ItemStack overflow = (ItemStack) leftover.get(0);
        assertNotNull(overflow);
        assertEquals(5, overflow.getAmount());
    }

    @Test
    void removeItemDecrementsExistingStack() {
        FakeView v = new FakeView();
        v.slots[36] = new InventoryItem(1, 10, 0);
        BukkitPlayerInventory bukkit = inv(v);
        HashMap leftover = bukkit.removeItem(new ItemStack[] { new ItemStack(Material.STONE, 4) });
        assertTrue(leftover.isEmpty());
        assertEquals(6, v.slots[36].count());
    }

    @Test
    void removeItemReportsLeftoverWhenMissing() {
        FakeView v = new FakeView();
        v.slots[36] = new InventoryItem(1, 2, 0); // only 2 stone available
        BukkitPlayerInventory bukkit = inv(v);
        HashMap leftover = bukkit.removeItem(new ItemStack[] { new ItemStack(Material.STONE, 5) });
        assertEquals(1, leftover.size());
        ItemStack residue = (ItemStack) leftover.get(0);
        assertEquals(3, residue.getAmount(), "should report 3 missing");
        assertTrue(v.slots[36].isEmpty(), "the 2 we did have must be consumed");
    }

    @Test
    void clearWipesEverySlot() {
        FakeView v = new FakeView();
        v.slots[36] = new InventoryItem(1, 5, 0);
        v.slots[5] = new InventoryItem(298, 1, 0);
        BukkitPlayerInventory bukkit = inv(v);
        bukkit.clear();
        for (int i = 0; i < 45; i++) {
            assertTrue(v.slots[i].isEmpty(), "wire slot " + i + " must be empty after clear");
        }
    }

    @Test
    void heldItemSlotMapsToHotbarOffset() {
        FakeView v = new FakeView();
        BukkitPlayerInventory bukkit = inv(v);
        bukkit.setHeldItemSlot(3);
        assertEquals(36 + 3, v.held);
        assertEquals(3, bukkit.getHeldItemSlot());
    }

    @Test
    void getItemInMainHandFollowsHeldSlot() {
        FakeView v = new FakeView();
        v.slots[40] = new InventoryItem(1, 1, 0); // hotbar pos 4
        v.held = 40;
        BukkitPlayerInventory bukkit = inv(v);
        ItemStack main = bukkit.getItemInMainHand();
        assertNotNull(main);
        assertEquals(1, main.getTypeId());
    }

    @Test
    void getItemInOffHandIsNull() {
        // Offhand isn't modeled — must return null, never throw.
        FakeView v = new FakeView();
        BukkitPlayerInventory bukkit = inv(v);
        assertNull(bukkit.getItemInOffHand());
    }
}
