package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * Alphaver variant of 0x05 (Client -> Server): Player Inventory.
 *
 * Identical to standard Alpha PlayerInventoryPacket except item IDs
 * are encoded as int (4 bytes) instead of short (2 bytes).
 *
 * Wire format (variable length):
 *   [int]   type (inventory section)
 *   [short] count (number of slots)
 *   for each slot:
 *     [int]   item ID (-1 = empty)   ← int, not short
 *     if item ID >= 0:
 *       [byte]  stack size
 *       [short] damage/durability
 */
public class PlayerInventoryPacketAlphaver implements Packet {

    private int type;
    private final Map<Integer, Integer> itemCounts = new HashMap<>();

    public PlayerInventoryPacketAlphaver() {}

    @Override
    public int getPacketId() {
        return 0x05;
    }

    @Override
    public void write(ByteBuf buf) {
        // S2C not needed — only used for C2S reads
    }

    @Override
    public void read(ByteBuf buf) {
        type = buf.readInt();
        itemCounts.clear();
        short count = buf.readShort();
        for (int i = 0; i < count; i++) {
            int itemId = buf.readInt();  // Alphaver uses int, not short
            if (itemId >= 0) {
                byte stackSize = buf.readByte();
                buf.readShort(); // damage/durability
                itemCounts.merge(itemId, (int) stackSize & 0xFF, Integer::sum);
            }
        }
    }

    public int getType() { return type; }

    public int getItemCount(int itemId) {
        return itemCounts.getOrDefault(itemId, 0);
    }
}
