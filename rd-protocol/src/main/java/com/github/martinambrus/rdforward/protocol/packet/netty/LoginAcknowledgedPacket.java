package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Login state, C2S packet 0x03: Login Acknowledged.
 *
 * Empty packet. Client sends this after receiving LoginSuccess to signal
 * it is ready to transition from LOGIN to CONFIGURATION state.
 */
public class LoginAcknowledgedPacket implements Packet {

    @Override
    public int getPacketId() { return 0x03; }

    @Override
    public void write(ByteBuf buf) {
        // Empty packet
    }

    @Override
    public void read(ByteBuf buf) {
        // Empty packet
    }
}
