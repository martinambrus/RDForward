package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.14 Play state, S2C packet 0x41: Set Chunk Cache Radius.
 *
 * Tells the client the server's view distance. Must be sent during join.
 *
 * Wire format:
 *   [VarInt] viewDistance
 */
public class SetChunkCacheRadiusPacketV477 implements Packet {

    private int viewDistance;

    public SetChunkCacheRadiusPacketV477() {}

    public SetChunkCacheRadiusPacketV477(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    @Override
    public int getPacketId() { return 0x41; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, viewDistance);
    }

    @Override
    public void read(ByteBuf buf) {
        viewDistance = McDataTypes.readVarInt(buf);
    }
}
