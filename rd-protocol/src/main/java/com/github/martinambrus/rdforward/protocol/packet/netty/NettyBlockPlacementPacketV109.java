package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.BlockPlacementData;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, C2S packet 0x1C: Player Block Placement.
 *
 * 1.9 removed the held item slot data, changed direction from byte to VarInt face,
 * and added VarInt hand. The server gives creative cobblestone, so getItemId()
 * returns cobblestone.
 *
 * Wire format:
 *   [Position] location (packed long)
 *   [VarInt]   face
 *   [VarInt]   hand
 *   [byte]     cursorX
 *   [byte]     cursorY
 *   [byte]     cursorZ
 */
public class NettyBlockPlacementPacketV109 implements Packet, BlockPlacementData {

    private int x;
    private int y;
    private int z;
    private int face;

    public NettyBlockPlacementPacketV109() {}

    @Override
    public int getPacketId() { return 0x1C; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writePosition(buf, x, y, z);
        McDataTypes.writeVarInt(buf, face);
        McDataTypes.writeVarInt(buf, 0); // main hand
        buf.writeByte(0);
        buf.writeByte(0);
        buf.writeByte(0);
    }

    @Override
    public void read(ByteBuf buf) {
        int[] pos = McDataTypes.readPosition(buf);
        x = pos[0];
        y = pos[1];
        z = pos[2];
        face = McDataTypes.readVarInt(buf);
        McDataTypes.readVarInt(buf); // hand
        buf.skipBytes(3); // cursorX, cursorY, cursorZ
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
