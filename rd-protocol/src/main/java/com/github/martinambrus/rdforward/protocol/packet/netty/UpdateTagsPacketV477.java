package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.14 Play state, S2C packet 0x5B: Update Tags.
 *
 * 1.14 added a 4th tag category (entity types) beyond the 3 in 1.13
 * (blocks, items, fluids). Sending only 3 causes the client to
 * misread subsequent packet data as the 4th count.
 *
 * Wire format:
 *   [VarInt] blockTagCount (0)
 *   [VarInt] itemTagCount (0)
 *   [VarInt] fluidTagCount (0)
 *   [VarInt] entityTypeTagCount (0)
 */
public class UpdateTagsPacketV477 implements Packet {

    @Override
    public int getPacketId() { return 0x5B; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 0); // 0 block tags
        McDataTypes.writeVarInt(buf, 0); // 0 item tags
        McDataTypes.writeVarInt(buf, 0); // 0 fluid tags
        McDataTypes.writeVarInt(buf, 0); // 0 entity type tags (new in 1.14)
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — no server-side decoding needed
    }
}
