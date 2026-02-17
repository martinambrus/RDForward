package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.14 Play state, S2C packet 0x0B: Block Change.
 *
 * Same as V393 but uses 1.14 Position encoding (x:26|z:26|y:12 instead of x:26|y:12|z:26).
 *
 * Wire format:
 *   [Position] location (packed long, 1.14 encoding)
 *   [VarInt]   blockStateId (1.13+ global state ID)
 */
public class NettyBlockChangePacketV477 implements Packet {

    private int x;
    private int y;
    private int z;
    private int blockStateId;

    public NettyBlockChangePacketV477() {}

    public NettyBlockChangePacketV477(int x, int y, int z, int blockStateId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockStateId = blockStateId;
    }

    @Override
    public int getPacketId() { return 0x0B; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writePositionV477(buf, x, y, z);
        McDataTypes.writeVarInt(buf, blockStateId);
    }

    @Override
    public void read(ByteBuf buf) {
        int[] pos = McDataTypes.readPositionV477(buf);
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
