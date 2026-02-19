package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19 Play state, S2C packet 0x05: Block Changed Ack.
 *
 * Replaces AcknowledgePlayerDigging (1.15-1.18.2). Simplified to just
 * a VarInt sequence ID that matches the C2S digging/placement sequence.
 *
 * Wire format:
 *   [VarInt] sequenceId
 */
public class BlockChangedAckPacketV759 implements Packet {

    private int sequenceId;

    public BlockChangedAckPacketV759() {}

    public BlockChangedAckPacketV759(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    @Override
    public int getPacketId() { return 0x05; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, sequenceId);
    }

    @Override
    public void read(ByteBuf buf) {
        sequenceId = McDataTypes.readVarInt(buf);
    }
}
