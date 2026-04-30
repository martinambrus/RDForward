// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * Minimal {@link Inventory} backing for {@link Bukkit#createInventory}.
 * EssentialsX's {@code /disposal} hands one of these to the player's
 * {@code openInventory} purely for the throwaway item-dump UI; RDForward
 * has no client-side container UI so the inventory is never actually
 * opened. The stub still needs to be a non-null {@link Inventory} so the
 * call chain doesn't NPE — array-backed storage keeps any item the
 * caller writes addressable on {@code getItem} / {@code setItem}.
 *
 * <p>Most surface methods are no-op or return safe defaults: there is
 * no viewer set, no location, and item-count helpers walk the local
 * array. {@code addItem} returns the input as "leftover" since the
 * inventory has no real placement model.
 */
public final class StubInventory implements Inventory {
    private final InventoryHolder holder;
    private final ItemStack[] items;
    private final InventoryType type;
    private final String title;
    private int maxStackSize = 64;

    public StubInventory(InventoryHolder holder, int size, String title) {
        this.holder = holder;
        this.items = new ItemStack[Math.max(0, size)];
        this.type = InventoryType.CHEST;
        this.title = title == null ? "" : title;
    }

    @Override public int getSize() { return items.length; }
    @Override public int getMaxStackSize() { return maxStackSize; }
    @Override public void setMaxStackSize(int n) { this.maxStackSize = n; }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= items.length) return null;
        return items[slot];
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        if (slot < 0 || slot >= items.length) return;
        items[slot] = item;
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack[] stacks) {
        // No real inventory model — every input is "leftover".
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        if (stacks != null) {
            for (int i = 0; i < stacks.length; i++) {
                if (stacks[i] != null) leftover.put(i, stacks[i]);
            }
        }
        return leftover;
    }

    @Override
    public HashMap<Integer, ItemStack> removeItem(ItemStack[] stacks) {
        HashMap<Integer, ItemStack> notRemoved = new HashMap<>();
        if (stacks != null) {
            for (int i = 0; i < stacks.length; i++) {
                if (stacks[i] != null) notRemoved.put(i, stacks[i]);
            }
        }
        return notRemoved;
    }

    @Override
    public HashMap<Integer, ItemStack> removeItemAnySlot(ItemStack[] stacks) {
        return removeItem(stacks);
    }

    @Override public ItemStack[] getContents() { return items.clone(); }

    @Override
    public void setContents(ItemStack[] src) {
        if (src == null) return;
        int n = Math.min(src.length, items.length);
        System.arraycopy(src, 0, items, 0, n);
        for (int i = n; i < items.length; i++) items[i] = null;
    }

    @Override public ItemStack[] getStorageContents() { return items.clone(); }
    @Override public void setStorageContents(ItemStack[] src) { setContents(src); }

    @Override public boolean contains(Material material) { return false; }
    @Override public boolean contains(ItemStack item) { return false; }
    @Override public boolean contains(Material material, int amount) { return false; }
    @Override public boolean contains(ItemStack item, int amount) { return false; }
    @Override public boolean containsAtLeast(ItemStack item, int amount) { return false; }

    @Override public HashMap<Integer, ItemStack> all(Material material) { return new HashMap<>(); }
    @Override public HashMap<Integer, ItemStack> all(ItemStack item) { return new HashMap<>(); }

    @Override public int first(Material material) { return -1; }
    @Override public int first(ItemStack item) { return -1; }

    @Override
    public int firstEmpty() {
        for (int i = 0; i < items.length; i++) if (items[i] == null) return i;
        return -1;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : items) if (item != null) return false;
        return true;
    }

    @Override public void remove(Material material) { /* no-op */ }
    @Override public void remove(ItemStack item) { /* no-op */ }

    @Override
    public void clear(int slot) {
        if (slot >= 0 && slot < items.length) items[slot] = null;
    }

    @Override
    public void clear() {
        Arrays.fill(items, null);
    }

    @Override public int close() { return 0; }
    @Override public List getViewers() { return new ArrayList<>(); }
    @Override public InventoryType getType() { return type; }
    @Override public InventoryHolder getHolder() { return holder; }
    @Override public InventoryHolder getHolder(boolean useSnapshot) { return holder; }

    @Override public ListIterator<ItemStack> iterator() { return Arrays.asList(items).listIterator(); }
    @Override public ListIterator<ItemStack> iterator(int index) { return Arrays.asList(items).listIterator(index); }

    @Override public org.bukkit.Location getLocation() { return null; }
}
