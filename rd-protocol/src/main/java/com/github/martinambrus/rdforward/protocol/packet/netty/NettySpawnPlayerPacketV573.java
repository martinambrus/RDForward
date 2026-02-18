package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.15 Play state, S2C packet 0x05: Spawn Player.
 *
 * Entity metadata was REMOVED in 1.15 — no 0xFF terminator.
 *
 * Wire format:
 *   [VarInt] entityId
 *   [UUID]   uuid (2 longs, msb then lsb)
 *   [double] x
 *   [double] y (feet)
 *   [double] z
 *   [byte]   yaw
 *   [byte]   pitch
 */
public class NettySpawnPlayerPacketV573 implements Packet {

    private int entityId;
    private long uuidMsb;
    private long uuidLsb;
    private double x;
    private double y;
    private double z;
    private int yaw;
    private int pitch;

    public NettySpawnPlayerPacketV573() {}

    public NettySpawnPlayerPacketV573(int entityId, String uuidStr,
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
    public int getPacketId() { return 0x05; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeLong(uuidMsb);
        buf.writeLong(uuidLsb);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        // No metadata terminator in 1.15+
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        uuidMsb = buf.readLong();
        uuidLsb = buf.readLong();
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readByte();
        pitch = buf.readByte();
        // No metadata in 1.15+
    }

    public int getEntityId() { return entityId; }
}
