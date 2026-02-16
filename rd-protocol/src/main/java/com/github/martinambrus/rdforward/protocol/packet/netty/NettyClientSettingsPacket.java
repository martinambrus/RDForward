package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x15: Client Settings.
 *
 * Wire format:
 *   [String] locale
 *   [byte]   viewDistance
 *   [byte]   chatMode
 *   [boolean] chatColors
 *   [byte]   difficulty
 *   [boolean] showCape
 */
public class NettyClientSettingsPacket implements Packet {

    public NettyClientSettingsPacket() {}

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
        buf.skipBytes(1); // difficulty
        buf.skipBytes(1); // showCape
    }
}
