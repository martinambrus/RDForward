package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Configuration state, C2S packet 0x01: Custom Payload (Plugin Message).
 *
 * Used to send the client brand ("vanilla") during CONFIGURATION.
 * Spigot expects this to complete the configuration handshake.
 *
 * Wire format:
 *   [Identifier] channel (e.g. "minecraft:brand")
 *   [byte[]] data (remaining bytes in frame)
 */
public class ConfigCustomPayloadC2SPacket implements Packet {

    private String channel;
    private String payload;

    /** Default: sends minecraft:brand = "vanilla" */
    public ConfigCustomPayloadC2SPacket() {
        this("minecraft:brand", "vanilla");
    }

    public ConfigCustomPayloadC2SPacket(String channel, String payload) {
        this.channel = channel;
        this.payload = payload;
    }

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, channel);
        McDataTypes.writeVarIntString(buf, payload);
    }

    @Override
    public void read(ByteBuf buf) {
        channel = McDataTypes.readVarIntString(buf);
        if (buf.readableBytes() > 0) {
            payload = McDataTypes.readVarIntString(buf);
            buf.skipBytes(buf.readableBytes()); // skip any remaining data
        } else {
            payload = "";
        }
    }
}
