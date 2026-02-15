package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x1B (Client -> Server): Input / Steer Vehicle.
 *
 * Sent for vehicle steering input. Not used on this server — payload
 * is silently consumed.
 *
 * Wire format (18 bytes payload):
 *   [float]   sideways
 *   [float]   forward
 *   [float]   yaw
 *   [float]   pitch
 *   [boolean] jump
 *   [boolean] unmount
 */
public class InputPacket implements Packet {

    public InputPacket() {}

    @Override
    public int getPacketId() {
        return 0x1B;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeZero(18);
    }

    @Override
    public void read(ByteBuf buf) {
        buf.skipBytes(18);
    }
}
