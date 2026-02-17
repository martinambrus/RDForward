package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.13 Play state, S2C packet 0x11: Declare Commands.
 *
 * Sends the server's command tree to the client for tab-completion.
 * We send a minimal tree with just a root node and no children.
 *
 * Wire format:
 *   [VarInt] nodeCount (1 = root only)
 *   Node 0 (root):
 *     [byte]   flags (0x00 = root type)
 *     [VarInt] childCount (0)
 *   [VarInt] rootIndex (0)
 */
public class DeclareCommandsPacketV393 implements Packet {

    @Override
    public int getPacketId() { return 0x11; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 1);    // 1 node (root)
        buf.writeByte(0x00);                // flags: root node type
        McDataTypes.writeVarInt(buf, 0);    // 0 children
        McDataTypes.writeVarInt(buf, 0);    // root index = 0
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — no server-side decoding needed
    }
}
