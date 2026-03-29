package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0x14 (Server -> Client): Named Entity Spawn.
 *
 * Same as pre-v39 SpawnPlayerPacket but with entity metadata appended.
 * Must send at least one metadata entry (index 0 = entity flags byte)
 * because the client's DataWatcher.readList() returns null for
 * terminator-only metadata, causing NPE in SpawnPlayer.c().
 *
 * Wire format:
 *   [int]      entity ID
 *   [string16] player name
 *   [int]      x (fixed-point)
 *   [int]      y (fixed-point)
 *   [int]      z (fixed-point)
 *   [byte]     yaw
 *   [byte]     pitch
 *   [short]    current item
 *   [metadata] entity metadata (at least index 0 byte + 0x7F terminator)
 */
public class SpawnPlayerPacketV39 implements Packet {

    private int entityId;
    private String playerName;
    private int x;
    private int y;
    private int z;
    private int yaw;
    private int pitch;
    private short currentItem;

    public SpawnPlayerPacketV39() {}

    public SpawnPlayerPacketV39(int entityId, String playerName, int x, int y, int z,
                                 int yaw, int pitch, short currentItem) {
        this.entityId = entityId;
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.currentItem = currentItem;
    }

    @Override
    public int getPacketId() {
        return 0x14;
    }

    /** Pre-Netty clients enforce max 16 chars for player names in readString(). */
    private static final int MAX_NAME_LENGTH = 16;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        String name = playerName.length() > MAX_NAME_LENGTH
                ? playerName.substring(0, MAX_NAME_LENGTH) : playerName;
        McDataTypes.writeString16(buf, name);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeShort(currentItem);
        // Entity metadata: index 0 (byte) = entity flags, value 0.
        // type=0 (byte) << 5 | index=0 → 0x00
        buf.writeByte(0x00);
        buf.writeByte(0);
        buf.writeByte(0x7F); // metadata terminator
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        playerName = McDataTypes.readString16(buf);
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        yaw = buf.readUnsignedByte();
        pitch = buf.readUnsignedByte();
        currentItem = buf.readShort();
        // Skip entity metadata — read until 0x7F terminator
        while (buf.readableBytes() > 0) {
            byte b = buf.readByte();
            if (b == 0x7F) break;
            // Metadata type is encoded in upper 3 bits of the first byte
            int type = (b >> 5) & 0x07;
            switch (type) {
                case 0: buf.readByte(); break;
                case 1: buf.readShort(); break;
                case 2: buf.readInt(); break;
                case 3: buf.readFloat(); break;
                case 4: McDataTypes.readString16(buf); break;
                case 5: buf.readShort(); buf.readByte(); buf.readShort(); break; // ItemStack
                case 6: buf.readInt(); buf.readInt(); buf.readInt(); break; // Vector
                default: break;
            }
        }
    }

    public int getEntityId() { return entityId; }
    public String getPlayerName() { return playerName; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getYaw() { return yaw; }
    public int getPitch() { return pitch; }
    public short getCurrentItem() { return currentItem; }
}
