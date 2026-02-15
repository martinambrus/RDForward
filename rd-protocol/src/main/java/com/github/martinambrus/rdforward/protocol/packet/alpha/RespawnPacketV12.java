package com.github.martinambrus.rdforward.protocol.packet.alpha;

import io.netty.buffer.ByteBuf;

/**
 * Beta 1.6+ protocol 0x09 (Client -> Server): Respawn.
 *
 * Beta 1.6 (v12) added a dimension byte to the Respawn packet.
 * Earlier Beta versions (v7-v11) used an empty payload.
 *
 * Wire format:
 *   [byte] dimension (0 = overworld, -1 = nether)
 */
public class RespawnPacketV12 extends RespawnPacket {

    private byte dimension;

    public RespawnPacketV12() {}

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(dimension);
    }

    @Override
    public void read(ByteBuf buf) {
        dimension = buf.readByte();
    }

    public byte getDimension() { return dimension; }
}
