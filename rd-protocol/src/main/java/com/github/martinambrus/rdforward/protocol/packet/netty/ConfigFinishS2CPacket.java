package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Configuration state, S2C packet 0x02: Finish Configuration.
 *
 * Empty packet. Server signals end of the Configuration phase.
 */
public class ConfigFinishS2CPacket implements Packet {

    @Override
    public int getPacketId() { return 0x02; }

    @Override
    public void write(ByteBuf buf) {
        // Empty packet
    }

    @Override
    public void read(ByteBuf buf) {
        // Empty packet
    }
}
