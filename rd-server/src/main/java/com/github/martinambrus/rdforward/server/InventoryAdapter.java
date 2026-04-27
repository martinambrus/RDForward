package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.api.inventory.InventoryItem;
import com.github.martinambrus.rdforward.protocol.BlockStateMapper;
import com.github.martinambrus.rdforward.protocol.Capability;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.alpha.PlayerInventoryPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.SetSlotPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.SetSlotPacketV22;
import com.github.martinambrus.rdforward.protocol.packet.alpha.SetSlotPacketV39;
import com.github.martinambrus.rdforward.protocol.packet.alpha.WindowItemsPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettySetSlotPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettySetSlotPacketV393;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettySetSlotPacketV404;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettySetSlotPacketV47;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettySetSlotPacketV756;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettySetSlotPacketV766;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyWindowItemsPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyWindowItemsPacketV47;

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
     * Read a slot in the public {@link InventoryItem} form. Returns
     * {@link InventoryItem#EMPTY} for empty/unknown slots — never null.
     */
    public InventoryItem getItem(String username, int slot) {
        ItemStack s = getSlot(username, slot);
        if (s == null || s.isEmpty()) return InventoryItem.EMPTY;
        return new InventoryItem(s.itemId, s.count, s.damage);
    }

    /**
     * Write a slot from the public {@link InventoryItem} form. Null and
     * empty items both clear the slot.
     */
    public void putItem(String username, int slot, InventoryItem item) {
        if (item == null || item.isEmpty()) {
            setSlot(username, slot, 0, 0, 0);
        } else {
            setSlot(username, slot, item.itemId(), item.count(), item.damage());
        }
    }

    /**
     * Push a single slot to the player's client. Picks the
     * version-correct SetSlot variant. No-op when {@code slot} is out of
     * range or the protocol does not support inventory packets.
     */
    public void sendSlotUpdate(ConnectedPlayer player, int slot) {
        if (!supportsInventory(player)) return;
        if (slot < 0 || slot >= INVENTORY_SIZE) return;
        ItemStack s = getSlot(player.getUsername(), slot);
        int legacyId = (s == null || s.isEmpty()) ? -1 : s.itemId;
        int count = (s == null || s.isEmpty()) ? 0 : s.count;
        int damage = (s == null || s.isEmpty()) ? 0 : s.damage;
        ProtocolVersion v = player.getProtocolVersion();
        ProtocolVersion.Family family = v.getFamily();

        if (family == ProtocolVersion.Family.RELEASE && v.isAtLeast(ProtocolVersion.RELEASE_1_7_2)) {
            sendNettySingleSlot(player, slot, legacyId, count, damage, v);
            return;
        }
        if (family == ProtocolVersion.Family.RELEASE && v.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
            player.sendPacket(new SetSlotPacketV39(0, slot, legacyId, count, damage));
            return;
        }
        if (family == ProtocolVersion.Family.RELEASE && v.isAtLeast(ProtocolVersion.RELEASE_1_0)) {
            player.sendPacket(new SetSlotPacketV22(0, slot, legacyId, count, damage));
            return;
        }
        if (family == ProtocolVersion.Family.LCE) {
            // LCE TU19 follows Java 1.6.4 wire format (pre-Netty Release V39).
            player.sendPacket(new SetSlotPacketV39(0, slot, legacyId, count, damage));
            return;
        }
        if (family == ProtocolVersion.Family.BETA && v.isAtLeast(ProtocolVersion.BETA_1_9_PRE5)) {
            player.sendPacket(new SetSlotPacketV22(0, slot, legacyId, count, damage));
            return;
        }
        if (family == ProtocolVersion.Family.BETA) {
            player.sendPacket(new SetSlotPacket(0, slot, legacyId, count, damage));
            return;
        }
        if (family == ProtocolVersion.Family.ALPHA) {
            // Pre-Beta-1.0 clients have no SetSlot equivalent. Push the
            // whole containing section via 0x05 S2C — minimal extra work
            // (one packet) for an Alpha-class server population.
            sendAlphaSectionFor(player, slot);
            return;
        }
        if (family == ProtocolVersion.Family.BEDROCK) {
            if (player.getMcpeSession() != null) {
                // Legacy MCPE (v9-v91 / 0.6.1-0.16.0). The MCPE codec
                // chain has separate per-version wire formats for
                // CONTAINER_SET_SLOT/CONTENT and is not yet wired here.
                // Server-side state is still updated; the client will
                // pick up the change at the next client-side reload.
                com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                        null, "rdforward.server.InventoryAdapter.sendSlotUpdate(MCPE)V");
                return;
            }
            // Modern Bedrock (CloudBurst). Routed through
            // BedrockSessionWrapper.translateAndSend ->
            // ClassicToBedrockTranslator's SetSlot translation, which
            // produces an InventorySlotPacket with the right
            // ContainerId based on the wire slot.
            player.sendPacket(new SetSlotPacket(0, slot, legacyId, count, damage));
        }
    }

    /**
     * Send full inventory contents to a player. Picks the version-correct
     * full-window packet (Alpha 0x05 sections, Beta/Release WindowItems,
     * Netty 1.7.2/1.8 NettyWindowItems). For Netty 1.13+ — where no
     * full-window packet variant exists in the codec table — falls back
     * to N x version-correct SetSlot.
     */
    public void sendFullInventory(ConnectedPlayer player) {
        if (!supportsInventory(player)) return;
        ItemStack[] inv = inventories.get(player.getUsername());
        if (inv == null) return;

        ProtocolVersion v = player.getProtocolVersion();
        ProtocolVersion.Family family = v.getFamily();

        if (family == ProtocolVersion.Family.ALPHA) {
            sendAlphaSection(player, inv, -1, 9, 36);   // main: wire 9-44
            sendAlphaSection(player, inv, -3, 5, 4);    // armor: wire 5-8
            sendAlphaSection(player, inv, -2, 1, 4);    // craft: wire 1-4
            return;
        }

        if (family == ProtocolVersion.Family.BEDROCK && player.getMcpeSession() != null) {
            // Legacy MCPE — see sendSlotUpdate for the rationale.
            com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                    null, "rdforward.server.InventoryAdapter.sendFullInventory(MCPE)V");
            return;
        }

        if (family == ProtocolVersion.Family.RELEASE && v.isAtLeast(ProtocolVersion.RELEASE_1_13)) {
            // No NettyWindowItems variant exists for v393+; fan out per slot.
            for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
                sendSlotUpdate(player, slot);
            }
            return;
        }

        // Build raw arrays (no per-version item-id translation needed —
        // pre-1.13 protocols use Notch IDs directly, matching the
        // adapter's storage form).
        short[] ids = new short[INVENTORY_SIZE];
        byte[] counts = new byte[INVENTORY_SIZE];
        short[] damages = new short[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack s = inv[i];
            if (s == null || s.isEmpty()) {
                ids[i] = -1;
            } else {
                ids[i] = (short) s.itemId;
                counts[i] = (byte) s.count;
                damages[i] = (short) s.damage;
            }
        }

        if (family == ProtocolVersion.Family.RELEASE && v.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            player.sendPacket(new NettyWindowItemsPacketV47(0, ids, counts, damages));
            return;
        }
        if (family == ProtocolVersion.Family.RELEASE && v.isAtLeast(ProtocolVersion.RELEASE_1_7_2)) {
            player.sendPacket(new NettyWindowItemsPacket(0, ids, counts, damages));
            return;
        }
        if (family == ProtocolVersion.Family.RELEASE
                || family == ProtocolVersion.Family.LCE
                || (family == ProtocolVersion.Family.BETA && v.isAtLeast(ProtocolVersion.BETA_1_9_PRE5))) {
            // V22+ format — adds optional NBT trailer for damageable items
            // so swords/tools round-trip cleanly. Non-damageable items
            // (blocks) decode the same as the plain Beta format, so this
            // is safe across the whole pre-Netty Release range and LCE.
            player.sendPacket(new com.github.martinambrus.rdforward.protocol.packet.alpha.WindowItemsPacketV22(
                    0, ids, counts, damages));
            return;
        }
        // Beta pre-1.9-pre5 + Bedrock.
        // Bedrock route: BedrockSessionWrapper.translateAndSend converts
        // WindowItemsPacket into a pair of InventoryContentPackets
        // (INVENTORY + ARMOR containers).
        player.sendPacket(new WindowItemsPacket(0, ids, counts, damages));
    }

    /**
     * Pick the Netty SetSlot variant for {@code version} and dispatch.
     * Mirrors the per-version chain in NettyConnectionHandler join. For
     * 1.13+ the legacy Notch ID is translated through BlockStateMapper.
     */
    private void sendNettySingleSlot(ConnectedPlayer player, int slot,
                                     int legacyItemId, int count, int damage,
                                     ProtocolVersion version) {
        if (legacyItemId < 0) {
            // Empty slot.
            if (version.isAtLeast(ProtocolVersion.RELEASE_1_21_2)) {
                player.sendPacket(new NettySetSlotPacketV766(0, 0, slot, -1, 0));
            } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_17_1)) {
                player.sendPacket(new NettySetSlotPacketV756(0, 0, slot, -1, 0));
            } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_13_2)) {
                player.sendPacket(new NettySetSlotPacketV404(0, slot, -1, 0));
            } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_13)) {
                player.sendPacket(new NettySetSlotPacketV393(0, slot, -1, 0));
            } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
                player.sendPacket(new NettySetSlotPacketV47(0, slot, -1, 0, 0));
            } else {
                player.sendPacket(new NettySetSlotPacket(0, slot, -1, 0, 0));
            }
            return;
        }
        // Translate Notch ID for registry-ID protocols (1.13+).
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_21_2)) {
            int id = BlockStateMapper.toV765ItemId(legacyItemId);
            player.sendPacket(new NettySetSlotPacketV766(0, 0, slot, id, count));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_20_5)) {
            int id = BlockStateMapper.toV765ItemId(legacyItemId);
            player.sendPacket(new NettySetSlotPacketV756(0, 0, slot, id, count));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_19)) {
            int id = BlockStateMapper.toV759ItemId(legacyItemId);
            player.sendPacket(new NettySetSlotPacketV756(0, 0, slot, id, count));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_17_1)) {
            int id = BlockStateMapper.toV755ItemId(legacyItemId);
            player.sendPacket(new NettySetSlotPacketV756(0, 0, slot, id, count));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_17)) {
            int id = BlockStateMapper.toV755ItemId(legacyItemId);
            player.sendPacket(new NettySetSlotPacketV404(0, slot, id, count));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_16)) {
            int id = BlockStateMapper.toV735ItemId(legacyItemId);
            player.sendPacket(new NettySetSlotPacketV404(0, slot, id, count));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_13_2)) {
            int id = BlockStateMapper.toV393ItemId(legacyItemId);
            player.sendPacket(new NettySetSlotPacketV404(0, slot, id, count));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_13)) {
            int id = BlockStateMapper.toV393ItemId(legacyItemId);
            player.sendPacket(new NettySetSlotPacketV393(0, slot, id, count));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            player.sendPacket(new NettySetSlotPacketV47(0, slot, legacyItemId, count, damage));
        } else {
            player.sendPacket(new NettySetSlotPacket(0, slot, legacyItemId, count, damage));
        }
    }

    /**
     * Send a single Alpha section (type=-1 main / -2 craft / -3 armor).
     * The section reads {@code length} consecutive wire slots starting
     * at {@code wireStart}.
     */
    private void sendAlphaSection(ConnectedPlayer player, ItemStack[] inv,
                                  int type, int wireStart, int length) {
        short[] ids = new short[length];
        byte[] counts = new byte[length];
        short[] damages = new short[length];
        for (int i = 0; i < length; i++) {
            ItemStack s = inv[wireStart + i];
            if (s == null || s.isEmpty()) {
                ids[i] = -1;
            } else {
                ids[i] = (short) s.itemId;
                counts[i] = (byte) s.count;
                damages[i] = (short) s.damage;
            }
        }
        player.sendPacket(new PlayerInventoryPacket(type, ids, counts, damages));
    }

    /** Resolve the Alpha section that contains {@code wireSlot} and resend it. */
    private void sendAlphaSectionFor(ConnectedPlayer player, int wireSlot) {
        ItemStack[] inv = inventories.get(player.getUsername());
        if (inv == null) return;
        if (wireSlot >= 9 && wireSlot <= 44) {
            sendAlphaSection(player, inv, -1, 9, 36);
        } else if (wireSlot >= 5 && wireSlot <= 8) {
            sendAlphaSection(player, inv, -3, 5, 4);
        } else if (wireSlot >= 1 && wireSlot <= 4) {
            sendAlphaSection(player, inv, -2, 1, 4);
        }
    }
}
