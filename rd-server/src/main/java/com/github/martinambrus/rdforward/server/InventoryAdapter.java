package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.Capability;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side inventory tracking for cross-version compatibility.
 *
 * Uses standard Minecraft 45-slot numbering:
 *   0 = craft output, 1-4 = craft grid, 5-8 = armor,
 *   9-35 = main inventory, 36-44 = hotbar.
 *
 * Supports WindowClick processing for Beta v7 through Netty v340.
 */
public class InventoryAdapter {

    /** Standard player inventory: 45 slots (0-44). */
    private static final int INVENTORY_SIZE = 45;

    /** Represents a single inventory slot. */
    static class ItemStack {
        int itemId;
        int count;
        int damage;

        ItemStack(int itemId, int count, int damage) {
            this.itemId = itemId;
            this.count = count;
            this.damage = damage;
        }

        boolean isEmpty() {
            return itemId <= 0 || count <= 0;
        }

        ItemStack copy() {
            return new ItemStack(itemId, count, damage);
        }
    }

    /** Per-player inventory state (key = player username). */
    private final Map<String, ItemStack[]> inventories = new ConcurrentHashMap<>();

    /** Per-player cursor (carried item) state. */
    private final Map<String, ItemStack> cursors = new ConcurrentHashMap<>();

    /**
     * Initialize inventory for a new player with empty slots and empty cursor.
     */
    public void initPlayer(String username) {
        ItemStack[] inv = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inv[i] = new ItemStack(0, 0, 0);
        }
        inventories.put(username, inv);
        cursors.put(username, new ItemStack(0, 0, 0));
    }

    /**
     * Remove inventory tracking for a disconnected player.
     */
    public void removePlayer(String username) {
        inventories.remove(username);
        cursors.remove(username);
    }

    /**
     * Set a specific slot's contents.
     */
    public void setSlot(String username, int slot, int itemId, int count, int damage) {
        ItemStack[] inv = inventories.get(username);
        if (inv == null || slot < 0 || slot >= INVENTORY_SIZE) return;
        inv[slot] = new ItemStack(itemId, count, damage);
    }

    /**
     * Get a specific slot's contents. Returns null if player not tracked.
     */
    public ItemStack getSlot(String username, int slot) {
        ItemStack[] inv = inventories.get(username);
        if (inv == null || slot < 0 || slot >= INVENTORY_SIZE) return null;
        return inv[slot];
    }

    /**
     * Get the cursor (carried item). Returns null if player not tracked.
     */
    public ItemStack getCursor(String username) {
        return cursors.get(username);
    }

    /**
     * Process a WindowClick action for the player's inventory.
     *
     * @param username player username
     * @param slot     clicked slot (-999 = outside window)
     * @param button   mouse button (0=left, 1=right)
     * @param mode     click mode (0=normal, 4=drop)
     * @return true if the action was accepted
     */
    public boolean processWindowClick(String username, int slot, int button, int mode) {
        ItemStack[] inv = inventories.get(username);
        ItemStack cursor = cursors.get(username);
        if (inv == null || cursor == null) return true;

        // Mode 4: Q-drop from slot (no cursor involvement)
        if (mode == 4 && slot >= 0 && slot < INVENTORY_SIZE) {
            ItemStack slotStack = inv[slot];
            if (!slotStack.isEmpty()) {
                if (button == 1) {
                    // Ctrl+Q: drop full stack
                    inv[slot] = new ItemStack(0, 0, 0);
                } else {
                    // Q: drop one
                    slotStack.count--;
                    if (slotStack.count <= 0) {
                        inv[slot] = new ItemStack(0, 0, 0);
                    }
                }
            }
            return true;
        }

        // Slot -999: drop from cursor
        if (slot == -999 && mode == 0) {
            if (!cursor.isEmpty()) {
                if (button == 0) {
                    // Left click outside: drop entire cursor
                    cursors.put(username, new ItemStack(0, 0, 0));
                } else if (button == 1) {
                    // Right click outside: drop one from cursor
                    cursor.count--;
                    if (cursor.count <= 0) {
                        cursors.put(username, new ItemStack(0, 0, 0));
                    }
                }
            }
            return true;
        }

        // Normal clicks on valid slots
        if (mode == 0 && slot >= 0 && slot < INVENTORY_SIZE) {
            ItemStack slotStack = inv[slot];

            if (button == 0) {
                // Left click: swap cursor and slot
                inv[slot] = cursor.copy();
                cursors.put(username, slotStack.copy());
            } else if (button == 1) {
                // Right click
                if (cursor.isEmpty()) {
                    // Pick up half (ceil) from slot
                    if (!slotStack.isEmpty()) {
                        int pickUp = (int) Math.ceil(slotStack.count / 2.0);
                        cursors.put(username, new ItemStack(slotStack.itemId, pickUp, slotStack.damage));
                        slotStack.count -= pickUp;
                        if (slotStack.count <= 0) {
                            inv[slot] = new ItemStack(0, 0, 0);
                        }
                    }
                } else if (slotStack.isEmpty() || slotStack.itemId == cursor.itemId) {
                    // Place one from cursor into slot
                    if (slotStack.isEmpty()) {
                        inv[slot] = new ItemStack(cursor.itemId, 1, cursor.damage);
                    } else {
                        slotStack.count++;
                    }
                    cursor.count--;
                    if (cursor.count <= 0) {
                        cursors.put(username, new ItemStack(0, 0, 0));
                    }
                } else {
                    // Different type: swap cursor and slot
                    inv[slot] = cursor.copy();
                    cursors.put(username, slotStack.copy());
                }
            }
            return true;
        }

        // All other modes: silently accept, no state change
        return true;
    }

    /**
     * Process a CloseWindow action. Returns cursor items to the first available slot.
     */
    public void processCloseWindow(String username) {
        ItemStack[] inv = inventories.get(username);
        ItemStack cursor = cursors.get(username);
        if (inv == null || cursor == null || cursor.isEmpty()) return;

        // Try to place cursor back into first available slot
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inv[i].isEmpty()) {
                inv[i] = cursor.copy();
                cursors.put(username, new ItemStack(0, 0, 0));
                return;
            }
        }
        // No space: drop (clear cursor)
        cursors.put(username, new ItemStack(0, 0, 0));
    }

    /**
     * Check if a player's client supports inventory.
     */
    public boolean supportsInventory(ConnectedPlayer player) {
        return Capability.INVENTORY.isAvailableIn(player.getProtocolVersion());
    }

    /**
     * Handle a block placement from a player.
     */
    public boolean handleBlockPlace(ConnectedPlayer player, int blockType) {
        if (!supportsInventory(player)) {
            return true;
        }
        return true;
    }

    /**
     * Handle a block break by a player.
     */
    public void handleBlockBreak(ConnectedPlayer player, int blockType,
                                  int x, int y, int z) {
        if (!supportsInventory(player)) {
            return;
        }
    }

    /**
     * Get the held item for a player (hotbar slot 0 = window slot 36).
     */
    public int getHeldItem(String username) {
        ItemStack[] inv = inventories.get(username);
        if (inv == null) return 0;
        ItemStack held = inv[36];
        return held != null ? held.itemId : 0;
    }

    /**
     * Send full inventory contents to a player.
     */
    public void sendFullInventory(ConnectedPlayer player) {
        if (!supportsInventory(player)) return;
    }
}
