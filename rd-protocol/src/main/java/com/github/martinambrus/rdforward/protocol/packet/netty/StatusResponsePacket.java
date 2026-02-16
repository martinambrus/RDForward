package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Status state, S2C packet 0x00: Status Response.
 *
 * Wire format: [String] JSON response
 */
public class StatusResponsePacket implements Packet {

    private String json;

    public StatusResponsePacket() {}

    public StatusResponsePacket(String json) {
        this.json = json;
    }

    @Override
    public int getPacketId() { return 0x00; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, json);
    }

    @Override
    public void read(ByteBuf buf) {
        json = McDataTypes.readVarIntString(buf);
    }

    public String getJson() { return json; }
}
