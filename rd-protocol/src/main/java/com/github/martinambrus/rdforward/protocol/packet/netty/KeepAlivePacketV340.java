package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.12.2 Play state, bidirectional packet: Keep Alive.
 *
 * 1.12.2 (v340) changed keepAliveId from VarInt to Long (8 bytes).
 *
 * Wire format: [Long] keepAliveId
 */
public class KeepAlivePacketV340 implements Packet {

    private long keepAliveId;

    public KeepAlivePacketV340() {}

    public KeepAlivePacketV340(long keepAliveId) {
        this.keepAliveId = keepAliveId;
    }

    @Override
    public int getPacketId() { return 0x0B; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(keepAliveId);
    }

    @Override
    public void read(ByteBuf buf) {
        keepAliveId = buf.readLong();
    }

    public long getKeepAliveId() { return keepAliveId; }
}
