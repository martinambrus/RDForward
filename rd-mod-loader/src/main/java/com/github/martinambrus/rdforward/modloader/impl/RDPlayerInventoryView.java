package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.api.inventory.InventoryItem;
import com.github.martinambrus.rdforward.api.inventory.PlayerInventoryView;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.InventoryAdapter;

import java.util.Objects;

/**
 * {@link PlayerInventoryView} backed by the rd-server
 * {@link InventoryAdapter}. Mutations update the per-username slot
 * tracking and immediately push the new state to the owning client.
 */
public final class RDPlayerInventoryView implements PlayerInventoryView {

    private static final int SIZE = 45;
    private static final int HOTBAR_FIRST = 36;
    private static final int HOTBAR_LAST = 44;

    private final ConnectedPlayer player;
    private final InventoryAdapter adapter;

    /**
     * The held wire-slot index. RDForward currently spawns players with
     * the held slot set to 36 (hotbar 0); this mirrors that until a
     * client-driven change arrives via HeldItemChange (not yet wired
     * through the public API).
     */
    private volatile int heldSlot = HOTBAR_FIRST;

    public RDPlayerInventoryView(ConnectedPlayer player, InventoryAdapter adapter) {
        this.player = Objects.requireNonNull(player, "player");
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    @Override
    public int size() { return SIZE; }

    @Override
    public InventoryItem getSlot(int wireSlot) {
        if (wireSlot < 0 || wireSlot >= SIZE) return InventoryItem.EMPTY;
        return adapter.getItem(player.getUsername(), wireSlot);
    }

    @Override
    public void setSlot(int wireSlot, InventoryItem item) {
        if (wireSlot < 0 || wireSlot >= SIZE) return;
        adapter.putItem(player.getUsername(), wireSlot, item);
        adapter.sendSlotUpdate(player, wireSlot);
    }

    @Override
    public InventoryItem[] getContents() {
        InventoryItem[] out = new InventoryItem[SIZE];
        for (int i = 0; i < SIZE; i++) {
            out[i] = adapter.getItem(player.getUsername(), i);
        }
        return out;
    }

    @Override
    public void setContents(InventoryItem[] items) {
        if (items == null) return;
        int n = Math.min(items.length, SIZE);
        for (int i = 0; i < n; i++) {
            adapter.putItem(player.getUsername(), i, items[i]);
        }
        // Zero any trailing slots if the caller passed a shorter array.
        for (int i = n; i < SIZE; i++) {
            adapter.putItem(player.getUsername(), i, InventoryItem.EMPTY);
        }
        adapter.sendFullInventory(player);
    }

    @Override
    public void clear() {
        for (int i = 0; i < SIZE; i++) {
            adapter.putItem(player.getUsername(), i, InventoryItem.EMPTY);
        }
        adapter.sendFullInventory(player);
    }

    @Override
    public int getHeldSlot() { return heldSlot; }

    @Override
    public void setHeldSlot(int wireSlot) {
        if (wireSlot < HOTBAR_FIRST) wireSlot = HOTBAR_FIRST;
        if (wireSlot > HOTBAR_LAST) wireSlot = HOTBAR_LAST;
        this.heldSlot = wireSlot;
    }
}
