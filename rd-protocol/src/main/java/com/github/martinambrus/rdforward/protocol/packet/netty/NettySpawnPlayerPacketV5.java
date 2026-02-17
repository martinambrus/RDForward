package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.6+ Play state, S2C packet 0x0C: Spawn Player.
 *
 * Same as {@link NettySpawnPlayerPacket} but with a property list
 * (VarInt count + entries) inserted between playerName and coordinates.
 * We always write an empty property list (VarInt 0).
 *
 * Wire format:
 *   [VarInt] entityId
 *   [String] uuid (hyphenated)
 *   [String] playerName
 *   [VarInt] propertyCount (0 for us)
 *   [int]    x (fixed-point)
 *   [int]    y (fixed-point, feet)
 *   [int]    z (fixed-point)
 *   [byte]   yaw
 *   [byte]   pitch
 *   [short]  currentItem
 *   [metadata] entity metadata
 */
public class NettySpawnPlayerPacketV5 implements Packet {

    private int entityId;
    private String uuid;
    private String playerName;
    private int x;
    private int y;
    private int z;
    private int yaw;
    private int pitch;
    private short currentItem;

    public NettySpawnPlayerPacketV5() {}

    public NettySpawnPlayerPacketV5(int entityId, String uuid, String playerName,
                                    int x, int y, int z,
                                    int yaw, int pitch, short currentItem) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.currentItem = currentItem;
    }

    @Override
    public int getPacketId() { return 0x0C; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        McDataTypes.writeVarIntString(buf, uuid);
        McDataTypes.writeVarIntString(buf, playerName);
        // 1.7.6+ property list — write empty (0 properties)
        McDataTypes.writeVarInt(buf, 0);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeShort(currentItem);
        // Entity metadata — same as v4: entity flags (index 0) + terminator
        buf.writeByte(0x00); // header: type=0(Byte), index=0 (entity flags)
        buf.writeByte(0x00); // value: no flags set
        buf.writeByte(0x7F); // metadata terminator
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        uuid = McDataTypes.readVarIntString(buf);
        playerName = McDataTypes.readVarIntString(buf);
        // Skip property list
        int propertyCount = McDataTypes.readVarInt(buf);
        for (int i = 0; i < propertyCount; i++) {
            McDataTypes.readVarIntString(buf); // name
            McDataTypes.readVarIntString(buf); // value
            McDataTypes.readVarIntString(buf); // signature
        }
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        yaw = buf.readByte();
        pitch = buf.readByte();
        currentItem = buf.readShort();
        // Skip metadata (read until 0x7F)
        while (buf.readableBytes() > 0) {
            byte b = buf.readByte();
            if (b == 0x7F) break;
        }
    }
}
