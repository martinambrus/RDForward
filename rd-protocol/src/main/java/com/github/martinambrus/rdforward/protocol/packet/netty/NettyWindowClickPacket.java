package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x0E: Click Window.
 *
 * Wire format:
 *   [byte]  windowId
 *   [short] slotIndex
 *   [byte]  button
 *   [short] actionNumber
 *   [byte]  mode
 *   [slot]  clickedItem (Netty slot data)
 */
public class NettyWindowClickPacket implements Packet {

    public NettyWindowClickPacket() {}

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
        McDataTypes.skipNettySlotData(buf);
    }
}
