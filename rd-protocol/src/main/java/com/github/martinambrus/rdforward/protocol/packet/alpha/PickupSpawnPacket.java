package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x15 (Server -> Client): Pickup Spawn.
 *
 * Spawns a dropped item entity in the world. Used to make items
 * appear on the ground for players to pick up.
 *
 * Wire format (22 bytes payload, protocol versions 6-10):
 *   [int]   entity ID
 *   [short] item ID
 *   [byte]  count
 *   [int]   x (absolute position * 32)
 *   [int]   y (absolute position * 32)
 *   [int]   z (absolute position * 32)
 *   [byte]  rotation
 *   [byte]  pitch
 *   [byte]  roll
 *
 * Note: later versions add a [short] damage field after count.
 */
public class PickupSpawnPacket implements Packet {

    private int entityId;
    private short itemId;
    private byte count;
    private int x;
    private int y;
    private int z;
    private byte rotation;
    private byte pitch;
    private byte roll;

    public PickupSpawnPacket() {}

    public PickupSpawnPacket(int entityId, int itemId, int count,
                             int x, int y, int z) {
        this.entityId = entityId;
        this.itemId = (short) itemId;
        this.count = (byte) count;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getPacketId() {
        return 0x15;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeShort(itemId);
        buf.writeByte(count);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(rotation);
        buf.writeByte(pitch);
        buf.writeByte(roll);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        itemId = buf.readShort();
        count = buf.readByte();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        rotation = buf.readByte();
        pitch = buf.readByte();
        roll = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public short getItemId() { return itemId; }
    public byte getCount() { return count; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
}
