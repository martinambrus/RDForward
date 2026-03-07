package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.14 Play state, S2C packet 0x05: Spawn Player.
 *
 * Same wire format as V109 (entity metadata still present; removed in 1.15).
 * Must send at least one metadata entry (index 0 = entity flags byte)
 * because some client versions return null for terminator-only metadata.
 *
 * Wire format:
 *   [VarInt] entityId
 *   [UUID]   uuid (2 longs, msb then lsb)
 *   [double] x
 *   [double] y (feet)
 *   [double] z
 *   [byte]   yaw
 *   [byte]   pitch
 *   [metadata] entity metadata (at least index 0 byte + 0xFF terminator)
 */
public class NettySpawnPlayerPacketV477 implements Packet {

    private int entityId;
    private long uuidMsb;
    private long uuidLsb;
    private double x;
    private double y;
    private double z;
    private int yaw;
    private int pitch;

    public NettySpawnPlayerPacketV477() {}

    public NettySpawnPlayerPacketV477(int entityId, String uuidStr,
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
        // 1.14 entity metadata (1.9+ format): index=0 (entity flags), type=0 (Byte), value=0
        buf.writeByte(0);    // index 0
        McDataTypes.writeVarInt(buf, 0); // type 0 = Byte
        buf.writeByte(0);    // value: no flags set
        buf.writeByte(0xFF); // metadata terminator
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
        // Skip metadata (read until 0xFF terminator)
        while (buf.readableBytes() > 0) {
            if (buf.readUnsignedByte() == 0xFF) break;
        }
    }

    public int getEntityId() { return entityId; }
}
