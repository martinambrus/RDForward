package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.14 Play state, S2C packet 0x25: Join Game.
 *
 * 1.14 (v477) removed difficulty from JoinGame (now sent separately)
 * and added a VarInt viewDistance field after levelType.
 *
 * Wire format:
 *   [int]     entityId
 *   [ubyte]   gameMode
 *   [int]     dimension
 *   [ubyte]   maxPlayers
 *   [String]  levelType
 *   [VarInt]  viewDistance (NEW in 1.14)
 *   [boolean] reducedDebugInfo
 */
public class JoinGamePacketV477 implements Packet {

    private int entityId;
    private int gameMode;
    private int dimension;
    private int maxPlayers;
    private String levelType;
    private int viewDistance;

    public JoinGamePacketV477() {}

    public JoinGamePacketV477(int entityId, int gameMode, int dimension,
                               int maxPlayers, String levelType, int viewDistance) {
        this.entityId = entityId;
        this.gameMode = gameMode;
        this.dimension = dimension;
        this.maxPlayers = maxPlayers;
        this.levelType = levelType;
        this.viewDistance = viewDistance;
    }

    @Override
    public int getPacketId() { return 0x25; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(gameMode);
        buf.writeInt(dimension);
        buf.writeByte(maxPlayers);
        McDataTypes.writeVarIntString(buf, levelType);
        McDataTypes.writeVarInt(buf, viewDistance);
        buf.writeBoolean(false); // reducedDebugInfo
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        gameMode = buf.readUnsignedByte();
        dimension = buf.readInt();
        maxPlayers = buf.readUnsignedByte();
        levelType = McDataTypes.readVarIntString(buf);
        viewDistance = McDataTypes.readVarInt(buf);
        buf.readBoolean(); // reducedDebugInfo
    }

    public int getEntityId() { return entityId; }
}
