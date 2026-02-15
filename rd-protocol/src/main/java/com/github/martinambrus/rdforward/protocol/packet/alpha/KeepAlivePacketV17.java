package com.github.martinambrus.rdforward.protocol.packet.alpha;

import io.netty.buffer.ByteBuf;

/**
 * Beta 1.8+ protocol 0x00 (bidirectional): Keep Alive with int ID.
 *
 * Beta 1.8 (v17) changed KeepAlive from zero-payload to include an int ID.
 * The server sends a KeepAlive with a unique ID, and the client echoes it back.
 *
 * Wire format (4 bytes payload):
 *   [int] keepAliveId
 */
public class KeepAlivePacketV17 extends KeepAlivePacket {

    private int keepAliveId;

    public KeepAlivePacketV17() {}

    public KeepAlivePacketV17(int keepAliveId) {
        this.keepAliveId = keepAliveId;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(keepAliveId);
    }

    @Override
    public void read(ByteBuf buf) {
        keepAliveId = buf.readInt();
    }

    public int getKeepAliveId() { return keepAliveId; }
}
