package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * Alpha protocol 0x05 (Client -> Server): Player Inventory.
 *
 * The client sends its inventory contents to the server after changes.
 * Type indicates which inventory section: -1 = main, -2 = crafting, -3 = armor.
 *
 * Wire format (variable length):
 *   [int]   type (inventory section)
 *   [short] count (number of slots)
 *   for each slot:
 *     [short] item ID (-1 = empty)
 *     if item ID >= 0:
 *       [byte]  stack size
 *       [short] damage/durability
 */
public class PlayerInventoryPacket implements Packet {

    private int type;
    /** Total count of each item ID across all slots in this inventory section. */
    private final Map<Integer, Integer> itemCounts = new HashMap<>();

    public PlayerInventoryPacket() {}

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
            short itemId = buf.readShort();
            if (itemId >= 0) {
                byte stackSize = buf.readByte();
                buf.readShort(); // damage/durability
                itemCounts.merge((int) itemId, (int) stackSize & 0xFF, Integer::sum);
            }
        }
    }

    public int getType() { return type; }

    /**
     * Returns the total count of the given item across all slots.
     * Only valid after {@link #read(ByteBuf)} has been called.
     */
    public int getItemCount(int itemId) {
        return itemCounts.getOrDefault(itemId, 0);
    }
}
