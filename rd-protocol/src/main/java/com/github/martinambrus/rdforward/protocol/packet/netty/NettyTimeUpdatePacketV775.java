package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 26.1 Play state, S2C packet: Set Time.
 *
 * Completely rewritten from V768. Format:
 *   [long]   gameTime (total ticks since world creation)
 *   [VarInt] clockUpdates map size
 *   Per entry:
 *     [VarInt]  WorldClock holder reference (registry index + 1)
 *     [VarLong] totalTicks (day time for this clock)
 *     [float]   partialTick
 *     [float]   rate (1.0 = normal speed, 0.0 = frozen)
 */
public class NettyTimeUpdatePacketV775 implements Packet {

    private long worldAge;
    private long timeOfDay;

    public NettyTimeUpdatePacketV775() {}

    public NettyTimeUpdatePacketV775(long worldAge, long timeOfDay) {
        this.worldAge = worldAge;
        this.timeOfDay = timeOfDay;
    }

    @Override
    public int getPacketId() { return 0x71; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(worldAge);
        // 1 clock update entry: overworld clock
        McDataTypes.writeVarInt(buf, 1);
        // WorldClock holder: inline reference = registry index + 1 (overworld = index 0 → value 1)
        McDataTypes.writeVarInt(buf, 1);
        // ClockNetworkState: totalTicks (VarLong), partialTick (float), rate (float)
        writeVarLong(buf, Math.abs(timeOfDay));
        buf.writeFloat(0.0f); // partialTick
        buf.writeFloat(timeOfDay >= 0 ? 1.0f : 0.0f); // rate: 1.0 = running, 0.0 = frozen
    }

    @Override
    public void read(ByteBuf buf) {
        worldAge = buf.readLong();
        int mapSize = McDataTypes.readVarInt(buf);
        for (int i = 0; i < mapSize; i++) {
            McDataTypes.readVarInt(buf); // holder ref
            readVarLong(buf); // totalTicks
            buf.readFloat(); // partialTick
            buf.readFloat(); // rate
        }
    }

    public long getTimeOfDay() { return timeOfDay; }

    private static void writeVarLong(ByteBuf buf, long value) {
        while (true) {
            if ((value & ~0x7FL) == 0) {
                buf.writeByte((int) value);
                return;
            }
            buf.writeByte((int) (value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }

    private static long readVarLong(ByteBuf buf) {
        long result = 0;
        int shift = 0;
        byte b;
        do {
            b = buf.readByte();
            result |= (long) (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }
}
