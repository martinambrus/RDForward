package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Play state, C2S: Chat Message (unsigned, for offline mode).
 *
 * Wire format:
 *   [String]  message
 *   [Long]    timestamp (millis since epoch)
 *   [Long]    salt (0 for unsigned)
 *   [Boolean] hasSignature (false for unsigned)
 *   [VarInt]  lastSeenMessages offset (0)
 *   [FixedBitSet(20)] acknowledged (3 bytes, all zeros)
 */
public class NettyChatC2SPacketV764 implements Packet {

    private String message;

    public NettyChatC2SPacketV764() {}

    public NettyChatC2SPacketV764(String message) {
        this.message = message;
    }

    @Override
    public int getPacketId() { return 0x05; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, message);
        buf.writeLong(System.currentTimeMillis()); // timestamp
        buf.writeLong(0L);                          // salt (no signing)
        buf.writeBoolean(false);                    // no signature
        McDataTypes.writeVarInt(buf, 0);            // lastSeenMessages offset
        buf.writeBytes(new byte[3]);                // acknowledged: FixedBitSet(20), all zeros
    }

    @Override
    public void read(ByteBuf buf) {
        message = McDataTypes.readVarIntString(buf);
        buf.skipBytes(buf.readableBytes()); // skip signing fields
    }

    public String getMessage() { return message; }
}
