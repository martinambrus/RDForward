package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x08: Player Position And Look.
 *
 * 1.8 changed boolean onGround to byte flags (0x00 = all absolute).
 *
 * Wire format:
 *   [double] x
 *   [double] y (eye-level)
 *   [double] z
 *   [float]  yaw
 *   [float]  pitch
 *   [byte]   flags (0=absolute, bits: 0x01=relX, 0x02=relY, 0x04=relZ, 0x08=relYaw, 0x10=relPitch)
 */
public class NettyPlayerPositionS2CPacketV47 implements Packet {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public NettyPlayerPositionS2CPacketV47() {}

    public NettyPlayerPositionS2CPacketV47(double x, double y, double z,
                                             float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() { return 0x08; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeByte(0x00); // flags: all absolute
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        buf.readByte(); // flags
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
}
