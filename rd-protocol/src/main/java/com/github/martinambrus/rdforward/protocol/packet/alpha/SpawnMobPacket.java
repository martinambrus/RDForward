package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x18 (Server -> Client): Spawn Mob.
 *
 * Spawns a non-player entity (mob) in the world.
 *
 * Wire format (19 bytes payload):
 *   [int]  entity ID
 *   [byte] mob type
 *   [int]  x (fixed-point, absolute)
 *   [int]  y (fixed-point, absolute)
 *   [int]  z (fixed-point, absolute)
 *   [byte] yaw (packed rotation)
 *   [byte] pitch (packed rotation)
 */
public class SpawnMobPacket implements Packet {

    private int entityId;
    private byte type;
    private int x;
    private int y;
    private int z;
    private byte yaw;
    private byte pitch;

    public SpawnMobPacket() {}

    public SpawnMobPacket(int entityId, int type, int x, int y, int z, int yaw, int pitch) {
        this.entityId = entityId;
        this.type = (byte) type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = (byte) yaw;
        this.pitch = (byte) pitch;
    }

    @Override
    public int getPacketId() {
        return 0x18;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(type);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        type = buf.readByte();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        yaw = buf.readByte();
        pitch = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public byte getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public byte getYaw() { return yaw; }
    public byte getPitch() { return pitch; }
}
