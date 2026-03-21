package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.DespawnPlayerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.MessagePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;

import java.nio.charset.StandardCharsets;
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
        // Classic uses fixed-point (x32), MCPE uses float
        float x = pkt.getX() / 32.0f;
        float y = pkt.getY() / 32.0f - (float) PLAYER_EYE_HEIGHT; // feet-level for AddPlayer
        float z = pkt.getZ() / 32.0f;
        float yaw = ((pkt.getYaw() + 128) & 0xFF) * 360.0f / 256.0f;
        float pitch = (pkt.getPitch() & 0xFF) * 360.0f / 256.0f;

        ConnectedPlayer spawnedPlayer = playerManager.getPlayer((byte) pkt.getPlayerId());
        byte[] skinData = (spawnedPlayer != null) ? spawnedPlayer.getMcpeSkinData() : null;
        int skinSlim = (spawnedPlayer != null) ? spawnedPlayer.getMcpeSkinSlim() : 0;

        MCPEPacketData.SpawnPlayerData data = new MCPEPacketData.SpawnPlayerData(
                pkt.getPlayerId(), pkt.getPlayerName(), x, y, z, yaw, pitch, skinData, skinSlim);
        codec.writeSpawnPlayer(server, session, data);
    }

    private void translateDespawnPlayer(DespawnPlayerPacket pkt) {
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;
        ConnectedPlayer despawned = playerManager.getPlayer((byte) pkt.getPlayerId());
        UUID despawnUuid = (despawned != null)
                ? UUID.nameUUIDFromBytes(despawned.getUsername().getBytes(StandardCharsets.UTF_8))
                : new UUID(0, entityId);

        MCPEPacketData.DespawnPlayerData data = new MCPEPacketData.DespawnPlayerData(entityId, despawnUuid);
        codec.writeDespawnPlayer(server, session, data);
    }

    private void translatePlayerTeleport(PlayerTeleportPacket pkt) {
        float x = pkt.getX() / 32.0f;
        // Pass eye-level Y — the codec adjusts to feet-level for versions that need it
        float y = pkt.getY() / 32.0f;
        float z = pkt.getZ() / 32.0f;
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;
        float yaw = ((pkt.getYaw() + 128) & 0xFF) * 360.0f / 256.0f;
        float pitch = (pkt.getPitch() & 0xFF) * 360.0f / 256.0f;

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        codec.writePlayerTeleport(buf, entityId, x, y, z, yaw, pitch);
        server.sendGamePacket(session, buf.getBuf());
    }

    private void translateMessage(MessagePacket pkt) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        codec.writeMessage(buf, pkt.getMessage());
        server.sendGamePacket(session, buf.getBuf());
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

    public void disconnect(String reason) {
        // Send disconnect packet
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(MCPEConstants.CLIENT_DISCONNECT);
        server.sendGamePacket(session, buf.getBuf());
        session.close();
    }
}
