package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.15 Play state, S2C packet 0x26: Join Game.
 *
 * 1.15 (v573) added two fields after reducedDebugInfo:
 *   - long hashedSeed
 *   - boolean enableRespawnScreen
 *
 * Wire format:
 *   [int]     entityId
 *   [ubyte]   gameMode
 *   [int]     dimension
 *   [long]    hashedSeed (NEW in 1.15)
 *   [ubyte]   maxPlayers
 *   [String]  levelType
 *   [VarInt]  viewDistance
 *   [boolean] reducedDebugInfo
 *   [boolean] enableRespawnScreen (NEW in 1.15)
 */
public class JoinGamePacketV573 implements Packet {

    private int entityId;
    private int gameMode;
    private int dimension;
    private long hashedSeed;
    private int maxPlayers;
    private String levelType;
    private int viewDistance;

    public JoinGamePacketV573() {}

    public JoinGamePacketV573(int entityId, int gameMode, int dimension,
                               int maxPlayers, String levelType, int viewDistance) {
        this.entityId = entityId;
        this.gameMode = gameMode;
        this.dimension = dimension;
        this.hashedSeed = 0L;
        this.maxPlayers = maxPlayers;
        this.levelType = levelType;
        this.viewDistance = viewDistance;
    }

    @Override
    public int getPacketId() { return 0x26; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(gameMode);
        buf.writeInt(dimension);
        buf.writeLong(hashedSeed);
        buf.writeByte(maxPlayers);
        McDataTypes.writeVarIntString(buf, levelType);
        McDataTypes.writeVarInt(buf, viewDistance);
        buf.writeBoolean(false); // reducedDebugInfo
        buf.writeBoolean(true);  // enableRespawnScreen
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        gameMode = buf.readUnsignedByte();
        dimension = buf.readInt();
        hashedSeed = buf.readLong();
        maxPlayers = buf.readUnsignedByte();
        levelType = McDataTypes.readVarIntString(buf);
        viewDistance = McDataTypes.readVarInt(buf);
        buf.readBoolean(); // reducedDebugInfo
        buf.readBoolean(); // enableRespawnScreen
    }

    public int getEntityId() { return entityId; }
}
