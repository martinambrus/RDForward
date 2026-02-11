package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.Capability;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side inventory tracking for cross-version compatibility.
 *
 * RubyDung and Classic have no inventory — players can place/break any
 * block type infinitely. Alpha introduces a full inventory system with
 * crafting, item durability, and limited resources.
 *
 * The adapter bridges this gap:
 *
 * For non-inventory clients (RubyDung/Classic):
 *   - Block placement is free (no item consumption)
 *   - Block breaking does not produce item drops
 *   - The server still tracks a virtual inventory per player for
 *     compatibility with inventory-capable clients in the same world
 *
 * For inventory-capable clients (Alpha+):
 *   - Full inventory packets are sent (open/close, set slot, etc.)
 *   - Block placement consumes items from the held slot
 *   - Block breaking produces item drops
 *   - Crafting, smelting, and chest interactions work normally
 *
 * TODO: Implement full inventory packet handling when Alpha inventory
 * packets are added to the protocol. Current implementation is a
 * structural shell that tracks inventory state but does not yet
 * send/receive inventory packets.
 */
public class InventoryAdapter {

    /** Alpha inventory size: 36 main + 4 armor + 4 crafting = 44 slots */
    private static final int INVENTORY_SIZE = 44;

    /** Main inventory starts at slot 9 (0-8 = hotbar conceptually, 9-35 = main, 36-39 = armor) */
    private static final int HOTBAR_START = 0;
    private static final int HOTBAR_SIZE = 9;

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
    }

    /** Per-player inventory state (key = player username). */
    private final Map<String, ItemStack[]> inventories = new ConcurrentHashMap<>();

    /**
     * Initialize inventory for a new player.
     * If the player has no saved inventory, starts with an empty one.
     */
    public void initPlayer(String username) {
        inventories.computeIfAbsent(username, k -> {
            ItemStack[] inv = new ItemStack[INVENTORY_SIZE];
            Arrays.fill(inv, new ItemStack(0, 0, 0));
            return inv;
        });
    }

    /**
     * Remove inventory tracking for a disconnected player.
     * The inventory state should be saved before calling this.
     */
    public void removePlayer(String username) {
        inventories.remove(username);
    }

    /**
     * Check if a player's client supports inventory.
     */
    public boolean supportsInventory(ConnectedPlayer player) {
        return Capability.INVENTORY.isAvailableIn(player.getProtocolVersion());
    }

    /**
     * Handle a block placement from a player.
     *
     * For inventory-capable clients: consumes one item from the held slot.
     * For legacy clients: always allows placement (creative-style).
     *
     * @param player    the player placing the block
     * @param blockType the block type to place
     * @return true if the placement is allowed
     */
    public boolean handleBlockPlace(ConnectedPlayer player, int blockType) {
        if (!supportsInventory(player)) {
            // Legacy clients get free placement
            return true;
        }

        // TODO: Check held item matches blockType, decrement count
        // For now, always allow (inventory tracking not yet wired to packets)
        return true;
    }

    /**
     * Handle a block break by a player.
     *
     * For inventory-capable clients: adds the block as an item drop
     * to the player's inventory (or spawns an item entity if full).
     * For legacy clients: no-op (blocks vanish on break).
     *
     * @param player    the player who broke the block
     * @param blockType the block type that was broken
     * @param x         block X coordinate
     * @param y         block Y coordinate
     * @param z         block Z coordinate
     */
    public void handleBlockBreak(ConnectedPlayer player, int blockType,
                                  int x, int y, int z) {
        if (!supportsInventory(player)) {
            // Legacy clients — no item drops
            return;
        }

        // TODO: Determine drop item (some blocks drop different items, e.g.
        // stone drops cobblestone, diamond ore drops diamond) and add to
        // player inventory or spawn as item entity
    }

    /**
     * Get the held item for a player (hotbar slot 0 by default).
     *
     * @return the item ID, or 0 if empty/unknown
     */
    public int getHeldItem(String username) {
        ItemStack[] inv = inventories.get(username);
        if (inv == null) return 0;
        ItemStack held = inv[HOTBAR_START];
        return held != null ? held.itemId : 0;
    }

    /**
     * Send full inventory contents to a player.
     * Only called for inventory-capable clients.
     *
     * TODO: Implement when inventory window packets are added.
     * Should send SetSlot or WindowItems packets for all 44 slots.
     */
    public void sendFullInventory(ConnectedPlayer player) {
        if (!supportsInventory(player)) return;

        // TODO: Build and send inventory packets
        // For each slot in the player's inventory:
        //   Send SetSlotPacket(windowId=0, slot, itemId, count, damage)
    }
}
