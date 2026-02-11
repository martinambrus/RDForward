package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x00 (Server -> Client): Server Identification.
 *
 * Sent by the server in response to Player Identification.
 * Contains server info and the client's user type (op or normal).
 *
 * Wire format (130 bytes payload):
 *   [1 byte]  protocol version (7 for Classic)
 *   [64 bytes] server name (space-padded US-ASCII)
 *   [64 bytes] server MOTD (space-padded US-ASCII)
 *   [1 byte]  user type (0x00 = normal, 0x64 = op)
 */
public class ServerIdentificationPacket implements Packet {

    public static final int USER_TYPE_NORMAL = 0x00;
    public static final int USER_TYPE_OP = 0x64;

    private int protocolVersion;
    private String serverName;
    private String serverMotd;
    private int userType;

    public ServerIdentificationPacket() {}

    public ServerIdentificationPacket(int protocolVersion, String serverName, String serverMotd, int userType) {
        this.protocolVersion = protocolVersion;
        this.serverName = serverName;
        this.serverMotd = serverMotd;
        this.userType = userType;
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
        buf.writeByte(userType);
    }

    @Override
    public void read(ByteBuf buf) {
        protocolVersion = buf.readUnsignedByte();
        serverName = McDataTypes.readClassicString(buf);
        serverMotd = McDataTypes.readClassicString(buf);
        userType = buf.readUnsignedByte();
    }

    public int getProtocolVersion() { return protocolVersion; }
    public String getServerName() { return serverName; }
    public String getServerMotd() { return serverMotd; }
    public int getUserType() { return userType; }
}
