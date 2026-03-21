package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.DespawnPlayerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.MessagePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
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
    private final PlayerManager playerManager;
    private MCPEGameplayHandler gameplayHandler;

    /** Convert canonical v12 ID to wire ID for this session. */
    private int wireId(int canonicalId) {
        return MCPEConstants.toWireId(canonicalId, session.getMcpeProtocolVersion());
    }

    public MCPESessionWrapper(LegacyRakNetSession session, LegacyRakNetServer server,
                              PlayerManager playerManager) {
        this.session = session;
        this.server = server;
        this.playerManager = playerManager;
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

        // v27+: UpdateBlock is silently ignored — resend chunk as workaround
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27
                && gameplayHandler != null) {
            gameplayHandler.sendChunkData(pkt.getX() >> 4, pkt.getZ() >> 4);
        }
    }

    private void translateSpawnPlayer(SpawnPlayerPacket pkt) {
        boolean isV34 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;
        boolean isV27 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27;
        // Classic uses fixed-point (x32), MCPE uses float
        float x = pkt.getX() / 32.0f;
        float y = pkt.getY() / 32.0f - (float) PLAYER_EYE_HEIGHT; // feet-level for AddPlayer
        float z = pkt.getZ() / 32.0f;
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;
        String name = pkt.getPlayerName();
        float yaw = ((pkt.getYaw() + 128) & 0xFF) * 360.0f / 256.0f;
        float pitch = (pkt.getPitch() & 0xFF) * 360.0f / 256.0f;

        // v34+: send PlayerListAdd before AddPlayer (registers skin)
        if (isV34) {
            boolean isV81 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_81;
            ConnectedPlayer spawnedPlayer = playerManager.getPlayer((byte) pkt.getPlayerId());
            byte[] skinData = (spawnedPlayer != null) ? spawnedPlayer.getMcpeSkinData() : null;
            if (skinData == null || skinData.length == 0) {
                skinData = MCPEConstants.DEFAULT_SKIN_64x64;
            }
            // Generate a proper UUID from player name (v38+ clients validate UUIDs)
            java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            MCPEPacketBuffer plPkt = new MCPEPacketBuffer();
            plPkt.writeByte(isV81 ? (MCPEConstants.V81_PLAYER_LIST & 0xFF)
                    : (MCPEConstants.V34_PLAYER_LIST & 0xFF));
            plPkt.writeByte(0); // TYPE_ADD
            plPkt.writeInt(1);  // entry count
            plPkt.writeLong(uuid.getMostSignificantBits());
            plPkt.writeLong(uuid.getLeastSignificantBits());
            plPkt.writeLong(entityId);  // entityId
            plPkt.writeString(name);
            if (isV81) {
                // v81: skinId(string) + skinData(string = raw bytes as string)
                plPkt.writeString("Standard_Steve");
                plPkt.writeShort(skinData.length);
                plPkt.writeBytes(skinData);
            } else {
                // v38+ (0.13.1+): skinName(string) replaced slim(byte)+skinTransparent(byte)
                if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_38) {
                    plPkt.writeString(""); // skinName (empty = Steve)
                } else {
                    plPkt.writeByte(0); // slim = 0 (Steve model)
                }
                plPkt.writeShort(skinData.length);
                plPkt.writeBytes(skinData);
            }
            server.sendGamePacket(session, plPkt.getBuf());
        }

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(wireId(MCPEConstants.ADD_PLAYER));
        if (isV34) {
            // v34: uuid(2 longs), username, entityId(long), x, y, z,
            //       speedX, speedY, speedZ, yaw, headYaw, pitch,
            //       slot(compound — air), metadata
            java.util.UUID addUuid = java.util.UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            buf.writeLong(addUuid.getMostSignificantBits());
            buf.writeLong(addUuid.getLeastSignificantBits());
            buf.writeString(name);
            buf.writeLong(entityId);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeFloat(0); // speedX
            buf.writeFloat(0); // speedY
            buf.writeFloat(0); // speedZ
            buf.writeFloat(yaw);
            buf.writeFloat(yaw);  // headYaw
            buf.writeFloat(pitch);
            buf.writeShort(0); // held item (air = slot compound)
        } else if (isV27) {
            // v27: clientId(long), username, entityId(long), x, y, z,
            //       speedX, speedY, speedZ, yaw, headYaw, pitch,
            //       itemId(short), itemDamage(short), slim(byte), skin(string), metadata
            buf.writeLong(entityId);
            buf.writeString(name);
            buf.writeLong(entityId);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeFloat(0); // speedX
            buf.writeFloat(0); // speedY
            buf.writeFloat(0); // speedZ
            buf.writeFloat(yaw);
            buf.writeFloat(yaw);  // headYaw
            buf.writeFloat(pitch);
            buf.writeShort(0); // item ID (air)
            buf.writeShort(0); // item damage
            // Use spawned player's MCPE skin if available, else default Steve
            ConnectedPlayer spawnedPlayer = playerManager.getPlayer((byte) pkt.getPlayerId());
            byte[] skinData = (spawnedPlayer != null) ? spawnedPlayer.getMcpeSkinData() : null;
            if (skinData != null && skinData.length > 0) {
                buf.writeByte(spawnedPlayer.getMcpeSkinSlim());
                buf.writeShort(skinData.length);
                buf.writeBytes(skinData);
            } else {
                buf.writeByte(0); // slim (0 = steve)
                buf.writeShort(MCPEConstants.DEFAULT_SKIN_64x64.length);
                buf.writeBytes(MCPEConstants.DEFAULT_SKIN_64x64);
            }
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
            meta.writeLong(entityId); // v27+: 64-bit entity ID
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
        boolean isV34 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;
        boolean isV81 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_81;

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        if (isV81) {
            // v81: REMOVE_PLAYER was dropped — use REMOVE_ENTITY instead
            buf.writeByte(MCPEConstants.V81_REMOVE_ENTITY & 0xFF);
            buf.writeLong(entityId);
        } else {
            buf.writeByte(wireId(MCPEConstants.REMOVE_PLAYER));
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
                buf.writeLong(entityId); // v27+: 64-bit entity ID
            } else {
                buf.writeInt(entityId);
            }
            if (isV34) {
                // v34: UUID (2 longs) instead of clientId
                ConnectedPlayer despawned = playerManager.getPlayer((byte) pkt.getPlayerId());
                java.util.UUID despawnUuid = (despawned != null)
                        ? java.util.UUID.nameUUIDFromBytes(despawned.getUsername().getBytes(java.nio.charset.StandardCharsets.UTF_8))
                        : new java.util.UUID(0, entityId);
                buf.writeLong(despawnUuid.getMostSignificantBits());
                buf.writeLong(despawnUuid.getLeastSignificantBits());
            } else {
                buf.writeLong(entityId); // clientID (always long)
            }
        }
        server.sendGamePacket(session, buf.getBuf());

        // v34+: also send PlayerList TYPE_REMOVE
        if (isV34) {
            ConnectedPlayer despawned = playerManager.getPlayer((byte) pkt.getPlayerId());
            java.util.UUID despawnUuid = (despawned != null)
                    ? java.util.UUID.nameUUIDFromBytes(despawned.getUsername().getBytes(java.nio.charset.StandardCharsets.UTF_8))
                    : new java.util.UUID(0, entityId);
            MCPEPacketBuffer plPkt = new MCPEPacketBuffer();
            plPkt.writeByte(isV81 ? (MCPEConstants.V81_PLAYER_LIST & 0xFF)
                    : (MCPEConstants.V34_PLAYER_LIST & 0xFF));
            plPkt.writeByte(1); // TYPE_REMOVE
            plPkt.writeInt(1);  // entry count
            plPkt.writeLong(despawnUuid.getMostSignificantBits());
            plPkt.writeLong(despawnUuid.getLeastSignificantBits());
            server.sendGamePacket(session, plPkt.getBuf());
        }
    }

    private void translatePlayerTeleport(PlayerTeleportPacket pkt) {
        float x = pkt.getX() / 32.0f;
        // v17+ MovePlayer S2C uses eye-level Y (PocketMine Alpha_1.4dev confirms);
        // v11-v13 use feet-level. Internal Y is already eye-level.
        float y;
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17) {
            y = pkt.getY() / 32.0f;  // eye-level (internal convention matches v17+)
        } else {
            y = pkt.getY() / 32.0f - (float) PLAYER_EYE_HEIGHT; // feet-level for v11-v13
        }
        float z = pkt.getZ() / 32.0f;
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;
        // Classic yaw 0=North, MCPE yaw 0=South → add 180°
        float yaw = ((pkt.getYaw() + 128) & 0xFF) * 360.0f / 256.0f;
        float pitch = (pkt.getPitch() & 0xFF) * 360.0f / 256.0f;

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(wireId(MCPEConstants.MOVE_PLAYER));
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            buf.writeLong(entityId); // v27+: 64-bit entity ID
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
            // v27+ TextPacket: type(byte) + source(string) + message(string)
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
