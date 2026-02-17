package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.BlockPlacementData;
import io.netty.buffer.ByteBuf;

/**
 * 1.14 Play state, C2S packet 0x2C: Player Block Placement.
 *
 * 1.14 (v477) moved Hand to the first field (before Location) and added
 * an IsInsideBlock boolean at the end.
 *
 * Wire format:
 *   [VarInt]    hand
 *   [Position]  location (packed long)
 *   [VarInt]    face
 *   [Float]     cursorX (0.0-1.0)
 *   [Float]     cursorY
 *   [Float]     cursorZ
 *   [Boolean]   isInsideBlock
 */
public class NettyBlockPlacementPacketV477 implements Packet, BlockPlacementData {

    private int x;
    private int y;
    private int z;
    private int face;

    public NettyBlockPlacementPacketV477() {}

    public NettyBlockPlacementPacketV477(int x, int y, int z, int face) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.face = face;
    }

    @Override
    public int getPacketId() { return 0x2C; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 0); // main hand
        McDataTypes.writePositionV477(buf, x, y, z);
        McDataTypes.writeVarInt(buf, face);
        buf.writeFloat(0); // cursorX
        buf.writeFloat(0); // cursorY
        buf.writeFloat(0); // cursorZ
        buf.writeBoolean(false); // isInsideBlock
    }

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarInt(buf); // hand
        int[] pos = McDataTypes.readPositionV477(buf);
        x = pos[0];
        y = pos[1];
        z = pos[2];
        face = McDataTypes.readVarInt(buf);
        buf.skipBytes(12); // cursorX, cursorY, cursorZ (3 floats)
        buf.readBoolean(); // isInsideBlock
    }

    @Override
    public int getX() { return x; }
    @Override
    public int getY() { return y; }
    @Override
    public int getZ() { return z; }
    @Override
    public int getDirection() { return face; }
    @Override
    public short getItemId() { return 4; } // cobblestone — server gives creative cobblestone
}
