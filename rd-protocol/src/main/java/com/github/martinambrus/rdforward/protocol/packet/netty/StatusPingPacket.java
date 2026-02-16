package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Status state, bidirectional packet 0x01: Ping/Pong.
 *
 * Wire format: [long] time
 */
public class StatusPingPacket implements Packet {

    private long time;

    public StatusPingPacket() {}

    public StatusPingPacket(long time) {
        this.time = time;
    }

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(time);
    }

    @Override
    public void read(ByteBuf buf) {
        time = buf.readLong();
    }

    public long getTime() { return time; }
}
