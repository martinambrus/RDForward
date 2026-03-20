package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Handles the MCPE 0.7.0 login sequence:
 * 1. C->S LoginPacket (0x82) — username, protocol versions, client ID, skin data
 * 2. S->C LoginStatusPacket (0x83) — success/failure
 * 3. S->C StartGamePacket (0x87) — seed, generator, gamemode, entity ID, spawn position
 * 4. S->C SetTimePacket (0x86)
 * 5. S->C SetSpawnPositionPacket (0xAA)
 * 6. S->C ChunkDataPackets (0x9E) — initial world chunks
 * 7. C->S ReadyPacket (0x84) — client ready to spawn
 * 8. S->C MovePlayerPacket (0x94) — teleport to spawn
 * 9. S->C AdventureSettingsPacket (0xB7) — game flags
 */
public class MCPELoginHandler {

    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;

    private final LegacyRakNetSession session;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;
    private final LegacyRakNetServer server;
    private final Runnable pongUpdater;

    private String username;
    private int clientSlim;
    private byte[] clientSkinData;
    private ConnectedPlayer player;
    private MCPESessionWrapper sessionWrapper;

    /** Convert canonical v12 ID to wire ID for this session. */
    private int wireId(int canonicalId) {
        return MCPEConstants.toWireId(canonicalId, session.getMcpeProtocolVersion());
    }

    public MCPELoginHandler(LegacyRakNetSession session, ServerWorld world,
                            PlayerManager playerManager, ChunkManager chunkManager,
                            LegacyRakNetServer server, Runnable pongUpdater) {
        this.session = session;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
        this.server = server;
        this.pongUpdater = pongUpdater;
    }

    public void handlePacket(ChannelHandlerContext ctx, int packetId, ByteBuf payload) {
        // Accept LOGIN from all versions: v11-v20 (0x82), v27 (0x01), v34 (0x8F)
        if (packetId == (MCPEConstants.LOGIN & 0xFF)
                || packetId == (MCPEConstants.V27_LOGIN & 0xFF)
                || packetId == (MCPEConstants.V34_LOGIN & 0xFF)) {
            handleLogin(ctx, payload);
        } else if (packetId == (MCPEConstants.READY & 0xFF)) {
            handleReady(ctx, payload);
        } else {
            System.out.println("[MCPE Login] Unhandled packet 0x"
                    + Integer.toHexString(packetId) + " (" + payload.readableBytes() + " bytes)"
                    + " (v" + session.getMcpeProtocolVersion() + ")");
        }
    }

    private void handleLogin(ChannelHandlerContext ctx, ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        username = buf.readString();
        int protocol1 = buf.readInt();
        int protocol2 = buf.readInt();

        // v34+: clientId is long (was int). Also has UUID, serverAddress, clientSecret, skinTransparent.
        if (protocol1 >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            long clientIdLong = buf.readLong();
            // Read UUID (16 bytes: 2 longs)
            if (buf.readableBytes() >= 16) {
                buf.readLong(); // UUID most significant
                buf.readLong(); // UUID least significant
            }
            // serverAddress (string), clientSecret (string)
            if (buf.readableBytes() > 0) buf.readString(); // serverAddress
            if (buf.readableBytes() > 0) buf.readString(); // clientSecret
            // skinTransparent(byte), slim(byte), skin(string)
            if (buf.readableBytes() > 0) {
                buf.readUnsignedByte(); // skinTransparent
                clientSlim = buf.readUnsignedByte();
                if (buf.readableBytes() >= 2) {
                    int skinLen = buf.readUnsignedShort();
                    if (skinLen > 0 && skinLen <= buf.readableBytes()) {
                        clientSkinData = new byte[skinLen];
                        buf.getBuf().readBytes(clientSkinData);
                        System.out.println("[MCPE] Client skin: " + skinLen + " bytes, slim=" + clientSlim);
                    }
                }
            }
        } else {
            int clientId = buf.readInt();
            // Read skin data from login packet (v21+: slim byte + skin string)
            if (buf.readableBytes() > 0) {
                clientSlim = buf.readUnsignedByte();
                if (buf.readableBytes() >= 2) {
                    int skinLen = buf.readUnsignedShort();
                    if (skinLen > 0 && skinLen <= buf.readableBytes()) {
                        clientSkinData = new byte[skinLen];
                        buf.getBuf().readBytes(clientSkinData);
                        System.out.println("[MCPE] Client skin: " + skinLen + " bytes, slim=" + clientSlim);
                    }
                }
            }
        }

        System.out.println("[MCPE] Login from " + username
                + " (protocol=" + protocol1 + ")");

        // Accept known protocol versions only (skip dev-only 13, 15-16, 19, 21-26, 28-33)
        boolean validProtocol = protocol1 == 11 || protocol1 == 12 || protocol1 == 14
                || protocol1 == 17 || protocol1 == 18 || protocol1 == 20
                || protocol1 == 27 || protocol1 == 34;
        if (!validProtocol) {
            int status = (protocol1 > MCPEConstants.MCPE_PROTOCOL_VERSION_MAX)
                    ? MCPEConstants.LOGIN_SERVER_OUTDATED
                    : MCPEConstants.LOGIN_CLIENT_OUTDATED;
            sendLoginStatus(status);
            return;
        }
        session.setMcpeProtocolVersion(protocol1);

        // Check for duplicate username
        if (username != null && !username.trim().isEmpty()) {
            playerManager.kickDuplicatePlayer(username.trim(), world);
        }

        // Register player
        player = playerManager.addPlayer(username, null, ProtocolVersion.BEDROCK);
        if (player == null) {
            // Server full — disconnect
            sendLoginStatus(MCPEConstants.LOGIN_CLIENT_OUTDATED); // No "server full" status in protocol 11
            return;
        }

        // Store skin data on player for forwarding to other MCPE clients
        if (clientSkinData != null && clientSkinData.length > 0) {
            player.setMcpeSkin(clientSlim, clientSkinData);
        }

        // Set up session wrapper for Classic packet translation
        sessionWrapper = new MCPESessionWrapper(session, server, playerManager);
        player.setMcpeSession(sessionWrapper);

        // Send login success
        sendLoginStatus(MCPEConstants.LOGIN_SUCCESS);

        // Calculate spawn position
        double spawnX, spawnY = 0, spawnZ;
        java.util.Map<String, short[]> savedPositions = world.loadPlayerPositions();
        short[] savedPos = savedPositions.get(player.getUsername());

        spawnX = world.getWidth() / 2.0 + 0.5;
        spawnZ = world.getDepth() / 2.0 + 0.5;

        if (savedPos != null && savedPos.length >= 5) {
            spawnX = savedPos[0] / 32.0;
            spawnY = savedPos[1] / 32.0;
            spawnZ = savedPos[2] / 32.0;
        }

        // Always validate spawn against terrain — recalculate if feet would be inside a solid block
        {
            int sx = (int) Math.floor(spawnX);
            int sz = (int) Math.floor(spawnZ);
            int feetBlock = (int) Math.floor(spawnY - PLAYER_EYE_HEIGHT);
            if (savedPos == null || savedPos.length < 5
                    || (feetBlock >= 0 && feetBlock < world.getHeight()
                        && world.getBlock(sx, feetBlock, sz) != 0)) {
                // Recalculate from terrain
                int feetY = 0;
                for (int y = world.getHeight() - 1; y >= 0; y--) {
                    if (world.getBlock(sx, y, sz) != 0) {
                        feetY = y + 1;
                        break;
                    }
                }
                spawnY = feetY + PLAYER_EYE_HEIGHT;
            }
        }

        player.updatePositionDouble(spawnX, spawnY, spawnZ, 0, 0);

        // Debug: print blocks at spawn column
        int sx = (int) Math.floor(spawnX);
        int sz = (int) Math.floor(spawnZ);
        StringBuilder blockDebug = new StringBuilder("[MCPE] Blocks at spawn column (" + sx + "," + sz + "):");
        for (int by = Math.max(0, (int)(spawnY - PLAYER_EYE_HEIGHT) - 3);
             by <= Math.min(world.getHeight() - 1, (int)(spawnY + 3)); by++) {
            blockDebug.append(" y=").append(by).append("=").append(world.getBlock(sx, by, sz));
        }
        System.out.println(blockDebug);
        System.out.println("[MCPE] spawnY(eye)=" + spawnY + " feetY=" + (spawnY - PLAYER_EYE_HEIGHT));

        // Send StartGame — v34: Y is eye-level; older: Y is feet-level
        boolean isV34 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;
        float startGameY = isV34 ? (float) spawnY : (float) (spawnY - PLAYER_EYE_HEIGHT);
        sendStartGame((float) spawnX, startGameY, (float) spawnZ);

        boolean isV17 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17;

        // Send SetTime + SetSpawnPosition before chunks (all versions)
        sendSetTime((int) (world.getWorldTime() % 24000));
        sendSetSpawnPosition((int) spawnX, (int) spawnZ, (int) (spawnY - PLAYER_EYE_HEIGHT));

        // v17+: send SetHealth before chunks (PocketMine does this)
        if (isV17) {
            MCPEPacketBuffer hp = new MCPEPacketBuffer();
            hp.writeByte(wireId(MCPEConstants.SET_HEALTH));
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
                hp.writeInt(20); // v27: int health
            } else {
                hp.writeByte(20); // v11-v20: byte health
            }
            server.sendGamePacket(session, hp.getBuf());
        }

