package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE PreLogin packet (ID 2, C2S).
 *
 * First packet sent by an LCE client. Contains netcode version for compatibility
 * check and username for identification. The remaining fields carry UGC privilege
 * and server settings data specific to console editions.
 *
 * Wire format:
 *   [short]      netcodeVersion (560 for TU19)
 *   [string16]   loginKey (username)
 *   [byte]       friendsOnlyBits
 *   [int]        ugcPlayersVersion
 *   [byte]       playerCount
 *   [long[]]     playerXuids (playerCount entries, 8 bytes each)
 *   [byte[14]]   uniqueSaveName
 *   [int]        serverSettings
 *   [byte]       hostIndex
 *   [int]        texturePackId
 */
public class LCEPreLoginC2SPacket implements Packet {

    private short netcodeVersion;
    private String loginKey;
    private byte friendsOnlyBits;
    private int ugcPlayersVersion;
    private int playerCount;
    private long[] playerXuids;
    private byte[] uniqueSaveName = new byte[14];
    private int serverSettings;
    private byte hostIndex;
    private int texturePackId;

    public LCEPreLoginC2SPacket() {}

    @Override
    public int getPacketId() { return 0x02; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(netcodeVersion);
        McDataTypes.writeString16(buf, loginKey);
        buf.writeByte(friendsOnlyBits);
        buf.writeInt(ugcPlayersVersion);
        buf.writeByte(playerCount);
        for (int i = 0; i < playerCount; i++) {
            buf.writeLong(playerXuids[i]);
        }
        buf.writeBytes(uniqueSaveName);
        buf.writeInt(serverSettings);
        buf.writeByte(hostIndex);
        buf.writeInt(texturePackId);
    }

    @Override
    public void read(ByteBuf buf) {
        netcodeVersion = buf.readShort();
        loginKey = McDataTypes.readString16(buf);
        friendsOnlyBits = buf.readByte();
        ugcPlayersVersion = buf.readInt();
        playerCount = buf.readUnsignedByte();
        if (playerCount > 8) playerCount = 8;
        playerXuids = new long[playerCount];
        for (int i = 0; i < playerCount; i++) {
            playerXuids[i] = buf.readLong();
        }
        buf.readBytes(uniqueSaveName);
        serverSettings = buf.readInt();
        hostIndex = buf.readByte();
        texturePackId = buf.readInt();
    }

    public short getNetcodeVersion() { return netcodeVersion; }
    public String getLoginKey() { return loginKey; }
    public byte getFriendsOnlyBits() { return friendsOnlyBits; }
    public int getUgcPlayersVersion() { return ugcPlayersVersion; }
    public int getPlayerCount() { return playerCount; }
    public long[] getPlayerXuids() { return playerXuids; }
    public byte[] getUniqueSaveName() { return uniqueSaveName; }
    public int getServerSettings() { return serverSettings; }
    public byte getHostIndex() { return hostIndex; }
    public int getTexturePackId() { return texturePackId; }
}
