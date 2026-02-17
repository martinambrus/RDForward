package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x0C: Spawn Player.
 *
 * 1.8 changed UUID from hyphenated String to raw 128-bit (2 longs),
 * removed name and property list (client resolves from tab list),
 * and coordinates are still fixed-point ints.
 *
 * Wire format:
 *   [VarInt] entityId
 *   [UUID]   uuid (2 longs, msb then lsb)
 *   [int]    x (fixed-point)
 *   [int]    y (fixed-point, feet)
 *   [int]    z (fixed-point)
 *   [byte]   yaw
 *   [byte]   pitch
 *   [short]  currentItem
 *   [metadata] entity metadata
 */
public class NettySpawnPlayerPacketV47 implements Packet {

    private int entityId;
    private long uuidMsb;
    private long uuidLsb;
    private int x;
    private int y;
    private int z;
    private int yaw;
    private int pitch;
    private short currentItem;

    public NettySpawnPlayerPacketV47() {}

    public NettySpawnPlayerPacketV47(int entityId, String uuidStr,
                                     int x, int y, int z,
                                     int yaw, int pitch, short currentItem) {
        this.entityId = entityId;
        parseUuid(uuidStr);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.currentItem = currentItem;
    }

    private void parseUuid(String uuidStr) {
        String noDashes = uuidStr.replace("-", "");
        uuidMsb = Long.parseUnsignedLong(noDashes.substring(0, 16), 16);
        uuidLsb = Long.parseUnsignedLong(noDashes.substring(16, 32), 16);
    }

    @Override
    public int getPacketId() { return 0x0C; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeLong(uuidMsb);
        buf.writeLong(uuidLsb);
        // No name, no properties — 1.8 resolves from PlayerListItem
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeShort(currentItem);
        // 1.8 entity metadata: type is (typeId << 5 | index) for pre-1.9 format
        // Index 0 = entity flags (byte), type=0
        buf.writeByte(0x00); // header: type=0(Byte), index=0
        buf.writeByte(0x00); // value: no flags set
        buf.writeByte(0x7F); // metadata terminator
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        uuidMsb = buf.readLong();
        uuidLsb = buf.readLong();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        yaw = buf.readByte();
        pitch = buf.readByte();
        currentItem = buf.readShort();
        // Skip metadata
        while (buf.readableBytes() > 0) {
            byte b = buf.readByte();
            if (b == 0x7F) break;
        }
    }
}
