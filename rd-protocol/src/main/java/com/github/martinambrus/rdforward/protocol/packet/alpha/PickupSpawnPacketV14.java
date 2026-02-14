package com.github.martinambrus.rdforward.protocol.packet.alpha;

import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x15: Pickup Spawn (protocol versions 10-14).
 *
 * Wire format (24 bytes payload):
 *   [int]   entity ID
 *   [short] item ID
 *   [byte]  count
 *   [short] damage    <-- extra field vs v6
 *   [int]   x (absolute position * 32)
 *   [int]   y (absolute position * 32)
 *   [int]   z (absolute position * 32)
 *   [byte]  rotation
 *   [byte]  pitch
 *   [byte]  roll
 *
 * Extends PickupSpawnPacket so existing instanceof checks still work.
 */
public class PickupSpawnPacketV14 extends PickupSpawnPacket {

    private short damage;

    public PickupSpawnPacketV14() {}

    public PickupSpawnPacketV14(int entityId, int itemId, int count, int damage,
                                int x, int y, int z) {
        super(entityId, itemId, count, x, y, z);
        this.damage = (short) damage;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(getEntityId());
        buf.writeShort(getItemId());
        buf.writeByte(getCount());
        buf.writeShort(damage);
        buf.writeInt(getX());
        buf.writeInt(getY());
        buf.writeInt(getZ());
        buf.writeByte(0); // rotation
        buf.writeByte(0); // pitch
        buf.writeByte(0); // roll
    }

    @Override
    public void read(ByteBuf buf) {
        // Re-read all fields since we can't call super.read() (wrong byte order)
        setEntityId(buf.readInt());
        setItemId(buf.readShort());
        setCount(buf.readByte());
        damage = buf.readShort();
        setX(buf.readInt());
        setY(buf.readInt());
        setZ(buf.readInt());
        buf.readByte(); // rotation
        buf.readByte(); // pitch
        buf.readByte(); // roll
    }

    public short getDamage() { return damage; }
}
