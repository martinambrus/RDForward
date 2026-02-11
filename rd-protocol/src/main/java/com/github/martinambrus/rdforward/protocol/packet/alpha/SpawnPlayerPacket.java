package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x14 (Server -> Client): Named Entity Spawn (Spawn Player).
 *
 * Sent when another player comes into visible range. Uses int entity IDs
 * (vs Classic's signed-byte player IDs) and fixed-point int coordinates
 * (vs Classic's fixed-point short coordinates).
 *
 * Wire format:
 *   [int]      entity ID
 *   [string16] player name
 *   [int]      x (absolute position * 32, fixed-point)
 *   [int]      y (absolute position * 32, fixed-point)
 *   [int]      z (absolute position * 32, fixed-point)
 *   [byte]     yaw (rotation * 256/360)
 *   [byte]     pitch (rotation * 256/360)
 *   [short]    current item (held item ID, 0 = empty)
 */
public class SpawnPlayerPacket implements Packet {

    private int entityId;
    private String playerName;
    private int x;
    private int y;
    private int z;
    private int yaw;
    private int pitch;
    private short currentItem;

    public SpawnPlayerPacket() {}

    public SpawnPlayerPacket(int entityId, String playerName, int x, int y, int z,
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

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        McDataTypes.writeString16(buf, playerName);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeShort(currentItem);
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
