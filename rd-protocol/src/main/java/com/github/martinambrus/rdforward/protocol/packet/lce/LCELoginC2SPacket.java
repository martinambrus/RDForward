package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE Login packet (ID 1, C2S).
 *
 * Sent after PreLogin handshake. Contains game protocol version,
 * player identity (username + XUIDs), and UGC/skin data.
 *
 * Wire format:
 *   [int]        clientVersion (78 = NETWORK_PROTOCOL_VERSION)
 *   [string16]   userName
 *   [string16]   levelType
 *   [long]       seed
 *   [int]        gameType
 *   [byte]       dimension
 *   [byte]       mapHeight
 *   [byte]       maxPlayers
 *   [long]       offlineXuid
 *   [long]       onlineXuid
 *   [boolean]    friendsOnlyUGC
 *   [int]        ugcPlayersVersion
 *   [byte]       difficulty
 *   [int]        multiplayerInstanceId
 *   [byte]       playerIndex
 *   [int]        playerSkinId
 *   [int]        playerCapeId
 *   [boolean]    isGuest
 *   [boolean]    newSeaLevel
 *   [int]        gamePrivileges
 */
public class LCELoginC2SPacket implements Packet {

    private int clientVersion;
    private String userName;
    private String levelType;
    private long seed;
    private int gameType;
    private byte dimension;
    private byte mapHeight;
    private byte maxPlayers;
    private long offlineXuid;
    private long onlineXuid;
    private boolean friendsOnlyUGC;
    private int ugcPlayersVersion;
    private byte difficulty;
    private int multiplayerInstanceId;
    private byte playerIndex;
    private int playerSkinId;
    private int playerCapeId;
    private boolean isGuest;
    private boolean newSeaLevel;
    private int gamePrivileges;
    private short xzSize;
    private byte hellScale;

    public LCELoginC2SPacket() {}

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(clientVersion);
        McDataTypes.writeString16(buf, userName);
        McDataTypes.writeString16(buf, levelType != null ? levelType : "");
        buf.writeLong(seed);
        buf.writeInt(gameType);
        buf.writeByte(dimension);
        buf.writeByte(mapHeight);
        buf.writeByte(maxPlayers);
        buf.writeLong(offlineXuid);
        buf.writeLong(onlineXuid);
        buf.writeBoolean(friendsOnlyUGC);
        buf.writeInt(ugcPlayersVersion);
        buf.writeByte(difficulty);
        buf.writeInt(multiplayerInstanceId);
        buf.writeByte(playerIndex);
        buf.writeInt(playerSkinId);
        buf.writeInt(playerCapeId);
        buf.writeBoolean(isGuest);
        buf.writeBoolean(newSeaLevel);
        buf.writeInt(gamePrivileges);
        buf.writeShort(xzSize);
        buf.writeByte(hellScale);
    }

    @Override
    public void read(ByteBuf buf) {
        clientVersion = buf.readInt();
        userName = McDataTypes.readString16(buf);
        levelType = McDataTypes.readString16(buf);
        seed = buf.readLong();
        gameType = buf.readInt();
        dimension = buf.readByte();
        mapHeight = buf.readByte();
        maxPlayers = buf.readByte();
        offlineXuid = buf.readLong();
        onlineXuid = buf.readLong();
        friendsOnlyUGC = buf.readBoolean();
        ugcPlayersVersion = buf.readInt();
        difficulty = buf.readByte();
        multiplayerInstanceId = buf.readInt();
        playerIndex = buf.readByte();
        playerSkinId = buf.readInt();
        playerCapeId = buf.readInt();
        isGuest = buf.readBoolean();
        newSeaLevel = buf.readBoolean();
        gamePrivileges = buf.readInt();
        xzSize = buf.readShort();
        hellScale = buf.readByte();
    }

    public int getClientVersion() { return clientVersion; }
    public String getUserName() { return userName; }
    public String getLevelType() { return levelType; }
    public long getSeed() { return seed; }
    public int getGameType() { return gameType; }
    public byte getDimension() { return dimension; }
    public byte getMapHeight() { return mapHeight; }
    public byte getMaxPlayers() { return maxPlayers; }
    public long getOfflineXuid() { return offlineXuid; }
    public long getOnlineXuid() { return onlineXuid; }
    public boolean isFriendsOnlyUGC() { return friendsOnlyUGC; }
    public int getUgcPlayersVersion() { return ugcPlayersVersion; }
    public byte getDifficulty() { return difficulty; }
    public int getMultiplayerInstanceId() { return multiplayerInstanceId; }
    public byte getPlayerIndex() { return playerIndex; }
    public int getPlayerSkinId() { return playerSkinId; }
    public int getPlayerCapeId() { return playerCapeId; }
    public boolean isGuest() { return isGuest; }
    public boolean isNewSeaLevel() { return newSeaLevel; }
    public int getGamePrivileges() { return gamePrivileges; }
}
