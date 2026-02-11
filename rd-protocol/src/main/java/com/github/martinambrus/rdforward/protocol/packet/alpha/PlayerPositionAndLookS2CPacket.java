package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x0D (Server -> Client): Player Position and Look.
 *
 * Used to teleport the player (initial spawn, or server-side correction).
 *
 * IMPORTANT: In the S->C direction, the field order swaps y and stance
 * compared to C->S: (x, stance, y, z) instead of (x, y, stance, z).
 * This is a well-known quirk of the Alpha/Beta protocol.
 *
 * Wire format (41 bytes payload):
 *   [double]  x
 *   [double]  stance (eyes) — NOTE: before y, unlike C->S
 *   [double]  y (feet)      — NOTE: after stance, unlike C->S
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
        buf.writeDouble(stance); // swapped: stance before y
        buf.writeDouble(y);      // swapped: y after stance
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readDouble();
        stance = buf.readDouble(); // swapped: stance before y
        y = buf.readDouble();      // swapped: y after stance
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
