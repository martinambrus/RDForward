package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.DespawnPlayerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.MessagePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.OrientationUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PositionOrientationUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PositionUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bridges the Classic packet broadcast system to the MCPE session.
 * When the server broadcasts Classic packets, ConnectedPlayer routes them
 * here for translation to MCPE format.
 *
 * Delegates all version-specific wire format logic to {@link MCPEPacketCodec}.
 */
public class MCPESessionWrapper {

    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;

    private final LegacyRakNetSession session;
    private final LegacyRakNetServer server;
    private final PlayerManager playerManager;
    private final MCPEPacketCodec codec;
    private MCPEGameplayHandler gameplayHandler;

    /** Per-entity tracked position for delta-to-absolute reconstruction (fixed-point x32). */
    private static final class EntityPosition {
        float x, y, z, yaw, pitch;
        EntityPosition(float x, float y, float z, float yaw, float pitch) {
            this.x = x; this.y = y; this.z = z; this.yaw = yaw; this.pitch = pitch;
        }
    }
    private final Map<Integer, EntityPosition> entityPositions = new HashMap<>();

    public MCPESessionWrapper(LegacyRakNetSession session, LegacyRakNetServer server,
                              PlayerManager playerManager) {
        this.session = session;
        this.server = server;
        this.playerManager = playerManager;
        this.codec = MCPEPacketCodec.forVersion(session.getMcpeProtocolVersion());
    }

    public void setGameplayHandler(MCPEGameplayHandler handler) {
        this.gameplayHandler = handler;
    }

    /**
     * Translate a Classic broadcast packet to MCPE and send it.
     * Called by ConnectedPlayer.sendPacket() for MCPE players.
     */
    public void translateAndSend(Packet classicPacket) {
        if (session.getState() == LegacyRakNetSession.State.DISCONNECTED) return;

        if (classicPacket instanceof SetBlockServerPacket) {
            translateSetBlock((SetBlockServerPacket) classicPacket);
        } else if (classicPacket instanceof SpawnPlayerPacket) {
            translateSpawnPlayer((SpawnPlayerPacket) classicPacket);
        } else if (classicPacket instanceof DespawnPlayerPacket) {
            translateDespawnPlayer((DespawnPlayerPacket) classicPacket);
        } else if (classicPacket instanceof PlayerTeleportPacket) {
            translatePlayerTeleport((PlayerTeleportPacket) classicPacket);
        } else if (classicPacket instanceof PositionOrientationUpdatePacket) {
            translatePositionOrientationUpdate((PositionOrientationUpdatePacket) classicPacket);
        } else if (classicPacket instanceof PositionUpdatePacket) {
            translatePositionUpdate((PositionUpdatePacket) classicPacket);
        } else if (classicPacket instanceof OrientationUpdatePacket) {
            translateOrientationUpdate((OrientationUpdatePacket) classicPacket);
        } else if (classicPacket instanceof MessagePacket) {
            translateMessage((MessagePacket) classicPacket);
        }
        // Other Classic packets silently dropped (not applicable to MCPE)
    }

    private void translateSetBlock(SetBlockServerPacket pkt) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        codec.writeSetBlock(buf, pkt.getX(), pkt.getY(), pkt.getZ(), pkt.getBlockType());
        server.sendGamePacket(session, buf.getBuf());

