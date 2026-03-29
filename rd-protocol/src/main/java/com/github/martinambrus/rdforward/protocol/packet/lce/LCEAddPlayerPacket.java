package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE AddPlayer packet (ID 20/0x14, S2C).
 *
 * Extended version of Java's SpawnPlayer with LCE-specific fields:
 * yHeadRot, xuid, OnlineXuid, playerIndex, skinId, capeId, gamePrivileges.
 *
 * Wire format:
 *   [int]        entityId
 *   [string16]   name
 *   [int]        x (fixed-point)
 *   [int]        y (fixed-point)
 *   [int]        z (fixed-point)
 *   [byte]       yRot
 *   [byte]       xRot
 *   [byte]       yHeadRot
 *   [short]      carriedItem
 *   [long]       xuid (PlayerUID)
 *   [long]       onlineXuid (PlayerUID)
 *   [byte]       playerIndex
 *   [int]        skinId
 *   [int]        capeId
 *   [int]        gamePrivileges
 *   [...]        entity metadata (SynchedEntityData)
 */
public class LCEAddPlayerPacket implements Packet {

    private int entityId;
    private String name;
    private int x, y, z;
    private byte yRot, xRot, yHeadRot;
    private short carriedItem;
    private long xuid;
    private long onlineXuid;
    private byte playerIndex;
    private int skinId;
    private int capeId;
    private int gamePrivileges;

    public LCEAddPlayerPacket() {}

    public LCEAddPlayerPacket(int entityId, String name,
                               int x, int y, int z,
                               byte yRot, byte xRot, byte yHeadRot,
                               short carriedItem,
                               long xuid, long onlineXuid,
                               byte playerIndex,
                               int skinId, int capeId,
                               int gamePrivileges) {
        this.entityId = entityId;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yRot = yRot;
        this.xRot = xRot;
        this.yHeadRot = yHeadRot;
        this.carriedItem = carriedItem;
        this.xuid = xuid;
        this.onlineXuid = onlineXuid;
        this.playerIndex = playerIndex;
        this.skinId = skinId;
        this.capeId = capeId;
        this.gamePrivileges = gamePrivileges;
    }

    @Override
    public int getPacketId() { return 0x14; }

    /** LCE clients (Java 1.6.4 base) enforce max 16 chars for player names. */
    private static final int MAX_NAME_LENGTH = 16;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        String truncatedName = name.length() > MAX_NAME_LENGTH
                ? name.substring(0, MAX_NAME_LENGTH) : name;
        McDataTypes.writeString16(buf, truncatedName);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yRot);
        buf.writeByte(xRot);
        buf.writeByte(yHeadRot);
        buf.writeShort(carriedItem);
        buf.writeLong(xuid);
        buf.writeLong(onlineXuid);
        buf.writeByte(playerIndex);
        buf.writeInt(skinId);
        buf.writeInt(capeId);
        buf.writeInt(gamePrivileges);
        // Entity metadata: index 0 (byte) = entity flags, value 0.
        // type=0 (byte) << 5 | index=0 → 0x00, then value byte 0, then terminator 0x7F
        buf.writeByte(0x00);
        buf.writeByte(0);
        buf.writeByte(0x7F);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        name = McDataTypes.readString16(buf);
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        yRot = buf.readByte();
        xRot = buf.readByte();
        yHeadRot = buf.readByte();
        carriedItem = buf.readShort();
        xuid = buf.readLong();
        onlineXuid = buf.readLong();
        playerIndex = buf.readByte();
        skinId = buf.readInt();
        capeId = buf.readInt();
        gamePrivileges = buf.readInt();
        // Skip entity metadata
        while (buf.readableBytes() > 0) {
            int item = buf.readUnsignedByte();
            if (item == 0x7F) break;
            int type = (item & 0xE0) >> 5;
            switch (type) {
                case 0: buf.readByte(); break;
                case 1: buf.readShort(); break;
                case 2: buf.readInt(); break;
                case 3: buf.readFloat(); break;
                case 4: McDataTypes.readString16(buf); break;
                case 5: buf.skipBytes(5); break; // ItemStack: short + byte + short
                case 6: buf.skipBytes(12); break; // Position: int + int + int
                default: break;
            }
        }
    }
}
