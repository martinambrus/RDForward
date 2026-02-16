package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0xC9 (S2C only): Player List Item.
 *
 * Added in Beta 1.8. Adds or removes a player from the Tab list overlay.
 * Same wire format in Beta 1.8 and Release 1.0.0.
 *
 * Wire format:
 *   [string16] username (max 16 chars)
 *   [byte]     online   (1 = add, 0 = remove)
 *   [short]    ping     (milliseconds, 0 = unknown)
 */
public class PlayerListItemPacket implements Packet {

    private String username;
    private boolean online;
    private short ping;

    public PlayerListItemPacket() {}

    public PlayerListItemPacket(String username, boolean online, int ping) {
        this.username = username;
        this.online = online;
        this.ping = (short) ping;
    }

    @Override
    public int getPacketId() {
        return 0xC9;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeStringAdaptive(buf, username);
        buf.writeByte(online ? 1 : 0);
        buf.writeShort(ping);
    }

    @Override
    public void read(ByteBuf buf) {
        username = McDataTypes.readStringAdaptive(buf);
        online = buf.readByte() != 0;
        ping = buf.readShort();
    }

    public String getUsername() { return username; }
    public boolean isOnline() { return online; }
    public short getPing() { return ping; }
}
