package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x04 (Server -> Client): Time Update.
 *
 * Sent periodically to sync the client's world time for day/night cycle.
 * Time is in ticks (0 = dawn, 6000 = noon, 12000 = sunset, 18000 = midnight).
 *
 * Wire format (8 bytes payload):
 *   [long] time (world ticks)
 */
public class TimeUpdatePacket implements Packet {

    private long time;

    public TimeUpdatePacket() {}

    public TimeUpdatePacket(long time) {
        this.time = time;
    }

    @Override
    public int getPacketId() {
        return 0x04;
    }

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