        if (codec.requiresChunkResendForBlockUpdate() && gameplayHandler != null) {
            gameplayHandler.sendChunkData(pkt.getX() >> 4, pkt.getZ() >> 4);
        }
    }

    private void translateSpawnPlayer(SpawnPlayerPacket pkt) {
        // Store absolute position for delta reconstruction
        entityPositions.put(pkt.getPlayerId(), new EntityPosition(
                pkt.getX(), pkt.getY(), pkt.getZ(), pkt.getYaw(), pkt.getPitch()));

        // Classic uses fixed-point (x32), MCPE uses float.
        // Internal Y is eye-level. AddPlayer expects feet-level for all versions.
        float x = pkt.getX() / 32.0f;
        float eyeY = pkt.getY() / 32.0f;
        float y = eyeY - (float) PLAYER_EYE_HEIGHT; // feet-level for AddPlayer
        float z = pkt.getZ() / 32.0f;
        float yaw = classicYawToMcpeDegrees(pkt.getYaw());
        float pitch = classicPitchToMcpeDegrees(pkt.getPitch());

        ConnectedPlayer spawnedPlayer = playerManager.getPlayer((byte) pkt.getPlayerId());
        byte[] skinData = (spawnedPlayer != null) ? spawnedPlayer.getMcpeSkinData() : null;
        int skinSlim = (spawnedPlayer != null) ? spawnedPlayer.getMcpeSkinSlim() : 0;

        MCPEPacketData.SpawnPlayerData data = new MCPEPacketData.SpawnPlayerData(
                pkt.getPlayerId(), pkt.getPlayerName(), x, y, z, yaw, pitch, skinData, skinSlim);
        codec.writeSpawnPlayer(server, session, data);

        // Send MovePlayer after AddPlayer to set correct rotation.
        // v9 AddPlayer has no rotation fields at all; v11-v20 use byte yaw whose
        // convention may differ from MovePlayer float yaw. Sending MovePlayer
        // ensures the correct float yaw (0=South) is always applied.
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        codec.writePlayerTeleport(buf, entityId, x, pkt.getY() / 32.0f, z, yaw, pitch);
        server.sendGamePacket(session, buf.getBuf());
    }

    private void translateDespawnPlayer(DespawnPlayerPacket pkt) {
        entityPositions.remove(pkt.getPlayerId());
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;
        ConnectedPlayer despawned = playerManager.getPlayer((byte) pkt.getPlayerId());
        UUID despawnUuid = (despawned != null)
                ? UUID.nameUUIDFromBytes(despawned.getUsername().getBytes(StandardCharsets.UTF_8))
                : new UUID(0, entityId);

        MCPEPacketData.DespawnPlayerData data = new MCPEPacketData.DespawnPlayerData(entityId, despawnUuid);
        codec.writeDespawnPlayer(server, session, data);
    }

    private void translatePlayerTeleport(PlayerTeleportPacket pkt) {
        // Store absolute position for delta reconstruction
        entityPositions.put(pkt.getPlayerId(), new EntityPosition(
                pkt.getX(), pkt.getY(), pkt.getZ(), pkt.getYaw(), pkt.getPitch()));

        float x = pkt.getX() / 32.0f;
        // Pass eye-level Y — the codec adjusts to feet-level for versions that need it
        float y = pkt.getY() / 32.0f;
        float z = pkt.getZ() / 32.0f;
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;
        float yaw = classicYawToMcpeDegrees(pkt.getYaw());
        float pitch = classicPitchToMcpeDegrees(pkt.getPitch());

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        codec.writePlayerTeleport(buf, entityId, x, y, z, yaw, pitch);
        server.sendGamePacket(session, buf.getBuf());
    }

    private void translatePositionOrientationUpdate(PositionOrientationUpdatePacket pkt) {
        EntityPosition pos = entityPositions.get(pkt.getPlayerId());
        if (pos == null) return;
        pos.x += pkt.getChangeX();
        pos.y += pkt.getChangeY();
        pos.z += pkt.getChangeZ();
        pos.yaw = pkt.getYaw();
        pos.pitch = pkt.getPitch();
        sendMoveFromTrackedPosition(pkt.getPlayerId(), pos);
    }

    private void translatePositionUpdate(PositionUpdatePacket pkt) {
        EntityPosition pos = entityPositions.get(pkt.getPlayerId());
        if (pos == null) return;
        pos.x += pkt.getChangeX();
        pos.y += pkt.getChangeY();
        pos.z += pkt.getChangeZ();
        sendMoveFromTrackedPosition(pkt.getPlayerId(), pos);
    }

    private void translateOrientationUpdate(OrientationUpdatePacket pkt) {
        EntityPosition pos = entityPositions.get(pkt.getPlayerId());
        if (pos == null) return;
        pos.yaw = pkt.getYaw();
        pos.pitch = pkt.getPitch();
        sendMoveFromTrackedPosition(pkt.getPlayerId(), pos);
    }

    private void sendMoveFromTrackedPosition(int playerId, EntityPosition pos) {
        int entityId = (playerId & 0xFF) + 1;
        float x = pos.x / 32.0f;
        float y = pos.y / 32.0f; // eye-level — codec adjusts if needed
        float z = pos.z / 32.0f;
        float yaw = classicYawToMcpeDegrees((int) pos.yaw);
        float pitch = classicPitchToMcpeDegrees((int) pos.pitch);

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        codec.writePlayerTeleport(buf, entityId, x, y, z, yaw, pitch);
        server.sendGamePacket(session, buf.getBuf());
    }

    private void translateMessage(MessagePacket pkt) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        codec.writeMessage(buf, pkt.getMessage());
        server.sendGamePacket(session, buf.getBuf());
    }

    /**
     * Convert a Classic byte yaw (0=North) to MCPE float degrees (0=South).
     * All MCPE versions (v9 through v91) use 0=South — add 180° (+128 bytes).
     */
    private float classicYawToMcpeDegrees(int classicYaw) {
        return ((classicYaw + 128) & 0xFF) * 360.0f / 256.0f;
    }

    private float classicPitchToMcpeDegrees(int classicPitch) {
        float degrees = (classicPitch & 0xFF) * 360.0f / 256.0f;
        if (degrees > 180.0f) degrees -= 360.0f;
        return degrees;
    }

    /**
     * Register an entity's position for delta-to-absolute reconstruction.
     * Called by MCPELoginHandler when existing players are sent via AddPlayer
     * outside of the translateAndSend pathway.
     */
    public void registerEntityPosition(int playerId, short x, short y, short z, int yaw, int pitch) {
        entityPositions.put(playerId, new EntityPosition(x, y, z, yaw, pitch));
    }

    public LegacyRakNetSession getSession() { return session; }

    public MCPEPacketCodec getCodec() { return codec; }

    /**
     * Send a SetTime packet to this MCPE client using the correct wire format.
     */
    public void sendTimeUpdate(int time) {
        if (session.getState() == LegacyRakNetSession.State.DISCONNECTED) return;
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        codec.writeTimeUpdate(pkt, time);
        server.sendGamePacket(session, pkt.getBuf());
    }

    /**
     * Send a chunk to this MCPE client using the correct wire format.
     * Called by ChunkManager when a player moves into range of a new chunk.
     */
    public void sendChunkData(ServerWorld world, int chunkX, int chunkZ) {
        if (session.getState() == LegacyRakNetSession.State.DISCONNECTED) return;
        codec.sendChunkData(server, session, world, chunkX, chunkZ);
    }

    public void disconnect(String reason) {
        // Send disconnect packet
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(MCPEConstants.CLIENT_DISCONNECT);
        server.sendGamePacket(session, buf.getBuf());
        session.close();
    }
}
