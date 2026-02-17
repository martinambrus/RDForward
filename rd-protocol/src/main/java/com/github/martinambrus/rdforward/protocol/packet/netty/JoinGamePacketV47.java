package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x01: Join Game.
 *
 * 1.8 added boolean reducedDebugInfo at the end.
 *
 * Wire format:
 *   [int]     entityId
 *   [ubyte]   gameMode
 *   [byte]    dimension
 *   [ubyte]   difficulty
 *   [ubyte]   maxPlayers
 *   [String]  levelType
 *   [boolean] reducedDebugInfo
 */
public class JoinGamePacketV47 implements Packet {

    private int entityId;
    private int gameMode;
    private int dimension;
    private int difficulty;
    private int maxPlayers;
    private String levelType;

    public JoinGamePacketV47() {}

    public JoinGamePacketV47(int entityId, int gameMode, int dimension,
                              int difficulty, int maxPlayers, String levelType) {
        this.entityId = entityId;
        this.gameMode = gameMode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.maxPlayers = maxPlayers;
        this.levelType = levelType;
    }

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(gameMode);
        buf.writeByte(dimension);
        buf.writeByte(difficulty);
        buf.writeByte(maxPlayers);
        McDataTypes.writeVarIntString(buf, levelType);
        buf.writeBoolean(false); // reducedDebugInfo
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        gameMode = buf.readUnsignedByte();
        dimension = buf.readByte();
        difficulty = buf.readUnsignedByte();
        maxPlayers = buf.readUnsignedByte();
        levelType = McDataTypes.readVarIntString(buf);
        buf.readBoolean(); // reducedDebugInfo
    }

    public int getEntityId() { return entityId; }
}
