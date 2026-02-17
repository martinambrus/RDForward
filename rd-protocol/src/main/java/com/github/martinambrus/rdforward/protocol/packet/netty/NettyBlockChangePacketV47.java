package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x23: Block Change.
 *
 * 1.8 changed coordinates to a packed Position long and combined
 * blockId + metadata into a single VarInt blockStateId (blockId << 4 | meta).
 *
 * Wire format:
 *   [Position] location (packed long)
 *   [VarInt]   blockStateId (blockId << 4 | metadata)
 */
public class NettyBlockChangePacketV47 implements Packet {

    private int x;
    private int y;
    private int z;
    private int blockId;
    private int metadata;

    public NettyBlockChangePacketV47() {}

    public NettyBlockChangePacketV47(int x, int y, int z, int blockId, int metadata) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
        this.metadata = metadata;
    }

    @Override
    public int getPacketId() { return 0x23; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writePosition(buf, x, y, z);
        McDataTypes.writeVarInt(buf, (blockId << 4) | (metadata & 0xF));
    }

    @Override
    public void read(ByteBuf buf) {
        int[] pos = McDataTypes.readPosition(buf);
        x = pos[0];
        y = pos[1];
        z = pos[2];
        int stateId = McDataTypes.readVarInt(buf);
        blockId = stateId >> 4;
        metadata = stateId & 0xF;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockId() { return blockId; }
}
