package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Login state, S2C packet 0x03: Set Compression.
 *
 * Sent during the login sequence (before LoginSuccess) to enable
 * packet compression. After this packet, all subsequent packets in
 * both directions use the compressed wire format:
 *
 *   [VarInt totalLength] [VarInt dataLength] [data]
 *
 * dataLength=0 means the packet is below threshold (uncompressed).
 * dataLength>0 means the data is zlib-compressed, and dataLength
 * is the uncompressed size.
 *
 * Supported by 1.8+ clients (protocol 47+). Must NOT be sent to
 * 1.7.x clients which do not understand this packet.
 */
public class SetCompressionPacket implements Packet {

    private int threshold;

    public SetCompressionPacket() {}

    public SetCompressionPacket(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public int getPacketId() { return 0x03; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, threshold);
    }

    @Override
    public void read(ByteBuf buf) {
        threshold = McDataTypes.readVarInt(buf);
    }

    public int getThreshold() { return threshold; }
}
