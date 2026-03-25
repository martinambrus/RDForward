package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 26.1 Configuration state, S2C packet 0x0E: Select Known Packs.
 *
 * 26.1 clients only recognize the single "26.1" core pack (not older
 * per-version packs). Sending only this pack ensures the client confirms
 * it and resolves all built-in registry entries from its cumulative data.
 */
public class SelectKnownPacksS2CPacketV775 implements Packet {

    @Override
    public int getPacketId() { return 0x0E; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 1);
        McDataTypes.writeVarIntString(buf, "minecraft");
        McDataTypes.writeVarIntString(buf, "core");
        McDataTypes.writeVarIntString(buf, "26.1");
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only
    }
}
