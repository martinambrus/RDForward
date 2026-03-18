package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.DespawnPlayerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.MessagePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket;
import com.github.martinambrus.rdforward.server.bedrock.BedrockSessionWrapper;
import io.netty.buffer.ByteBuf;

/**
 * Bridges the Classic packet broadcast system to the MCPE 0.7.0 session.
 * When the server broadcasts Classic packets, ConnectedPlayer routes them
 * here for translation to MCPE format.
 *
 * Extends BedrockSessionWrapper's interface pattern but translates to
 * MCPE 0.7.0 packets instead of modern Bedrock.
 */
public class MCPESessionWrapper {

    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;

    private final LegacyRakNetSession session;
    private final LegacyRakNetServer server;

    public MCPESessionWrapper(LegacyRakNetSession session, LegacyRakNetServer server) {
        this.session = session;
        this.server = server;
    }

    /**
     * Translate a Classic broadcast packet to MCPE 0.7.0 and send it.
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
        buf.writeByte(MCPEConstants.UPDATE_BLOCK);
        buf.writeInt(pkt.getX());
        buf.writeInt(pkt.getZ());
        buf.writeByte(pkt.getY());
        buf.writeByte(pkt.getBlockType());
        buf.writeByte(0); // metadata
        server.sendGamePacket(session, buf.getBuf());
    }

    private void translateSpawnPlayer(SpawnPlayerPacket pkt) {
        // Classic uses fixed-point (x32), MCPE uses float
        float x = pkt.getX() / 32.0f;
        float y = pkt.getY() / 32.0f - (float) PLAYER_EYE_HEIGHT; // feet-level for AddPlayer
        float z = pkt.getZ() / 32.0f;
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;
        String name = pkt.getPlayerName();

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(MCPEConstants.ADD_PLAYER);
        buf.writeLong(entityId);      // clientID (use entity ID as placeholder)
        buf.writeString(name);
        buf.writeInt(entityId);       // entity ID
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeByte((pkt.getYaw() + 128) & 0xFF);  // Classic→MCPE yaw: +128 (180°)
        buf.writeByte(pkt.getPitch()); // pitch
        buf.writeShort(0);            // held item ID
        buf.writeShort(0);            // held item aux value
        buf.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
        buf.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
        buf.writeMetaString(MCPEConstants.META_NAMETAG, name);
        buf.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
        buf.writeMetaEnd();
        server.sendGamePacket(session, buf.getBuf());

        // Send SET_ENTITY_DATA with nametag (some clients only read metadata from this packet)
        MCPEPacketBuffer meta = new MCPEPacketBuffer();
        meta.writeByte(MCPEConstants.SET_ENTITY_DATA);
        meta.writeInt(entityId);
        meta.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
        meta.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
        meta.writeMetaString(MCPEConstants.META_NAMETAG, name);
        meta.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
        meta.writeMetaEnd();
        server.sendGamePacket(session, meta.getBuf());
    }

    private void translateDespawnPlayer(DespawnPlayerPacket pkt) {
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(MCPEConstants.REMOVE_PLAYER);
        buf.writeInt(entityId);
        buf.writeLong(entityId); // clientID
        server.sendGamePacket(session, buf.getBuf());
    }

    private void translatePlayerTeleport(PlayerTeleportPacket pkt) {
        float x = pkt.getX() / 32.0f;
        float y = pkt.getY() / 32.0f - (float) PLAYER_EYE_HEIGHT; // feet-level for MCPE
        float z = pkt.getZ() / 32.0f;
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;
        // Classic yaw 0=North, MCPE yaw 0=South → add 180°
        float yaw = ((pkt.getYaw() + 128) & 0xFF) * 360.0f / 256.0f;
        float pitch = (pkt.getPitch() & 0xFF) * 360.0f / 256.0f;

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(MCPEConstants.MOVE_PLAYER);
        buf.writeInt(entityId);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        server.sendGamePacket(session, buf.getBuf());
    }

    private void translateMessage(MessagePacket pkt) {
        // Protocol 11 MessagePacket has only message (no source field)
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(MCPEConstants.MESSAGE);
        buf.writeString(pkt.getMessage());
        server.sendGamePacket(session, buf.getBuf());
    }

    public LegacyRakNetSession getSession() { return session; }

    public void disconnect(String reason) {
        // Send disconnect packet
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(MCPEConstants.CLIENT_DISCONNECT);
        server.sendGamePacket(session, buf.getBuf());
        session.close();
    }
}
