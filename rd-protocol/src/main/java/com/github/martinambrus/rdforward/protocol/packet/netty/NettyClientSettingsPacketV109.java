package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, C2S packet 0x04: Client Settings.
 *
 * 1.9 changed chatMode from byte to VarInt and added VarInt mainHand.
 *
 * Wire format:
 *   [String]  locale
 *   [byte]    viewDistance
 *   [VarInt]  chatMode
 *   [boolean] chatColors
 *   [ubyte]   skinParts
 *   [VarInt]  mainHand
 */
public class NettyClientSettingsPacketV109 implements Packet {

    public NettyClientSettingsPacketV109() {}

    @Override
    public int getPacketId() { return 0x04; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarIntString(buf); // locale
        buf.readByte(); // viewDistance
        McDataTypes.readVarInt(buf); // chatMode
        buf.readBoolean(); // chatColors
        buf.readUnsignedByte(); // skinParts
        McDataTypes.readVarInt(buf); // mainHand
    }
}
