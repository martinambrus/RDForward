package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19 Play state, C2S packet 0x03: Chat Command.
 *
 * New in 1.19 — sent when the player types a slash command.
 * The command field does NOT include the leading "/".
 *
 * Wire format:
 *   [String]  command (without leading "/")
 *   [Long]    timestamp (Instant.now().toEpochMilli())
 *   [Long]    salt
 *   [VarInt]  argumentSignatureCount
 *   Per argument:
 *     [String]  argumentName
 *     [bytes]   signature (256 bytes)
 *   [Boolean] signedPreview
 *
 * We only read the command and skip the rest.
 */
public class ChatCommandC2SPacketV759 implements Packet {

    private String command;

    public ChatCommandC2SPacketV759() {}

    @Override
    public int getPacketId() { return 0x03; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, command);
    }

    @Override
    public void read(ByteBuf buf) {
        command = McDataTypes.readVarIntString(buf);
        // Skip remaining fields (timestamp, salt, argument signatures, signedPreview)
        // rather than parsing them — we only need the command string.
        buf.skipBytes(buf.readableBytes());
    }

    /**
     * Returns the command with a leading "/" prefix so it can be handled
     * as a regular chat message by the command dispatcher.
     */
    public String getMessage() {
        return "/" + command;
    }
}
