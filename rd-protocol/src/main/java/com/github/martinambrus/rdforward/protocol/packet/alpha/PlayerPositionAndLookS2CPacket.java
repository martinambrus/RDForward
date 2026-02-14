package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x0D (Server -> Client): Player Position and Look.
 *
 * Used to teleport the player (initial spawn, or server-side correction).
 *
 * IMPORTANT: In the S2C direction, y = posY (eyes/camera position),
 * NOT feet. The client sets posY from this value, and computes
 * BB.minY (feet) = posY - 1.62.
 *
 * In the C2S direction, y = BB.minY (feet) and stance = posY (eyes).
 * The asymmetry exists because the same Packet13 class is used for both
 * directions, but the client writes BB.minY as y (C2S) while reading
 * y directly into posY (S2C).
 *
 * Wire format (41 bytes payload):
 *   [double]  x
 *   [double]  y (posY = eyes for S2C, feet for C2S)
 *   [double]  stance (feet for S2C, posY = eyes for C2S)
 *   [double]  z
 *   [float]   yaw (degrees)
 *   [float]   pitch (degrees)
 *   [boolean] on ground
 */
public class PlayerPositionAndLookS2CPacket implements Packet {

    private double x;
    private double y;
    private double stance;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;

    public PlayerPositionAndLookS2CPacket() {}

    public PlayerPositionAndLookS2CPacket(double x, double y, double stance, double z,
                                          float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.stance = stance;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public int getPacketId() {
        return 0x0D;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(stance);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        stance = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        onGround = buf.readBoolean();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getStance() { return stance; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public boolean isOnGround() { return onGround; }
}
