package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, C2S packet 0x01: Tab Complete.
 *
 * 1.9 added boolean assumeCommand and boolean hasPosition + optional Position.
 *
 * Wire format:
 *   [String]  text
 *   [boolean] assumeCommand
 *   [boolean] hasPosition
 *   [if hasPosition: Position (long)]
 */
public class NettyTabCompletePacketV109 implements Packet {

    public NettyTabCompletePacketV109() {}

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarIntString(buf); // text
        buf.readBoolean(); // assumeCommand
        boolean hasPosition = buf.readBoolean();
        if (hasPosition) {
            buf.skipBytes(8); // Position long
        }
    }
}
