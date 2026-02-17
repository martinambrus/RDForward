package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x04: Player Position.
 *
 * 1.8 removed the stance (eye Y) double. Only feet Y is sent.
 *
 * Wire format (25 bytes):
 *   [double] x
 *   [double] feetY
 *   [double] z
 *   [boolean] onGround
 */
public class PlayerPositionPacketV47 implements Packet {

    private double x;
    private double y;
    private double z;
    private boolean onGround;

    public PlayerPositionPacketV47() {}

    @Override
    public int getPacketId() { return 0x04; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        onGround = buf.readBoolean();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
}
