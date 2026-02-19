package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Play state, S2C packet 0x29: Join Game.
 *
 * Completely restructured from V763. The dimension codec has been moved to
 * the Configuration phase (RegistryData packets). Field order is different.
 *
 * Wire format:
 *   [Int]     entityId
 *   [Boolean] isHardcore
 *   [VarInt]  worldCount
 *   [String]  worldNames[] (worldCount strings)
 *   [VarInt]  maxPlayers
 *   [VarInt]  viewDistance
 *   [VarInt]  simulationDistance
 *   [Boolean] reducedDebugInfo
 *   [Boolean] enableRespawnScreen
 *   [Boolean] doLimitedCrafting (NEW)
 *   [String]  dimensionType
 *   [String]  worldName
 *   [Long]    hashedSeed
 *   [Byte]    gameMode (MOVED after hashedSeed)
 *   [Byte]    previousGameMode
 *   [Boolean] isDebug
 *   [Boolean] isFlat
 *   [Boolean] hasDeathLocation
 *   [VarInt]  portalCooldown
 */
public class JoinGamePacketV764 implements Packet {

    private int entityId;
    private int gameMode;
    private int maxPlayers;
    private int viewDistance;
    private int simulationDistance;

    public JoinGamePacketV764() {}

    public JoinGamePacketV764(int entityId, int gameMode, int maxPlayers,
                               int viewDistance, int simulationDistance) {
        this.entityId = entityId;
        this.gameMode = gameMode;
        this.maxPlayers = maxPlayers;
        this.viewDistance = viewDistance;
        this.simulationDistance = simulationDistance;
    }

    @Override
    public int getPacketId() { return 0x29; }

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

        // dimensionType (String, not NBT — references a registry entry sent during Configuration)
        McDataTypes.writeVarIntString(buf, "minecraft:overworld");

        // worldName
        McDataTypes.writeVarIntString(buf, "minecraft:overworld");

        buf.writeLong(0L); // hashedSeed
        buf.writeByte(gameMode); // gameMode (moved after hashedSeed)
        buf.writeByte(-1); // previousGameMode (none)
        buf.writeBoolean(false); // isDebug
        buf.writeBoolean(false); // isFlat
        buf.writeBoolean(false); // hasDeathLocation (Optional absent)
        McDataTypes.writeVarInt(buf, 0); // portalCooldown
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
        McDataTypes.readVarIntString(buf); // dimensionType
        McDataTypes.readVarIntString(buf); // worldName
        buf.readLong(); // hashedSeed
        gameMode = buf.readByte(); // gameMode
        buf.readByte(); // previousGameMode
        buf.readBoolean(); // isDebug
        buf.readBoolean(); // isFlat
        buf.readBoolean(); // hasDeathLocation
        McDataTypes.readVarInt(buf); // portalCooldown
    }

    public int getEntityId() { return entityId; }
}
