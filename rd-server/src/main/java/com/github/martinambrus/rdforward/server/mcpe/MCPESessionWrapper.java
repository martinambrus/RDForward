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
    private MCPEGameplayHandler gameplayHandler;

    /** Convert canonical v12 ID to wire ID for this session. */
    private int wireId(int canonicalId) {
        return MCPEConstants.toWireId(canonicalId, session.getMcpeProtocolVersion());
    }

    public MCPESessionWrapper(LegacyRakNetSession session, LegacyRakNetServer server) {
        this.session = session;
        this.server = server;
    }

    public void setGameplayHandler(MCPEGameplayHandler handler) {
        this.gameplayHandler = handler;
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
        buf.writeByte(wireId(MCPEConstants.UPDATE_BLOCK));
        buf.writeInt(pkt.getX());
        buf.writeInt(pkt.getZ());
        buf.writeByte(pkt.getY());
        buf.writeByte(pkt.getBlockType());
        // v27: upper nibble = flags (NEIGHBORS|NETWORK|PRIORITY = 0x0B)
        int flags = (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) ? 0x0B : 0;
        buf.writeByte((flags << 4));
        server.sendGamePacket(session, buf.getBuf());

        // v27: UpdateBlock is silently ignored — resend chunk as workaround
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27
                && gameplayHandler != null) {
            gameplayHandler.sendChunkData(pkt.getX() >> 4, pkt.getZ() >> 4);
        }
    }

    private void translateSpawnPlayer(SpawnPlayerPacket pkt) {
        boolean isV27 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27;
        // Classic uses fixed-point (x32), MCPE uses float
        float x = pkt.getX() / 32.0f;
        float y = pkt.getY() / 32.0f - (float) PLAYER_EYE_HEIGHT; // feet-level for AddPlayer
        float z = pkt.getZ() / 32.0f;
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;
        String name = pkt.getPlayerName();
        float yaw = ((pkt.getYaw() + 128) & 0xFF) * 360.0f / 256.0f;
        float pitch = (pkt.getPitch() & 0xFF) * 360.0f / 256.0f;

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(wireId(MCPEConstants.ADD_PLAYER));
        if (isV27) {
            // v27: clientId(long), username, entityId(long), x, y, z,
            //       speedX, speedY, speedZ, yaw, headYaw, pitch,
            //       itemId(short), itemDamage(short), slim(byte), skin(string), metadata
            buf.writeLong(entityId);
            buf.writeString(name);
            buf.writeLong(entityId);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            // NOTE: speed fields removed — testing if Ninecraft v27 client expects them
            buf.writeFloat(yaw);
            buf.writeFloat(yaw);  // headYaw
            buf.writeFloat(pitch);
            buf.writeShort(0); // item ID (air)
            buf.writeShort(0); // item damage
            buf.writeByte(0);  // slim (0 = steve)
            buf.writeShort(0); // empty skin (non-empty causes client crash — needs investigation)
        } else {
            buf.writeLong(entityId);      // clientID
            buf.writeString(name);
            buf.writeInt(entityId);       // entity ID
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeByte((pkt.getYaw() + 128) & 0xFF);  // Classic→MCPE yaw: +128 (180°)
            buf.writeByte(pkt.getPitch()); // pitch
            buf.writeShort(0);            // held item ID
            buf.writeShort(0);            // held item aux value
        }
        buf.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
        buf.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
        buf.writeMetaString(MCPEConstants.META_NAMETAG, name);
        buf.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
        buf.writeMetaEnd();
        server.sendGamePacket(session, buf.getBuf());

        // Send SET_ENTITY_DATA with nametag (some clients only read metadata from this packet)
        MCPEPacketBuffer meta = new MCPEPacketBuffer();
        meta.writeByte(wireId(MCPEConstants.SET_ENTITY_DATA));
        if (isV27) {
            meta.writeLong(entityId); // v27: 64-bit entity ID
        } else {
            meta.writeInt(entityId);
        }
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
        buf.writeByte(wireId(MCPEConstants.REMOVE_PLAYER));
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            buf.writeLong(entityId); // v27: 64-bit entity ID
        } else {
            buf.writeInt(entityId);
        }
        buf.writeLong(entityId); // clientID (always long)
        server.sendGamePacket(session, buf.getBuf());
    }

    private void translatePlayerTeleport(PlayerTeleportPacket pkt) {
        float x = pkt.getX() / 32.0f;
        // v27 MovePlayer uses eye-level Y; older versions use feet-level
        float y = (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27)
                ? pkt.getY() / 32.0f  // eye-level (internal convention matches)
                : pkt.getY() / 32.0f - (float) PLAYER_EYE_HEIGHT; // feet-level
        float z = pkt.getZ() / 32.0f;
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;
        // Classic yaw 0=North, MCPE yaw 0=South → add 180°
        float yaw = ((pkt.getYaw() + 128) & 0xFF) * 360.0f / 256.0f;
        float pitch = (pkt.getPitch() & 0xFF) * 360.0f / 256.0f;

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(wireId(MCPEConstants.MOVE_PLAYER));
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            buf.writeLong(entityId); // v27: 64-bit entity ID
        } else {
            buf.writeInt(entityId);
        }
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            buf.writeFloat(yaw);
            buf.writeFloat(yaw);  // headYaw (same as body yaw)
            buf.writeFloat(pitch);
            buf.writeByte(0);     // mode = normal
            buf.writeByte(0);     // onGround
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
            buf.writeFloat(yaw);  // bodyYaw
            buf.writeFloat(pitch);
            buf.writeFloat(yaw);  // headYaw
        } else {
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
        }
        server.sendGamePacket(session, buf.getBuf());
    }

    private void translateMessage(MessagePacket pkt) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(wireId(MCPEConstants.MESSAGE));
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            // v27 TextPacket: type(byte) + source(string) + message(string)
            buf.writeByte(1); // type = CHAT
            buf.writeString(""); // source
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_12) {
            buf.writeString(""); // source (system message = empty)
        }
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
