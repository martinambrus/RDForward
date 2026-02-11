package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x0A (Server -> Client): Position Update.
 *
 * Relative position update without orientation change.
 * Used when a player moves but doesn't change their look direction.
 *
 * Wire format (4 bytes payload):
 *   [1 byte] player ID (signed)
 *   [1 byte] change X (signed, fixed-point delta)
 *   [1 byte] change Y (signed, fixed-point delta)
 *   [1 byte] change Z (signed, fixed-point delta)
 */
public class PositionUpdatePacket implements Packet {

    private int playerId;
    private int changeX;
    private int changeY;
    private int changeZ;

    public PositionUpdatePacket() {}

    public PositionUpdatePacket(int playerId, int changeX, int changeY, int changeZ) {
        this.playerId = playerId;
        this.changeX = changeX;
        this.changeY = changeY;
        this.changeZ = changeZ;
    }

    @Override
    public int getPacketId() {
        return 0x0A;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(playerId);
        buf.writeByte(changeX);
        buf.writeByte(changeY);
        buf.writeByte(changeZ);
    }

    @Override
    public void read(ByteBuf buf) {
        playerId = buf.readByte(); // signed
        changeX = buf.readByte();  // signed
        changeY = buf.readByte();  // signed
        changeZ = buf.readByte();  // signed
    }

    public int getPlayerId() { return playerId; }
    public int getChangeX() { return changeX; }
    public int getChangeY() { return changeY; }
    public int getChangeZ() { return changeZ; }
}
