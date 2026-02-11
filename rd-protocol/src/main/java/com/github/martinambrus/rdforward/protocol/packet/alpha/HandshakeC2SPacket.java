package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x02 (Client -> Server): Handshake.
 *
 * The very first packet sent by an Alpha client, containing the username.
 *
 * Wire format:
 *   [string16] username
 */
public class HandshakeC2SPacket implements Packet {

    private String username;

    public HandshakeC2SPacket() {}

    public HandshakeC2SPacket(String username) {
        this.username = username;
    }

    @Override
    public int getPacketId() {
        return 0x02;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeString16(buf, username);
    }

    @Override
    public void read(ByteBuf buf) {
        username = McDataTypes.readString16(buf);
    }

    public String getUsername() { return username; }
}
