package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic 0.0.15a protocol 0x00 (Server -> Client): Server Identification.
 *
 * The 0.0.15a server identification contains ONLY the server name.
 * No protocol version byte, no MOTD, no user type.
 *
 * Wire format (64 bytes payload):
 *   [64 bytes] server name (space-padded UTF-8)
 */
public class ServerIdentificationPacketV015a implements Packet {

    private String serverName;

    public ServerIdentificationPacketV015a() {}

    public ServerIdentificationPacketV015a(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public int getPacketId() {
        return 0x00;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeClassicString(buf, serverName);
    }

    @Override
    public void read(ByteBuf buf) {
        serverName = McDataTypes.readClassicString(buf);
    }

    public String getServerName() { return serverName; }
}
