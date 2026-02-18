package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.16 C2S Player Abilities.
 *
 * In 1.16, the C2S format was simplified to just the flags byte.
 * The fly speed and walk speed floats were removed (S2C still has them).
 *
 * Wire format (C2S only):
 *   [byte] flags (bit 1 = isFlying)
 */
public class PlayerAbilitiesPacketV735 implements Packet {

    private byte flags;

    public PlayerAbilitiesPacketV735() {}

    @Override
    public int getPacketId() { return 0x1A; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(flags);
    }

    @Override
    public void read(ByteBuf buf) {
        flags = buf.readByte();
    }

    public byte getFlags() { return flags; }
}
