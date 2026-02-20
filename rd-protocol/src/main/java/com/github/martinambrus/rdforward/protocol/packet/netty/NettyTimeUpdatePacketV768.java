package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.2 Play state, S2C packet 0x6B: Time Update.
 *
 * Extends the base format with a boolean doDaylightCycle field.
 * The dayTime value is always non-negative (no more negative = frozen trick).
 *
 * Wire format:
 *   [long]    worldAge
 *   [long]    dayTime (always non-negative)
 *   [boolean] doDaylightCycle
 */
public class NettyTimeUpdatePacketV768 implements Packet {

    private long worldAge;
    private long timeOfDay;

    public NettyTimeUpdatePacketV768() {}

    public NettyTimeUpdatePacketV768(long worldAge, long timeOfDay) {
        this.worldAge = worldAge;
        this.timeOfDay = timeOfDay;
    }

    @Override
    public int getPacketId() { return 0x6B; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(worldAge);
        // V768: dayTime is always non-negative; doDaylightCycle boolean replaces the negative trick
        buf.writeLong(Math.abs(timeOfDay));
        buf.writeBoolean(timeOfDay >= 0); // doDaylightCycle: true when not frozen
    }

    @Override
    public void read(ByteBuf buf) {
        worldAge = buf.readLong();
        timeOfDay = buf.readLong();
        buf.readBoolean(); // doDaylightCycle
    }

    public long getWorldAge() { return worldAge; }
    public long getTimeOfDay() { return timeOfDay; }
}
