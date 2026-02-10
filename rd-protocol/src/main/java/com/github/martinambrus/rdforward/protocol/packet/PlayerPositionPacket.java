package com.github.martinambrus.rdforward.protocol.packet;

import io.netty.buffer.ByteBuf;

/**
 * Sent by client to update position and by server to broadcast
 * other players' positions.
 *
 * Contains the player's unique ID (assigned by server on join),
 * position (x, y, z as doubles for sub-block precision),
 * and rotation (yaw, pitch as floats).
 */
public class PlayerPositionPacket implements Packet {

    private int playerId;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public PlayerPositionPacket() {
    }

    public PlayerPositionPacket(int playerId, double x, double y, double z, float yaw, float pitch) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public PacketType getType() {
        return PacketType.PLAYER_POSITION;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(playerId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }

    @Override
    public void read(ByteBuf buf) {
        playerId = buf.readInt();
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readFloat();
        pitch = buf.readFloat();
    }

    public int getPlayerId() { return playerId; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
}
