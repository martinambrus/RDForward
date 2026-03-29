package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * 1.13 Play state, S2C packet 0x11: Declare Commands.
 *
 * Sends the server's command tree to the client for tab-completion.
 *
 * Supports two modes:
 * - Empty tree (no-arg constructor): root node with no children.
 * - Command list (factory method): root node with literal children
 *   for each command name, enabling tab completion.
 *
 * Node format:
 *   [byte]   flags (bits: 0-1=type, 2=executable, 3=redirect, 4=hasSuggestionsType)
 *   [VarInt] childCount
 *   [VarInt] childIndices...
 *   [VarInt] redirectNode (if flag bit 3)
 *   [String] name (if type=literal or argument)
 *   [String] parser (if type=argument)
 *   [...]    parser properties (if type=argument)
 *   [String] suggestionsType (if flag bit 4)
 */
public class DeclareCommandsPacketV393 implements Packet {

    private List<String> commandNames;

    public DeclareCommandsPacketV393() {}

    /**
     * Create a DeclareCommands packet with the given command names as
     * literal children of the root node. Each command is marked executable.
     */
    public static DeclareCommandsPacketV393 withCommands(List<String> commandNames) {
        DeclareCommandsPacketV393 pkt = new DeclareCommandsPacketV393();
        pkt.commandNames = commandNames;
        return pkt;
    }

    @Override
    public int getPacketId() { return 0x11; }

    @Override
    public void write(ByteBuf buf) {
        if (commandNames == null || commandNames.isEmpty()) {
            // Minimal tree: root only
            McDataTypes.writeVarInt(buf, 1);    // 1 node (root)
            buf.writeByte(0x00);                // flags: root node type
            McDataTypes.writeVarInt(buf, 0);    // 0 children
            McDataTypes.writeVarInt(buf, 0);    // root index = 0
            return;
        }

        // Node count: 1 root + N command literals
        int nodeCount = 1 + commandNames.size();
        McDataTypes.writeVarInt(buf, nodeCount);

        // Node 0: root
        // flags: type=0 (root)
        buf.writeByte(0x00);
        // children: indices 1..N
        McDataTypes.writeVarInt(buf, commandNames.size());
        for (int i = 0; i < commandNames.size(); i++) {
            McDataTypes.writeVarInt(buf, i + 1);
        }

        // Nodes 1..N: literal command names
        for (String name : commandNames) {
            // flags: type=1 (literal) | bit 2 (executable) = 0x05
            buf.writeByte(0x05);
            // 0 children
            McDataTypes.writeVarInt(buf, 0);
            // name
            McDataTypes.writeVarIntString(buf, name);
        }

        // root index = 0
        McDataTypes.writeVarInt(buf, 0);
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — no server-side decoding needed
    }
}
