package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.BlockPlacementData;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.2 Play state, C2S packet 0x3A: Player Block Placement (Use Item On).
 *
 * Same as V759 but with a boolean worldBorderHit field inserted between
 * isInsideBlock and sequence.
 *
 * Wire format:
 *   [VarInt]    hand
 *   [Position]  location (packed long)
 *   [VarInt]    face
 *   [Float]     cursorX (0.0-1.0)
 *   [Float]     cursorY
 *   [Float]     cursorZ
 *   [Boolean]   isInsideBlock
 *   [Boolean]   worldBorderHit  (NEW in 1.21.2)
 *   [VarInt]    sequence
 */
public class NettyBlockPlacementPacketV768 implements Packet, BlockPlacementData {

    private int x;
    private int y;
    private int z;
    private int face;
    private int sequence;

    public NettyBlockPlacementPacketV768() {}

    @Override
    public int getPacketId() { return 0x3A; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 0); // main hand
        McDataTypes.writePositionV477(buf, x, y, z);
        McDataTypes.writeVarInt(buf, face);
        buf.writeFloat(0); // cursorX
        buf.writeFloat(0); // cursorY
        buf.writeFloat(0); // cursorZ
        buf.writeBoolean(false); // isInsideBlock
        buf.writeBoolean(false); // worldBorderHit
        McDataTypes.writeVarInt(buf, sequence);
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
        buf.readBoolean(); // worldBorderHit
        sequence = McDataTypes.readVarInt(buf);
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

    public int getSequence() { return sequence; }
}
