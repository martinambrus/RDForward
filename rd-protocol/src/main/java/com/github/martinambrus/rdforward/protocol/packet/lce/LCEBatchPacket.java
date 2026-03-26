package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE Batch/Multiplexed packet (ID 0x12, C2S).
 *
 * The LCE client bundles multiple game packets into a single framed packet.
 * Wire format:
 *   [int]   playerEntityId
 *   [byte]  flags (always 1)
 *   [byte+payload]...  sub-packets: each is [packetId byte][standard payload]
 *
 * Sub-packets use the same wire format as their standalone counterparts.
 * Common sub-packets: 0x0A (OnGround), 0x0B (Position), 0x0C (Look),
 * 0x0D (PosLook), 0x0E (Digging), 0x0F (Placement), 0x12 (Animation).
 */
public class LCEBatchPacket implements Packet {

    private byte[] subPacketData;

    @Override
    public int getPacketId() { return 0x12; }

    @Override
    public void read(ByteBuf buf) {
        // Skip header: int entityId + byte flags
        buf.skipBytes(5);
        // Store remaining bytes — sub-packets are decoded in the handler
        subPacketData = new byte[buf.readableBytes()];
        buf.readBytes(subPacketData);
    }

    @Override
    public void write(ByteBuf buf) {
        // C2S only, never sent by server
    }

    public byte[] getSubPacketData() { return subPacketData; }
}
