package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * Alpha protocol 0x05 (bidirectional): Player Inventory.
 *
 * C2S: client sends its inventory to the server after changes; server
 * parses {@link #getItemCount(int)} totals (used to track cobblestone).
 *
 * S2C: server sends a full inventory section to the client. The
 * pre-Beta-1.0 client has no SetSlot/WindowItems analogue, so a section
 * push (type=-1 main / -2 craft / -3 armor) is the only way to bulk-set
 * an Alpha player's inventory.
 *
 * Wire format (variable length):
 *   [int]   type (inventory section: -1 main, -2 craft, -3 armor)
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

    /** S2C wire payload — item ID per slot (-1 = empty). */
    private short[] itemIds;
    /** S2C stack sizes per slot. */
    private byte[] counts;
    /** S2C damage values per slot. */
    private short[] damages;

    public PlayerInventoryPacket() {}

    /**
     * S2C constructor. {@code type} selects the inventory section
     * (-1 main, -2 craft, -3 armor) and the three arrays must share
     * the same length.
     */
    public PlayerInventoryPacket(int type, short[] itemIds, byte[] counts, short[] damages) {
        this.type = type;
        this.itemIds = itemIds;
        this.counts = counts;
        this.damages = damages;
    }

    @Override
    public int getPacketId() {
        return 0x05;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(type);
        int slotCount = itemIds == null ? 0 : itemIds.length;
        buf.writeShort(slotCount);
        for (int i = 0; i < slotCount; i++) {
            short id = itemIds[i];
            buf.writeShort(id);
            if (id >= 0) {
                buf.writeByte(counts[i]);
                buf.writeShort(damages[i]);
            }
        }
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
