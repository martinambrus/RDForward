package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Play state, C2S: Chat Command (unsigned, for offline mode).
 *
 * Used for messages starting with "/" — the command text excludes the leading slash.
 *
 * Wire format:
 *   [String]  command (without leading /)
 *   [Long]    timestamp (millis since epoch)
 *   [Long]    salt (0 for unsigned)
 *   [VarInt]  argumentSignatureCount (0 for unsigned)
 *   [VarInt]  lastSeenMessages offset (0)
 *   [FixedBitSet(20)] acknowledged (3 bytes, all zeros)
 */
public class NettyChatCommandC2SPacketV764 implements Packet {

    private String command;

    public NettyChatCommandC2SPacketV764() {}

    public NettyChatCommandC2SPacketV764(String command) {
        this.command = command;
    }

    @Override
    public int getPacketId() { return 0x04; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, command);
        buf.writeLong(System.currentTimeMillis()); // timestamp
        buf.writeLong(0L);                          // salt (no signing)
        McDataTypes.writeVarInt(buf, 0);            // no argument signatures
        McDataTypes.writeVarInt(buf, 0);            // lastSeenMessages offset
        buf.writeBytes(new byte[3]);                // acknowledged: FixedBitSet(20), all zeros
    }

    @Override
    public void read(ByteBuf buf) {
        command = McDataTypes.readVarIntString(buf);
        buf.skipBytes(buf.readableBytes());
    }

    public String getCommand() { return command; }
}
