package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x14: Tab-Complete.
 *
 * 1.8 added a boolean hasPosition and optional Position long.
 *
 * Wire format:
 *   [String]  text
 *   [boolean] hasPosition
 *   if hasPosition: [Position] lookedAtBlock (packed long)
 */
public class NettyTabCompletePacketV47 implements Packet {

    public NettyTabCompletePacketV47() {}

    @Override
    public int getPacketId() { return 0x14; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarIntString(buf); // text
        boolean hasPosition = buf.readBoolean();
        if (hasPosition) {
            buf.skipBytes(8); // Position long
        }
    }
}
