package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x10: Creative Inventory Action.
 *
 * Same structure as 1.7.2 but uses V47 slot format (byte TAG_End for no NBT).
 *
 * Wire format:
 *   [short] slotIndex
 *   [slot]  clickedItem (V47 format)
 */
public class NettyCreativeSlotPacketV47 implements Packet {

    public NettyCreativeSlotPacketV47() {}

    @Override
    public int getPacketId() { return 0x10; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        buf.skipBytes(2); // slotIndex
        McDataTypes.skipV47SlotData(buf);
    }
}
