package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.5 Play state, S2C packet 0x2B: Join Game.
 *
 * Changes from V764:
 * - dimensionType changed from String to VarInt (index into dimension_type registry)
 * - enforcesSecureChat boolean appended after portalCooldown
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
 *   [Boolean] enforcesSecureChat (NEW)
 */
public class JoinGamePacketV766 implements Packet {

    private int entityId;
    private int gameMode;
    private int maxPlayers;
    private int viewDistance;
    private int simulationDistance;

    public JoinGamePacketV766() {}

    public JoinGamePacketV766(int entityId, int gameMode, int maxPlayers,
                               int viewDistance, int simulationDistance) {
        this.entityId = entityId;
        this.gameMode = gameMode;
        this.maxPlayers = maxPlayers;
        this.viewDistance = viewDistance;
        this.simulationDistance = simulationDistance;
    }

    @Override
    public int getPacketId() { return 0x2B; }

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
        buf.writeBoolean(false); // enforcesSecureChat (NEW in 1.20.5)
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
        buf.readBoolean(); // enforcesSecureChat
    }

    public int getEntityId() { return entityId; }
}
