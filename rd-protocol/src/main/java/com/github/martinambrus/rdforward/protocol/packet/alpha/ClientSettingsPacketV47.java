package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.4.2+ protocol 0xCC (Client -> Server): Client Settings.
 *
 * Same as v39 ClientSettingsPacket but with an additional boolean showCape field.
 * Silently consumed by the server.
 *
 * Wire format (v47):
 *   [String16] locale (e.g. "en_US")
 *   [byte]     viewDistance (0=far, 1=normal, 2=short, 3=tiny)
 *   [byte]     chatFlags
 *   [byte]     difficulty (client-side setting)
 *   [boolean]  showCape
 */
public class ClientSettingsPacketV47 implements Packet {

    @SuppressWarnings("unused") private String locale;
    @SuppressWarnings("unused") private byte viewDistance;
    @SuppressWarnings("unused") private byte chatFlags;
    @SuppressWarnings("unused") private byte difficulty;
    @SuppressWarnings("unused") private boolean showCape;

    public ClientSettingsPacketV47() {}

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
        buf.writeBoolean(showCape);
    }

    @Override
    public void read(ByteBuf buf) {
        locale = McDataTypes.readString16(buf);
        viewDistance = buf.readByte();
        chatFlags = buf.readByte();
        difficulty = buf.readByte();
        showCape = buf.readBoolean();
    }
}
