package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x09 (Server -> Client): Position and Orientation Update.
 *
 * Relative position update with orientation. Sent when a player moves
 * a small distance (delta fits in a signed byte: -128 to 127).
 * The delta values are in fixed-point units (divide by 32 for blocks).
 *
 * Wire format (6 bytes payload):
 *   [1 byte]  player ID (signed)
 *   [1 byte]  change X (signed, fixed-point delta)
 *   [1 byte]  change Y (signed, fixed-point delta)
 *   [1 byte]  change Z (signed, fixed-point delta)
 *   [1 byte]  yaw (0-255)
 *   [1 byte]  pitch (0-255)
 */
public class PositionOrientationUpdatePacket implements Packet {

    private int playerId;
    private int changeX;
    private int changeY;
    private int changeZ;
    private int yaw;
    private int pitch;

    public PositionOrientationUpdatePacket() {}

    public PositionOrientationUpdatePacket(int playerId, int changeX, int changeY, int changeZ, int yaw, int pitch) {
        this.playerId = playerId;
        this.changeX = changeX;
        this.changeY = changeY;
        this.changeZ = changeZ;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() {
        return 0x09;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(playerId);
        buf.writeByte(changeX);
        buf.writeByte(changeY);
        buf.writeByte(changeZ);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
    }

    @Override
    public void read(ByteBuf buf) {
        playerId = buf.readByte(); // signed
        changeX = buf.readByte(); // signed
        changeY = buf.readByte(); // signed
        changeZ = buf.readByte(); // signed
        yaw = buf.readUnsignedByte();
        pitch = buf.readUnsignedByte();
    }

    public int getPlayerId() { return playerId; }
    public int getChangeX() { return changeX; }
    public int getChangeY() { return changeY; }
    public int getChangeZ() { return changeZ; }
    public int getYaw() { return yaw; }
    public int getPitch() { return pitch; }
}
