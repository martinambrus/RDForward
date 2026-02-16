package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Status state, C2S packet 0x00: Status Request.
 * Empty payload.
 */
public class StatusRequestPacket implements Packet {

    @Override
    public int getPacketId() { return 0x00; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {}
}
