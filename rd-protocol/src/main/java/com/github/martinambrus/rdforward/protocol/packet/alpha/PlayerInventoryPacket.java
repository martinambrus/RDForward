package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

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
        short count = buf.readShort();
        for (int i = 0; i < count; i++) {
            short itemId = buf.readShort();
            if (itemId >= 0) {
                buf.readByte();  // stack size
                buf.readShort(); // damage/durability
            }
        }
    }

    public int getType() { return type; }
}
