package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0x01 (Server -> Client): Login Response.
 *
 * v39 removed the empty username String16 and changed gameMode/dimension
 * from int to byte.
 *
 * Wire format:
 *   [int]      entity ID
 *   [string16] level type ("default")
 *   [byte]     game mode (0 = survival, 1 = creative)
 *   [byte]     dimension (0 = overworld, -1 = nether, 1 = end)
 *   [byte]     difficulty (0 = peaceful, 1 = easy, 2 = normal, 3 = hard)
 *   [byte]     world height (0, ignored by client)
 *   [byte]     max players
 */
public class LoginS2CPacketV39 implements Packet {

    private int entityId;
    private String levelType;
    private byte gameMode;
    private byte dimension;
    private byte difficulty;
    private byte worldHeight;
    private byte maxPlayers;

    public LoginS2CPacketV39() {}

    public LoginS2CPacketV39(int entityId, String levelType, int gameMode,
                              int dimension, byte difficulty, byte worldHeight, byte maxPlayers) {
        this.entityId = entityId;
        this.levelType = levelType;
        this.gameMode = (byte) gameMode;
        this.dimension = (byte) dimension;
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
        McDataTypes.writeString16(buf, levelType);
        buf.writeByte(gameMode);
        buf.writeByte(dimension);
        buf.writeByte(difficulty);
        buf.writeByte(worldHeight);
        buf.writeByte(maxPlayers);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        levelType = McDataTypes.readString16(buf);
        gameMode = buf.readByte();
        dimension = buf.readByte();
        difficulty = buf.readByte();
        worldHeight = buf.readByte();
        maxPlayers = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public String getLevelType() { return levelType; }
    public byte getGameMode() { return gameMode; }
    public byte getDimension() { return dimension; }
    public byte getDifficulty() { return difficulty; }
    public byte getWorldHeight() { return worldHeight; }
    public byte getMaxPlayers() { return maxPlayers; }
}
