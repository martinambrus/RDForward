package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x0B: Entity Action.
 *
 * 1.8 changed all three fields from int+byte+int to VarInt+VarInt+VarInt.
 *
 * Wire format:
 *   [VarInt] entityId
 *   [VarInt] actionId
 *   [VarInt] jumpBoost
 */
public class NettyEntityActionPacketV47 implements Packet {

    public NettyEntityActionPacketV47() {}

    @Override
    public int getPacketId() { return 0x0B; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarInt(buf); // entityId
        McDataTypes.readVarInt(buf); // actionId
        McDataTypes.readVarInt(buf); // jumpBoost
    }
}
