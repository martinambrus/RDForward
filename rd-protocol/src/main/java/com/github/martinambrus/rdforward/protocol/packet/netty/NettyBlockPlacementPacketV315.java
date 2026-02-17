package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.BlockPlacementData;
import io.netty.buffer.ByteBuf;

/**
 * 1.11 Play state, C2S packet 0x1C: Player Block Placement.
 *
 * 1.11 (v315) changed cursor position fields from unsigned bytes to floats.
 *
 * Wire format:
 *   [Position]  location (packed long)
 *   [VarInt]    face
 *   [VarInt]    hand
 *   [Float]     cursorX (0.0-1.0)
 *   [Float]     cursorY
 *   [Float]     cursorZ
 */
public class NettyBlockPlacementPacketV315 implements Packet, BlockPlacementData {

    private int x;
    private int y;
    private int z;
    private int face;

    public NettyBlockPlacementPacketV315() {}

    @Override
    public int getPacketId() { return 0x1C; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writePosition(buf, x, y, z);
        McDataTypes.writeVarInt(buf, face);
        McDataTypes.writeVarInt(buf, 0); // main hand
        buf.writeFloat(0); // cursorX
        buf.writeFloat(0); // cursorY
        buf.writeFloat(0); // cursorZ
    }

    @Override
    public void read(ByteBuf buf) {
        int[] pos = McDataTypes.readPosition(buf);
        x = pos[0];
        y = pos[1];
        z = pos[2];
        face = McDataTypes.readVarInt(buf);
        McDataTypes.readVarInt(buf); // hand
        buf.skipBytes(12); // cursorX, cursorY, cursorZ (3 floats)
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
