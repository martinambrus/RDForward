package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.14 Play state, S2C packet 0x40: Set Chunk Cache Center.
 *
 * Tells the client where to center its chunk loading. Must be sent before chunks.
 *
 * Wire format:
 *   [VarInt] chunkX
 *   [VarInt] chunkZ
 */
public class SetChunkCacheCenterPacketV477 implements Packet {

    private int chunkX;
    private int chunkZ;

    public SetChunkCacheCenterPacketV477() {}

    public SetChunkCacheCenterPacketV477(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public int getPacketId() { return 0x40; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, chunkX);
        McDataTypes.writeVarInt(buf, chunkZ);
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = McDataTypes.readVarInt(buf);
        chunkZ = McDataTypes.readVarInt(buf);
    }
}
