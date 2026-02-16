package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, S2C packet 0x38: Player List Item.
 *
 * Wire format:
 *   [String]  playerName
 *   [boolean] online
 *   [short]   ping (ms)
 */
public class NettyPlayerListItemPacket implements Packet {

    private String playerName;
    private boolean online;
    private short ping;

    public NettyPlayerListItemPacket() {}

    public NettyPlayerListItemPacket(String playerName, boolean online, int ping) {
        this.playerName = playerName;
        this.online = online;
        this.ping = (short) ping;
    }

    @Override
    public int getPacketId() { return 0x38; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, playerName);
        buf.writeBoolean(online);
        buf.writeShort(ping);
    }

    @Override
    public void read(ByteBuf buf) {
        playerName = McDataTypes.readVarIntString(buf);
        online = buf.readBoolean();
        ping = buf.readShort();
    }

    public String getPlayerName() { return playerName; }
    public boolean isOnline() { return online; }
}
