package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.2.1+ protocol 0x09 (Client -> Server): Respawn.
 *
 * Release 1.2.1 (v28) removed the map seed field and changed dimension from
 * byte to int. The levelType field (added in v23) remains at the end.
 *
 * Wire format:
 *   [int]      dimension (0 = overworld, -1 = nether, 1 = end)
 *   [byte]     difficulty (0-3)
 *   [byte]     game mode (0 = survival, 1 = creative)
 *   [short]    world height (256)
 *   [string16] level type ("default")
 */
public class RespawnPacketV28 extends RespawnPacket {

    private int dimension;
    private byte difficulty;
    private byte gameMode;
    private short worldHeight;
    private String levelType;

    public RespawnPacketV28() {}

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeByte(difficulty);
        buf.writeByte(gameMode);
        buf.writeShort(worldHeight);
        McDataTypes.writeStringAdaptive(buf, levelType != null ? levelType : "default");
    }

    @Override
    public void read(ByteBuf buf) {
        dimension = buf.readInt();
        difficulty = buf.readByte();
        gameMode = buf.readByte();
        worldHeight = buf.readShort();
        levelType = McDataTypes.readStringAdaptive(buf);
    }

    public int getDimension() { return dimension; }
    public byte getDifficulty() { return difficulty; }
    public byte getGameMode() { return gameMode; }
    public short getWorldHeight() { return worldHeight; }
    public String getLevelType() { return levelType; }
}
