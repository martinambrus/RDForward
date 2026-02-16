package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.1+ protocol 0x01 (Server -> Client): Login Response.
 *
 * Release 1.1 (v23) added a String16 levelType field between seed and gameMode.
 *
 * Wire format:
 *   [int]      entity ID
 *   [string16] unused ("")
 *   [long]     map seed
 *   [string16] level type ("default")
 *   [int]      game mode (0 = survival, 1 = creative)
 *   [byte]     dimension (0 = overworld, -1 = nether)
 *   [byte]     difficulty (0 = peaceful, 1 = easy, 2 = normal, 3 = hard)
 *   [byte]     world height (128)
 *   [byte]     max players
 */
public class LoginS2CPacketV23 implements Packet {

    private int entityId;
    private long mapSeed;
    private String levelType;
    private int gameMode;
    private byte dimension;
    private byte difficulty;
    private byte worldHeight;
    private byte maxPlayers;

    public LoginS2CPacketV23() {}

    public LoginS2CPacketV23(int entityId, long mapSeed, String levelType, int gameMode,
                              byte dimension, byte difficulty, byte worldHeight, byte maxPlayers) {
        this.entityId = entityId;
        this.mapSeed = mapSeed;
        this.levelType = levelType;
        this.gameMode = gameMode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.worldHeight = worldHeight;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public int getPacketId() {
        return 0x01;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        McDataTypes.writeStringAdaptive(buf, "");
        buf.writeLong(mapSeed);
        McDataTypes.writeStringAdaptive(buf, levelType);
        buf.writeInt(gameMode);
        buf.writeByte(dimension);
        buf.writeByte(difficulty);
        buf.writeByte(worldHeight);
        buf.writeByte(maxPlayers);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        McDataTypes.readStringAdaptive(buf); // unused
        mapSeed = buf.readLong();
        levelType = McDataTypes.readStringAdaptive(buf);
        gameMode = buf.readInt();
        dimension = buf.readByte();
        difficulty = buf.readByte();
        worldHeight = buf.readByte();
        maxPlayers = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public long getMapSeed() { return mapSeed; }
    public String getLevelType() { return levelType; }
    public int getGameMode() { return gameMode; }
    public byte getDimension() { return dimension; }
    public byte getDifficulty() { return difficulty; }
    public byte getWorldHeight() { return worldHeight; }
    public byte getMaxPlayers() { return maxPlayers; }
}
