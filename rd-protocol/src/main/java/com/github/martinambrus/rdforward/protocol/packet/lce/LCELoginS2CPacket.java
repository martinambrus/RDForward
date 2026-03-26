package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE Login response packet (ID 1, S2C).
 *
 * Server sends this after validating the client's login.
 * Contains world parameters (seed, gameType, dimension) and player assignment.
 *
 * Same wire format as LCELoginC2SPacket but server-populated.
 */
public class LCELoginS2CPacket implements Packet {

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

    public LCELoginS2CPacket() {}

    private LCELoginS2CPacket(Builder b) {
        this.clientVersion = b.clientVersion;
        this.userName = b.userName;
        this.levelType = b.levelType;
        this.seed = b.seed;
        this.gameType = b.gameType;
        this.dimension = b.dimension;
        this.mapHeight = b.mapHeight;
        this.maxPlayers = b.maxPlayers;
        this.offlineXuid = b.offlineXuid;
        this.onlineXuid = b.onlineXuid;
        this.friendsOnlyUGC = b.friendsOnlyUGC;
        this.ugcPlayersVersion = b.ugcPlayersVersion;
        this.difficulty = b.difficulty;
        this.multiplayerInstanceId = b.multiplayerInstanceId;
        this.playerIndex = b.playerIndex;
        this.playerSkinId = b.playerSkinId;
        this.playerCapeId = b.playerCapeId;
        this.isGuest = b.isGuest;
        this.newSeaLevel = b.newSeaLevel;
        this.gamePrivileges = b.gamePrivileges;
        this.xzSize = b.xzSize;
        this.hellScale = b.hellScale;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int clientVersion;
        private String userName = "";
        private String levelType = "default";
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

        public Builder clientVersion(int v) { this.clientVersion = v; return this; }
        public Builder userName(String v) { this.userName = v; return this; }
        public Builder levelType(String v) { this.levelType = v; return this; }
        public Builder seed(long v) { this.seed = v; return this; }
        public Builder gameType(int v) { this.gameType = v; return this; }
        public Builder dimension(byte v) { this.dimension = v; return this; }
        public Builder mapHeight(byte v) { this.mapHeight = v; return this; }
        public Builder maxPlayers(byte v) { this.maxPlayers = v; return this; }
        public Builder offlineXuid(long v) { this.offlineXuid = v; return this; }
        public Builder onlineXuid(long v) { this.onlineXuid = v; return this; }
        public Builder friendsOnlyUGC(boolean v) { this.friendsOnlyUGC = v; return this; }
        public Builder ugcPlayersVersion(int v) { this.ugcPlayersVersion = v; return this; }
        public Builder difficulty(byte v) { this.difficulty = v; return this; }
        public Builder multiplayerInstanceId(int v) { this.multiplayerInstanceId = v; return this; }
        public Builder playerIndex(byte v) { this.playerIndex = v; return this; }
        public Builder playerSkinId(int v) { this.playerSkinId = v; return this; }
        public Builder playerCapeId(int v) { this.playerCapeId = v; return this; }
        public Builder isGuest(boolean v) { this.isGuest = v; return this; }
        public Builder newSeaLevel(boolean v) { this.newSeaLevel = v; return this; }
        public Builder gamePrivileges(int v) { this.gamePrivileges = v; return this; }
        public Builder xzSize(short v) { this.xzSize = v; return this; }
        public Builder hellScale(byte v) { this.hellScale = v; return this; }

        public LCELoginS2CPacket build() { return new LCELoginS2CPacket(this); }
    }

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
        // _LARGE_WORLDS fields (always present on Windows64 build)
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
        // _LARGE_WORLDS fields
        xzSize = buf.readShort();
        hellScale = buf.readByte();
    }
}
