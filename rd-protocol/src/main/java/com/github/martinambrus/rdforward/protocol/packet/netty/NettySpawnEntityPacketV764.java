package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Play state, S2C packet 0x01: Spawn Entity.
 *
 * In 1.20.2, SpawnPlayer was removed and players are spawned via the
 * generic SpawnEntity packet with entityType=122 (Player).
 *
 * Wire format:
 *   [VarInt]  entityId
 *   [UUID]    entityUUID (2 longs)
 *   [VarInt]  entityType (122 = Player)
 *   [Double]  x
 *   [Double]  y (feet-level)
 *   [Double]  z
 *   [Byte]    pitch (NOTE: pitch BEFORE yaw, swapped vs SpawnPlayer)
 *   [Byte]    yaw
 *   [Byte]    headYaw (= yaw)
 *   [VarInt]  data (0)
 *   [Short]   velocityX (0)
 *   [Short]   velocityY (0)
 *   [Short]   velocityZ (0)
 */
public class NettySpawnEntityPacketV764 implements Packet {

    private static final int ENTITY_TYPE_PLAYER = 122;

    private int entityId;
    private long uuidMsb;
    private long uuidLsb;
    private double x;
    private double y;
    private double z;
    private int yaw;
    private int pitch;

    public NettySpawnEntityPacketV764() {}

    public NettySpawnEntityPacketV764(int entityId, String uuidStr,
                                      double x, double y, double z,
                                      int yaw, int pitch) {
        this.entityId = entityId;
        parseUuid(uuidStr);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    private void parseUuid(String uuidStr) {
        String noDashes = uuidStr.replace("-", "");
        uuidMsb = Long.parseUnsignedLong(noDashes.substring(0, 16), 16);
        uuidLsb = Long.parseUnsignedLong(noDashes.substring(16, 32), 16);
    }

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeLong(uuidMsb);
        buf.writeLong(uuidLsb);
        McDataTypes.writeVarInt(buf, ENTITY_TYPE_PLAYER);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeByte(pitch); // pitch BEFORE yaw
        buf.writeByte(yaw);
        buf.writeByte(yaw); // headYaw = yaw
        McDataTypes.writeVarInt(buf, 0); // data
        buf.writeShort(0); // velocityX
        buf.writeShort(0); // velocityY
        buf.writeShort(0); // velocityZ
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        uuidMsb = buf.readLong();
        uuidLsb = buf.readLong();
        McDataTypes.readVarInt(buf); // entityType
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        pitch = buf.readByte();
        yaw = buf.readByte();
        buf.readByte(); // headYaw
        McDataTypes.readVarInt(buf); // data
        buf.readShort(); // velocityX
        buf.readShort(); // velocityY
        buf.readShort(); // velocityZ
    }

    public int getEntityId() { return entityId; }
}
