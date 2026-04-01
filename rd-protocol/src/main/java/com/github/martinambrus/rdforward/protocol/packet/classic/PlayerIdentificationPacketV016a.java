package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic 0.0.16a protocol 0x00 (Client -> Server): Player Identification.
 *
 * Same as the standard Classic PlayerIdentification but WITHOUT the trailing
 * unused byte. Classic 0.0.16a-0.0.19a (protocol version 3-6) omit this byte.
 *
 * Wire format (129 bytes payload):
 *   [1 byte]  protocol version (3 for 0.0.16a)
 *   [64 bytes] username (space-padded US-ASCII)
 *   [64 bytes] verification key (space-padded US-ASCII, or "--" for offline)
 */
public class PlayerIdentificationPacketV016a implements Packet {

    private int protocolVersion;
    private String username;
    private String verificationKey;

    public PlayerIdentificationPacketV016a() {}

    public PlayerIdentificationPacketV016a(int protocolVersion, String username, String verificationKey) {
        this.protocolVersion = protocolVersion;
        this.username = username;
        this.verificationKey = verificationKey;
    }

    @Override
    public int getPacketId() {
        return 0x00;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(protocolVersion);
        McDataTypes.writeClassicString(buf, username);
        McDataTypes.writeClassicString(buf, verificationKey);
    }

    @Override
    public void read(ByteBuf buf) {
        protocolVersion = buf.readUnsignedByte();
        username = McDataTypes.readClassicString(buf);
        verificationKey = McDataTypes.readClassicString(buf);
    }

    public int getProtocolVersion() { return protocolVersion; }
    public String getUsername() { return username; }
    public String getVerificationKey() { return verificationKey; }
}
