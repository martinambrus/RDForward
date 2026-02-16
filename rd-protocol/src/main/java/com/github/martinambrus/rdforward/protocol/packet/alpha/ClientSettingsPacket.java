package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0xCC (Client -> Server): Client Settings.
 *
 * Sent by the client immediately after login and whenever settings change.
 * Silently consumed by the server.
 *
 * Wire format (v39):
 *   [String16] locale (e.g. "en_US")
 *   [byte]     viewDistance (0=far, 1=normal, 2=short, 3=tiny)
 *   [byte]     chatFlags
 *   [byte]     difficulty (client-side setting)
 */
public class ClientSettingsPacket implements Packet {

    @SuppressWarnings("unused") private String locale;
    @SuppressWarnings("unused") private byte viewDistance;
    @SuppressWarnings("unused") private byte chatFlags;
    @SuppressWarnings("unused") private byte difficulty;

    public ClientSettingsPacket() {}

    @Override
    public int getPacketId() {
        return 0xCC;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeString16(buf, locale != null ? locale : "en_US");
        buf.writeByte(viewDistance);
        buf.writeByte(chatFlags);
        buf.writeByte(difficulty);
    }

    @Override
    public void read(ByteBuf buf) {
        locale = McDataTypes.readString16(buf);
        viewDistance = buf.readByte();
        chatFlags = buf.readByte();
        difficulty = buf.readByte();
    }
}
