package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 1.20.3 Play state, S2C packet 0x1B: Disconnect.
 *
 * Changed from pre-1.20.3: reason changed from JSON String to NBT Tag.
 *
 * Wire format: [NBT Tag] reason (TAG_String = 0x08 + short(len) + UTF-8 bytes)
 */
public class NettyDisconnectPacketV765 implements Packet {

    private String plainText;

    public NettyDisconnectPacketV765() {}

    public NettyDisconnectPacketV765(String plainText) {
        this.plainText = plainText;
    }

    @Override
    public int getPacketId() { return 0x1B; }

    @Override
    public void write(ByteBuf buf) {
        // Write as NBT TAG_String (type 0x08) — network NBT nameless root tag
        byte[] textBytes = plainText.getBytes(StandardCharsets.UTF_8);
        buf.writeByte(0x08); // TAG_String type
        buf.writeShort(textBytes.length);
        buf.writeBytes(textBytes);
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — no server-side decoding needed
    }

    public String getPlainText() { return plainText; }
}
