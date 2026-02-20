package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.2 Play state, S2C packet 0x2C: Join Game.
 *
 * Same as V766 but with VarInt seaLevel appended at the end.
 *
 * Wire format:
 *   [Int]     entityId
 *   [Boolean] isHardcore
 *   [VarInt]  worldCount
 *   [String]  worldNames[]
 *   [VarInt]  maxPlayers
 *   [VarInt]  viewDistance
 *   [VarInt]  simulationDistance
 *   [Boolean] reducedDebugInfo
 *   [Boolean] enableRespawnScreen
 *   [Boolean] doLimitedCrafting
 *   [VarInt]  dimensionType (0 = overworld index)
 *   [String]  worldName
 *   [Long]    hashedSeed
 *   [Byte]    gameMode
 *   [Byte]    previousGameMode
 *   [Boolean] isDebug
 *   [Boolean] isFlat
 *   [Boolean] hasDeathLocation
 *   [VarInt]  portalCooldown
 *   [VarInt]  seaLevel (NEW in 1.21.2, default 64)
 *   [Boolean] enforcesSecureChat
 */
public class JoinGamePacketV768 implements Packet {

    private int entityId;
    private int gameMode;
    private int maxPlayers;
    private int viewDistance;
    private int simulationDistance;

    public JoinGamePacketV768() {}

    public JoinGamePacketV768(int entityId, int gameMode, int maxPlayers,
                               int viewDistance, int simulationDistance) {
        this.entityId = entityId;
        this.gameMode = gameMode;
        this.maxPlayers = maxPlayers;
        this.viewDistance = viewDistance;
        this.simulationDistance = simulationDistance;
    }

    @Override
    public int getPacketId() { return 0x2C; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(false); // isHardcore

        // worldCount + worldNames
        McDataTypes.writeVarInt(buf, 1);
        McDataTypes.writeVarIntString(buf, "minecraft:overworld");

        McDataTypes.writeVarInt(buf, maxPlayers);
        McDataTypes.writeVarInt(buf, viewDistance);
        McDataTypes.writeVarInt(buf, simulationDistance);
        buf.writeBoolean(false); // reducedDebugInfo
        buf.writeBoolean(true);  // enableRespawnScreen
        buf.writeBoolean(false); // doLimitedCrafting

        // dimensionType (VarInt index, not String — references dimension_type registry)
        McDataTypes.writeVarInt(buf, 0); // 0 = overworld

        // worldName
        McDataTypes.writeVarIntString(buf, "minecraft:overworld");

        buf.writeLong(0L); // hashedSeed
        buf.writeByte(gameMode); // gameMode
        buf.writeByte(-1); // previousGameMode (none)
        buf.writeBoolean(false); // isDebug
        buf.writeBoolean(false); // isFlat
        buf.writeBoolean(false); // hasDeathLocation
        McDataTypes.writeVarInt(buf, 0); // portalCooldown
        McDataTypes.writeVarInt(buf, 64); // seaLevel (NEW in 1.21.2)
        buf.writeBoolean(false); // enforcesSecureChat
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        buf.readBoolean(); // isHardcore
        int worldCount = McDataTypes.readVarInt(buf);
        for (int i = 0; i < worldCount; i++) {
            McDataTypes.readVarIntString(buf); // worldName
        }
        maxPlayers = McDataTypes.readVarInt(buf);
        viewDistance = McDataTypes.readVarInt(buf);
        simulationDistance = McDataTypes.readVarInt(buf);
        buf.readBoolean(); // reducedDebugInfo
        buf.readBoolean(); // enableRespawnScreen
        buf.readBoolean(); // doLimitedCrafting
        McDataTypes.readVarInt(buf); // dimensionType
        McDataTypes.readVarIntString(buf); // worldName
        buf.readLong(); // hashedSeed
        gameMode = buf.readByte(); // gameMode
        buf.readByte(); // previousGameMode
        buf.readBoolean(); // isDebug
        buf.readBoolean(); // isFlat
        buf.readBoolean(); // hasDeathLocation
        McDataTypes.readVarInt(buf); // portalCooldown
        McDataTypes.readVarInt(buf); // seaLevel
        buf.readBoolean(); // enforcesSecureChat
    }

    public int getEntityId() { return entityId; }
}
