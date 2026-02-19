package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.BlockPlacementData;
import io.netty.buffer.ByteBuf;

/**
 * 1.19 Play state, C2S packet 0x30: Player Block Placement.
 *
 * Same as V477 but with a VarInt sequence field appended after isInsideBlock.
 * The sequence is used for BlockChangedAck responses.
 *
 * Wire format:
 *   [VarInt]    hand
 *   [Position]  location (packed long)
 *   [VarInt]    face
 *   [Float]     cursorX (0.0-1.0)
 *   [Float]     cursorY
 *   [Float]     cursorZ
 *   [Boolean]   isInsideBlock
 *   [VarInt]    sequence
 */
public class NettyBlockPlacementPacketV759 implements Packet, BlockPlacementData {

    private int x;
    private int y;
    private int z;
    private int face;
    private int sequence;

    public NettyBlockPlacementPacketV759() {}

    @Override
    public int getPacketId() { return 0x30; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 0); // main hand
        McDataTypes.writePositionV477(buf, x, y, z);
        McDataTypes.writeVarInt(buf, face);
        buf.writeFloat(0); // cursorX
        buf.writeFloat(0); // cursorY
        buf.writeFloat(0); // cursorZ
        buf.writeBoolean(false); // isInsideBlock
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
