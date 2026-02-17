package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Netty protocol S2C: Time Update.
 *
 * Sent periodically to sync the client's world time.
 * Base packet ID: 0x03 (1.7.2-1.7.10).
 * Remapped per version via NettyPacketRegistry reverse maps.
 *
 * Wire format (16 bytes):
 *   [long] worldAge (total ticks since world creation)
 *   [long] timeOfDay (0-24000, negative = frozen)
 */
public class NettyTimeUpdatePacket implements Packet {

    private long worldAge;
    private long timeOfDay;

    public NettyTimeUpdatePacket() {}

    public NettyTimeUpdatePacket(long worldAge, long timeOfDay) {
        this.worldAge = worldAge;
        this.timeOfDay = timeOfDay;
    }

    @Override
    public int getPacketId() { return 0x03; }

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
