package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.2.1+ protocol 0x01 (Server -> Client): Login Response.
 *
 * Release 1.2.1 (v28) removed the map seed field and changed dimension from
 * byte to int. The levelType field (added in v23) remains.
 *
 * Wire format:
 *   [int]      entity ID
 *   [string16] unused ("")
 *   [string16] level type ("default")
 *   [int]      game mode (0 = survival, 1 = creative)
 *   [int]      dimension (0 = overworld, -1 = nether, 1 = end)
 *   [byte]     difficulty (0 = peaceful, 1 = easy, 2 = normal, 3 = hard)
 *   [byte]     world height (0, ignored by client)
 *   [byte]     max players
 */
public class LoginS2CPacketV28 implements Packet {

    private int entityId;
    private String levelType;
    private int gameMode;
    private int dimension;
    private byte difficulty;
    private byte worldHeight;
    private byte maxPlayers;

    public LoginS2CPacketV28() {}

    public LoginS2CPacketV28(int entityId, String levelType, int gameMode,
                              int dimension, byte difficulty, byte worldHeight, byte maxPlayers) {
        this.entityId = entityId;
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
        McDataTypes.writeStringAdaptive(buf, levelType);
        buf.writeInt(gameMode);
        buf.writeInt(dimension);
        buf.writeByte(difficulty);
        buf.writeByte(worldHeight);
        buf.writeByte(maxPlayers);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        McDataTypes.readStringAdaptive(buf); // unused
        levelType = McDataTypes.readStringAdaptive(buf);
        gameMode = buf.readInt();
        dimension = buf.readInt();
        difficulty = buf.readByte();
        worldHeight = buf.readByte();
        maxPlayers = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public String getLevelType() { return levelType; }
    public int getGameMode() { return gameMode; }
    public int getDimension() { return dimension; }
    public byte getDifficulty() { return difficulty; }
    public byte getWorldHeight() { return worldHeight; }
    public byte getMaxPlayers() { return maxPlayers; }
}
