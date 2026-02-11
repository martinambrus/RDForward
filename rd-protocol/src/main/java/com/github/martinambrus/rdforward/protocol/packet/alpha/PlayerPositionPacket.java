package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x0B (Client -> Server): Player Position.
 *
 * Sent when position changes but look direction doesn't.
 * Y is feet position, stance is eye position (typically Y + 1.62).
 *
 * Wire format (33 bytes payload):
 *   [double] x
 *   [double] y (feet)
 *   [double] stance (eyes, y + 1.62)
 *   [double] z
 *   [boolean] on ground
 */
public class PlayerPositionPacket implements Packet {

    private double x;
    private double y;
    private double stance;
    private double z;
    private boolean onGround;

    public PlayerPositionPacket() {}

    public PlayerPositionPacket(double x, double y, double stance, double z, boolean onGround) {
        this.x = x;
        this.y = y;
        this.stance = stance;
        this.z = z;
        this.onGround = onGround;
    }

    @Override
    public int getPacketId() {
        return 0x0B;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(stance);
        buf.writeDouble(z);
        buf.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        stance = buf.readDouble();
        z = buf.readDouble();
        onGround = buf.readBoolean();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getStance() { return stance; }
    public double getZ() { return z; }
    public boolean isOnGround() { return onGround; }
}
