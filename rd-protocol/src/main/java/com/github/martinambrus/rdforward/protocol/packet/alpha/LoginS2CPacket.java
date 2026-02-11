package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x01 (Server -> Client): Login Response.
 *
 * Sent by the server to confirm login, assigns entity ID and world info.
 *
 * Wire format:
 *   [int]      entity ID (player's EID)
 *   [string16] unused ("")
 *   [string16] unused ("")
 *   [long]     map seed
 *   [byte]     dimension (0 = overworld)
 */
public class LoginS2CPacket implements Packet {

    private int entityId;
    private long mapSeed;
    private byte dimension;

    public LoginS2CPacket() {}

    public LoginS2CPacket(int entityId, long mapSeed, byte dimension) {
        this.entityId = entityId;
        this.mapSeed = mapSeed;
        this.dimension = dimension;
    }

    @Override
    public int getPacketId() {
        return 0x01;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        McDataTypes.writeString16(buf, "");
        McDataTypes.writeString16(buf, "");
        buf.writeLong(mapSeed);
        buf.writeByte(dimension);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        McDataTypes.readString16(buf); // unused
        McDataTypes.readString16(buf); // unused
        mapSeed = buf.readLong();
        dimension = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public long getMapSeed() { return mapSeed; }
    public byte getDimension() { return dimension; }
}
