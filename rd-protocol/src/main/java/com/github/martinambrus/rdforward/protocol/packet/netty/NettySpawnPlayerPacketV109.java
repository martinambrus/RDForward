package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, S2C packet 0x05: Spawn Player.
 *
 * 1.9 changed coordinates from fixed-point int to double, removed currentItem,
 * and restructured entity metadata (VarInt type IDs, 0xFF terminator).
 *
 * Wire format:
 *   [VarInt] entityId
 *   [UUID]   uuid (2 longs, msb then lsb)
 *   [double] x
 *   [double] y (feet)
 *   [double] z
 *   [byte]   yaw
 *   [byte]   pitch
 *   [metadata] entity metadata (1.9 format: ubyte index + VarInt type, 0xFF terminator)
 */
public class NettySpawnPlayerPacketV109 implements Packet {

    private int entityId;
    private long uuidMsb;
    private long uuidLsb;
    private double x;
    private double y;
    private double z;
    private int yaw;
    private int pitch;

    public NettySpawnPlayerPacketV109() {}

    public NettySpawnPlayerPacketV109(int entityId, String uuidStr,
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
        // 1.9 entity metadata: index=0 (entity flags), type=0 (Byte), value=0x00
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
        // Skip metadata
        while (buf.readableBytes() > 0) {
            int index = buf.readUnsignedByte();
            if (index == 0xFF) break;
            int type = McDataTypes.readVarInt(buf);
            skipMetadataValue(buf, type);
        }
    }

    private static void skipMetadataValue(ByteBuf buf, int type) {
        switch (type) {
            case 0: buf.skipBytes(1); break; // Byte
            case 1: McDataTypes.readVarInt(buf); break; // VarInt
            case 2: buf.skipBytes(4); break; // Float
            case 3: McDataTypes.readVarIntString(buf); break; // String
            case 4: McDataTypes.readVarIntString(buf); break; // Chat
            case 5: // Slot
                short itemId = buf.readShort();
                if (itemId >= 0) {
                    buf.skipBytes(1 + 2); // count + damage
                    byte nbt = buf.readByte();
                    if (nbt == 0x0A) {
                        // Skip compound
                        while (buf.readableBytes() > 0) {
                            byte t = buf.readByte();
                            if (t == 0) break;
                            buf.skipBytes(buf.readUnsignedShort()); // name
                        }
                    }
                }
                break;
            case 6: buf.skipBytes(1); break; // Boolean
            case 7: buf.skipBytes(12); break; // Rotation (3 floats)
            case 8: buf.skipBytes(8); break; // Position
            case 9: // Optional Position
                if (buf.readBoolean()) buf.skipBytes(8);
                break;
            case 10: McDataTypes.readVarInt(buf); break; // Direction (VarInt)
            case 11: // Optional UUID
                if (buf.readBoolean()) buf.skipBytes(16);
                break;
            case 12: McDataTypes.readVarInt(buf); break; // BlockID (VarInt)
        }
    }

    public int getEntityId() { return entityId; }
}
