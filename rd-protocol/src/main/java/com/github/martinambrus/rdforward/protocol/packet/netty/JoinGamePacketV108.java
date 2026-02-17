package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9.1 Play state, S2C packet 0x23: Join Game.
 *
 * 1.9.1 (v108) changed dimension from byte to int.
 *
 * Wire format:
 *   [int]     entityId
 *   [ubyte]   gameMode
 *   [int]     dimension (was byte in v47/v107)
 *   [ubyte]   difficulty
 *   [ubyte]   maxPlayers
 *   [String]  levelType
 *   [boolean] reducedDebugInfo
 */
public class JoinGamePacketV108 implements Packet {

    private int entityId;
    private int gameMode;
    private int dimension;
    private int difficulty;
    private int maxPlayers;
    private String levelType;

    public JoinGamePacketV108() {}

    public JoinGamePacketV108(int entityId, int gameMode, int dimension,
                               int difficulty, int maxPlayers, String levelType) {
        this.entityId = entityId;
        this.gameMode = gameMode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.maxPlayers = maxPlayers;
        this.levelType = levelType;
    }

    @Override
    public int getPacketId() { return 0x23; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(gameMode);
        buf.writeInt(dimension);
        buf.writeByte(difficulty);
        buf.writeByte(maxPlayers);
        McDataTypes.writeVarIntString(buf, levelType);
        buf.writeBoolean(false); // reducedDebugInfo
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        gameMode = buf.readUnsignedByte();
        dimension = buf.readInt();
        difficulty = buf.readUnsignedByte();
        maxPlayers = buf.readUnsignedByte();
        levelType = McDataTypes.readVarIntString(buf);
        buf.readBoolean(); // reducedDebugInfo
    }

    public int getEntityId() { return entityId; }
}
