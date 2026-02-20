package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.2 Play state, S2C packet 0x42: Player Position.
 *
 * Completely rewritten wire format from V762. Teleport ID moved to the front,
 * delta movement velocity fields added, flags changed from byte to int.
 *
 * Wire format:
 *   [VarInt]  teleportId
 *   [double]  x
 *   [double]  y (feet-level)
 *   [double]  z
 *   [double]  deltaMovX (0.0)
 *   [double]  deltaMovY (0.0)
 *   [double]  deltaMovZ (0.0)
 *   [float]   yaw
 *   [float]   pitch
 *   [int]     flags (0 = all absolute)
 */
public class NettyPlayerPositionS2CPacketV768 implements Packet {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private int teleportId;

    public NettyPlayerPositionS2CPacketV768() {}

    public NettyPlayerPositionS2CPacketV768(double x, double y, double z,
                                              float yaw, float pitch, int teleportId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.teleportId = teleportId;
    }

    @Override
    public int getPacketId() { return 0x42; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, teleportId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(0.0); // deltaMovX
        buf.writeDouble(0.0); // deltaMovY
        buf.writeDouble(0.0); // deltaMovZ
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeInt(0); // flags: all absolute
    }

    @Override
    public void read(ByteBuf buf) {
        teleportId = McDataTypes.readVarInt(buf);
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        buf.readDouble(); // deltaMovX
        buf.readDouble(); // deltaMovY
        buf.readDouble(); // deltaMovZ
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        buf.readInt(); // flags
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public int getTeleportId() { return teleportId; }
}
