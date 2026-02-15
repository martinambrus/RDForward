package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta 1.8+ protocol 0x01 (Server -> Client): Login Response.
 *
 * Beta 1.8 (v17) added gameMode, difficulty, worldHeight, and maxPlayers fields.
 * The gameMode field is inserted between mapSeed and dimension.
 *
 * Wire format:
 *   [int]      entity ID
 *   [string16] unused ("")
 *   [long]     map seed
 *   [int]      game mode (0 = survival, 1 = creative)
 *   [byte]     dimension (0 = overworld, -1 = nether)
 *   [byte]     difficulty (0 = peaceful, 1 = easy, 2 = normal, 3 = hard)
 *   [byte]     world height (128)
 *   [byte]     max players
 *
 * Note: Pre-Beta-1.8 Login S2C has TWO String16 fields; Beta 1.8 removed the second.
 */
public class LoginS2CPacketV17 implements Packet {

    private int entityId;
    private long mapSeed;
    private int gameMode;
    private byte dimension;
    private byte difficulty;
    private byte worldHeight;
    private byte maxPlayers;

    public LoginS2CPacketV17() {}

    public LoginS2CPacketV17(int entityId, long mapSeed, int gameMode,
                              byte dimension, byte difficulty, byte worldHeight, byte maxPlayers) {
        this.entityId = entityId;
        this.mapSeed = mapSeed;
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
        gameMode = buf.readInt();
        dimension = buf.readByte();
        difficulty = buf.readByte();
        worldHeight = buf.readByte();
        maxPlayers = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public long getMapSeed() { return mapSeed; }
    public int getGameMode() { return gameMode; }
    public byte getDimension() { return dimension; }
    public byte getDifficulty() { return difficulty; }
    public byte getWorldHeight() { return worldHeight; }
    public byte getMaxPlayers() { return maxPlayers; }
}
