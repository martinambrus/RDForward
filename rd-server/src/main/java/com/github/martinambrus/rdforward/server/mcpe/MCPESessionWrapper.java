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
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            // v91: blockCoords + UnsignedVarInt(blockId) + UnsignedVarInt((flags<<4)|meta)
            buf.writeBlockCoords(pkt.getX(), pkt.getY(), pkt.getZ());
            buf.writeUnsignedVarInt(pkt.getBlockType());
            buf.writeUnsignedVarInt(0x0B << 4); // FLAG_ALL_PRIORITY, meta=0
        } else {
            buf.writeInt(pkt.getX());
            buf.writeInt(pkt.getZ());
            buf.writeByte(pkt.getY());
            buf.writeByte(pkt.getBlockType());
            // v27: upper nibble = flags (NEIGHBORS|NETWORK|PRIORITY = 0x0B)
            int flags = (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) ? 0x0B : 0;
            buf.writeByte((flags << 4));
        }
        server.sendGamePacket(session, buf.getBuf());

        // v27+: UpdateBlock is silently ignored — resend chunk as workaround
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27
                && gameplayHandler != null) {
            gameplayHandler.sendChunkData(pkt.getX() >> 4, pkt.getZ() >> 4);
        }
    }

    private void translateSpawnPlayer(SpawnPlayerPacket pkt) {
        boolean isV91 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91;
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
            ConnectedPlayer spawnedPlayer = playerManager.getPlayer((byte) pkt.getPlayerId());
            byte[] skinData = (spawnedPlayer != null) ? spawnedPlayer.getMcpeSkinData() : null;
            if (skinData == null || skinData.length == 0) {
                skinData = MCPEConstants.DEFAULT_SKIN_64x64;
            }
            java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            MCPEPacketBuffer plPkt = new MCPEPacketBuffer();
            if (isV91) {
                plPkt.writeByte(MCPEConstants.V91_PLAYER_LIST & 0xFF);
                plPkt.writeByte(0); // TYPE_ADD
                plPkt.writeUnsignedVarInt(1); // entry count
                plPkt.writeLong(uuid.getMostSignificantBits());
                plPkt.writeLong(uuid.getLeastSignificantBits());
                plPkt.writeSignedVarInt(entityId);
                plPkt.writeStringV91(name);
                plPkt.writeStringV91("Standard_Steve");
                plPkt.writeUnsignedVarInt(skinData.length); // raw skin bytes
                plPkt.writeBytes(skinData);
            } else {
            boolean isV81 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_81;
            plPkt.writeByte(isV81 ? (MCPEConstants.V81_PLAYER_LIST & 0xFF)
                    : (MCPEConstants.V34_PLAYER_LIST & 0xFF));
            plPkt.writeByte(0); // TYPE_ADD
            plPkt.writeInt(1);  // entry count
            plPkt.writeLong(uuid.getMostSignificantBits());
            plPkt.writeLong(uuid.getLeastSignificantBits());
            plPkt.writeLong(entityId);  // entityId
            plPkt.writeString(name);
            if (isV81) {
                plPkt.writeString("Standard_Steve");
                plPkt.writeShort(skinData.length);
                plPkt.writeBytes(skinData);
            } else {
                if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_38) {
                    plPkt.writeString("");
                } else {
                    plPkt.writeByte(0);
                }
                plPkt.writeShort(skinData.length);
                plPkt.writeBytes(skinData);
            }
            }
            server.sendGamePacket(session, plPkt.getBuf());
        }

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(wireId(MCPEConstants.ADD_PLAYER));
        if (isV91) {
            // v91: UUID + VarInt-string name + VarInt uniqueId + VarInt runtimeId
            //   + 3x LFloat pos + 3x LFloat speed + LFloat yaw/headYaw/pitch
            //   + VarInt(0) air slot + metadata
            java.util.UUID addUuid = java.util.UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            buf.writeLong(addUuid.getMostSignificantBits());
            buf.writeLong(addUuid.getLeastSignificantBits());
            buf.writeStringV91(name);
            buf.writeSignedVarInt(entityId); // uniqueId
            buf.writeSignedVarInt(entityId); // runtimeId
            buf.writeLFloat(x);
            buf.writeLFloat(y);
            buf.writeLFloat(z);
            buf.writeLFloat(0); // speedX
            buf.writeLFloat(0); // speedY
            buf.writeLFloat(0); // speedZ
            buf.writeLFloat(yaw);
            buf.writeLFloat(yaw);  // headYaw
            buf.writeLFloat(pitch);
            buf.writeSignedVarInt(0); // held item (air)
        } else if (isV34) {
            java.util.UUID addUuid = java.util.UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            buf.writeLong(addUuid.getMostSignificantBits());
            buf.writeLong(addUuid.getLeastSignificantBits());
            buf.writeString(name);
            buf.writeLong(entityId);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeFloat(0); buf.writeFloat(0); buf.writeFloat(0);
            buf.writeFloat(yaw);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeShort(0); // held item (air)
        } else if (isV27) {
            buf.writeLong(entityId);
            buf.writeString(name);
            buf.writeLong(entityId);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeFloat(0); buf.writeFloat(0); buf.writeFloat(0);
            buf.writeFloat(yaw);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeShort(0); buf.writeShort(0);
            ConnectedPlayer spawnedPlayer = playerManager.getPlayer((byte) pkt.getPlayerId());
            byte[] skinData = (spawnedPlayer != null) ? spawnedPlayer.getMcpeSkinData() : null;
            if (skinData != null && skinData.length > 0) {
                buf.writeByte(spawnedPlayer.getMcpeSkinSlim());
                buf.writeShort(skinData.length);
                buf.writeBytes(skinData);
            } else {
                buf.writeByte(0);
                buf.writeShort(MCPEConstants.DEFAULT_SKIN_64x64.length);
                buf.writeBytes(MCPEConstants.DEFAULT_SKIN_64x64);
            }
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_11) {
            buf.writeLong(entityId);
            buf.writeString(name);
            buf.writeInt(entityId);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeByte((pkt.getYaw() + 128) & 0xFF);
            buf.writeByte(pkt.getPitch());
            buf.writeShort(0); buf.writeShort(0);
        } else {
            buf.writeLong(entityId);
            buf.writeString(name);
            buf.writeInt(entityId);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
        }

        boolean isV9 = session.getMcpeProtocolVersion() < MCPEConstants.MCPE_PROTOCOL_VERSION_11;
        if (isV91) {
            // v91 metadata: VarInt count + per-entry VarInt key + VarInt type + value
            int flags91 = (1 << MCPEConstants.V91_FLAG_CAN_SHOW_NAMETAG)
                        | (1 << MCPEConstants.V91_FLAG_ALWAYS_SHOW_NAMETAG);
            buf.writeMetadataV91Start(6);
            buf.writeMetaLongV91(MCPEConstants.V91_META_FLAGS, flags91);
            buf.writeMetaShortV91(MCPEConstants.V91_META_AIR, (short) 400);
            buf.writeMetaShortV91(MCPEConstants.V91_META_MAX_AIR, (short) 400);
            buf.writeMetaStringV91Entry(MCPEConstants.V91_META_NAMETAG, name);
            buf.writeMetaLongV91(MCPEConstants.V91_META_LEAD_HOLDER_EID, -1);
            buf.writeMetaFloatV91(MCPEConstants.V91_META_SCALE, 1.0f);
        } else if (isV9) {
            buf.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
            buf.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
            buf.writeMetaByte(16, (byte) 0);
            buf.writeMetaPosition(17, 0, 0, 0);
            buf.writeMetaEnd();
        } else {
            buf.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
            buf.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
            buf.writeMetaString(MCPEConstants.META_NAMETAG, name);
            buf.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
            buf.writeMetaEnd();
        }
        server.sendGamePacket(session, buf.getBuf());

        if (isV9) {
            MCPEPacketBuffer eqPkt = new MCPEPacketBuffer();
            eqPkt.writeByte(wireId(MCPEConstants.PLAYER_EQUIPMENT));
            eqPkt.writeInt(entityId);
            eqPkt.writeShort(0); eqPkt.writeShort(0);
            server.sendGamePacket(session, eqPkt.getBuf());
        } else {
            MCPEPacketBuffer meta = new MCPEPacketBuffer();
            meta.writeByte(wireId(MCPEConstants.SET_ENTITY_DATA));
            if (isV91) {
                meta.writeSignedVarInt(entityId);
                int metaFlags91 = (1 << MCPEConstants.V91_FLAG_CAN_SHOW_NAMETAG)
                                | (1 << MCPEConstants.V91_FLAG_ALWAYS_SHOW_NAMETAG);
                meta.writeMetadataV91Start(6);
                meta.writeMetaLongV91(MCPEConstants.V91_META_FLAGS, metaFlags91);
                meta.writeMetaShortV91(MCPEConstants.V91_META_AIR, (short) 400);
                meta.writeMetaShortV91(MCPEConstants.V91_META_MAX_AIR, (short) 400);
                meta.writeMetaStringV91Entry(MCPEConstants.V91_META_NAMETAG, name);
                meta.writeMetaLongV91(MCPEConstants.V91_META_LEAD_HOLDER_EID, -1);
                meta.writeMetaFloatV91(MCPEConstants.V91_META_SCALE, 1.0f);
            } else if (isV27) {
                meta.writeLong(entityId);
                meta.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
                meta.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
                meta.writeMetaString(MCPEConstants.META_NAMETAG, name);
                meta.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
                meta.writeMetaEnd();
            } else {
                meta.writeInt(entityId);
                meta.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
                meta.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
                meta.writeMetaString(MCPEConstants.META_NAMETAG, name);
                meta.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
                meta.writeMetaEnd();
            }
            server.sendGamePacket(session, meta.getBuf());
        }
    }

    private void translateDespawnPlayer(DespawnPlayerPacket pkt) {
        int entityId = (pkt.getPlayerId() & 0xFF) + 1;
        boolean isV91 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91;
        boolean isV34 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;
        boolean isV81 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_81;

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        if (isV91) {
            // v91: REMOVE_ENTITY with VarInt entity ID
            buf.writeByte(MCPEConstants.V91_REMOVE_ENTITY & 0xFF);
            buf.writeSignedVarInt(entityId);
        } else if (isV81) {
            buf.writeByte(MCPEConstants.V81_REMOVE_ENTITY & 0xFF);
            buf.writeLong(entityId);
        } else {
            buf.writeByte(wireId(MCPEConstants.REMOVE_PLAYER));
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
                buf.writeLong(entityId);
            } else {
                buf.writeInt(entityId);
            }
            if (isV34) {
                ConnectedPlayer despawned = playerManager.getPlayer((byte) pkt.getPlayerId());
                java.util.UUID despawnUuid = (despawned != null)
                        ? java.util.UUID.nameUUIDFromBytes(despawned.getUsername().getBytes(java.nio.charset.StandardCharsets.UTF_8))
                        : new java.util.UUID(0, entityId);
                buf.writeLong(despawnUuid.getMostSignificantBits());
                buf.writeLong(despawnUuid.getLeastSignificantBits());
            } else {
                buf.writeLong(entityId);
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
            if (isV91) {
                plPkt.writeByte(MCPEConstants.V91_PLAYER_LIST & 0xFF);
                plPkt.writeByte(1); // TYPE_REMOVE
                plPkt.writeUnsignedVarInt(1);
            } else {
                plPkt.writeByte(isV81 ? (MCPEConstants.V81_PLAYER_LIST & 0xFF)
                        : (MCPEConstants.V34_PLAYER_LIST & 0xFF));
                plPkt.writeByte(1); // TYPE_REMOVE
                plPkt.writeInt(1);
            }
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
        float yaw = ((pkt.getYaw() + 128) & 0xFF) * 360.0f / 256.0f;
        float pitch = (pkt.getPitch() & 0xFF) * 360.0f / 256.0f;

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(wireId(MCPEConstants.MOVE_PLAYER));
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            buf.writeSignedVarInt(entityId);
            buf.writeLFloat(x);
            buf.writeLFloat(y);
            buf.writeLFloat(z);
            buf.writeLFloat(pitch);
            buf.writeLFloat(yaw);
            buf.writeLFloat(yaw); // bodyYaw
            buf.writeByte(0);     // mode = normal
            buf.writeByte(0);     // onGround
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            buf.writeLong(entityId);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeFloat(yaw);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeByte(0);
            buf.writeByte(0);
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
            buf.writeInt(entityId);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeFloat(yaw);
        } else {
            buf.writeInt(entityId);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
        }
        server.sendGamePacket(session, buf.getBuf());
    }

    private void translateMessage(MessagePacket pkt) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(wireId(MCPEConstants.MESSAGE));
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            // v91: byte type + VarInt-prefixed strings
            buf.writeByte(1); // type = CHAT
            buf.writeStringV91(""); // source
            buf.writeStringV91(pkt.getMessage());
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            buf.writeByte(1); // type = CHAT
            buf.writeString("");
            buf.writeString(pkt.getMessage());
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_12) {
            buf.writeString("");
            buf.writeString(pkt.getMessage());
        } else {
            buf.writeString(pkt.getMessage());
        }
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
