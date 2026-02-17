package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, C2S packet 0x1A: Animation.
 *
 * 1.9 added VarInt hand field (was empty in 1.8).
 *
 * Wire format:
 *   [VarInt] hand (0 = main hand, 1 = off hand)
 */
public class AnimationPacketV109 implements Packet {

    public AnimationPacketV109() {}

    @Override
    public int getPacketId() { return 0x1A; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 0); // main hand
    }

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarInt(buf); // hand
    }
}
