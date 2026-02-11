package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x0E (Server -> Client): Disconnect Player.
 *
 * Sent by the server to disconnect a client with a reason message.
 * The client should display the reason and close the connection.
 *
 * Wire format (64 bytes payload):
 *   [64 bytes] disconnect reason (space-padded US-ASCII)
 */
public class DisconnectPacket implements Packet {

    private String reason;

    public DisconnectPacket() {}

    public DisconnectPacket(String reason) {
        this.reason = reason;
    }

    @Override
    public int getPacketId() {
        return 0x0E;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeClassicString(buf, reason);
    }

    @Override
    public void read(ByteBuf buf) {
        reason = McDataTypes.readClassicString(buf);
    }

    public String getReason() { return reason; }
}
