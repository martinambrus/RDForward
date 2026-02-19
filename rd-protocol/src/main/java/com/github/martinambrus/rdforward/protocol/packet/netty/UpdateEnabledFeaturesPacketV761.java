package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19.3 Play state, S2C packet 0x67: Update Enabled Features.
 *
 * New mandatory packet sent after JoinGame to declare which feature flags
 * are enabled. Without this, the 1.19.3 client may not function correctly.
 *
 * Wire format:
 *   [VarInt] count
 *   Per feature:
 *     [String] featureId (Identifier)
 */
public class UpdateEnabledFeaturesPacketV761 implements Packet {

    @Override
    public int getPacketId() { return 0x67; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 1);
        McDataTypes.writeVarIntString(buf, "minecraft:vanilla");
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only
    }
}
