package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.13 Play state, S2C packet 0x0B: Block Change.
 *
 * Same wire format as V47 but the VarInt block state ID is a 1.13 global
 * block state ID (flat registry) instead of {@code (blockId << 4) | metadata}.
 * Callers must pass the pre-mapped 1.13 state ID via
 * {@link com.github.martinambrus.rdforward.protocol.BlockStateMapper#toV393BlockState(int)}.
 *
 * Wire format:
 *   [Position] location (packed long, x:26|y:12|z:26)
 *   [VarInt]   blockStateId (1.13 global state ID)
 */
public class NettyBlockChangePacketV393 implements Packet {

    private int x;
    private int y;
    private int z;
    private int blockStateId;

    public NettyBlockChangePacketV393() {}

    public NettyBlockChangePacketV393(int x, int y, int z, int blockStateId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockStateId = blockStateId;
    }

    @Override
    public int getPacketId() { return 0x0B; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writePosition(buf, x, y, z);
        McDataTypes.writeVarInt(buf, blockStateId);
    }

    @Override
    public void read(ByteBuf buf) {
        int[] pos = McDataTypes.readPosition(buf);
        x = pos[0];
        y = pos[1];
        z = pos[2];
        blockStateId = McDataTypes.readVarInt(buf);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockStateId() { return blockStateId; }
}
