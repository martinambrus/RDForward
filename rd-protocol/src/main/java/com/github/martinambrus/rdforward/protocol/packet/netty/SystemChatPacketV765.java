package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;


/**
 * 1.20.3 Play state, S2C packet 0x69: System Chat Message.
 *
 * Changed from V760: text component switched from JSON String to NBT Tag.
 * The NBT tag can be TAG_String (simple) or TAG_Compound (formatted text).
 *
 * Wire format:
 *   [NBT Tag]   textComponent
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
        buf.writeByte(0x08); // TAG_String type
        McDataTypes.writeNbtStringPayload(buf, plainText);
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
        } else if (tagType == 0x0A) { // TAG_Compound
            plainText = McDataTypes.readNbtTextComponent(buf);
        } else {
            // Unknown tag type — skip remaining bytes except trailing overlay boolean
            if (buf.readableBytes() > 1) {
                buf.skipBytes(buf.readableBytes() - 1);
            }
            plainText = "";
        }

        if (buf.readableBytes() >= 1) {
            overlay = buf.readBoolean();
        }
    }

    public String getPlainText() { return plainText; }
}
