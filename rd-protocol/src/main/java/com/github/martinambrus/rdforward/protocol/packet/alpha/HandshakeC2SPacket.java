package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x02 (Client -> Server): Handshake.
 *
 * The very first packet sent by an Alpha client, containing the username.
 *
 * Pre-v39 wire format:
 *   [string16] username (or "username;host:port" for Beta 1.8+)
 *
 * v39+ wire format:
 *   [byte]     protocol version
 *   [string16] username
 *   [string16] hostname
 *   [int]      port
 *
 * Detection: The first payload byte after the packet ID distinguishes the
 * formats. Pre-v39 starts with a 2-byte length prefix — for ASCII usernames
 * the first byte is always 0x00 (high byte of String16 char count for
 * writeUTF's byte count prefix for short usernames). v39+ starts with the
 * protocol version byte (39+, always non-zero and not a plausible writeUTF
 * byte count high byte for usernames).
 *
 * More precisely: pre-v39 readStringAuto peeks at first byte after the
 * 2-byte length prefix. But v39 sends a protocol version byte BEFORE any
 * string. So we peek at the very first byte: if it's small enough to be a
 * valid String16/writeUTF high byte (0x00), it's old format. If it's >= 29
 * (the next protocol version after v29), it's the v39+ format.
 */
public class HandshakeC2SPacket implements Packet {

    private String username;
    private boolean detectedString16;
    private boolean v39Format;
    private int protocolVersion;
    private String hostname;
    private int port;

    public HandshakeC2SPacket() {}

    public HandshakeC2SPacket(String username) {
        this.username = username;
    }

    public HandshakeC2SPacket(int protocolVersion, String username, String hostname, int port) {
        this.v39Format = true;
        this.protocolVersion = protocolVersion;
        this.username = username;
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public int getPacketId() {
        return 0x02;
    }

    @Override
    public void write(ByteBuf buf) {
        if (v39Format) {
            buf.writeByte(protocolVersion);
            McDataTypes.writeString16(buf, username);
            McDataTypes.writeString16(buf, hostname != null ? hostname : "");
            buf.writeInt(port);
        } else {
            McDataTypes.writeStringAdaptive(buf, username);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        // Peek at the first byte to determine format.
        // Old format: starts with 2-byte length prefix. For String16, the first
        // byte of the length is 0x00 for usernames < 256 chars. For writeUTF,
        // the first byte of the length is 0x00 for usernames < 256 bytes.
        // v39 format: starts with protocol version byte (39 = 0x27).
        byte firstByte = buf.getByte(buf.readerIndex());

        if (firstByte != 0x00) {
            // v39+ format: protocol version byte first
            v39Format = true;
            detectedString16 = true;
            protocolVersion = buf.readUnsignedByte();
            username = McDataTypes.readString16(buf);
            hostname = McDataTypes.readString16(buf);
            port = buf.readInt();
        } else {
            // Pre-v39 format: string auto-detect
            v39Format = false;
            Object[] result = McDataTypes.readStringAuto(buf);
            username = (String) result[0];
            detectedString16 = (Boolean) result[1];
        }
    }

    public String getUsername() { return username; }
    public boolean isDetectedString16() { return detectedString16; }
    public boolean isV39Format() { return v39Format; }
    public int getProtocolVersion() { return protocolVersion; }
    public String getHostname() { return hostname; }
    public int getPort() { return port; }
}
