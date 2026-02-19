package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19.3 Play state, S2C packet 0x35: Player Info Remove.
 *
 * Replaces the REMOVE_PLAYER action from the old PlayerListItem packet.
 * Simple UUID-only removal format.
 *
 * Wire format:
 *   [VarInt] count
 *   Per player:
 *     [UUID] (2 longs)
 */
public class NettyPlayerInfoRemovePacketV761 implements Packet {

    private long uuidMsb;
    private long uuidLsb;

    public NettyPlayerInfoRemovePacketV761() {}

    public static NettyPlayerInfoRemovePacketV761 removePlayer(String uuid) {
        NettyPlayerInfoRemovePacketV761 pkt = new NettyPlayerInfoRemovePacketV761();
        String noDashes = uuid.replace("-", "");
        pkt.uuidMsb = Long.parseUnsignedLong(noDashes.substring(0, 16), 16);
        pkt.uuidLsb = Long.parseUnsignedLong(noDashes.substring(16, 32), 16);
        return pkt;
    }

    @Override
    public int getPacketId() { return 0x35; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 1); // 1 entry
        buf.writeLong(uuidMsb);
        buf.writeLong(uuidLsb);
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — skip
        buf.skipBytes(buf.readableBytes());
    }
}
