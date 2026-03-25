package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 1.13 Play state, S2C packet 0x55: Update Tags.
 *
 * Sends block, item, and fluid tag registries to the client.
 * We send empty tag lists since our server doesn't use tags.
 *
 * Wire format:
 *   [VarInt] blockTagCount (0)
 *   [VarInt] itemTagCount (0)
 *   [VarInt] fluidTagCount (0)
 */
public class UpdateTagsPacketV393 implements Packet {

    public static final UpdateTagsPacketV393 INSTANCE = new UpdateTagsPacketV393();

    private static final byte[] SERIALIZED;
    static {
        ByteBuf tmp = Unpooled.buffer();
        serializePayload(tmp);
        SERIALIZED = new byte[tmp.readableBytes()];
        tmp.readBytes(SERIALIZED);
        tmp.release();
    }

    @Override
    public int getPacketId() { return 0x55; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeBytes(SERIALIZED);
    }

    private static void serializePayload(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 0); // 0 block tags
        McDataTypes.writeVarInt(buf, 0); // 0 item tags
        McDataTypes.writeVarInt(buf, 0); // 0 fluid tags
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — no server-side decoding needed
    }
}
