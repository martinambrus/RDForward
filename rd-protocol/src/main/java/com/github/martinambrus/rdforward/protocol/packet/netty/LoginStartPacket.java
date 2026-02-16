package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Login state, C2S packet 0x00: Login Start.
 *
 * Wire format: [String] username
 */
public class LoginStartPacket implements Packet {

    private String username;

    public LoginStartPacket() {}

    @Override
    public int getPacketId() { return 0x00; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, username);
    }

    @Override
    public void read(ByteBuf buf) {
        username = McDataTypes.readVarIntString(buf);
    }

    public String getUsername() { return username; }
}
