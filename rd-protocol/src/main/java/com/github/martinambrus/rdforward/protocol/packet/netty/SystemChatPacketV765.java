package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 1.20.3 Play state, S2C packet 0x69: System Chat Message.
 *
 * Changed from V760: text component switched from JSON String to NBT Tag.
 *
 * Wire format:
 *   [NBT Tag]   textComponent (for plain text: TAG_String = 0x08 + short(len) + UTF-8 bytes)
 *   [Boolean]   overlay (true = action bar, false = chat area)
 */
public class SystemChatPacketV765 implements Packet {

    private String plainText;
    private boolean overlay;

    public SystemChatPacketV765() {}

    public SystemChatPacketV765(String plainText, boolean overlay) {
        this.plainText = plainText;
        this.overlay = overlay;
    }

    @Override
    public int getPacketId() { return 0x69; }

    @Override
    public void write(ByteBuf buf) {
        // Write as NBT TAG_String (type 0x08) — network NBT nameless root tag
        byte[] textBytes = plainText.getBytes(StandardCharsets.UTF_8);
        buf.writeByte(0x08); // TAG_String type
        buf.writeShort(textBytes.length);
        buf.writeBytes(textBytes);
        buf.writeBoolean(overlay);
    }

    @Override
    public void read(ByteBuf buf) {
        int tagType = buf.readByte();
        if (tagType == 0x08) { // TAG_String
            int len = buf.readUnsignedShort();
            byte[] bytes = new byte[len];
            buf.readBytes(bytes);
            plainText = new String(bytes, StandardCharsets.UTF_8);
        }
        overlay = buf.readBoolean();
    }

    public String getPlainText() { return plainText; }
}
