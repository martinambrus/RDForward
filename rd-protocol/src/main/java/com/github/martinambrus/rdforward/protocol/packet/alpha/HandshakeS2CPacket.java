package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x02 (Server -> Client): Handshake Response.
 *
 * Sent by the server in response to the client handshake.
 * Contains the connection hash for authentication, or "-" for offline mode.
 *
 * Wire format:
 *   [string16] connection hash ("-" = offline mode)
 */
public class HandshakeS2CPacket implements Packet {

    private String connectionHash;

    public HandshakeS2CPacket() {}

    public HandshakeS2CPacket(String connectionHash) {
        this.connectionHash = connectionHash;
    }

    @Override
    public int getPacketId() {
        return 0x02;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeJavaUTF(buf, connectionHash);
    }

    @Override
    public void read(ByteBuf buf) {
        connectionHash = McDataTypes.readJavaUTF(buf);
    }

    public String getConnectionHash() { return connectionHash; }
}
