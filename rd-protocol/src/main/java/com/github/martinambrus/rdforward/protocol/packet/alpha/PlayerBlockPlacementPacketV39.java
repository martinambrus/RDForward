package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0x0F (Client -> Server): Player Block Placement.
 *
 * Same as V22 but NBT is unconditional for ALL items (not just damageable),
 * and 3 cursor offset bytes are appended at the end.
 *
 * Wire format:
 *   [int]   x (block position)
 *   [byte]  y (block position, 0-255)
 *   [int]   z (block position)
 *   [byte]  direction (face clicked, or -1)
 *   [short] item ID (-1 = empty hand)
 *   if item ID >= 0:
 *     [byte]  amount
 *     [short] damage/metadata
 *     [short] nbt length (-1 = no NBT, >=0 = gzipped NBT bytes follow)
 *   [byte]  cursor X
 *   [byte]  cursor Y
 *   [byte]  cursor Z
 */
public class PlayerBlockPlacementPacketV39 implements Packet, BlockPlacementData {

    private int x;
    private int y;
    private int z;
    private int direction;
    private short itemId;
    private byte amount;
    private short damage;

    public PlayerBlockPlacementPacketV39() {}

    @Override
    public int getPacketId() {
        return 0x0F;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeByte(y);
        buf.writeInt(z);
        buf.writeByte(direction);
        buf.writeShort(itemId);
        if (itemId >= 0) {
            buf.writeByte(amount);
            buf.writeShort(damage);
            McDataTypes.writeEmptyNbtItemTag(buf);
        }
        buf.writeByte(0); // cursorX
        buf.writeByte(0); // cursorY
        buf.writeByte(0); // cursorZ
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readByte();
        z = buf.readInt();
        direction = buf.readByte();
        itemId = buf.readShort();
        if (itemId >= 0) {
            amount = buf.readByte();
            damage = buf.readShort();
            McDataTypes.skipNbtItemTag(buf);
        }
        buf.readByte(); // cursorX
        buf.readByte(); // cursorY
        buf.readByte(); // cursorZ
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getDirection() { return direction; }
    public short getItemId() { return itemId; }
    public byte getAmount() { return amount; }
    public short getDamage() { return damage; }
}
