package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.server.api.ServerProperties;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.zip.InflaterInputStream;

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
    private final LegacyRakNetServer server;
    private final Runnable pongUpdater;

    private String username;
    private int clientSlim;
    private byte[] clientSkinData;
    private String clientSkinId = "";
    private ConnectedPlayer player;
    private MCPESessionWrapper sessionWrapper;
    private MCPEPacketCodec codec;

    public MCPELoginHandler(LegacyRakNetSession session, ServerWorld world,
                            PlayerManager playerManager,
                            LegacyRakNetServer server, Runnable pongUpdater) {
        this.session = session;
        this.world = world;
        this.playerManager = playerManager;
        this.server = server;
        this.pongUpdater = pongUpdater;
    }

    public void handlePacket(ChannelHandlerContext ctx, int packetId, ByteBuf payload) {
        // Accept LOGIN from all versions: v11-v20 (0x82), v27 (0x01), v34 (0x8F), v81 (0x01)
        if (packetId == (MCPEConstants.LOGIN & 0xFF)
                || packetId == (MCPEConstants.V27_LOGIN & 0xFF)
                || packetId == (MCPEConstants.V34_LOGIN & 0xFF)
                || packetId == (MCPEConstants.V81_LOGIN & 0xFF)) {
            handleLogin(ctx, payload);
        } else if (packetId == (MCPEConstants.READY & 0xFF)) {
            handleReady(ctx, payload);
        } else if (packetId == (MCPEConstants.REQUEST_CHUNK & 0xFF)) {
            // v9/v11 clients request chunks during login phase (before READY)
            handleRequestChunkDuringLogin(payload);
        } else if (packetId == (MCPEConstants.V91_RESOURCE_PACK_CLIENT_RESPONSE & 0xFF)
                && session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            // v91: client sends ResourcePackClientResponse — ignore it (server doesn't wait)
            System.out.println("[MCPE Login] ResourcePackClientResponse received (ignored)");
        } else {
            System.out.println("[MCPE Login] Unhandled packet 0x"
                    + Integer.toHexString(packetId) + " (" + payload.readableBytes() + " bytes)"
                    + " (v" + session.getMcpeProtocolVersion() + ")");
        }
    }

    private void handleLogin(ChannelHandlerContext ctx, ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);

        // v81 (0.15.0): JWT-based login format.
        // Wire format: [int protocol][int compressedLen][zlib(LInt chainLen + JSON chain + LInt skinTokenLen + JWT skin)]
        // We detect v81 by peeking: v81 sends a single int protocol (81+), while older versions
        // send string username first (which starts with a short length prefix, never looking like 81).
        // Actually, the simplest detection: the first field in pre-v81 is a String (2-byte length prefix).
        // In v81, it's an int (4 bytes). Since username strings are short, the first 2 bytes will be
        // a small number. But for v81, the int 81 = 0x00000051. The first 2 bytes are 0x0000 which
        // would mean an empty string in older format — usernames are never empty.
        // So: peek at first 4 bytes as int. If it's a known v81+ protocol number, use JWT path.
        int savedReaderIdx = buf.readerIndex();
        int firstInt = buf.readInt();
        if (firstInt >= 81 && firstInt <= 200) {
            // v81+ JWT login
            handleLoginV81(ctx, buf, firstInt);
            return;
        }
        // Not v81 — rewind and parse as older format (string username first)
        buf.readerIndex(savedReaderIdx);

        username = buf.readString();
        int protocol1 = buf.readInt();
        int protocol2 = buf.readInt();

        // v34+: clientId is long (was int). Also has UUID, serverAddress, clientSecret, skin.
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
            // v38+ (0.13.1+): skinName(string) replaced skinTransparent(byte)+slim(byte)
            // v34: skinTransparent(byte), slim(byte), skin(short+bytes)
            if (buf.readableBytes() > 0) {
                if (protocol1 >= MCPEConstants.MCPE_PROTOCOL_VERSION_38) {
                    String skinName = buf.readString(); // skinName (e.g. "Standard_Steve")
                    clientSlim = skinName.toLowerCase().contains("slim") ? 1 : 0;
                } else {
                    buf.readUnsignedByte(); // skinTransparent
                    clientSlim = buf.readUnsignedByte();
                }
                if (buf.readableBytes() >= 2) {
                    int skinLen = buf.readUnsignedShort();
                    if (skinLen > 0 && skinLen <= buf.readableBytes()) {
                        clientSkinData = new byte[skinLen];
                        buf.getBuf().readBytes(clientSkinData);
                        System.out.println("[MCPE] Client skin: " + skinLen + " bytes, slim=" + clientSlim);
                    }
                }
            }
        } else if (protocol1 >= MCPEConstants.MCPE_PROTOCOL_VERSION_11) {
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
        // v9: login has no clientId and no skin data

        System.out.println("[MCPE] Login from " + username
                + " (protocol=" + protocol1 + ")");

        // Accept known protocol versions only (skip dev-only 13, 15-16, 19, 21-26, 28-33, 39-44)
        boolean validProtocol = protocol1 == 9
                || protocol1 == 11 || protocol1 == 12 || protocol1 == 14
                || protocol1 == 17 || protocol1 == 18 || protocol1 == 20
                || protocol1 == 27 || protocol1 == 34 || protocol1 == 38
                || protocol1 == 45;
        if (!validProtocol) {
            int status = (protocol1 > MCPEConstants.MCPE_PROTOCOL_VERSION_MAX)
                    ? MCPEConstants.LOGIN_SERVER_OUTDATED
                    : MCPEConstants.LOGIN_CLIENT_OUTDATED;
            sendLoginStatus(status);
            return;
        }
        session.setMcpeProtocolVersion(protocol1);
        codec = MCPEPacketCodec.forVersion(protocol1);
        continueAfterLogin(ctx);
    }

    /** Common post-login sequence shared by all protocol versions. */
    private void continueAfterLogin(ChannelHandlerContext ctx) {
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

        // v91+: send ResourcePacksInfo after login success (client expects this before StartGame)
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            sendResourcePacksInfo();
        }

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

        // Send StartGame — v34: Y is eye-level; older: Y is feet-level
        boolean isV34 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;
        float startGameY = isV34 ? (float) spawnY : (float) (spawnY - PLAYER_EYE_HEIGHT);
        sendStartGame((float) spawnX, startGameY, (float) spawnZ);

        boolean isV17 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17;

        // Send SetTime + SetSpawnPosition before chunks (all versions)
        sendSetTime((int) (world.getWorldTime() % 24000));
        sendSetSpawnPosition((int) spawnX, (int) spawnZ, (int) (spawnY - PLAYER_EYE_HEIGHT));

        // v91: send UpdateAttributes right after SetTime, before chunks (Genisys order)
        // Must use same entityId as StartGame's entityRuntimeId for client to recognize as "self"
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            sendUpdateAttributes(player.getPlayerId() + 1);
        }

        // v17+: send SetHealth before chunks (PocketMine does this)
        if (isV17) {
            MCPEPacketBuffer hp = new MCPEPacketBuffer();
            hp.writeByte(codec.wireId(MCPEConstants.SET_HEALTH));
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
                hp.writeSignedVarInt(20); // v91: signed VarInt
            } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
                hp.writeInt(20); // v27: int health
            } else {
                hp.writeByte(20); // v11-v20: byte health
            }
            server.sendGamePacket(session, hp.getBuf());
        }

        // v34+: send SetDifficulty before chunks (PocketMine does this)
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            MCPEPacketBuffer diff = new MCPEPacketBuffer();
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
                diff.writeByte(MCPEConstants.V91_SET_DIFFICULTY & 0xFF);
                diff.writeUnsignedVarInt(ServerProperties.getDifficulty()); // v91 uses UnsignedVarInt
            } else {
                int diffId = (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_81)
                        ? (MCPEConstants.V81_SET_DIFFICULTY & 0xFF)
                        : (MCPEConstants.V34_SET_DIFFICULTY & 0xFF);
                diff.writeByte(diffId);
                diff.writeInt(ServerProperties.getDifficulty()); // PEACEFUL=0, EASY=1, NORMAL=2, HARD=3
            }
            server.sendGamePacket(session, diff.getBuf());
        }

        // Send initial chunks around spawn.
        // v9/v11 clients request chunks on demand via REQUEST_CHUNK — don't flood them.
        // v17+ clients need proactive chunk sending (they don't request during login).
        int spawnCX = (int) Math.floor(spawnX) >> 4;
        int spawnCZ = (int) Math.floor(spawnZ) >> 4;
        int viewRadius = 4; // Send 4-chunk radius initially
        int chunkCount = 0;
        if (isV17) {
            for (int cx = spawnCX - viewRadius; cx <= spawnCX + viewRadius; cx++) {
                for (int cz = spawnCZ - viewRadius; cz <= spawnCZ + viewRadius; cz++) {
                    sendChunkData(cx, cz);
                    chunkCount++;
                }
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
                // v34+: match PocketMine doFirstSpawn order:
                // AdventureSettings → SetTime → Respawn → PlayStatus(PLAYER_SPAWN)
                // Genisys v91: autoJump=0x20, allowFlight=0x40, noClip=0x80
                // v81 PocketMine: AUTO_JUMP=0x40, ALLOW_FLIGHT=0x80
                // v34-v45: AUTO_JUMP=0x20, ALLOW_FLIGHT=0x40
                int advSettingsFlags;
                if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
                    advSettingsFlags = 0x20 | 0x40; // autoJump + allowFlight (v91 flag positions)
                } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_81) {
                    advSettingsFlags = 0x40 | 0x80; // autoJump + allowFlight (v81 flag positions)
                } else {
                    advSettingsFlags = 0x20 | 0x40;
                }
                sendAdventureSettings(advSettingsFlags);
                sendSetTime((int) (world.getWorldTime() % 24000)); // resend time
                MCPEPacketBuffer resp = new MCPEPacketBuffer();
                resp.writeByte(codec.wireId(MCPEConstants.RESPAWN));
                if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
                    resp.writeLFloat((float) x);
                    resp.writeLFloat((float) y);
                    resp.writeLFloat((float) z);
                } else {
                    resp.writeFloat((float) x);
                    resp.writeFloat((float) y); // v34+: eye-level Y
                    resp.writeFloat((float) z);
                }
                server.sendGamePacket(session, resp.getBuf());
                sendLoginStatus(3); // PLAYER_SPAWN
                // v34+: PocketMine does NOT send MovePlayer in doFirstSpawn
                doSpawnV34();
                return; // skip doSpawn() below
            } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
                // v27: send PlayStatus(PLAYER_SPAWN=3) to trigger terrain building
                sendLoginStatus(3); // PLAYER_SPAWN
            } else {
                MCPEPacketBuffer resp = new MCPEPacketBuffer();
                resp.writeByte(codec.wireId(MCPEConstants.RESPAWN));
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

    /**
     * Handle v81 (0.15.0) JWT-based login.
     * Wire format after packet ID: [int protocol][int compressedLen][zlib payload]
     * Decompressed payload: [LInt chainLen][chain JSON][LInt skinTokenLen][skin JWT]
     * Chain JSON: {"chain":["jwt1","jwt2",...]} — extract displayName + identity from extraData.
     * Skin JWT payload: contains SkinId, SkinData (base64), ClientRandomId.
     */
    private void handleLoginV81(ChannelHandlerContext ctx, MCPEPacketBuffer buf, int protocol) {
        System.out.println("[MCPE] v81 JWT login, protocol=" + protocol);

        // Validate protocol
        if (protocol != 81 && protocol != 91) {
            int status = (protocol > MCPEConstants.MCPE_PROTOCOL_VERSION_MAX)
                    ? MCPEConstants.LOGIN_SERVER_OUTDATED
                    : MCPEConstants.LOGIN_CLIENT_OUTDATED;
            session.setMcpeProtocolVersion(protocol);
            codec = MCPEPacketCodec.forVersion(protocol);
            sendLoginStatus(status);
            return;
        }
        session.setMcpeProtocolVersion(protocol);
        codec = MCPEPacketCodec.forVersion(protocol);

        // v91+: gameEdition byte after protocol int
        if (protocol >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            buf.readUnsignedByte(); // gameEdition (0 = Pocket Edition)
        }

        // Read compressed payload: v91 uses UnsignedVarInt length, v81 uses int
        int compressedLen;
        if (protocol >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            compressedLen = buf.readUnsignedVarInt();
        } else {
            compressedLen = buf.readInt();
        }
        byte[] compressed = new byte[Math.min(compressedLen, buf.readableBytes())];
        buf.getBuf().readBytes(compressed);

        // Decompress
        byte[] decompressed;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
            InflaterInputStream iis = new InflaterInputStream(bais);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] tmp = new byte[4096];
            int read;
            while ((read = iis.read(tmp)) != -1) {
                baos.write(tmp, 0, read);
            }
            decompressed = baos.toByteArray();
        } catch (Exception e) {
            System.err.println("[MCPE] v81 login decompress failed: " + e.getMessage());
            sendLoginStatus(MCPEConstants.LOGIN_CLIENT_OUTDATED);
            return;
        }

        ByteBuffer bb = ByteBuffer.wrap(decompressed).order(ByteOrder.LITTLE_ENDIAN);

        // Read chain data: LInt length + JSON string
        int chainLen = bb.getInt();
        byte[] chainBytes = new byte[chainLen];
        bb.get(chainBytes);
        String chainJson = new String(chainBytes, java.nio.charset.StandardCharsets.UTF_8);

        // Parse chain JSON to extract displayName and identity UUID
        // Format: {"chain":["jwt1","jwt2",...]}
        // Each JWT is header.payload.signature, we need to decode the payload (middle part)
        username = "Player";
        String skinId = "";
        try {
            // Extract the chain array from JSON (simple parsing without a JSON library)
            int chainStart = chainJson.indexOf("[");
            int chainEnd = chainJson.lastIndexOf("]");
            if (chainStart >= 0 && chainEnd > chainStart) {
                String chainArray = chainJson.substring(chainStart + 1, chainEnd);
                // Split by comma (JWTs don't contain commas outside of quoted strings)
                String[] jwts = splitJwtArray(chainArray);
                for (String jwt : jwts) {
                    jwt = jwt.trim();
                    if (jwt.startsWith("\"")) jwt = jwt.substring(1);
                    if (jwt.endsWith("\"")) jwt = jwt.substring(0, jwt.length() - 1);
                    // Decode JWT payload (second part)
                    String[] parts = jwt.split("\\.");
                    if (parts.length >= 2) {
                        String payloadJson = new String(
                                Base64.getUrlDecoder().decode(padBase64(parts[1])),
                                java.nio.charset.StandardCharsets.UTF_8);
                        // Look for extraData containing displayName
                        String displayName = extractJsonString(payloadJson, "displayName");
                        if (displayName != null && !displayName.isEmpty()) {
                            username = displayName;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[MCPE] v81 chain parse error: " + e.getMessage());
        }

        // Read skin token: LInt length + JWT string
        if (bb.remaining() >= 4) {
            int skinTokenLen = bb.getInt();
            if (skinTokenLen > 0 && skinTokenLen <= bb.remaining()) {
                byte[] skinTokenBytes = new byte[skinTokenLen];
                bb.get(skinTokenBytes);
                String skinJwt = new String(skinTokenBytes, java.nio.charset.StandardCharsets.UTF_8);
                try {
                    String[] parts = skinJwt.split("\\.");
                    if (parts.length >= 2) {
                        String payloadJson = new String(
                                Base64.getUrlDecoder().decode(padBase64(parts[1])),
                                java.nio.charset.StandardCharsets.UTF_8);
                        // Extract SkinId and SkinData (base64-encoded RGBA)
                        skinId = extractJsonString(payloadJson, "SkinId");
                        if (skinId == null) skinId = "";
                        clientSlim = skinId.toLowerCase().contains("slim") ? 1 : 0;

                        String skinDataB64 = extractJsonString(payloadJson, "SkinData");
                        if (skinDataB64 != null && !skinDataB64.isEmpty()) {
                            clientSkinData = Base64.getDecoder().decode(skinDataB64);
                            System.out.println("[MCPE] v81 skin: " + clientSkinData.length
                                    + " bytes, skinId=" + skinId);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[MCPE] v81 skin parse error: " + e.getMessage());
                }
            }
        }

        // Store skinId for v81 PlayerList (which uses string-based skin format)
        clientSkinId = skinId;

        System.out.println("[MCPE] v81 Login from " + username + " (protocol=" + protocol + ")");

        // Continue with common post-login logic (same as older versions from this point)
        continueAfterLogin(ctx);
    }

    /** Split a JWT array string, handling commas inside JWT tokens correctly. */
    private static String[] splitJwtArray(String arrayContent) {
        java.util.List<String> result = new java.util.ArrayList<>();
        boolean inQuote = false;
        int start = 0;
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '"' && (i == 0 || arrayContent.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
            } else if (!inQuote && c == ',') {
                result.add(arrayContent.substring(start, i));
                start = i + 1;
            }
        }
        if (start < arrayContent.length()) {
            result.add(arrayContent.substring(start));
        }
        return result.toArray(new String[0]);
    }

    /** Pad a Base64 string to be a multiple of 4 characters. */
    private static String padBase64(String b64) {
        int mod = b64.length() % 4;
        if (mod == 0) return b64;
        return b64 + "====".substring(mod);
    }

    /** Extract a simple string value from a JSON object (no nested objects). */
    private static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        idx = json.indexOf(':', idx + search.length());
        if (idx < 0) return null;
        idx++; // skip ':'
        // Skip whitespace
        while (idx < json.length() && json.charAt(idx) == ' ') idx++;
        if (idx >= json.length() || json.charAt(idx) != '"') return null;
        idx++; // skip opening quote
        StringBuilder sb = new StringBuilder();
        while (idx < json.length()) {
            char c = json.charAt(idx);
            if (c == '\\' && idx + 1 < json.length()) {
                sb.append(json.charAt(idx + 1));
                idx += 2;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
                idx++;
            }
        }
        return sb.toString();
    }

    /**
     * Handle REQUEST_CHUNK during login phase (before gameplay handler is set up).
     * v9/v11 clients request chunks after receiving StartGame, before sending READY.
     */
    private void handleRequestChunkDuringLogin(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int chunkX = buf.readInt();
        int chunkZ = buf.readInt();
        sendChunkData(chunkX, chunkZ);
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
        // v91: attributes already sent early (after SetTime, before chunks, matching Genisys)
        // Pre-v91: send attributes now before inventory
        if (session.getMcpeProtocolVersion() < MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            sendUpdateAttributes(player.getPlayerId() + 1);
        }
        sendInventory();

        // Transition to gameplay handler
        MCPEGameplayHandler gameplayHandler = new MCPEGameplayHandler(
                session, world, playerManager, server,
                player, pongUpdater);
        session.setGameplayHandler(gameplayHandler);
        sessionWrapper.setGameplayHandler(gameplayHandler);

        // Send existing players
        boolean isV91 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91;
        for (ConnectedPlayer other : playerManager.getAllPlayers()) {
            if (other == player) continue;
            float ox = other.getX() / 32.0f;
            float oy = other.getY() / 32.0f - (float) PLAYER_EYE_HEIGHT;
            float oz = other.getZ() / 32.0f;
            int oeid = (other.getPlayerId() & 0xFF) + 1;
            float yaw = ((other.getYaw() + 128) & 0xFF) * 360.0f / 256.0f;
            float pitch = (other.getPitch() & 0xFF) * 360.0f / 256.0f;

            sendPlayerListAddForOther(other, oeid);

            java.util.UUID otherUuid = java.util.UUID.nameUUIDFromBytes(
                    other.getUsername().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            MCPEPacketBuffer addPkt = new MCPEPacketBuffer();
            addPkt.writeByte(codec.wireId(MCPEConstants.ADD_PLAYER));
            addPkt.writeLong(otherUuid.getMostSignificantBits());
            addPkt.writeLong(otherUuid.getLeastSignificantBits());
            if (isV91) {
                addPkt.writeStringV91(other.getUsername());
                addPkt.writeSignedVarInt(oeid);   // entityUniqueId
                addPkt.writeSignedVarInt(oeid);   // entityRuntimeId
                addPkt.writeLFloat(ox);
                addPkt.writeLFloat(oy);
                addPkt.writeLFloat(oz);
                addPkt.writeLFloat(0); // speedX
                addPkt.writeLFloat(0); // speedY
                addPkt.writeLFloat(0); // speedZ
                addPkt.writeLFloat(yaw);
                addPkt.writeLFloat(yaw);  // headYaw
                addPkt.writeLFloat(pitch);
                addPkt.writeSignedVarInt(0); // held item (air slot: VarInt 0)
                int addFlags91 = (1 << MCPEConstants.V91_FLAG_CAN_SHOW_NAMETAG)
                               | (1 << MCPEConstants.V91_FLAG_ALWAYS_SHOW_NAMETAG);
                addPkt.writeMetadataV91Start(6);
                addPkt.writeMetaLongV91(MCPEConstants.V91_META_FLAGS, addFlags91);
                addPkt.writeMetaShortV91(MCPEConstants.V91_META_AIR, (short) 400);
                addPkt.writeMetaShortV91(MCPEConstants.V91_META_MAX_AIR, (short) 400);
                addPkt.writeMetaStringV91Entry(MCPEConstants.V91_META_NAMETAG, other.getUsername());
                addPkt.writeMetaLongV91(MCPEConstants.V91_META_LEAD_HOLDER_EID, -1);
                addPkt.writeMetaFloatV91(MCPEConstants.V91_META_SCALE, 1.0f);
            } else {
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
            }
            server.sendGamePacket(session, addPkt.getBuf());

            MCPEPacketBuffer meta = new MCPEPacketBuffer();
            meta.writeByte(codec.wireId(MCPEConstants.SET_ENTITY_DATA));
            if (isV91) {
                meta.writeSignedVarInt(oeid);
                int metaFlags91 = (1 << MCPEConstants.V91_FLAG_CAN_SHOW_NAMETAG)
                                | (1 << MCPEConstants.V91_FLAG_ALWAYS_SHOW_NAMETAG);
                meta.writeMetadataV91Start(6);
                meta.writeMetaLongV91(MCPEConstants.V91_META_FLAGS, metaFlags91);
                meta.writeMetaShortV91(MCPEConstants.V91_META_AIR, (short) 400);
                meta.writeMetaShortV91(MCPEConstants.V91_META_MAX_AIR, (short) 400);
                meta.writeMetaStringV91Entry(MCPEConstants.V91_META_NAMETAG, other.getUsername());
                meta.writeMetaLongV91(MCPEConstants.V91_META_LEAD_HOLDER_EID, -1);
                meta.writeMetaFloatV91(MCPEConstants.V91_META_SCALE, 1.0f);
            } else {
                meta.writeLong(oeid);
                meta.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
                meta.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
                meta.writeMetaString(MCPEConstants.META_NAMETAG, other.getUsername());
                meta.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
                meta.writeMetaEnd();
            }
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

        // v17+ MovePlayer Y = eye-level (PocketMine Alpha_1.4dev confirms); v11-v13 = feet-level
        float moveY = (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17)
                ? (float) y : (float) (y - PLAYER_EYE_HEIGHT);
        sendMovePlayer(player.getPlayerId() + 1, (float) x, moveY, (float) z, 0, 0, 2);
        // v27+: 0x01=WORLD_IMMUTABLE (prevents block breaking!), 0x20=AUTO_JUMP, 0x40=ALLOW_FLIGHT
        // v14: 0x01=WORLD_IMMUTABLE (same as v27 — prevents RemoveBlock), use 0x20|0x40 instead
        // 0x01 meaning differs per version:
        // v11-v13: creative/fly flag (needed)
        // v14, v20: WORLD_IMMUTABLE (prevents block breaking — skip it)
        // v17-v18: needed for proper behavior (v17 confirmed working with it)
        // v27+: WORLD_IMMUTABLE (skip it)
        // v9 (0.6.1): PocketMine Alpha_1.1 never sends AdventureSettings.
        // Sending it (even 0xFF) puts the client in a broken mode where no block interaction works.
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_11) {
            int advFlags;
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27
                    || session.getMcpeProtocolVersion() == MCPEConstants.MCPE_PROTOCOL_VERSION_14
                    || session.getMcpeProtocolVersion() == MCPEConstants.MCPE_PROTOCOL_VERSION_20) {
                advFlags = 0x20 | 0x40; // AUTO_JUMP + ALLOW_FLIGHT, no WORLD_IMMUTABLE
            } else {
                advFlags = 0x01 | 0x40; // v11-v13, v17-v18
            }
            sendAdventureSettings(advFlags);
        }
        sendInventory();

        // Transition to gameplay handler
        MCPEGameplayHandler gameplayHandler = new MCPEGameplayHandler(
                session, world, playerManager, server,
                player, pongUpdater);
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
            addPkt.writeByte(codec.wireId(MCPEConstants.ADD_PLAYER));
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
                    addPkt.writeShort(MCPEConstants.getDefaultSkin().length);
                    addPkt.writeBytes(MCPEConstants.getDefaultSkin());
                }
            } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_11) {
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
            } else {
                // v9: clientId(long), username, entityId(int), x, y, z, metadata
                // No yaw/pitch/held item fields
                addPkt.writeLong(oeid);
                addPkt.writeString(other.getUsername());
                addPkt.writeInt(oeid);
                addPkt.writeFloat(ox);
                addPkt.writeFloat(oy);
                addPkt.writeFloat(oz);
            }

            boolean isV9 = session.getMcpeProtocolVersion() < MCPEConstants.MCPE_PROTOCOL_VERSION_11;
            if (isV9) {
                // v9: PocketMine sends metadata indices 0,1,16,17 (no nametag/showNametag)
                addPkt.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
                addPkt.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
                addPkt.writeMetaByte(16, (byte) 0);
                addPkt.writeMetaPosition(17, 0, 0, 0);
                addPkt.writeMetaEnd();
            } else {
                addPkt.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
                addPkt.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
                addPkt.writeMetaString(MCPEConstants.META_NAMETAG, other.getUsername());
                addPkt.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
                addPkt.writeMetaEnd();
            }
            server.sendGamePacket(session, addPkt.getBuf());

            if (isV9) {
                // v9: PocketMine sends PLAYER_EQUIPMENT after AddPlayer (not SET_ENTITY_DATA)
                MCPEPacketBuffer eqPkt = new MCPEPacketBuffer();
                eqPkt.writeByte(codec.wireId(MCPEConstants.PLAYER_EQUIPMENT));
                eqPkt.writeInt(oeid);
                eqPkt.writeShort(0); // block = air
                eqPkt.writeShort(0); // meta = 0
                server.sendGamePacket(session, eqPkt.getBuf());
            } else {
                MCPEPacketBuffer meta = new MCPEPacketBuffer();
                meta.writeByte(codec.wireId(MCPEConstants.SET_ENTITY_DATA));
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
        // Use MCPEConstants.toWireId directly: this method is called both before and
        // after the codec is initialized (error path during login parsing has no codec).
        pkt.writeByte(MCPEConstants.toWireId(MCPEConstants.LOGIN_STATUS, session.getMcpeProtocolVersion()));
        pkt.writeInt(status);
        server.sendGamePacket(session, pkt.getBuf());
    }

    /** Send ResourcePacksInfo (v91+): no packs, client should immediately respond and continue. */
    private void sendResourcePacksInfo() {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.V91_RESOURCE_PACKS_INFO & 0xFF);
        pkt.writeByte(0);      // mustAccept (bool = false)
        pkt.writeShort(0);     // behaviourPackCount = 0
        pkt.writeShort(0);     // resourcePackCount = 0
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendStartGame(float x, float y, float z) {
        boolean isV91 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91;
        boolean isV34 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;
        boolean isV27 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27;
        boolean isV17 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17;
        int entityId = player.getPlayerId() + 1;

        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(codec.wireId(MCPEConstants.START_GAME));

        if (isV91) {
            // v91: completely new format with VarInts and LFloats
            pkt.writeSignedVarInt(0);                 // entityUniqueId
            pkt.writeSignedVarInt(entityId);          // entityRuntimeId
            pkt.writeLFloat(x);                       // position X
            pkt.writeLFloat(y);                       // position Y
            pkt.writeLFloat(z);                       // position Z
            pkt.writeLFloat(0);                       // yaw (padding)
            pkt.writeLFloat(0);                       // pitch (padding)
            pkt.writeSignedVarInt(0);                 // seed
            pkt.writeSignedVarInt(0);                 // dimension
            pkt.writeSignedVarInt(MCPEConstants.GENERATOR_FLAT); // generator
            pkt.writeSignedVarInt(ServerProperties.getGameMode()); // gamemode
            pkt.writeSignedVarInt(ServerProperties.getDifficulty()); // difficulty
            pkt.writeBlockCoords((int) x, (int) y, (int) z); // spawn position
            pkt.writeByte(1);                         // hasAchievementsDisabled
            pkt.writeSignedVarInt(-1);                // dayCycleStopTime (-1 = not stopped)
            pkt.writeByte(0);                         // eduMode
            pkt.writeLFloat(0);                       // rainLevel
            pkt.writeLFloat(0);                       // lightningLevel
            pkt.writeByte(1);                         // commandsEnabled
            pkt.writeByte(0);                         // isTexturePacksRequired
            pkt.writeStringV91("");                    // unknown
            pkt.writeStringV91("RDForward");           // worldName
        } else {
            pkt.writeInt(0);                          // seed
            if (isV34) {
                pkt.writeByte(0);                     // v34: dimension (byte)
            } else if (isV27) {
                pkt.writeInt(0);                      // v27: dimension (int)
            }
            // v11-v20: NO dimension field (PocketMine Alpha_1.4dev confirms)
            pkt.writeInt(MCPEConstants.GENERATOR_FLAT); // generator type
            if (!isV27 || isV34) {
                pkt.writeInt(ServerProperties.getGameMode()); // gamemode (removed in v27, restored in v34)
            }
            if (isV27) {
                pkt.writeLong(entityId); // entity ID (64-bit in v27+)
            } else {
                pkt.writeInt(entityId);  // entity ID (32-bit)
            }
            if (isV17) {
                // v17+: spawn position as 3 ints (v27 and v17-v20 share the same format)
                pkt.writeInt((int) x);
                pkt.writeInt((int) y);
                pkt.writeInt((int) z);
            }
            pkt.writeFloat(x);                        // player X
            pkt.writeFloat(y);                        // player Y
            pkt.writeFloat(z);                        // player Z
            if (isV34) {
                if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_81) {
                    pkt.writeByte(1);                 // enableNewInventorySystem (bool)
                    pkt.writeByte(1);                 // enableNewCraftingSystem (bool)
                    pkt.writeByte(0);                 // unknown byte
                    pkt.writeString("");              // levelId (empty)
                } else {
                    pkt.writeByte(0);                 // v34: terminator byte
                }
            }
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendSetTime(int time) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        codec.writeTimeUpdate(pkt, time);
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendSetSpawnPosition(int x, int z, int y) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(codec.wireId(MCPEConstants.SET_SPAWN_POSITION));
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            // v91: VarInt unknown + blockCoords + bool
            pkt.writeSignedVarInt(0);         // unknown (spawnType?)
            pkt.writeBlockCoords(x, y, z);    // VarInt(x) + byte(y) + VarInt(z)
            pkt.writeByte(0);                 // unknown bool
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
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
        pkt.writeByte(codec.wireId(MCPEConstants.MOVE_PLAYER));
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            pkt.writeSignedVarInt(entityId); // v91: signed VarInt entity ID
            pkt.writeLFloat(x);
            pkt.writeLFloat(y);
            pkt.writeLFloat(z);
            pkt.writeLFloat(pitch);
            pkt.writeLFloat(yaw);
            pkt.writeLFloat(yaw);   // bodyYaw
            pkt.writeByte(mode);
            pkt.writeByte(0);       // onGround
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            pkt.writeLong(entityId);
            pkt.writeFloat(x);
            pkt.writeFloat(y);
            pkt.writeFloat(z);
            pkt.writeFloat(yaw);
            pkt.writeFloat(pitch);
            pkt.writeFloat(yaw);   // headYaw
            pkt.writeByte(mode);
            pkt.writeByte(0);      // onGround
        } else {
            pkt.writeInt(entityId);
            pkt.writeFloat(x);
            pkt.writeFloat(y);
            pkt.writeFloat(z);
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
                pkt.writeFloat(yaw);   // bodyYaw
                pkt.writeFloat(pitch);
                pkt.writeFloat(yaw);   // headYaw
            } else {
                pkt.writeFloat(yaw);
                pkt.writeFloat(pitch);
            }
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendAdventureSettings(int flags) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        int pv = session.getMcpeProtocolVersion();
        if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            // v91: UnsignedVarInt flags + UnsignedVarInt userPermission
            pkt.writeByte(MCPEConstants.V91_ADVENTURE_SETTINGS & 0xFF);
            pkt.writeUnsignedVarInt(flags);
            pkt.writeUnsignedVarInt(2); // PERMISSION_HOST
        } else if (pv < MCPEConstants.MCPE_PROTOCOL_VERSION_11) {
            // v9: ADVENTURE_SETTINGS at wire 0xB6 writes single byte 0xFF
            pkt.writeByte(MCPEConstants.toWireId(MCPEConstants.ADVENTURE_SETTINGS_V11, pv));
            pkt.writeByte(0xFF);
        } else {
            if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
                pkt.writeByte(MCPEConstants.toWireId(MCPEConstants.ADVENTURE_SETTINGS_V12, pv));
            } else if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_12) {
                pkt.writeByte(MCPEConstants.ADVENTURE_SETTINGS_V12);
            } else {
                pkt.writeByte(MCPEConstants.ADVENTURE_SETTINGS_V11);
            }
            pkt.writeInt(flags);
            if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_81) {
                pkt.writeInt(2); // PERMISSION_HOST
                pkt.writeInt(2); // globalPermission = HOST
            }
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    /**
     * Send a v34 PlayerListPacket (TYPE_ADD) to register a player's skin.
     * Must be sent before AddPlayer so the client knows the skin.
     */
    private void sendPlayerListAdd(ConnectedPlayer p) {
        java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(
                p.getUsername().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        boolean isV91 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91;
        boolean isV81 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_81;
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        if (isV91) {
            pkt.writeByte(MCPEConstants.V91_PLAYER_LIST & 0xFF);
        } else {
            pkt.writeByte(isV81 ? (MCPEConstants.V81_PLAYER_LIST & 0xFF)
                    : (MCPEConstants.V34_PLAYER_LIST & 0xFF));
        }
        pkt.writeByte(0); // TYPE_ADD
        long eid = p.getPlayerId() + 1;
        byte[] skinData = p.getMcpeSkinData();
        if (skinData == null || skinData.length == 0) {
            skinData = MCPEConstants.getDefaultSkin();
        }
        if (isV91) {
            // v91: UnsignedVarInt count, then entries with VarInt strings
            pkt.writeUnsignedVarInt(1); // entry count
            pkt.writeLong(uuid.getMostSignificantBits());
            pkt.writeLong(uuid.getLeastSignificantBits());
            pkt.writeSignedVarInt((int) eid); // entityId (VarInt)
            pkt.writeStringV91(p.getUsername());
            pkt.writeStringV91(clientSkinId.isEmpty() ? "Standard_Steve" : clientSkinId);
            pkt.writeUnsignedVarInt(skinData.length); // raw skin bytes with VarInt length prefix
            pkt.writeBytes(skinData);
        } else {
            pkt.writeInt(1);  // entry count
            pkt.writeLong(uuid.getMostSignificantBits());
            pkt.writeLong(uuid.getLeastSignificantBits());
            pkt.writeLong(eid); // entityId
            pkt.writeString(p.getUsername());
            if (isV81) {
                pkt.writeString(clientSkinId.isEmpty() ? "Standard_Steve" : clientSkinId);
                pkt.writeShort(skinData.length);
                pkt.writeBytes(skinData);
            } else {
                if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_38) {
                    pkt.writeString(""); // skinName
                } else {
                    pkt.writeByte(0); // slim
                }
                pkt.writeShort(skinData.length);
                pkt.writeBytes(skinData);
            }
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    /** Send a PlayerListAdd for another player (used in doSpawn loop). */
    private void sendPlayerListAddForOther(ConnectedPlayer p, int entityId) {
        java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(
                p.getUsername().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        boolean isV91 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91;
        boolean isV81 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_81;
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        if (isV91) {
            pkt.writeByte(MCPEConstants.V91_PLAYER_LIST & 0xFF);
        } else {
            pkt.writeByte(isV81 ? (MCPEConstants.V81_PLAYER_LIST & 0xFF)
                    : (MCPEConstants.V34_PLAYER_LIST & 0xFF));
        }
        pkt.writeByte(0); // TYPE_ADD
        byte[] skinData = p.getMcpeSkinData();
        if (skinData == null || skinData.length == 0) {
            skinData = MCPEConstants.getDefaultSkin();
        }
        if (isV91) {
            pkt.writeUnsignedVarInt(1);
            pkt.writeLong(uuid.getMostSignificantBits());
            pkt.writeLong(uuid.getLeastSignificantBits());
            pkt.writeSignedVarInt(entityId);
            pkt.writeStringV91(p.getUsername());
            pkt.writeStringV91("Standard_Steve");
            pkt.writeUnsignedVarInt(skinData.length); // raw skin bytes
            pkt.writeBytes(skinData);
        } else {
            pkt.writeInt(1);  // entry count
            pkt.writeLong(uuid.getMostSignificantBits());
            pkt.writeLong(uuid.getLeastSignificantBits());
            pkt.writeLong(entityId);  // entityId
            pkt.writeString(p.getUsername());
            if (isV81) {
                pkt.writeString("Standard_Steve");
                pkt.writeShort(skinData.length);
                pkt.writeBytes(skinData);
            } else {
                if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_38) {
                    pkt.writeString("");
                } else {
                    pkt.writeByte(0);
                }
                pkt.writeShort(skinData.length);
                pkt.writeBytes(skinData);
            }
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    /**
     * Send a v34 UpdateAttributesPacket with health and movement speed.
     */
    private void sendUpdateAttributes(int entityId) {
        boolean isV91 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91;
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        if (isV91) {
            pkt.writeByte(MCPEConstants.V91_UPDATE_ATTRIBUTES & 0xFF);
            pkt.writeSignedVarInt(entityId); // must match StartGame entityRuntimeId
            pkt.writeUnsignedVarInt(9); // 9 attributes (match Genisys sendAttributes(true))
            // absorption: min=0, max=FLT_MAX, value=0, default=0
            pkt.writeLFloat(0.0f); pkt.writeLFloat(Float.MAX_VALUE); pkt.writeLFloat(0.0f); pkt.writeLFloat(0.0f);
            pkt.writeStringV91("minecraft:absorption");
            // saturation: min=0, max=20, value=5, default=5
            pkt.writeLFloat(0.0f); pkt.writeLFloat(20.0f); pkt.writeLFloat(5.0f); pkt.writeLFloat(5.0f);
            pkt.writeStringV91("minecraft:player.saturation");
            // exhaustion: min=0, max=5, value=0.41, default=0.41
            pkt.writeLFloat(0.0f); pkt.writeLFloat(5.0f); pkt.writeLFloat(0.41f); pkt.writeLFloat(0.41f);
            pkt.writeStringV91("minecraft:player.exhaustion");
            // knockback_resistance: min=0, max=1, value=0, default=0
            pkt.writeLFloat(0.0f); pkt.writeLFloat(1.0f); pkt.writeLFloat(0.0f); pkt.writeLFloat(0.0f);
            pkt.writeStringV91("minecraft:knockback_resistance");
            // health: min=0, max=20, value=20, default=20
            pkt.writeLFloat(0.0f); pkt.writeLFloat(20.0f); pkt.writeLFloat(20.0f); pkt.writeLFloat(20.0f);
            pkt.writeStringV91("minecraft:health");
            // movement: min=0, max=FLT_MAX, value=0.1, default=0.1
            pkt.writeLFloat(0.0f); pkt.writeLFloat(Float.MAX_VALUE); pkt.writeLFloat(0.1f); pkt.writeLFloat(0.1f);
            pkt.writeStringV91("minecraft:movement");
            // hunger: min=0, max=20, value=20, default=20
            pkt.writeLFloat(0.0f); pkt.writeLFloat(20.0f); pkt.writeLFloat(20.0f); pkt.writeLFloat(20.0f);
            pkt.writeStringV91("minecraft:player.hunger");
            // experience_level: min=0, max=24791, value=0, default=0
            pkt.writeLFloat(0.0f); pkt.writeLFloat(24791.0f); pkt.writeLFloat(0.0f); pkt.writeLFloat(0.0f);
            pkt.writeStringV91("minecraft:player.level");
            // experience: min=0, max=1, value=0, default=0
            pkt.writeLFloat(0.0f); pkt.writeLFloat(1.0f); pkt.writeLFloat(0.0f); pkt.writeLFloat(0.0f);
            pkt.writeStringV91("minecraft:player.experience");
        } else {
            int attrId = (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_81)
                    ? (MCPEConstants.V81_UPDATE_ATTRIBUTES & 0xFF)
                    : (MCPEConstants.V34_UPDATE_ATTRIBUTES & 0xFF);
            pkt.writeByte(attrId);
            pkt.writeLong(entityId);
            pkt.writeShort(2);
            pkt.writeFloat(0.0f);   pkt.writeFloat(20.0f);  pkt.writeFloat(20.0f);
            pkt.writeString("minecraft:health");
            pkt.writeFloat(0.0f);   pkt.writeFloat(0.3f);   pkt.writeFloat(0.1f);
            pkt.writeString("minecraft:movement");
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendInventory() {
        boolean isV91 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91;
        boolean isV27 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27;
        boolean isV34 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;

        // Send empty player inventory
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(codec.wireId(MCPEConstants.SEND_INVENTORY));
        if (!isV27) {
            pkt.writeInt(player.getPlayerId() + 1); // v11-v20: entity ID
        }
        pkt.writeByte(0); // windowId = player inventory
        if (isV91) {
            pkt.writeUnsignedVarInt(36); // v91: UnsignedVarInt slot count
            for (int i = 0; i < 36; i++) {
                pkt.writeSignedVarInt(0); // air slot (VarInt 0)
            }
            // v91: hotbar indices
            pkt.writeUnsignedVarInt(9);
            for (int i = 0; i < 9; i++) {
                pkt.writeSignedVarInt(-1);
            }
        } else {
            pkt.writeShort(36); // 36 slots
            for (int i = 0; i < 36; i++) {
                if (isV34) {
                    pkt.writeShort(0); // v34: air = just itemId 0
                } else {
                    pkt.writeShort(0);  // itemId = air
                    pkt.writeByte(0);   // count
                    pkt.writeShort(0);  // damage/metadata
                }
            }
            if (isV27) {
                pkt.writeShort(9);
                for (int i = 0; i < 9; i++) {
                    pkt.writeInt(-1);
                }
            }
        }
        server.sendGamePacket(session, pkt.getBuf());

        // Send empty armor
        pkt = new MCPEPacketBuffer();
        pkt.writeByte(codec.wireId(MCPEConstants.SEND_INVENTORY));
        if (!isV27) {
            pkt.writeInt(player.getPlayerId() + 1);
        }
        pkt.writeByte(isV27 ? 0x78 : 1); // v27: 0x78 (armor container), v11-v20: 1
        if (isV91) {
            pkt.writeUnsignedVarInt(4);
            for (int i = 0; i < 4; i++) {
                pkt.writeSignedVarInt(0); // air
            }
            pkt.writeUnsignedVarInt(0); // no hotbar entries
        } else {
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
        pkt.writeByte(codec.wireId(MCPEConstants.CONTAINER_SET_CONTENT));
        pkt.writeByte(0x79); // SPECIAL_CREATIVE windowId
        boolean isV91 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91;
        if (isV91) {
            pkt.writeUnsignedVarInt(creativeItems.length);
            for (int[] item : creativeItems) {
                // v91 putSlot: VarInt(id), VarInt(damage<<8 | count), LShort(nbtLen)
                pkt.writeSignedVarInt(item[0]);
                pkt.writeSignedVarInt((item[1] << 8) | 1); // damage<<8 | count=1
                pkt.writeLShort(0); // no NBT
            }
            pkt.writeUnsignedVarInt(0); // no hotbar entries
        } else {
            pkt.writeShort(creativeItems.length);
            boolean useLEShortNbt = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_45;
            for (int[] item : creativeItems) {
                pkt.writeShort(item[0]); // itemId
                pkt.writeByte(1);        // count
                pkt.writeShort(item[1]); // damage/meta
                if (useLEShortNbt) {
                    pkt.writeLShort(0);  // v45+: nbtLen is little-endian short
                } else {
                    pkt.writeShort(0);   // v34: nbtLen is big-endian short
                }
            }
            pkt.writeShort(0); // v34: hotbar count = 0 for non-inventory windows
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    /** Send chunk data using the version-specific codec. */
    private void sendChunkData(int chunkX, int chunkZ) {
        codec.sendChunkData(server, session, world, chunkX, chunkZ);
    }

    public ConnectedPlayer getPlayer() { return player; }
    public MCPESessionWrapper getSessionWrapper() { return sessionWrapper; }
}
