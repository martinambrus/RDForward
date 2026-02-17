package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x0C: Steer Vehicle.
 *
 * 1.8 changed the two booleans (jump, unmount) into a single flags byte.
 *
 * Wire format (9 bytes):
 *   [float] sideways
 *   [float] forward
 *   [byte]  flags (0x01=jump, 0x02=unmount)
 */
public class NettySteerVehiclePacketV47 implements Packet {

    public NettySteerVehiclePacketV47() {}

    @Override
    public int getPacketId() { return 0x0C; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        buf.skipBytes(9); // 2 floats + 1 byte
    }
}
