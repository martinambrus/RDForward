package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, S2C packet 0x2E: Player Position And Look.
 *
 * 1.9 added VarInt teleportId at end. Client must respond with TeleportConfirm.
 *
 * Wire format:
 *   [double]  x
 *   [double]  y (eye-level)
 *   [double]  z
 *   [float]   yaw
 *   [float]   pitch
 *   [byte]    flags (0=absolute)
 *   [VarInt]  teleportId
 */
public class NettyPlayerPositionS2CPacketV109 implements Packet {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private int teleportId;

    public NettyPlayerPositionS2CPacketV109() {}

    public NettyPlayerPositionS2CPacketV109(double x, double y, double z,
                                              float yaw, float pitch, int teleportId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.teleportId = teleportId;
    }

    @Override
    public int getPacketId() { return 0x2E; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeByte(0x00); // flags: all absolute
        McDataTypes.writeVarInt(buf, teleportId);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        buf.readByte(); // flags
        teleportId = McDataTypes.readVarInt(buf);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public int getTeleportId() { return teleportId; }
}
