package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x00 (Client -> Server): Player Identification.
 *
 * Sent by the client as the very first packet to identify itself.
 * This is the Classic equivalent of a login/handshake.
 *
 * Wire format (130 bytes payload):
 *   [1 byte]  protocol version (7 for Classic)
 *   [64 bytes] username (space-padded US-ASCII)
 *   [64 bytes] verification key (space-padded US-ASCII, or empty for offline)
 *   [1 byte]  unused (0x00)
 */
public class PlayerIdentificationPacket implements Packet {

    private int protocolVersion;
    private String username;
    private String verificationKey;

    public PlayerIdentificationPacket() {}

    public PlayerIdentificationPacket(int protocolVersion, String username, String verificationKey) {
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
        buf.writeByte(0x00); // unused
    }

    @Override
    public void read(ByteBuf buf) {
        protocolVersion = buf.readUnsignedByte();
        username = McDataTypes.readClassicString(buf);
        verificationKey = McDataTypes.readClassicString(buf);
        buf.readByte(); // unused
    }

    public int getProtocolVersion() { return protocolVersion; }
    public String getUsername() { return username; }
    public String getVerificationKey() { return verificationKey; }
}
