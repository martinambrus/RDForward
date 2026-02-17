package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x0E: Click Window.
 *
 * Same structure as 1.7.2 but uses V47 slot format (byte TAG_End for no NBT).
 *
 * Wire format:
 *   [byte]  windowId
 *   [short] slotIndex
 *   [byte]  button
 *   [short] actionNumber
 *   [byte]  mode
 *   [slot]  clickedItem (V47 format)
 */
public class NettyWindowClickPacketV47 implements Packet {

    public NettyWindowClickPacketV47() {}

    @Override
    public int getPacketId() { return 0x0E; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        buf.skipBytes(1); // windowId
        buf.skipBytes(2); // slotIndex
        buf.skipBytes(1); // button
        buf.skipBytes(2); // actionNumber
        buf.skipBytes(1); // mode
        McDataTypes.skipV47SlotData(buf);
    }
}
