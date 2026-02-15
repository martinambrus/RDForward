package com.github.martinambrus.rdforward.protocol.packet.alpha;

import io.netty.buffer.ByteBuf;

/**
 * Beta 1.8+ protocol 0x09 (Client -> Server): Respawn.
 *
 * Beta 1.8 (v17) expanded the Respawn packet from just a dimension byte
 * to include difficulty, game mode, world height, and map seed.
 *
 * Wire format:
 *   [byte]  dimension (0 = overworld, -1 = nether)
 *   [byte]  difficulty (0-3)
 *   [byte]  game mode (0 = survival, 1 = creative)
 *   [short] world height (128)
 *   [long]  map seed
 */
public class RespawnPacketV17 extends RespawnPacket {

    private byte dimension;
    private byte difficulty;
    private byte gameMode;
    private short worldHeight;
    private long mapSeed;

    public RespawnPacketV17() {}

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(dimension);
        buf.writeByte(difficulty);
        buf.writeByte(gameMode);
        buf.writeShort(worldHeight);
        buf.writeLong(mapSeed);
    }

    @Override
    public void read(ByteBuf buf) {
        dimension = buf.readByte();
        difficulty = buf.readByte();
        gameMode = buf.readByte();
        worldHeight = buf.readShort();
        mapSeed = buf.readLong();
    }

    public byte getDimension() { return dimension; }
    public byte getDifficulty() { return difficulty; }
    public byte getGameMode() { return gameMode; }
    public short getWorldHeight() { return worldHeight; }
    public long getMapSeed() { return mapSeed; }
}
