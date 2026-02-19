package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19 Play state, C2S packet 0x1C: Player Digging.
 *
 * Same as V477 but with a VarInt sequence field appended.
 * The sequence is used for BlockChangedAck responses.
 *
 * Wire format:
 *   [byte]     status
 *   [Position] location (packed long, 1.14 encoding)
 *   [byte]     face
 *   [VarInt]   sequence
 */
public class PlayerDiggingPacketV759 extends PlayerDiggingPacketV477 {

    private int sequence;

    public PlayerDiggingPacketV759() {}

    @Override
    public int getPacketId() { return 0x1C; }

    @Override
    public void read(ByteBuf buf) {
        super.read(buf);
        sequence = McDataTypes.readVarInt(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        super.write(buf);
        McDataTypes.writeVarInt(buf, sequence);
    }

    public int getSequence() { return sequence; }
}
