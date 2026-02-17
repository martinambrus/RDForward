package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Pre-Netty protocol 0x46 (Server -> Client): Change Game State.
 *
 * Used for weather changes and game mode changes.
 * Introduced in Beta 1.5.
 *
 * Wire format (2 bytes payload):
 *   [byte] reason (1=begin rain, 2=end rain, 3=change game mode)
 *   [byte] value (game mode for reason 3, 0 otherwise)
 */
public class ChangeGameStatePacket implements Packet {

    public static final int BEGIN_RAIN = 1;
    public static final int END_RAIN = 2;
    public static final int CHANGE_GAME_MODE = 3;

    private byte reason;
    private byte value;

    public ChangeGameStatePacket() {}

    public ChangeGameStatePacket(int reason, int value) {
        this.reason = (byte) reason;
        this.value = (byte) value;
    }

    @Override
    public int getPacketId() { return 0x46; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(reason);
        buf.writeByte(value);
    }

    @Override
    public void read(ByteBuf buf) {
        reason = buf.readByte();
        value = buf.readByte();
    }

    public byte getReason() { return reason; }
    public byte getValue() { return value; }
}
