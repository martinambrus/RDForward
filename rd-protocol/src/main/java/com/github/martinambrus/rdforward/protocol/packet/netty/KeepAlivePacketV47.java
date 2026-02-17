package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, bidirectional packet 0x00: Keep Alive.
 *
 * 1.8 changed keepAliveId from int to VarInt.
 *
 * Wire format: [VarInt] keepAliveId
 */
public class KeepAlivePacketV47 implements Packet {

    private int keepAliveId;

    public KeepAlivePacketV47() {}

    public KeepAlivePacketV47(int keepAliveId) {
        this.keepAliveId = keepAliveId;
    }

    @Override
    public int getPacketId() { return 0x00; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, keepAliveId);
    }

    @Override
    public void read(ByteBuf buf) {
        keepAliveId = McDataTypes.readVarInt(buf);
    }

    public int getKeepAliveId() { return keepAliveId; }
}
