package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic 0.0.16a protocol 0x00 (Server -> Client): Server Identification.
 *
 * Same as the standard Classic ServerIdentification but WITHOUT the trailing
 * userType byte. Classic 0.0.16a-0.0.19a (protocol version 3-6) omit this byte.
 *
 * Wire format (129 bytes payload):
 *   [1 byte]  protocol version (3 for 0.0.16a)
 *   [64 bytes] server name (space-padded US-ASCII)
 *   [64 bytes] server MOTD (space-padded US-ASCII)
 */
public class ServerIdentificationPacketV016a implements Packet {

    private int protocolVersion;
    private String serverName;
    private String serverMotd;

    public ServerIdentificationPacketV016a() {}

    public ServerIdentificationPacketV016a(int protocolVersion, String serverName, String serverMotd) {
        this.protocolVersion = protocolVersion;
        this.serverName = serverName;
        this.serverMotd = serverMotd;
    }

    @Override
    public int getPacketId() {
        return 0x00;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(protocolVersion);
        McDataTypes.writeClassicString(buf, serverName);
        McDataTypes.writeClassicString(buf, serverMotd);
    }

    @Override
    public void read(ByteBuf buf) {
        protocolVersion = buf.readUnsignedByte();
        serverName = McDataTypes.readClassicString(buf);
        serverMotd = McDataTypes.readClassicString(buf);
    }

    public int getProtocolVersion() { return protocolVersion; }
    public String getServerName() { return serverName; }
    public String getServerMotd() { return serverMotd; }
}
