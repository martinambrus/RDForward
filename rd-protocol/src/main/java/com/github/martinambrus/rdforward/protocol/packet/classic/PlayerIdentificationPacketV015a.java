package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic 0.0.15a protocol 0x00 (Client -> Server): Player Identification.
 *
 * The 0.0.15a identification packet contains ONLY the username.
 * No protocol version byte, no verification key, no unused byte.
 * This is the simplest form of the Classic identification packet,
 * used before protocol versioning was introduced in 0.0.16a.
 *
 * Wire format (64 bytes payload):
 *   [64 bytes] username (space-padded UTF-8)
 */
public class PlayerIdentificationPacketV015a implements Packet {

    private String username;

    public PlayerIdentificationPacketV015a() {}

    public PlayerIdentificationPacketV015a(String username) {
        this.username = username;
    }

    @Override
    public int getPacketId() {
        return 0x00;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeClassicString(buf, username);
    }

    @Override
    public void read(ByteBuf buf) {
        username = McDataTypes.readClassicString(buf);
    }

    public String getUsername() { return username; }
}