        // v34+: send SetDifficulty before chunks (PocketMine does this)
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            MCPEPacketBuffer diff = new MCPEPacketBuffer();
            diff.writeByte(MCPEConstants.V34_SET_DIFFICULTY); // 0xC0
            diff.writeInt(1); // PEACEFUL=0, EASY=1, NORMAL=2, HARD=3
            server.sendGamePacket(session, diff.getBuf());
        }

        // Send initial chunks around spawn
        int spawnCX = (int) Math.floor(spawnX) >> 4;
        int spawnCZ = (int) Math.floor(spawnZ) >> 4;
        int viewRadius = 4; // Send 4-chunk radius initially
        int chunkCount = 0;
        for (int cx = spawnCX - viewRadius; cx <= spawnCX + viewRadius; cx++) {
            for (int cz = spawnCZ - viewRadius; cz <= spawnCZ + viewRadius; cz++) {
                sendChunkData(cx, cz);
                chunkCount++;
            }
        }
        System.out.println("[MCPE] Sent " + chunkCount + " chunks around (" + spawnCX + "," + spawnCZ + ")");

        // v17+: send RespawnPacket after chunks to trigger client terrain loading,
        // then wait for Ready packet before completing spawn (MovePlayer etc.)
        if (isV17) {
            double x = player.getDoubleX();
            double y = player.getDoubleY();
            double z = player.getDoubleZ();
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
                // v34: match PocketMine doFirstSpawn order:
                // AdventureSettings → SetTime → Respawn → PlayStatus(PLAYER_SPAWN)
                sendAdventureSettings(0x20 | 0x40); // v34: AUTO_JUMP(0x20) + ALLOW_FLIGHT(0x40). NOT 0x01 which is WORLD_IMMUTABLE!
                sendSetTime((int) (world.getWorldTime() % 24000)); // resend time
                MCPEPacketBuffer resp = new MCPEPacketBuffer();
                resp.writeByte(MCPEConstants.V34_RESPAWN); // 0xB3 direct wire ID
                resp.writeFloat((float) x);
                resp.writeFloat((float) y); // v34: eye-level Y
                resp.writeFloat((float) z);
                server.sendGamePacket(session, resp.getBuf());
                sendLoginStatus(3); // PLAYER_SPAWN
                // v34: PocketMine does NOT send MovePlayer in doFirstSpawn
                doSpawnV34();
                return; // skip doSpawn() below
            } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
                // v27: send PlayStatus(PLAYER_SPAWN=3) to trigger terrain building
                sendLoginStatus(3); // PLAYER_SPAWN
            } else {
                MCPEPacketBuffer resp = new MCPEPacketBuffer();
                resp.writeByte(wireId(MCPEConstants.RESPAWN));
                resp.writeInt(player.getPlayerId() + 1); // v17-v20: entityId (int)
                resp.writeFloat((float) x);
                resp.writeFloat((float) (y - PLAYER_EYE_HEIGHT));
                resp.writeFloat((float) z);
                server.sendGamePacket(session, resp.getBuf());
            }
            // v17+ client doesn't send Ready — it starts sending MovePlayer immediately
            doSpawn();
        }
    }

    private void handleReady(ChannelHandlerContext ctx, ByteBuf payload) {
        int status = payload.readUnsignedByte();
        System.out.println("[MCPE] Ready packet received: status=" + status
                + " (v" + session.getMcpeProtocolVersion() + ")");
        if (status == MCPEConstants.READY_SPAWN_REQUEST) {
            doSpawn();
        }
    }

    /** v34 spawn — AdventureSettings already sent; no MovePlayer (PocketMine doesn't send one). */
    private void doSpawnV34() {
        sendInventory();

        // Transition to gameplay handler
        MCPEGameplayHandler gameplayHandler = new MCPEGameplayHandler(
                session, world, playerManager, chunkManager, server,
                player, sessionWrapper, pongUpdater);
        session.setGameplayHandler(gameplayHandler);
        sessionWrapper.setGameplayHandler(gameplayHandler);

        // Send existing players
        for (ConnectedPlayer other : playerManager.getAllPlayers()) {
            if (other == player) continue;
            float ox = other.getX() / 32.0f;
            float oy = other.getY() / 32.0f - (float) PLAYER_EYE_HEIGHT;
            float oz = other.getZ() / 32.0f;
            int oeid = (other.getPlayerId() & 0xFF) + 1;
            float yaw = ((other.getYaw() + 128) & 0xFF) * 360.0f / 256.0f;
            float pitch = (other.getPitch() & 0xFF) * 360.0f / 256.0f;

            sendPlayerListAddForOther(other, oeid);

            MCPEPacketBuffer addPkt = new MCPEPacketBuffer();
            addPkt.writeByte(wireId(MCPEConstants.ADD_PLAYER));
            addPkt.writeLong(0);    // UUID most significant
            addPkt.writeLong(oeid); // UUID least significant
            addPkt.writeString(other.getUsername());
            addPkt.writeLong(oeid);
            addPkt.writeFloat(ox);
            addPkt.writeFloat(oy);
            addPkt.writeFloat(oz);
            addPkt.writeFloat(0); // speedX
            addPkt.writeFloat(0); // speedY
            addPkt.writeFloat(0); // speedZ
            addPkt.writeFloat(yaw);
            addPkt.writeFloat(yaw);  // headYaw
            addPkt.writeFloat(pitch);
            addPkt.writeShort(0); // held item ID (air)
            addPkt.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
            addPkt.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
            addPkt.writeMetaString(MCPEConstants.META_NAMETAG, other.getUsername());
            addPkt.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
            addPkt.writeMetaEnd();
            server.sendGamePacket(session, addPkt.getBuf());

            MCPEPacketBuffer meta = new MCPEPacketBuffer();
            meta.writeByte(wireId(MCPEConstants.SET_ENTITY_DATA));
            meta.writeLong(oeid);
            meta.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
            meta.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
            meta.writeMetaString(MCPEConstants.META_NAMETAG, other.getUsername());
            meta.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
            meta.writeMetaEnd();
            server.sendGamePacket(session, meta.getBuf());
        }

        playerManager.broadcastPlayerListAdd(player);
        playerManager.broadcastPlayerSpawn(player);
        playerManager.broadcastChat((byte) 0, player.getUsername() + " joined the game");

        com.github.martinambrus.rdforward.server.event.ServerEvents.PLAYER_JOIN
                .invoker().onPlayerJoin(player.getUsername(), ProtocolVersion.BEDROCK);
        pongUpdater.run();

        System.out.println("[MCPE] " + username + " spawned at "
                + String.format("%.1f, %.1f, %.1f",
                    player.getDoubleX(), player.getDoubleY(), player.getDoubleZ()));
    }

    /** Complete the spawn sequence — called from handleReady for all versions. */
    private void doSpawn() {
        boolean isV34 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;
        boolean isV27 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27;
        double x = player.getDoubleX();
        double y = player.getDoubleY(); // eye-level (internal)
        double z = player.getDoubleZ();

        // v27+ MovePlayer Y = eye-level; v11-v20 MovePlayer Y = feet-level
        float moveY = (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27)
                ? (float) y : (float) (y - PLAYER_EYE_HEIGHT);
        sendMovePlayer(player.getPlayerId() + 1, (float) x, moveY, (float) z, 0, 0, 2);
        // v27+: 0x01=WORLD_IMMUTABLE (bad!), 0x20=AUTO_JUMP, 0x40=ALLOW_FLIGHT
        // v11-v20: 0x01 was creative/fly flag (different meaning)
        int advFlags = (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27)
                ? 0x20 | 0x40  // AUTO_JUMP + ALLOW_FLIGHT
                : 0x01 | 0x40; // v11-v20 flags
        sendAdventureSettings(advFlags);
        sendInventory();

        // Transition to gameplay handler
        MCPEGameplayHandler gameplayHandler = new MCPEGameplayHandler(
                session, world, playerManager, chunkManager, server,
                player, sessionWrapper, pongUpdater);
        session.setGameplayHandler(gameplayHandler);
        sessionWrapper.setGameplayHandler(gameplayHandler);

        // Send existing players to MCPE client
        for (ConnectedPlayer other : playerManager.getAllPlayers()) {
            if (other == player) continue;
            float ox = other.getX() / 32.0f;
            float oy = other.getY() / 32.0f - (float) PLAYER_EYE_HEIGHT;
            float oz = other.getZ() / 32.0f;
            int oeid = (other.getPlayerId() & 0xFF) + 1;
            float yaw = ((other.getYaw() + 128) & 0xFF) * 360.0f / 256.0f;
            float pitch = (other.getPitch() & 0xFF) * 360.0f / 256.0f;

            // v34: send PlayerListAdd before AddPlayer (registers skin)
            if (isV34) {
                sendPlayerListAddForOther(other, oeid);
            }

            MCPEPacketBuffer addPkt = new MCPEPacketBuffer();
            addPkt.writeByte(wireId(MCPEConstants.ADD_PLAYER));
            if (isV34) {
                // v34: uuid(2 longs), username, entityId(long), x, y, z,
                //       speedX, speedY, speedZ, yaw, headYaw, pitch,
                //       slot(compound), metadata — skin moved to PlayerListPacket
                addPkt.writeLong(0);    // UUID most significant
                addPkt.writeLong(oeid); // UUID least significant
                addPkt.writeString(other.getUsername());
                addPkt.writeLong(oeid);
                addPkt.writeFloat(ox);
                addPkt.writeFloat(oy);
                addPkt.writeFloat(oz);
                addPkt.writeFloat(0); // speedX
                addPkt.writeFloat(0); // speedY
                addPkt.writeFloat(0); // speedZ
                addPkt.writeFloat(yaw);
                addPkt.writeFloat(yaw);  // headYaw
                addPkt.writeFloat(pitch);
                addPkt.writeShort(0); // held item ID (air = slot compound)
            } else if (isV27) {
                // v27: clientId(long), username, entityId(long), x, y, z,
                //       speedX, speedY, speedZ, yaw, headYaw, pitch,
                //       itemId(short), itemDamage(short), slim(byte), skin(string), metadata
                addPkt.writeLong(oeid);
                addPkt.writeString(other.getUsername());
                addPkt.writeLong(oeid);
                addPkt.writeFloat(ox);
                addPkt.writeFloat(oy);
                addPkt.writeFloat(oz);
                addPkt.writeFloat(0); // speedX
                addPkt.writeFloat(0); // speedY
                addPkt.writeFloat(0); // speedZ
                addPkt.writeFloat(yaw);
                addPkt.writeFloat(yaw);  // headYaw
                addPkt.writeFloat(pitch);
                addPkt.writeShort(0); // item ID (air)
                addPkt.writeShort(0); // item damage
                // Use other player's MCPE skin if available, else default Steve
                byte[] otherSkin = other.getMcpeSkinData();
                if (otherSkin != null && otherSkin.length > 0) {
                    addPkt.writeByte(other.getMcpeSkinSlim());
                    addPkt.writeShort(otherSkin.length);
                    addPkt.writeBytes(otherSkin);
                } else {
                    addPkt.writeByte(0); // slim (0 = steve)
                    addPkt.writeShort(MCPEConstants.DEFAULT_SKIN_64x64.length);
                    addPkt.writeBytes(MCPEConstants.DEFAULT_SKIN_64x64);
                }
            } else {
                // v11-v20: clientId(long), username, entityId(int), x, y, z,
                //          yaw(byte), pitch(byte), itemId(short), itemAux(short)
                addPkt.writeLong(oeid);
                addPkt.writeString(other.getUsername());
                addPkt.writeInt(oeid);
                addPkt.writeFloat(ox);
                addPkt.writeFloat(oy);
                addPkt.writeFloat(oz);
                addPkt.writeByte((other.getYaw() + 128) & 0xFF);
                addPkt.writeByte(other.getPitch());
                addPkt.writeShort(0);
                addPkt.writeShort(0);
            }
            addPkt.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
            addPkt.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
            addPkt.writeMetaString(MCPEConstants.META_NAMETAG, other.getUsername());
            addPkt.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
            addPkt.writeMetaEnd();
            server.sendGamePacket(session, addPkt.getBuf());

            MCPEPacketBuffer meta = new MCPEPacketBuffer();
            meta.writeByte(wireId(MCPEConstants.SET_ENTITY_DATA));
            if (isV27) {
                meta.writeLong(oeid); // v27+: long entity ID
            } else {
                meta.writeInt(oeid);
            }
            meta.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
            meta.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
            meta.writeMetaString(MCPEConstants.META_NAMETAG, other.getUsername());
            meta.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
            meta.writeMetaEnd();
            server.sendGamePacket(session, meta.getBuf());
        }

        playerManager.broadcastPlayerListAdd(player);
        playerManager.broadcastPlayerSpawn(player);
        playerManager.broadcastChat((byte) 0, player.getUsername() + " joined the game");

        com.github.martinambrus.rdforward.server.event.ServerEvents.PLAYER_JOIN
                .invoker().onPlayerJoin(player.getUsername(), ProtocolVersion.BEDROCK);
        pongUpdater.run();

        System.out.println("[MCPE] " + username + " spawned at "
                + String.format("%.1f, %.1f, %.1f", x, y, z));
    }

    // ========== Packet Builders ==========

    private void sendLoginStatus(int status) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(wireId(MCPEConstants.LOGIN_STATUS));
        pkt.writeInt(status);
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendStartGame(float x, float y, float z) {
        boolean isV34 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;
        boolean isV27 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27;
        boolean isV17 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17;

        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(wireId(MCPEConstants.START_GAME));
        pkt.writeInt(0);                          // seed
        if (isV34) {
            pkt.writeByte(0);                     // v34: dimension (byte, not int!)
        } else if (isV17) {
            pkt.writeInt(0);                      // v17-v27: dimension (int)
        }
        pkt.writeInt(MCPEConstants.GENERATOR_FLAT); // generator type
        if (!isV27 || isV34) {
            pkt.writeInt(MCPEConstants.GAMEMODE_CREATIVE); // gamemode (removed in v27, restored in v34)
        }
        if (isV27) {
            pkt.writeLong(player.getPlayerId() + 1); // entity ID (64-bit in v27+)
        } else {
            pkt.writeInt(player.getPlayerId() + 1);  // entity ID (32-bit)
        }
        if (isV27) {
            // v27+: 3D world spawn position (spawnX, spawnY, spawnZ)
            pkt.writeInt((int) x);
            pkt.writeInt((int) y);
            pkt.writeInt((int) z);
        } else if (isV17) {
            // v17-v20: 2D world spawn position (spawnX, spawnZ)
            pkt.writeInt((int) x);
            pkt.writeInt((int) z);
        }
        pkt.writeFloat(x);                        // player X
        pkt.writeFloat(y);                        // player Y
        pkt.writeFloat(z);                        // player Z
        if (isV34) {
            pkt.writeByte(0);                     // v34: terminator byte
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendSetTime(int time) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(wireId(MCPEConstants.SET_TIME));
        pkt.writeInt(time);
        // v34: flag is 1/0 (boolean), older: 0x80/0x00
        pkt.writeByte(session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34 ? 1 : 0x80);
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendSetSpawnPosition(int x, int z, int y) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(wireId(MCPEConstants.SET_SPAWN_POSITION));
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            // v27: int x, int y, int z (all ints)
            pkt.writeInt(x);
            pkt.writeInt(y);
            pkt.writeInt(z);
        } else {
            pkt.writeInt(x);
            pkt.writeInt(z);
            pkt.writeByte(y);
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendMovePlayer(int entityId, float x, float y, float z, float yaw, float pitch, int mode) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(wireId(MCPEConstants.MOVE_PLAYER));
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            pkt.writeLong(entityId);  // 64-bit entity ID
        } else {
            pkt.writeInt(entityId);
        }
        pkt.writeFloat(x);
        pkt.writeFloat(y);
        pkt.writeFloat(z);
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            pkt.writeFloat(yaw);   // yaw
            pkt.writeFloat(pitch); // pitch
            pkt.writeFloat(yaw);   // headYaw
            pkt.writeByte(mode);   // mode: 0=normal, 1=reset, 2=teleport
            pkt.writeByte(0);      // onGround
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
            pkt.writeFloat(yaw);   // bodyYaw
            pkt.writeFloat(pitch);
            pkt.writeFloat(yaw);   // headYaw
        } else {
            pkt.writeFloat(yaw);
            pkt.writeFloat(pitch);
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendAdventureSettings(int flags) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        int pv = session.getMcpeProtocolVersion();
        if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
            pkt.writeByte(MCPEConstants.toWireId(MCPEConstants.ADVENTURE_SETTINGS_V12, pv));
        } else if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_12) {
            pkt.writeByte(MCPEConstants.ADVENTURE_SETTINGS_V12);
        } else {
            pkt.writeByte(MCPEConstants.ADVENTURE_SETTINGS_V11);
        }
        pkt.writeInt(flags);
        server.sendGamePacket(session, pkt.getBuf());
    }

    /**
     * Send a v34 PlayerListPacket (TYPE_ADD) to register a player's skin.
     * Must be sent before AddPlayer so the client knows the skin.
     */
    private void sendPlayerListAdd(ConnectedPlayer p) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.V34_PLAYER_LIST); // 0xC3 (direct wire ID)
        pkt.writeByte(0); // TYPE_ADD
        pkt.writeInt(1);  // entry count
        // Entry: uuid(2 longs), entityId(long), playerName(string), slim(byte), skinData(putString)
        long eid = p.getPlayerId() + 1;
        pkt.writeLong(0);   // UUID most significant bits
        pkt.writeLong(eid); // UUID least significant bits
        pkt.writeLong(eid); // entityId
        pkt.writeString(p.getUsername());
        byte[] skinData = p.getMcpeSkinData();
        if (skinData == null || skinData.length == 0) {
            skinData = MCPEConstants.DEFAULT_SKIN_64x64;
        }
        pkt.writeByte(0); // slim = 0 (Steve model)
        pkt.writeShort(skinData.length);
        pkt.writeBytes(skinData);
        server.sendGamePacket(session, pkt.getBuf());
    }

    /** Send a v34 PlayerListAdd for another player (used in doSpawn loop). */
    private void sendPlayerListAddForOther(ConnectedPlayer p, int entityId) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.V34_PLAYER_LIST);
        pkt.writeByte(0); // TYPE_ADD
        pkt.writeInt(1);  // entry count
        pkt.writeLong(0);         // UUID most significant
        pkt.writeLong(entityId);  // UUID least significant
        pkt.writeLong(entityId);  // entityId
        pkt.writeString(p.getUsername());
        byte[] skinData = p.getMcpeSkinData();
        if (skinData == null || skinData.length == 0) {
            skinData = MCPEConstants.DEFAULT_SKIN_64x64;
        }
        pkt.writeByte(0); // slim = 0 (Steve model)
        pkt.writeShort(skinData.length);
        pkt.writeBytes(skinData);
        server.sendGamePacket(session, pkt.getBuf());
    }

    /**
     * Send a v34 UpdateAttributesPacket with health and movement speed.
     */
    private void sendUpdateAttributes(int entityId) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.V34_UPDATE_ATTRIBUTES); // 0xA6 (direct wire ID)
        pkt.writeLong(entityId);
        pkt.writeShort(2); // 2 attributes
        // Health
        pkt.writeFloat(0.0f);   // minValue
        pkt.writeFloat(20.0f);  // maxValue
        pkt.writeFloat(20.0f);  // value
        pkt.writeString("minecraft:health");
        // Movement speed
        pkt.writeFloat(0.0f);   // minValue
        pkt.writeFloat(0.3f);   // maxValue
        pkt.writeFloat(0.1f);   // value
        pkt.writeString("minecraft:movement");
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendInventory() {
        boolean isV27 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27;
        boolean isV34 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;

        // Send empty player inventory
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(wireId(MCPEConstants.SEND_INVENTORY));
        if (!isV27) {
            pkt.writeInt(player.getPlayerId() + 1); // v11-v20: entity ID
        }
        pkt.writeByte(0); // windowId = player inventory
        pkt.writeShort(36); // 36 slots
        for (int i = 0; i < 36; i++) {
            if (isV34) {
                pkt.writeShort(0); // v34: air = just itemId 0 (variable-length slot)
            } else {
                pkt.writeShort(0);  // itemId = air
                pkt.writeByte(0);   // count
                pkt.writeShort(0);  // damage/metadata
            }
        }
        if (isV27) {
            // v27+: hotbar slot indices for player inventory window
            pkt.writeShort(9);
            for (int i = 0; i < 9; i++) {
                pkt.writeInt(-1); // -1 = empty hotbar slot
            }
        }
        server.sendGamePacket(session, pkt.getBuf());

        // Send empty armor
        pkt = new MCPEPacketBuffer();
        pkt.writeByte(wireId(MCPEConstants.SEND_INVENTORY));
        if (!isV27) {
            pkt.writeInt(player.getPlayerId() + 1);
        }
        pkt.writeByte(isV27 ? 0x78 : 1); // v27: 0x78 (armor container), v11-v20: 1
        pkt.writeShort(4); // 4 armor slots
        for (int i = 0; i < 4; i++) {
            if (isV34) {
                pkt.writeShort(0); // v34: air = just itemId 0
            } else {
                pkt.writeShort(0);
                pkt.writeByte(0);
                pkt.writeShort(0);
            }
        }
        if (isV34) {
            pkt.writeShort(0); // v34: hotbar count = 0 for non-inventory windows
        }
        server.sendGamePacket(session, pkt.getBuf());

        // v34 creative mode: send creative inventory (windowId 0x79)
        // Without this, the client has no items to place
        if (isV34) {
            sendCreativeInventory();
        }
    }

    /**
     * Send the creative inventory (windowId 0x79) for v34 clients.
     * Contains all blocks available for creative mode placement.
     */
    private void sendCreativeInventory() {
        // Basic blocks available in MCPE 0.12.1 creative mode
        // Using standard MCPE block IDs (same as PC pre-1.13)
        int[][] creativeItems = {
            {1, 0},   // Stone
            {2, 0},   // Grass
            {3, 0},   // Dirt
            {4, 0},   // Cobblestone
            {5, 0},   // Oak Planks
            {6, 0},   // Sapling
            {7, 0},   // Bedrock
            {12, 0},  // Sand
            {13, 0},  // Gravel
            {14, 0},  // Gold Ore
            {15, 0},  // Iron Ore
            {16, 0},  // Coal Ore
            {17, 0},  // Oak Log
            {18, 0},  // Leaves
            {19, 0},  // Sponge
            {20, 0},  // Glass
            {21, 0},  // Lapis Ore
            {22, 0},  // Lapis Block
            {24, 0},  // Sandstone
            {35, 0},  // White Wool
            {35, 1},  // Orange Wool
            {35, 2},  // Magenta Wool
            {35, 3},  // Light Blue Wool
            {35, 4},  // Yellow Wool
            {35, 5},  // Lime Wool
            {35, 6},  // Pink Wool
            {35, 7},  // Gray Wool
            {35, 8},  // Light Gray Wool
            {35, 9},  // Cyan Wool
            {35, 10}, // Purple Wool
            {35, 11}, // Blue Wool
            {35, 12}, // Brown Wool
            {35, 13}, // Green Wool
            {35, 14}, // Red Wool
            {35, 15}, // Black Wool
            {41, 0},  // Gold Block
            {42, 0},  // Iron Block
            {43, 0},  // Double Slab
            {44, 0},  // Slab
            {45, 0},  // Bricks
            {46, 0},  // TNT
            {47, 0},  // Bookshelf
            {48, 0},  // Mossy Cobblestone
            {49, 0},  // Obsidian
            {50, 0},  // Torch
            {53, 0},  // Oak Stairs
            {54, 0},  // Chest
            {56, 0},  // Diamond Ore
            {57, 0},  // Diamond Block
            {58, 0},  // Crafting Table
            {61, 0},  // Furnace
            {65, 0},  // Ladder
            {66, 0},  // Rail
            {67, 0},  // Cobblestone Stairs
            {79, 0},  // Ice
            {80, 0},  // Snow Block
            {81, 0},  // Cactus
            {82, 0},  // Clay
            {85, 0},  // Fence
            {86, 0},  // Pumpkin
            {87, 0},  // Netherrack
            {88, 0},  // Soul Sand
            {89, 0},  // Glowstone
            {91, 0},  // Jack o'Lantern
        };

        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(wireId(MCPEConstants.CONTAINER_SET_CONTENT));
        pkt.writeByte(0x79); // SPECIAL_CREATIVE windowId
        pkt.writeShort(creativeItems.length);
        for (int[] item : creativeItems) {
            pkt.writeShort(item[0]); // itemId
            pkt.writeByte(1);        // count
            pkt.writeShort(item[1]); // damage/meta
            pkt.writeShort(0);       // nbtLen = 0
        }
        pkt.writeShort(0); // v34: hotbar count = 0 for non-inventory windows
        server.sendGamePacket(session, pkt.getBuf());
    }

    /**
     * Convert a world column to MCPE 0.7.0 chunk format and send it.
     * PocketMine Alpha_1.3 sends one Y-section per packet (getOrderedMiniChunk).
     * Each packet: chunkX, chunkZ, then 256 columns of (flag + 16 blockIDs + 8 meta).
     * Flag = (1 << Y) indicating which section this packet represents.
     */
    private void sendChunkData(int chunkX, int chunkZ) {
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            sendFullChunkDataV27(chunkX, chunkZ);
            return;
        }
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17) {
            sendFullChunkDataV17(chunkX, chunkZ);
            return;
        }

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        // Send one packet per Y-section (0-7), matching PocketMine's approach
        for (int section = 0; section < 8; section++) {
            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(wireId(MCPEConstants.CHUNK_DATA));
            pkt.writeInt(chunkX);
            pkt.writeInt(chunkZ);

            int flag = 1 << section;
            int sectionBaseY = section * 16;

            // 256 columns: X inner (varies fastest), Z outer
            for (int j = 0; j < 256; j++) {
                int localX = j & 0x0F;
                int localZ = (j >> 4) & 0x0F;
                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;

                pkt.writeByte(flag);

                // 16 block IDs for this column in this section
                for (int localY = 0; localY < 16; localY++) {
                    int worldY = sectionBaseY + localY;
                    if (worldX >= 0 && worldX < world.getWidth()
                            && worldZ >= 0 && worldZ < world.getDepth()
                            && worldY < world.getHeight()) {
                        pkt.writeByte(mapBlockId(world.getBlock(worldX, worldY, worldZ)));
                    } else {
                        pkt.writeByte(0); // air
                    }
                }

                // 8 metadata nibble bytes (all zero for basic blocks)
                for (int i = 0; i < 8; i++) {
                    pkt.writeByte(0);
                }
            }

            server.sendGamePacket(session, pkt.getBuf());
        }
    }

    /**
     * Send a v17 (0.9.0) FullChunkDataPacket (0xBA).
     * Full column format: block IDs (32768) + metadata (16384) + skylight (16384)
     * + blocklight (16384) + heightmap (256) + biome IDs (256) + biome colors (1024).
     * All zlib compressed. Block order: x*2048 + z*128 + y (XZY).
     */
    private void sendFullChunkDataV17(int chunkX, int chunkZ) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int height = 128;

        // Block IDs: 16*16*128 = 32768 bytes, XZY order
        byte[] blockIds = new byte[32768];
        byte[] heightMap = new byte[256];

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                int topY = 0;

                for (int y = 0; y < height; y++) {
                    int idx = (x << 11) | (z << 7) | y;
                    if (worldX >= 0 && worldX < world.getWidth()
                            && worldZ >= 0 && worldZ < world.getDepth()
                            && y < world.getHeight()) {
                        int block = mapBlockId(world.getBlock(worldX, y, worldZ));
                        blockIds[idx] = (byte) block;
                        if (block != 0) topY = y + 1;
                    }
                }
                heightMap[(z << 4) | x] = (byte) topY;
            }
        }

        // Metadata nibbles: 16384 bytes (all zero for basic blocks)
        byte[] metadata = new byte[16384];

        // Sky light: 16384 nibble bytes — compute from height map
        byte[] skyLight = new byte[16384];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = heightMap[(z << 4) | x] & 0xFF;
                for (int y = 0; y < height; y++) {
                    int idx = (x << 11) | (z << 7) | y;
                    int nibbleIdx = idx >> 1;
                    int light = (y >= top) ? 15 : 0;
                    if ((idx & 1) == 0) {
                        skyLight[nibbleIdx] |= (byte) (light & 0x0F);
                    } else {
                        skyLight[nibbleIdx] |= (byte) ((light << 4) & 0xF0);
                    }
                }
            }
        }

        // Block light: 16384 bytes (all zero)
        byte[] blockLight = new byte[16384];

        // Biome IDs: 256 bytes (all plains = 1)
        byte[] biomeIds = new byte[256];
        java.util.Arrays.fill(biomeIds, (byte) 1);

        // Biome colors: 256 * 4 = 1024 bytes (grass green ARGB)
        byte[] biomeColors = new byte[1024];
        for (int i = 0; i < 256; i++) {
            int offset = i * 4;
            // Grass green: R=0x7A, G=0xBD, B=0x6B, A=0x00
            biomeColors[offset] = 0x01;     // flag (non-zero = has color)
            biomeColors[offset + 1] = 0x7A; // R
            biomeColors[offset + 2] = (byte) 0xBD; // G
            biomeColors[offset + 3] = 0x6B; // B
        }

        // Assemble uncompressed payload: chunkX + chunkZ + terrain data (all inside compressed blob).
        // Binary analysis of FullChunkDataPacket::read confirms: client decompresses first,
        // then reads chunkX/chunkZ from the decompressed stream.
        // Terrain format (from deserializeTerrain): blockIDs(32768) + metadata(16384)
        // + skylight(16384) + blocklight(16384) + biomeIDs(256) + biomeColors(1024).
        int totalSize = 4 + 4 + blockIds.length + metadata.length + skyLight.length
                + blockLight.length + biomeIds.length + biomeColors.length;
        byte[] uncompressed = new byte[totalSize];
        int pos = 0;
        // chunkX and chunkZ (Little-Endian ints) inside the compressed payload
        // x86 client reads these via memcpy + native mov (LE)
        uncompressed[pos++] = (byte) chunkX;
        uncompressed[pos++] = (byte) (chunkX >> 8);
        uncompressed[pos++] = (byte) (chunkX >> 16);
        uncompressed[pos++] = (byte) (chunkX >> 24);
        uncompressed[pos++] = (byte) chunkZ;
        uncompressed[pos++] = (byte) (chunkZ >> 8);
        uncompressed[pos++] = (byte) (chunkZ >> 16);
        uncompressed[pos++] = (byte) (chunkZ >> 24);
        System.arraycopy(blockIds, 0, uncompressed, pos, blockIds.length); pos += blockIds.length;
        System.arraycopy(metadata, 0, uncompressed, pos, metadata.length); pos += metadata.length;
        System.arraycopy(skyLight, 0, uncompressed, pos, skyLight.length); pos += skyLight.length;
        System.arraycopy(blockLight, 0, uncompressed, pos, blockLight.length); pos += blockLight.length;
        System.arraycopy(biomeIds, 0, uncompressed, pos, biomeIds.length); pos += biomeIds.length;
        System.arraycopy(biomeColors, 0, uncompressed, pos, biomeColors.length);

        // Zlib compress the entire payload
        byte[] compressed;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DeflaterOutputStream dos = new DeflaterOutputStream(baos,
                    new Deflater(Deflater.DEFAULT_COMPRESSION));
            dos.write(uncompressed);
            dos.finish();
            dos.close();
            compressed = baos.toByteArray();
        } catch (java.io.IOException e) {
            throw new RuntimeException("zlib compress failed", e);
        }

        // Send FullChunkDataPacket: 0xBA + compressed(chunkX + chunkZ + terrainData)
        // Note: FULL_CHUNK_DATA_V17 is already a v17 wire ID, not v12 canonical — don't use wireId()
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.FULL_CHUNK_DATA_V17);
        pkt.writeBytes(compressed);
        server.sendGamePacket(session, pkt.getBuf());
    }

    /**
     * Send a v27 (0.11.0) FullChunkDataPacket using v27 packet ID 0x2E.
     * PocketMine format: [0x2E][chunkX BE][chunkZ BE][dataLen BE][raw terrain].
     * Terrain: blockIds + metadata + skyLight + blockLight + heightMap + biomeColors + extraData.
     * Batch-wrapped by sendGamePacket().
     */
    private void sendFullChunkDataV27(int chunkX, int chunkZ) {
        byte[] terrain = buildV27Terrain(chunkX, chunkZ);

        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            pkt.writeByte(MCPEConstants.V34_FULL_CHUNK_DATA); // v34: 0xBF (direct wire ID)
            pkt.writeInt(chunkX);
            pkt.writeInt(chunkZ);
            pkt.writeByte(0); // v34: order byte (0 = ORDER_COLUMNS)
            pkt.writeInt(terrain.length);
            pkt.writeBytes(terrain);
        } else {
            pkt.writeByte(0xAF); // v27 FullChunkData wire ID: (0x2E + 0x81) & 0xFF
            pkt.writeInt(chunkX);
            pkt.writeInt(chunkZ);
            pkt.writeInt(terrain.length);
            pkt.writeBytes(terrain);
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    /** Build raw terrain for v27/v34 format: blockIds + meta + skyLight + blockLight + heightMap + biomeColors [+ v34 extraData]. */
    private byte[] buildV27Terrain(int chunkX, int chunkZ) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int height = 128;

        byte[] blockIds = new byte[32768];
        byte[] heightMap = new byte[256];

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                int topY = 0;
                for (int y = 0; y < height; y++) {
                    int idx = (x << 11) | (z << 7) | y;
                    if (worldX >= 0 && worldX < world.getWidth()
                            && worldZ >= 0 && worldZ < world.getDepth()
                            && y < world.getHeight()) {
                        int block = mapBlockId(world.getBlock(worldX, y, worldZ));
                        blockIds[idx] = (byte) block;
                        if (block != 0) topY = y + 1;
                    }
                }
                heightMap[(z << 4) | x] = (byte) topY;
            }
        }

        byte[] metadata = new byte[16384];
        byte[] skyLight = new byte[16384];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = heightMap[(z << 4) | x] & 0xFF;
                for (int y = 0; y < height; y++) {
                    int idx = (x << 11) | (z << 7) | y;
                    int nibbleIdx = idx >> 1;
                    int light = (y >= top) ? 15 : 0;
                    if ((idx & 1) == 0) {
                        skyLight[nibbleIdx] |= (byte) (light & 0x0F);
                    } else {
                        skyLight[nibbleIdx] |= (byte) ((light << 4) & 0xF0);
                    }
                }
            }
        }

        byte[] blockLight = new byte[16384];
        byte[] biomeColors = new byte[1024];
        for (int i = 0; i < 256; i++) {
            int offset = i * 4;
            biomeColors[offset] = 0x01;
            biomeColors[offset + 1] = 0x7A;
            biomeColors[offset + 2] = (byte) 0xBD;
            biomeColors[offset + 3] = 0x6B;
        }

        // v34: extra data count (int 0 = no extra data) appended after biomeColors
        boolean isV34 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;
        int extraDataSize = isV34 ? 4 : 0;

        int terrainSize = blockIds.length + metadata.length + skyLight.length
                + blockLight.length + heightMap.length + biomeColors.length + extraDataSize;
        byte[] terrain = new byte[terrainSize];
        int pos = 0;
        System.arraycopy(blockIds, 0, terrain, pos, blockIds.length); pos += blockIds.length;
        System.arraycopy(metadata, 0, terrain, pos, metadata.length); pos += metadata.length;
        System.arraycopy(skyLight, 0, terrain, pos, skyLight.length); pos += skyLight.length;
        System.arraycopy(blockLight, 0, terrain, pos, blockLight.length); pos += blockLight.length;
        System.arraycopy(heightMap, 0, terrain, pos, heightMap.length); pos += heightMap.length;
        System.arraycopy(biomeColors, 0, terrain, pos, biomeColors.length); pos += biomeColors.length;
        // v34: write extraData count = 0 (big-endian int, already zeroed in array)
        return terrain;
    }

    /** Build raw terrain byte array for a chunk (uncompressed). */
    private byte[] buildRawTerrain(int chunkX, int chunkZ) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int height = 128;

        byte[] blockIds = new byte[32768];
        byte[] heightMap = new byte[256];

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                int topY = 0;
                for (int y = 0; y < height; y++) {
                    int idx = (x << 11) | (z << 7) | y;
                    if (worldX >= 0 && worldX < world.getWidth()
                            && worldZ >= 0 && worldZ < world.getDepth()
                            && y < world.getHeight()) {
                        int block = mapBlockId(world.getBlock(worldX, y, worldZ));
                        blockIds[idx] = (byte) block;
                        if (block != 0) topY = y + 1;
                    }
                }
                heightMap[(z << 4) | x] = (byte) topY;
            }
        }

        byte[] metadata = new byte[16384];

        byte[] skyLight = new byte[16384];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = heightMap[(z << 4) | x] & 0xFF;
                for (int y = 0; y < height; y++) {
                    int idx = (x << 11) | (z << 7) | y;
                    int nibbleIdx = idx >> 1;
                    int light = (y >= top) ? 15 : 0;
                    if ((idx & 1) == 0) {
                        skyLight[nibbleIdx] |= (byte) (light & 0x0F);
                    } else {
                        skyLight[nibbleIdx] |= (byte) ((light << 4) & 0xF0);
                    }
                }
            }
        }

        byte[] blockLight = new byte[16384];
        byte[] biomeIds = new byte[256];
        java.util.Arrays.fill(biomeIds, (byte) 1);

        byte[] biomeColors = new byte[1024];
        for (int i = 0; i < 256; i++) {
            int offset = i * 4;
            biomeColors[offset] = 0x01;
            biomeColors[offset + 1] = 0x7A;
            biomeColors[offset + 2] = (byte) 0xBD;
            biomeColors[offset + 3] = 0x6B;
        }

        int totalSize = blockIds.length + metadata.length + skyLight.length
                + blockLight.length + biomeIds.length + biomeColors.length;
        byte[] terrain = new byte[totalSize];
        int pos = 0;
        System.arraycopy(blockIds, 0, terrain, pos, blockIds.length); pos += blockIds.length;
        System.arraycopy(metadata, 0, terrain, pos, metadata.length); pos += metadata.length;
        System.arraycopy(skyLight, 0, terrain, pos, skyLight.length); pos += skyLight.length;
        System.arraycopy(blockLight, 0, terrain, pos, blockLight.length); pos += blockLight.length;
        System.arraycopy(biomeIds, 0, terrain, pos, biomeIds.length); pos += biomeIds.length;
        System.arraycopy(biomeColors, 0, terrain, pos, biomeColors.length);
        return terrain;
    }

    /** Build zlib-compressed terrain data for a chunk (used by v17). */
    private byte[] buildCompressedTerrain(int chunkX, int chunkZ) {
        byte[] raw = buildRawTerrain(chunkX, chunkZ);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DeflaterOutputStream dos = new DeflaterOutputStream(baos,
                    new Deflater(Deflater.DEFAULT_COMPRESSION));
            dos.write(raw);
            dos.finish();
            dos.close();
            return baos.toByteArray();
        } catch (java.io.IOException e) {
            throw new RuntimeException("zlib compress failed", e);
        }
    }

    /**
     * Map internal block IDs to MCPE 0.7.0 block IDs.
     * For basic blocks, the IDs are the same as Java Classic/Alpha.
     */
    private int mapBlockId(int internalId) {
        // MCPE 0.7.0 uses mostly standard Minecraft block IDs
        // Basic blocks (0-49) are identical
        if (internalId >= 0 && internalId <= 49) {
            return internalId;
        }
        // Unknown blocks -> stone
        return 1;
    }

    public ConnectedPlayer getPlayer() { return player; }
    public MCPESessionWrapper getSessionWrapper() { return sessionWrapper; }
}
