package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0xCB (Client -> Server): Tab-Complete.
 *
 * Sent when the client presses Tab in the chat box.
 * Silently consumed by the server.
 *
 * Wire format:
 *   [String16] text (partial chat input)
 */
public class TabCompletePacket implements Packet {

    @SuppressWarnings("unused") private String text;

    public TabCompletePacket() {}

    @Override
    public int getPacketId() {
        return 0xCB;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeString16(buf, text != null ? text : "");
    }

    @Override
    public void read(ByteBuf buf) {
        text = McDataTypes.readString16(buf);
    }
}
