package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x06: Player Position and Look.
 *
 * 1.8 removed the stance (eye Y) double. Only feet Y is sent.
 *
 * Wire format (33 bytes):
 *   [double]  x
 *   [double]  feetY
 *   [double]  z
 *   [float]   yaw
 *   [float]   pitch
 *   [boolean] onGround
 */
public class PlayerPositionAndLookC2SPacketV47 implements Packet {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;

    public PlayerPositionAndLookC2SPacketV47() {}

    @Override
    public int getPacketId() { return 0x06; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        onGround = buf.readBoolean();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
}
