package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Configuration state, C2S packet 0x00: Client Information.
 *
 * Sends minimal client settings to the server. Spigot requires this
 * packet before it will send ConfigFinish.
 *
 * Wire format:
 *   [String] locale (e.g. "en_us")
 *   [Byte]   view distance
 *   [VarInt] chat mode (0=enabled, 1=commands only, 2=hidden)
 *   [Boolean] chat colors
 *   [Byte]   displayed skin parts (bitmask)
 *   [VarInt] main hand (0=left, 1=right)
 *   [Boolean] text filtering
 *   [Boolean] allow server listings
 */
public class ConfigClientInformationC2SPacket implements Packet {

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
    }

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarIntString(buf); // locale
        buf.readByte();                     // view distance
        McDataTypes.readVarInt(buf);        // chat mode
        buf.readBoolean();                  // chat colors
        buf.readByte();                     // skin parts
        McDataTypes.readVarInt(buf);        // main hand
        buf.readBoolean();                  // text filtering
        buf.readBoolean();                  // allow server listings
    }
}
