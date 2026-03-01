package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.4.2+ (v47+) pre-Netty S2C 0x04: Time Update.
 *
 * Changed from 1 long (v39) to 2 longs (v47+).
 *
 * Wire format (16 bytes payload):
 *   [long] worldAge (total ticks since world creation)
 *   [long] timeOfDay (0-24000, negative = frozen)
 */
public class TimeUpdatePacketV47 implements Packet {

    private long worldAge;
    private long timeOfDay;

    public TimeUpdatePacketV47() {}

    public TimeUpdatePacketV47(long worldAge, long timeOfDay) {
        this.worldAge = worldAge;
        this.timeOfDay = timeOfDay;
    }

    @Override
    public int getPacketId() {
        return 0x04;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(worldAge);
        buf.writeLong(timeOfDay);
    }

    @Override
    public void read(ByteBuf buf) {
        worldAge = buf.readLong();
        timeOfDay = buf.readLong();
    }

    public long getWorldAge() { return worldAge; }
    public long getTimeOfDay() { return timeOfDay; }
}
