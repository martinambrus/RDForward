package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x65 (Client -> Server): Close Window.
 *
 * Sent when the player closes a container window.
 *
 * Wire format (1 byte payload):
 *   [byte] window ID
 */
public class CloseWindowPacket implements Packet {

    private int windowId;

    public CloseWindowPacket() {}

    @Override
    public int getPacketId() {
        return 0x65;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
    }

    public int getWindowId() { return windowId; }
}
