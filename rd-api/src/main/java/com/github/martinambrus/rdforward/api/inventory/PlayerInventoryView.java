package com.github.martinambrus.rdforward.api.inventory;

/**
 * Public view onto a connected player's inventory state. Mutations
 * update server-side tracking and immediately push the resulting state
 * to the player's client (per-version SetSlot or full-window dispatch,
 * gated by {@code Capability.INVENTORY}).
 *
 * <p>Slot indices follow the Minecraft wire-protocol layout (45 slots):
 * <ul>
 *   <li>0 = craft output</li>
 *   <li>1-4 = craft grid</li>
 *   <li>5-8 = armor (helmet, chestplate, leggings, boots)</li>
 *   <li>9-35 = main inventory</li>
 *   <li>36-44 = hotbar (slot 36 is hotbar position 0)</li>
 * </ul>
 *
 * <p>Bridges that surface a different slot convention (e.g. Bukkit's
 * 0-8 hotbar, 9-35 main, 36-39 armor) are responsible for translating
 * to and from this layout.
 */
public interface PlayerInventoryView {

    /** @return number of wire slots; always 45 for the player inventory. */
    int size();

    /** @return contents of the given wire slot, or {@link InventoryItem#EMPTY} when empty. Never null. */
    InventoryItem getSlot(int wireSlot);

    /** Replace the contents of a single wire slot and push a SetSlot to the owner. */
    void setSlot(int wireSlot, InventoryItem item);

    /** @return immutable 45-slot snapshot in wire-slot order. */
    InventoryItem[] getContents();

    /**
     * Replace every wire slot in one batch and push a single full-inventory
     * dispatch (one packet on protocols that support WindowItems; otherwise
     * a fan-out of per-slot updates).
     *
     * @param items array of length {@link #size()}; nulls treated as
     *              {@link InventoryItem#EMPTY}.
     */
    void setContents(InventoryItem[] items);

    /** Clear every slot and push the resulting empty inventory to the owner. */
    void clear();

    /** @return wire-slot index of the currently held item (36-44). */
    int getHeldSlot();

    /** Set the held wire slot (36-44). Out-of-range values are clamped. */
    void setHeldSlot(int wireSlot);
}
