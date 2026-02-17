package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Netty protocol S2C: Change Game State.
 *
 * Used for weather changes, game mode changes, etc.
 * Base packet ID: 0x2B (1.7.2-1.7.10).
 * Remapped per version via NettyPacketRegistry reverse maps.
 *
 * Wire format (5 bytes):
 *   [ubyte] reason
 *   [float] value
 *
 * Weather reasons:
 *   1 = end rain (begin clear)
 *   2 = begin rain
 *   7 = rain level (value = 0.0-1.0)
 *   8 = thunder level (value = 0.0-1.0)
 */
public class NettyChangeGameStatePacket implements Packet {

    public static final int END_RAIN = 1;
    public static final int BEGIN_RAIN = 2;
    public static final int CHANGE_GAME_MODE = 3;
    public static final int RAIN_LEVEL = 7;
    public static final int THUNDER_LEVEL = 8;

    private int reason;
    private float value;

    public NettyChangeGameStatePacket() {}

    public NettyChangeGameStatePacket(int reason, float value) {
        this.reason = reason;
        this.value = value;
    }

    @Override
    public int getPacketId() { return 0x2B; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(reason);
        buf.writeFloat(value);
    }

    @Override
    public void read(ByteBuf buf) {
        reason = buf.readByte() & 0xFF;
        value = buf.readFloat();
    }

    public int getReason() { return reason; }
    public float getValue() { return value; }
}
