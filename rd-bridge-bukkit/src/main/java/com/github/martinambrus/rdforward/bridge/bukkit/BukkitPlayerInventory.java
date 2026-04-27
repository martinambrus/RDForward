package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.inventory.InventoryItem;
import com.github.martinambrus.rdforward.api.inventory.PlayerInventoryView;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Bukkit {@link PlayerInventory} backed by an rd-api
 * {@link PlayerInventoryView}. Mutations propagate through the view
 * to the connected client; reads pull live state.
 *
 * <p>Bukkit slot layout (modern Paper, size 41):
 * <ul>
 *   <li>0-8 = hotbar (maps to wire 36-44)</li>
 *   <li>9-35 = storage (maps to wire 9-35)</li>
 *   <li>36 = boots, 37 = leggings, 38 = chestplate, 39 = helmet
 *       (maps to wire 8, 7, 6, 5 respectively)</li>
 *   <li>40 = offhand (not modeled by RDForward; reads as empty,
 *       writes are silently dropped)</li>
 * </ul>
 *
 * <p>Item stacks round-trip through {@link Material#getId()} /
 * {@link Material#getMaterial(int)} — the same pre-Flattening Notch
 * IDs used by the wire protocol on Alpha/Beta/Netty pre-1.13. Items
 * outside RDForward's surfaced {@link Material} set become
 * {@link Material#AIR} in the Bukkit-facing array, but the underlying
 * wire-format ID survives the round trip in the view.
 */
public final class BukkitPlayerInventory implements PlayerInventory {

    /** Modern Paper inventory size. */
    private static final int SIZE = 41;
    private static final int OFFHAND_SLOT = 40;

    /**
     * Lazy reference to the rd-api {@code Player}. The supplier indirection
     * matters across reconnects: {@link BukkitPlayer.Handler} caches a
     * single Bukkit Player proxy per name and refreshes the {@code backing}
     * field on every login so plugins that hold onto {@code Player} or
     * {@code PlayerInventory} references keep observing the live session.
     */
    private final Supplier<com.github.martinambrus.rdforward.api.player.Player> backingSupplier;

    public BukkitPlayerInventory(Supplier<com.github.martinambrus.rdforward.api.player.Player> backingSupplier) {
        this.backingSupplier = backingSupplier;
    }

    private com.github.martinambrus.rdforward.api.player.Player backing() {
        return backingSupplier == null ? null : backingSupplier.get();
    }

    /**
     * Translate a Bukkit slot index to the rd-api wire slot. Returns
     * {@code -1} if the slot is the offhand (not modeled) or out of
     * range.
     */
    private static int toWireSlot(int bukkitSlot) {
        if (bukkitSlot < 0) return -1;
        if (bukkitSlot <= 8) return 36 + bukkitSlot;       // hotbar
        if (bukkitSlot <= 35) return bukkitSlot;            // storage 9-35 stays
        if (bukkitSlot == 36) return 8;                     // boots
        if (bukkitSlot == 37) return 7;                     // leggings
        if (bukkitSlot == 38) return 6;                     // chestplate
        if (bukkitSlot == 39) return 5;                     // helmet
        return -1;                                           // offhand or beyond
    }

    private static InventoryItem toItem(ItemStack stack) {
        if (stack == null) return InventoryItem.EMPTY;
        int id = stack.getTypeId();
        if (id <= 0 || stack.getAmount() <= 0) return InventoryItem.EMPTY;
        return new InventoryItem(id, stack.getAmount(), stack.getDurability());
    }

    private static ItemStack toStack(InventoryItem item) {
        if (item == null || item.isEmpty()) return null;
        return new ItemStack(item.itemId(), item.count(), (short) item.damage());
    }

    private PlayerInventoryView view() {
        com.github.martinambrus.rdforward.api.player.Player p = backing();
        return p == null ? null : p.getInventory();
    }

    @Override
    public int getSize() { return SIZE; }

    @Override
    public int getMaxStackSize() { return 64; }

    @Override
    public void setMaxStackSize(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null, "org.bukkit.inventory.PlayerInventory.setMaxStackSize(I)V");
    }

    @Override
    public ItemStack getItem(int slot) {
        PlayerInventoryView v = view();
        if (v == null) return null;
        int wire = toWireSlot(slot);
        if (wire < 0) return null;
        return toStack(v.getSlot(wire));
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        PlayerInventoryView v = view();
        if (v == null) return;
        int wire = toWireSlot(slot);
        if (wire < 0) return;
        v.setSlot(wire, toItem(stack));
    }

    @Override
    public void setItem(EquipmentSlot equipmentSlot, ItemStack stack) {
        PlayerInventoryView v = view();
        if (v == null || equipmentSlot == null) return;
        int wire = wireForEquipment(equipmentSlot);
        if (wire < 0) return;
        v.setSlot(wire, toItem(stack));
    }

    @Override
    public ItemStack getItem(EquipmentSlot equipmentSlot) {
        PlayerInventoryView v = view();
        if (v == null || equipmentSlot == null) return null;
        int wire = wireForEquipment(equipmentSlot);
        if (wire < 0) return null;
        return toStack(v.getSlot(wire));
    }

    private int wireForEquipment(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD     -> 5;
            case CHEST    -> 6;
            case LEGS     -> 7;
            case FEET     -> 8;
            case HAND     -> 36; // main hand defaults to hotbar 0
            case OFF_HAND -> -1;
            default       -> -1;
        };
    }

    @Override
    public ItemStack[] getContents() {
        PlayerInventoryView v = view();
        ItemStack[] out = new ItemStack[SIZE];
        if (v == null) return out;
        for (int b = 0; b < SIZE; b++) {
            int wire = toWireSlot(b);
            out[b] = wire < 0 ? null : toStack(v.getSlot(wire));
        }
        return out;
    }

    @Override
    public void setContents(ItemStack[] items) {
        PlayerInventoryView v = view();
        if (v == null || items == null) return;
        // Read-modify-write so we don't blow away armor when caller
        // passed only the storage+hotbar slice (older Bukkit getContents
        // returned 36 entries, modern returns 41).
        InventoryItem[] full = v.getContents();
        int n = Math.min(items.length, SIZE);
        for (int b = 0; b < n; b++) {
            int wire = toWireSlot(b);
            if (wire < 0) continue;
            full[wire] = toItem(items[b]);
        }
        v.setContents(full);
    }

    @Override
    public ItemStack[] getStorageContents() {
        PlayerInventoryView v = view();
        ItemStack[] out = new ItemStack[36];
        if (v == null) return out;
        // Bukkit storage = 0-35 (hotbar 0-8 + main 9-35).
        for (int b = 0; b < 36; b++) {
            int wire = toWireSlot(b);
            out[b] = wire < 0 ? null : toStack(v.getSlot(wire));
        }
        return out;
    }

    @Override
    public void setStorageContents(ItemStack[] items) {
        PlayerInventoryView v = view();
        if (v == null || items == null) return;
        InventoryItem[] full = v.getContents();
        int n = Math.min(items.length, 36);
        for (int b = 0; b < n; b++) {
            int wire = toWireSlot(b);
            if (wire < 0) continue;
            full[wire] = toItem(items[b]);
        }
        v.setContents(full);
    }

    @Override
    public ItemStack[] getArmorContents() {
        PlayerInventoryView v = view();
        ItemStack[] out = new ItemStack[4];
        if (v == null) return out;
        // Bukkit order: boots, leggings, chestplate, helmet => wire 8, 7, 6, 5.
        out[0] = toStack(v.getSlot(8));
        out[1] = toStack(v.getSlot(7));
        out[2] = toStack(v.getSlot(6));
        out[3] = toStack(v.getSlot(5));
        return out;
    }

    @Override
    public void setArmorContents(ItemStack[] items) {
        PlayerInventoryView v = view();
        if (v == null || items == null) return;
        InventoryItem[] full = v.getContents();
        if (items.length > 0) full[8] = toItem(items[0]); // boots
        if (items.length > 1) full[7] = toItem(items[1]); // leggings
        if (items.length > 2) full[6] = toItem(items[2]); // chestplate
        if (items.length > 3) full[5] = toItem(items[3]); // helmet
        v.setContents(full);
    }

    @Override
    public ItemStack[] getExtraContents() {
        // Offhand slot only — not modeled.
        return new ItemStack[1];
    }

    @Override
    public void setExtraContents(ItemStack[] items) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null, "org.bukkit.inventory.PlayerInventory.setExtraContents([Lorg/bukkit/inventory/ItemStack;)V");
    }

    @Override
    public ItemStack getHelmet()      { return view() == null ? null : toStack(view().getSlot(5)); }
    @Override
    public ItemStack getChestplate()  { return view() == null ? null : toStack(view().getSlot(6)); }
    @Override
    public ItemStack getLeggings()    { return view() == null ? null : toStack(view().getSlot(7)); }
    @Override
    public ItemStack getBoots()       { return view() == null ? null : toStack(view().getSlot(8)); }

    @Override
    public void setHelmet(ItemStack stack)     { setArmorWire(5, stack); }
    @Override
    public void setChestplate(ItemStack stack) { setArmorWire(6, stack); }
    @Override
    public void setLeggings(ItemStack stack)   { setArmorWire(7, stack); }
    @Override
    public void setBoots(ItemStack stack)      { setArmorWire(8, stack); }

    private void setArmorWire(int wire, ItemStack stack) {
        PlayerInventoryView v = view();
        if (v == null) return;
        v.setSlot(wire, toItem(stack));
    }

    @Override
    public ItemStack getItemInMainHand() {
        PlayerInventoryView v = view();
        return v == null ? null : toStack(v.getSlot(v.getHeldSlot()));
    }

    @Override
    public void setItemInMainHand(ItemStack stack) {
        PlayerInventoryView v = view();
        if (v == null) return;
        v.setSlot(v.getHeldSlot(), toItem(stack));
    }

    @Override
    public ItemStack getItemInOffHand() { return null; }

    @Override
    public void setItemInOffHand(ItemStack stack) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null, "org.bukkit.inventory.PlayerInventory.setItemInOffHand(Lorg/bukkit/inventory/ItemStack;)V");
    }

    @Override
    public ItemStack getItemInHand() { return getItemInMainHand(); }

    @Override
    public void setItemInHand(ItemStack stack) { setItemInMainHand(stack); }

    @Override
    public int getHeldItemSlot() {
        PlayerInventoryView v = view();
        if (v == null) return 0;
        return v.getHeldSlot() - 36;
    }

    @Override
    public void setHeldItemSlot(int slot) {
        PlayerInventoryView v = view();
        if (v == null) return;
        v.setHeldSlot(36 + slot);
    }

    @Override
    public org.bukkit.entity.HumanEntity getHolder() { return null; }

    @Override
    public org.bukkit.entity.HumanEntity getHolder(boolean useSnapshot) { return null; }

    // ---- Inventory queries (Bukkit). Implemented as best-effort over
    //      the live storage+hotbar slice; the full Bukkit semantics
    //      (max-stack-aware fills, item-flag matching, etc.) is not
    //      modeled. Plugins relying on these for game logic are noted
    //      via StubCallLog so they surface in the server console. ----

    /**
     * Add stacks to the inventory: merge into matching slots first (up
     * to {@link #getMaxStackSize()}), then place the remainder in empty
     * slots. Only the storage range (Bukkit 0-35) is targeted — armor
     * and offhand are not eligible for fill. Items that did not fit
     * are returned, keyed by their input-array index, matching real
     * Bukkit semantics that callers (InventoryPresets, ChestShop,
     * Essentials' /give) rely on.
     *
     * <p>One {@code setContents} dispatch covers all the writes so the
     * client receives a single full-window update instead of N
     * SetSlot packets.
     */
    @Override
    public HashMap addItem(ItemStack[] items) {
        HashMap<Integer, ItemStack> overflow = new HashMap<>();
        PlayerInventoryView v = view();
        if (v == null || items == null) return overflow;

        InventoryItem[] full = v.getContents();
        int maxStack = getMaxStackSize();

        for (int idx = 0; idx < items.length; idx++) {
            ItemStack stack = items[idx];
            if (stack == null || stack.getTypeId() <= 0 || stack.getAmount() <= 0) continue;
            int itemId = stack.getTypeId();
            int damage = stack.getDurability();
            int remaining = stack.getAmount();

            // Merge phase — top up existing matching stacks.
            for (int b = 0; b < 36 && remaining > 0; b++) {
                int wire = toWireSlot(b);
                if (wire < 0) continue;
                InventoryItem cur = full[wire];
                if (cur == null || cur.isEmpty()) continue;
                if (cur.itemId() != itemId || cur.damage() != damage) continue;
                int room = maxStack - cur.count();
                if (room <= 0) continue;
                int add = Math.min(room, remaining);
                full[wire] = new InventoryItem(itemId, cur.count() + add, damage);
                remaining -= add;
            }

            // Fill phase — drop the rest into empty slots.
            for (int b = 0; b < 36 && remaining > 0; b++) {
                int wire = toWireSlot(b);
                if (wire < 0) continue;
                InventoryItem cur = full[wire];
                if (cur != null && !cur.isEmpty()) continue;
                int add = Math.min(maxStack, remaining);
                full[wire] = new InventoryItem(itemId, add, damage);
                remaining -= add;
            }

            if (remaining > 0) {
                overflow.put(idx, new ItemStack(itemId, remaining, (short) damage));
            }
        }

        v.setContents(full);
        return overflow;
    }

    /**
     * Remove stacks from the inventory by iterating storage slots and
     * decrementing matching counts. Items the inventory didn't hold are
     * returned (keyed by input-array index) — same return contract as
     * Bukkit's {@code removeItem}.
     */
    @Override
    public HashMap removeItem(ItemStack[] items) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        PlayerInventoryView v = view();
        if (v == null || items == null) return leftover;

        InventoryItem[] full = v.getContents();

        for (int idx = 0; idx < items.length; idx++) {
            ItemStack stack = items[idx];
            if (stack == null || stack.getTypeId() <= 0 || stack.getAmount() <= 0) continue;
            int itemId = stack.getTypeId();
            int damage = stack.getDurability();
            int remaining = stack.getAmount();

            for (int b = 0; b < 36 && remaining > 0; b++) {
                int wire = toWireSlot(b);
                if (wire < 0) continue;
                InventoryItem cur = full[wire];
                if (cur == null || cur.isEmpty()) continue;
                if (cur.itemId() != itemId || cur.damage() != damage) continue;
                int take = Math.min(cur.count(), remaining);
                int newCount = cur.count() - take;
                full[wire] = newCount > 0 ? new InventoryItem(itemId, newCount, damage) : InventoryItem.EMPTY;
                remaining -= take;
            }

            if (remaining > 0) {
                leftover.put(idx, new ItemStack(itemId, remaining, (short) damage));
            }
        }

        v.setContents(full);
        return leftover;
    }

    @Override
    public HashMap removeItemAnySlot(ItemStack[] items) {
        return removeItem(items);
    }

    @Override
    public boolean contains(Material material) { return first(material) != -1; }

    @Override
    public boolean contains(ItemStack item) { return first(item) != -1; }

    @Override
    public boolean contains(Material material, int amount) {
        if (material == null) return false;
        int found = 0;
        for (int b = 0; b < SIZE; b++) {
            ItemStack s = getItem(b);
            if (s != null && s.getType() == material) found += s.getAmount();
            if (found >= amount) return true;
        }
        return false;
    }

    @Override
    public boolean contains(ItemStack item, int amount) {
        if (item == null) return false;
        int found = 0;
        for (int b = 0; b < SIZE; b++) {
            ItemStack s = getItem(b);
            if (s != null && s.getTypeId() == item.getTypeId()) found += s.getAmount();
            if (found >= amount) return true;
        }
        return false;
    }

    @Override
    public boolean containsAtLeast(ItemStack item, int amount) { return contains(item, amount); }

    @Override
    public HashMap all(Material material) {
        HashMap<Integer, ItemStack> out = new HashMap<>();
        if (material == null) return out;
        for (int b = 0; b < SIZE; b++) {
            ItemStack s = getItem(b);
            if (s != null && s.getType() == material) out.put(b, s);
        }
        return out;
    }

    @Override
    public HashMap all(ItemStack item) {
        HashMap<Integer, ItemStack> out = new HashMap<>();
        if (item == null) return out;
        for (int b = 0; b < SIZE; b++) {
            ItemStack s = getItem(b);
            if (s != null && s.getTypeId() == item.getTypeId()) out.put(b, s);
        }
        return out;
    }

    @Override
    public int first(Material material) {
        if (material == null) return -1;
        for (int b = 0; b < SIZE; b++) {
            ItemStack s = getItem(b);
            if (s != null && s.getType() == material) return b;
        }
        return -1;
    }

    @Override
    public int first(ItemStack item) {
        if (item == null) return -1;
        for (int b = 0; b < SIZE; b++) {
            ItemStack s = getItem(b);
            if (s != null && s.getTypeId() == item.getTypeId()) return b;
        }
        return -1;
    }

    @Override
    public int firstEmpty() {
        for (int b = 0; b < SIZE; b++) {
            ItemStack s = getItem(b);
            if (s == null || s.getTypeId() <= 0) return b;
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        for (int b = 0; b < SIZE; b++) {
            ItemStack s = getItem(b);
            if (s != null && s.getTypeId() > 0 && s.getAmount() > 0) return false;
        }
        return true;
    }

    @Override
    public void remove(Material material) {
        if (material == null) return;
        for (int b = 0; b < SIZE; b++) {
            ItemStack s = getItem(b);
            if (s != null && s.getType() == material) setItem(b, null);
        }
    }

    @Override
    public void remove(ItemStack item) {
        if (item == null) return;
        for (int b = 0; b < SIZE; b++) {
            ItemStack s = getItem(b);
            if (s != null && s.getTypeId() == item.getTypeId()) setItem(b, null);
        }
    }

    @Override
    public void clear(int slot) { setItem(slot, null); }

    @Override
    public void clear() {
        PlayerInventoryView v = view();
        if (v != null) v.clear();
    }

    @Override
    public int close() { return 0; }

    @Override
    public java.util.List getViewers() { return Collections.emptyList(); }

    @Override
    public org.bukkit.event.inventory.InventoryType getType() {
        return org.bukkit.event.inventory.InventoryType.PLAYER;
    }

    @Override
    public org.bukkit.Location getLocation() { return null; }

    @Override
    public ListIterator iterator() { return iterator(0); }

    @Override
    public ListIterator iterator(int startIndex) {
        return new InventoryListIterator(startIndex);
    }

    /** Snapshot iterator — does not reflect concurrent mutations. */
    private final class InventoryListIterator implements ListIterator<ItemStack> {
        private int idx;

        InventoryListIterator(int startIndex) {
            this.idx = Math.max(0, Math.min(startIndex, SIZE));
        }

        @Override public boolean hasNext()   { return idx < SIZE; }
        @Override public ItemStack next()    {
            if (idx >= SIZE) throw new NoSuchElementException();
            return getItem(idx++);
        }
        @Override public boolean hasPrevious() { return idx > 0; }
        @Override public ItemStack previous() {
            if (idx <= 0) throw new NoSuchElementException();
            return getItem(--idx);
        }
        @Override public int nextIndex()     { return idx; }
        @Override public int previousIndex() { return idx - 1; }
        @Override public void remove()       { /* no-op */ }
        @Override public void set(ItemStack stack) {
            int target = idx == 0 ? 0 : idx - 1;
            setItem(target, stack);
        }
        @Override public void add(ItemStack stack) { /* no-op */ }
    }
}
