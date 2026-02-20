package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.5 Configuration state, C2S packet 0x07: Select Known Packs.
 *
 * Client confirms which data packs it has built-in. Server then only
 * needs to send registries it customizes; confirmed packs use built-in data.
 *
 * Wire format:
 *   VarInt  count
 *   Per pack:
 *     String  namespace
 *     String  id
 *     String  version
 */
public class SelectKnownPacksC2SPacket implements Packet {

    @Override
    public int getPacketId() { return 0x07; }

    @Override
    public void write(ByteBuf buf) {
        // C2S only — no server-side encoding needed
    }

    @Override
    public void read(ByteBuf buf) {
        int count = McDataTypes.readVarInt(buf);
        for (int i = 0; i < count; i++) {
            McDataTypes.readVarIntString(buf); // namespace
            McDataTypes.readVarIntString(buf); // id
            McDataTypes.readVarIntString(buf); // version
        }
    }
}
