package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.BlockPlacementData;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x08: Block Placement.
 *
 * 1.8 changed coordinates to a packed Position long and uses V47 slot format
 * (byte TAG_End for no NBT instead of short(-1)).
 *
 * Wire format:
 *   [Position] location (packed long)
 *   [byte]     direction
 *   [slot]     held item (V47 format)
 *   [byte]     cursorX
 *   [byte]     cursorY
 *   [byte]     cursorZ
 */
public class NettyBlockPlacementPacketV47 implements Packet, BlockPlacementData {

    private int x;
    private int y;
    private int z;
    private int direction;
    private short heldItemId;

    public NettyBlockPlacementPacketV47() {}

    public NettyBlockPlacementPacketV47(int x, int y, int z, int direction) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
        this.heldItemId = -1;
    }

    public NettyBlockPlacementPacketV47(int x, int y, int z, int direction, short heldItemId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
        this.heldItemId = heldItemId;
    }

    @Override
    public int getPacketId() { return 0x08; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writePosition(buf, x, y, z);
        buf.writeByte(direction);
        if (heldItemId >= 0) {
            McDataTypes.writeV47SlotItem(buf, heldItemId, 64, 0);
        } else {
            McDataTypes.writeEmptyV47Slot(buf);
        }
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
        direction = buf.readByte();
        // Read item ID, then skip the rest of slot data (V47 format)
        heldItemId = buf.readShort();
        if (heldItemId >= 0) {
            buf.skipBytes(1); // count
            buf.skipBytes(2); // damage
            // V47 NBT: byte 0x00 = TAG_End, 0x0A = TAG_Compound
            byte nbtType = buf.readByte();
            if (nbtType == 0x0A) {
                // Skip compound contents — reuse McDataTypes internal method
                // by calling skipV47SlotData indirectly. Since we already read
                // the compound tag byte, we need to skip the compound body.
                McDataTypes.skipNbtCompound(buf);
            }
        }
        buf.skipBytes(3); // cursorX, cursorY, cursorZ
    }

    @Override
    public int getX() { return x; }
    @Override
    public int getY() { return y; }
    @Override
    public int getZ() { return z; }
    @Override
    public int getDirection() { return direction; }
    @Override
    public short getItemId() { return heldItemId; }
}
