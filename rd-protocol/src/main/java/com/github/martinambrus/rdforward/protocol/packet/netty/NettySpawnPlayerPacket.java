package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, S2C packet 0x0C: Spawn Player.
 *
 * Wire format:
 *   [VarInt] entityId
 *   [String] uuid (hyphenated)
 *   [String] playerName
 *   [int]    x (fixed-point)
 *   [int]    y (fixed-point, feet)
 *   [int]    z (fixed-point)
 *   [byte]   yaw
 *   [byte]   pitch
 *   [short]  currentItem
 *   [metadata] entity metadata (must have at least one entry — see below)
 */
public class NettySpawnPlayerPacket implements Packet {

    private int entityId;
    private String uuid;
    private String playerName;
    private int x;
    private int y;
    private int z;
    private int yaw;
    private int pitch;
    private short currentItem;

    public NettySpawnPlayerPacket() {}

    public NettySpawnPlayerPacket(int entityId, String uuid, String playerName,
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
        // Note: 1.7.2 does NOT have a property count/list field here.
        // That was added in 1.7.6. ViaLegacy confirms: r1_7_2_5Tor1_7_6_10
        // inserts VarInt(0) when converting from 1.7.2 to 1.7.6 format.
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeShort(currentItem);
        // Entity metadata — MUST include at least one entry. The 1.7.2 client's
        // SpawnPlayer packet getter c() tries to fall back to this.i.c() (DataWatcher)
        // when the metadata list is null, but this.i is never set during read().
        // Sending just 0x7F produces a null list → NPE. Send entity flags (index 0).
        // Format: byte header = (type << 5) | index, then type-specific data.
        buf.writeByte(0x00); // header: type=0(Byte), index=0 (entity flags)
        buf.writeByte(0x00); // value: no flags set
        buf.writeByte(0x7F); // metadata terminator
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        uuid = McDataTypes.readVarIntString(buf);
        playerName = McDataTypes.readVarIntString(buf);
        // Note: 1.7.2 has no property count/list field (added in 1.7.6)
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        yaw = buf.readByte();
        pitch = buf.readByte();
        currentItem = buf.readShort();
        // Skip metadata (just read until 0x7F)
        while (buf.readableBytes() > 0) {
            byte b = buf.readByte();
            if (b == 0x7F) break;
            // Skip metadata entry based on type — we don't parse it
        }
    }

    public int getEntityId() { return entityId; }
    public String getPlayerName() { return playerName; }
    public String getUuid() { return uuid; }
}
