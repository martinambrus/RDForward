package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, C2S packet 0x1D: Use Item.
 *
 * New packet in 1.9. Right-click air with item in hand.
 * Silently consumed.
 *
 * Wire format:
 *   [VarInt] hand (0 = main hand, 1 = off hand)
 */
public class UseItemPacketV109 implements Packet {

    public UseItemPacketV109() {}

    @Override
    public int getPacketId() { return 0x1D; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 0); // main hand
    }

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarInt(buf); // hand
    }
}
