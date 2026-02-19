package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19.4 Play state, S2C packet 0x3C: Player Position And Look.
 *
 * Same as V755 but with the dismountVehicle boolean REMOVED.
 *
 * Wire format:
 *   [double]  x
 *   [double]  y (feet-level)
 *   [double]  z
 *   [float]   yaw
 *   [float]   pitch
 *   [byte]    flags (0=absolute)
 *   [VarInt]  teleportId
 */
public class NettyPlayerPositionS2CPacketV762 implements Packet {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private int teleportId;

    public NettyPlayerPositionS2CPacketV762() {}

    public NettyPlayerPositionS2CPacketV762(double x, double y, double z,
                                              float yaw, float pitch, int teleportId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.teleportId = teleportId;
    }

    @Override
    public int getPacketId() { return 0x3C; }

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
