package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, S2C packet 0x40: Disconnect.
 *
 * Wire format: [String] JSON reason
 */
public class NettyDisconnectPacket implements Packet {

    private String jsonReason;

    public NettyDisconnectPacket() {}

    public NettyDisconnectPacket(String jsonReason) {
        this.jsonReason = jsonReason;
    }

    @Override
    public int getPacketId() { return 0x40; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, jsonReason);
    }

    @Override
    public void read(ByteBuf buf) {
        jsonReason = McDataTypes.readVarIntString(buf);
    }

    public String getJsonReason() { return jsonReason; }
}
