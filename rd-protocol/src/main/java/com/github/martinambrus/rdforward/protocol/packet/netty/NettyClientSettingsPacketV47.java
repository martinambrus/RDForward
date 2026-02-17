package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x15: Client Settings.
 *
 * 1.8 removed the difficulty byte and changed showCape boolean to a
 * skinParts byte (bit flags for cape, jacket, sleeves, pants, hat).
 *
 * Wire format:
 *   [String] locale
 *   [byte]   viewDistance
 *   [byte]   chatMode
 *   [boolean] chatColors
 *   [ubyte]  skinParts (bit flags)
 */
public class NettyClientSettingsPacketV47 implements Packet {

    public NettyClientSettingsPacketV47() {}

    @Override
    public int getPacketId() { return 0x15; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarIntString(buf); // locale
        buf.skipBytes(1); // viewDistance
        buf.skipBytes(1); // chatMode
        buf.skipBytes(1); // chatColors
        buf.skipBytes(1); // skinParts
    }
}
