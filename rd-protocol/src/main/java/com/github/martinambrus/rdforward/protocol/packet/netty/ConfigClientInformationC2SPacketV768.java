package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.2 Configuration state, C2S: Client Information.
 *
 * Same as v764 format but adds VarInt particleStatus at the end.
 * Added in 1.21.2 (v768).
 *
 * Wire format:
 *   [String]  locale
 *   [Byte]    view distance
 *   [VarInt]  chat mode (0=enabled, 1=commands only, 2=hidden)
 *   [Boolean] chat colors
 *   [Byte]    displayed skin parts (bitmask)
 *   [VarInt]  main hand (0=left, 1=right)
 *   [Boolean] text filtering
 *   [Boolean] allow server listings
 *   [VarInt]  particle status (0=all, 1=decreased, 2=minimal) — NEW in v768
 */
public class ConfigClientInformationC2SPacketV768 implements Packet {

    @Override
    public int getPacketId() { return 0x00; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, "en_us"); // locale
        buf.writeByte(2);                             // view distance (minimal)
        McDataTypes.writeVarInt(buf, 0);              // chat mode: enabled
        buf.writeBoolean(true);                       // chat colors
        buf.writeByte(0x7F);                          // all skin parts
        McDataTypes.writeVarInt(buf, 1);              // main hand: right
        buf.writeBoolean(false);                      // text filtering
        buf.writeBoolean(true);                       // allow server listings
        McDataTypes.writeVarInt(buf, 0);              // particle status: all
    }

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarIntString(buf);
        buf.readByte();
        McDataTypes.readVarInt(buf);
        buf.readBoolean();
        buf.readByte();
        McDataTypes.readVarInt(buf);
        buf.readBoolean();
        buf.readBoolean();
        McDataTypes.readVarInt(buf); // particle status
    }
}
