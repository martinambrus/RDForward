package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x0C: Steer Vehicle.
 *
 * Wire format (10 bytes):
 *   [float]   sideways
 *   [float]   forward
 *   [boolean] jump
 *   [boolean] unmount
 */
public class NettySteerVehiclePacket implements Packet {

    public NettySteerVehiclePacket() {}

    @Override
    public int getPacketId() { return 0x0C; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeZero(10);
    }

    @Override
    public void read(ByteBuf buf) {
        buf.skipBytes(10);
    }
}
