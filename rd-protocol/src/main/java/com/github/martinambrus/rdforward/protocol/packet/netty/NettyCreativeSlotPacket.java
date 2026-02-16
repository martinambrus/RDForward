package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x10: Creative Inventory Action.
 *
 * Wire format:
 *   [short] slotIndex
 *   [slot]  clickedItem (Netty slot data)
 */
public class NettyCreativeSlotPacket implements Packet {

    public NettyCreativeSlotPacket() {}

    @Override
    public int getPacketId() { return 0x10; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        buf.skipBytes(2); // slotIndex
        McDataTypes.skipNettySlotData(buf);
    }
}
