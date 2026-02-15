package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x3C (Server -> Client): Explosion.
 *
 * Sent when an explosion occurs. Contains the center position,
 * radius, and a list of affected block offsets.
 *
 * Wire format:
 *   [double] x
 *   [double] y
 *   [double] z
 *   [float]  radius
 *   [int]    record count
 *   [byte*3] per record: dx, dy, dz (offsets from center)
 */
public class ExplosionPacket implements Packet {

    private double x;
    private double y;
    private double z;
    private float radius;
    private byte[] records;

    public ExplosionPacket() {}

    public ExplosionPacket(double x, double y, double z, float radius, byte[] records) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.records = records;
    }

    @Override
    public int getPacketId() {
        return 0x3C;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(radius);
        int count = records != null ? records.length / 3 : 0;
        buf.writeInt(count);
        if (records != null) {
            buf.writeBytes(records);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        radius = buf.readFloat();
        int count = buf.readInt();
        records = new byte[count * 3];
        buf.readBytes(records);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getRadius() { return radius; }
    public byte[] getRecords() { return records; }
}
