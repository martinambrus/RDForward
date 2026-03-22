package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

/**
 * 1.19.3 Login state, C2S packet 0x00: Login Start.
 *
 * Starting from 1.19.3 (v761), the Login Start packet includes a player UUID.
 *
 * Wire format:
 *   [String] username
 *   [Long]   UUID most significant bits
 *   [Long]   UUID least significant bits
 */
public class LoginStartPacketV761 implements Packet {

    private String username;
    private UUID playerUuid;

    public LoginStartPacketV761() {}

    public LoginStartPacketV761(String username) {
        this.username = username;
        // Generate an offline-mode UUID (same algorithm as Spigot offline mode)
        this.playerUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
    }

    @Override
    public int getPacketId() { return 0x00; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, username);
        buf.writeLong(playerUuid.getMostSignificantBits());
        buf.writeLong(playerUuid.getLeastSignificantBits());
    }

    @Override
    public void read(ByteBuf buf) {
        username = McDataTypes.readVarIntString(buf);
        long msb = buf.readLong();
        long lsb = buf.readLong();
        playerUuid = new UUID(msb, lsb);
    }

    public String getUsername() { return username; }
    public UUID getPlayerUuid() { return playerUuid; }
}
