package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.5 Configuration state, S2C packet 0x0E: Select Known Packs.
 *
 * Server declares which data packs it knows. Client confirms which it
 * also has built-in, allowing the server to skip sending unmodified
 * registry data for confirmed packs.
 *
 * Wire format:
 *   VarInt  count
 *   Per pack:
 *     String  namespace
 *     String  id
 *     String  version
 */
public class SelectKnownPacksS2CPacket implements Packet {

    @Override
    public int getPacketId() { return 0x0E; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 1); // 1 pack

        // Pack 1: minecraft core 1.20.5
        McDataTypes.writeVarIntString(buf, "minecraft");
        McDataTypes.writeVarIntString(buf, "core");
        McDataTypes.writeVarIntString(buf, "1.20.5");
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — no server-side decoding needed
    }
}
