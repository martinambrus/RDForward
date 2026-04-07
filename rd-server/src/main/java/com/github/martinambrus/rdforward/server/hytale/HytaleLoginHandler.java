package com.github.martinambrus.rdforward.server.hytale;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import io.netty.incubator.codec.quic.QuicStreamType;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

import static com.github.martinambrus.rdforward.server.hytale.HytaleProtocolConstants.*;

/**
 * Handles the Hytale login sequence on a QUIC stream.
 *
 * Login flow (offline/insecure mode):
 * 1. Client opens a QUIC stream and sends Connect (ID 0)
 * 2. Server validates protocol CRC, sends ConnectAccept (ID 14)
 * 3. Server sends SetClientId (ID 100) with the entity network ID
 * 4. Server sends SetGameMode (ID 101)
 * 5. Server sends block/item registry packets (UpdateBlockTypes etc.)
 * 6. Server sends JoinWorld (ID 104)
 * 7. Client sends ClientReady (ID 105) with firstJoin=true
 * 8. Server sends ClientTeleport (ID 109) with spawn position
 * 9. Server sends initial chunks (SetChunk on Chunks stream)
 * 10. Server sends WorldLoadFinished (ID 22)
 *
 * After login completes, replaces itself with HytaleGameplayHandler.
 */
public class HytaleLoginHandler extends SimpleChannelInboundHandler<HytalePacketBuffer> {

    /** Required common assets: [hash, name, resourcePath]. Shared between WorldSettings and asset delivery. */
    private static final String[][] COMMON_ASSETS = {
        {"11c5f4d7cfa18a12e83caa3c01b64a2770bb5b45fa7fd448c1a51cebe52e3ea4",
         "BlockTextures/Unknown.png", "/hytale/textures/Unknown.png"},
        {"82cd708f1e6963fe6bd5947dad14b10f11c2223180e263391daba5164de6b1e1",
         "BlockTextures/Cracks/T_Crack_Generic_01.png", "/hytale/textures/Cracks/T_Crack_Generic_01.png"},
        {"f092c4ba61e162c4b64bdd5c0c1446f536f649730e87dc294aeaf77ceb790b84",
         "BlockTextures/Cracks/T_Crack_Generic_02.png", "/hytale/textures/Cracks/T_Crack_Generic_02.png"},
        {"cfcfd7a18b076a64b3f8edd5ff503cda142a01c83909e8178b25ff7f59725039",
         "BlockTextures/Cracks/T_Crack_Generic_03.png", "/hytale/textures/Cracks/T_Crack_Generic_03.png"},
        {"3da32b2c6a43db48b164f11fa8761f8c2e3281e51b735e00d9c7c25dc87a9a31",
         "BlockTextures/Cracks/T_Crack_Generic_04.png", "/hytale/textures/Cracks/T_Crack_Generic_04.png"},
        {"57a696e6cebe9adc1eebb795527ea976e4fb017a33ad42a410f81b4fa5c55c81",
         "BlockTextures/Cracks/T_Crack_Generic_05.png", "/hytale/textures/Cracks/T_Crack_Generic_05.png"},
        {"9ded460561c9982310d8693a35d02a5848c229a98cf6fdfda98bbd250c670ed9",
         "BlockTextures/Cracks/T_Crack_Generic_06.png", "/hytale/textures/Cracks/T_Crack_Generic_06.png"},
        {"fbc783e73435c29dfaeb5778a61793257f1d4dbeefa64d423cfefa6c1e1b9169",
         "BlockTextures/Cracks/T_Crack_Generic_07.png", "/hytale/textures/Cracks/T_Crack_Generic_07.png"},
    };

    private final HytaleSession session;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;
    private final HytaleBlockMapper blockMapper;
    private final HytaleChunkConverter chunkConverter;

    public HytaleLoginHandler(HytaleSession session, ServerWorld world,
                               PlayerManager playerManager, ChunkManager chunkManager,
                               HytaleBlockMapper blockMapper, HytaleChunkConverter chunkConverter) {
        this.session = session;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
        this.blockMapper = blockMapper;
        this.chunkConverter = chunkConverter;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Register this stream with the session
        QuicStreamChannel streamChannel = (QuicStreamChannel) ctx.channel();
        long streamId = streamChannel.streamId();
        session.registerStream(streamId, streamChannel);

        // Auto-detect Hytale channel from client-initiated bidi stream IDs.
        // Client bidi streams: IDs 0, 4, 8, 12... (type bits 0b00).
        // Channel = sequence number = streamId / 4.
        if ((streamId & 0x03) == 0x00) {
            int channel = (int) (streamId / 4);
            session.setChannelStream(channel, streamChannel);
            System.out.println("[Hytale] Registered client stream " + streamId
                    + " as channel " + channel);
        }

        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HytalePacketBuffer msg) throws Exception {
        int packetId = msg.getPacketId();

        switch (packetId) {
            case PACKET_CONNECT:
                handleConnect(ctx, msg);
                break;
            case PACKET_AUTH_TOKEN:
                handleAuthToken(ctx, msg);
                break;
            case PACKET_REQUEST_ASSETS:
                handleRequestAssets(ctx, msg);
                break;
            case PACKET_PLAYER_OPTIONS:
                handlePlayerOptions(ctx, msg);
                break;
            case PACKET_CLIENT_READY:
                handleClientReady(ctx, msg);
                break;
            case PACKET_PONG:
                handlePong(msg);
                break;
            case PACKET_VIEW_RADIUS:
                handleViewRadius(ctx, msg);
                break;
            default:
                System.out.println("[Hytale] Login phase: ignoring packet ID " + packetId);
                break;
        }
    }

    /**
     * Handle Connect (ID 0) from client.
     *
     * Fixed block layout (66 bytes):
     *   [0]     byte    nullBits
     *   [1]     int32LE protocolCrc
     *   [5]     int32LE protocolBuildNumber
     *   [9]     char[20] clientVersion (fixed ASCII)
     *   [29]    byte    clientType (0=Game, 1=Editor)
     *   [30]    UUID    uuid (16 bytes big-endian)
     *   [46]    int32LE username offset -> variable block
     *   [50]    int32LE identityToken offset (nullable)
     *   [54]    int32LE language offset
     *   [58]    int32LE referralData offset (nullable)
     *   [62]    int32LE referralSource offset (nullable)
     * Variable block starts at 66.
     */
    private void handleConnect(ChannelHandlerContext ctx, HytalePacketBuffer msg) {
        if (session.getState() != HytaleSession.State.AWAITING_CONNECT) {
            System.out.println("[Hytale] Duplicate Connect packet, ignoring");
            return;
        }

        // Fixed block: read sequentially (all at known offsets 0-65)
        int nullBits = msg.readUnsignedByte();       // offset 0
        int protocolCrc = msg.readIntLE();            // offset 1
        int buildNumber = msg.readIntLE();            // offset 5
        String clientVersion = msg.readFixedAscii(20); // offset 9
        int clientType = msg.readUnsignedByte();      // offset 29
        UUID uuid = msg.readUUID();                   // offset 30

        // Variable field offset slots (offset 46-65)
        int usernameOffset = msg.readIntLE();         // offset 46
        int identityTokenOffset = msg.readIntLE();    // offset 50
        int languageOffset = msg.readIntLE();         // offset 54
        int referralDataOffset = msg.readIntLE();     // offset 58
        int referralSourceOffset = msg.readIntLE();   // offset 62

        // Reader is now at VARIABLE_BLOCK_START (66).
        // Variable fields use offset-based random access, not sequential reads.
        // Each offset is relative to VARIABLE_BLOCK_START.
        int varBlockBase = msg.readerIndex(); // = 66

        // Read username (non-nullable, always present)
        msg.readerIndex(varBlockBase + usernameOffset);
        String username = msg.readAsciiString();

        // Read identity token (nullable, bit 0 of nullBits)
        String identityToken = null;
        if ((nullBits & 0x01) != 0 && identityTokenOffset >= 0) {
            msg.readerIndex(varBlockBase + identityTokenOffset);
            identityToken = msg.readString(); // UTF-8, not ASCII
        }

        System.out.println("[Hytale] Connect from " + username + " (uuid=" + uuid
                + ", version=" + clientVersion + ", crc=" + protocolCrc
                + ", build=" + buildNumber + ", type=" + (clientType == 0 ? "Game" : "Editor")
                + ", hasIdentityToken=" + (identityToken != null) + ")");

        // Validate protocol CRC
        if (protocolCrc != PROTOCOL_CRC) {
            System.out.println("[Hytale] Protocol CRC mismatch: expected " + PROTOCOL_CRC
                    + ", got " + protocolCrc + " — attempting connection anyway");
        }

        session.setUsername(username);
        session.setPlayerUUID(uuid);

        // Release client requires authenticated flow (AuthGrant/AuthToken exchange)
        // before ConnectAccept. Development flow (immediate ConnectAccept) is rejected.
        if (identityToken != null) {
            System.out.println("[Hytale] Client sent identity token, starting authenticated flow");
            session.setState(HytaleSession.State.AWAITING_AUTH_TOKEN);
            sendAuthGrant(ctx, identityToken);
        } else {
            // Fallback: development flow for non-release clients
            System.out.println("[Hytale] No identity token, using development flow");
            session.setState(HytaleSession.State.SENDING_REGISTRIES);
            sendConnectAcceptAndRegistries(ctx);
        }
    }

    /**
     * Send AuthGrant (ID 11) to start the authenticated flow.
     *
     * AuthGrant wire format (variable-block packet):
     *   [0]     byte     nullBits (bit0=authorizationGrant, bit1=serverIdentityToken)
     *   [1]     int32LE  authorizationGrant offset into variable block
     *   [5]     int32LE  serverIdentityToken offset into variable block
     *   [9+]    variable block: VarInt-prefixed strings
     *
     * Uses real tokens from HytaleAuthManager if available,
     * falls back to dummy tokens for testing.
     */
    private void sendAuthGrant(ChannelHandlerContext ctx, String clientIdentityToken) {
        HytaleAuthManager auth = HytaleAuthManager.getInstance();

        String authorizationGrant;
        String serverIdentityToken;

        if (auth.isAuthenticated()) {
            // Request real auth grant from session service
            authorizationGrant = auth.requestAuthGrant(clientIdentityToken);
            serverIdentityToken = auth.getIdentityToken();

            if (authorizationGrant == null || serverIdentityToken == null) {
                System.err.println("[Hytale] Failed to get auth grant from session service, sending dummy tokens");
                authorizationGrant = "rdforward-auth-grant-" + session.getPlayerUUID();
                serverIdentityToken = auth.getIdentityToken() != null
                        ? auth.getIdentityToken() : "rdforward-server-identity";
            }
            System.out.println("[Hytale] Got real auth grant from session service");
        } else {
            System.out.println("[Hytale] WARNING: Server not authenticated with Hytale. "
                    + "Run 'hytale-auth' command to authenticate.");
            System.out.println("[Hytale] Sending dummy auth tokens (client will reject)");
            authorizationGrant = "rdforward-auth-grant-" + session.getPlayerUUID();
            serverIdentityToken = "rdforward-server-identity";
        }

        HytalePacketBuffer pkt = HytalePacketBuffer.create(PACKET_AUTH_GRANT, ctx.alloc());

        // Both fields present
        pkt.writeByte(0x03); // nullBits: bit0 + bit1

        // Reserve offset slots for backpatching
        int authGrantOffsetSlot = pkt.markWriterIndex();
        pkt.writeIntLE(0); // placeholder
        int serverIdTokenOffsetSlot = pkt.markWriterIndex();
        pkt.writeIntLE(0); // placeholder

        // Variable block starts at position 9
        int varBlockStart = pkt.markWriterIndex();

        // Write authorizationGrant
        pkt.setIntLE(authGrantOffsetSlot, pkt.markWriterIndex() - varBlockStart);
        pkt.writeString(authorizationGrant);

        // Write serverIdentityToken
        pkt.setIntLE(serverIdTokenOffsetSlot, pkt.markWriterIndex() - varBlockStart);
        pkt.writeString(serverIdentityToken);

        session.sendPacket(pkt);
        System.out.println("[Hytale] Sent AuthGrant to " + session.getUsername()
                + " (awaiting AuthToken)");
    }

    /**
     * Handle AuthToken (ID 12) from client.
     *
     * AuthToken wire format:
     *   [0]     byte     nullBits (bit0=accessToken, bit1=serverAuthorizationGrant)
     *   [1]     int32LE  accessToken offset
     *   [5]     int32LE  serverAuthorizationGrant offset
     *   [9+]    variable block: VarInt-prefixed strings
     */
    private void handleAuthToken(ChannelHandlerContext ctx, HytalePacketBuffer msg) {
        if (session.getState() != HytaleSession.State.AWAITING_AUTH_TOKEN) {
            System.out.println("[Hytale] Unexpected AuthToken in state " + session.getState());
            return;
        }

        int nullBits = msg.readUnsignedByte();
        int accessTokenOffset = msg.readIntLE();
        int serverAuthGrantOffset = msg.readIntLE();

        String accessToken = null;
        String serverAuthGrant = null;

        if ((nullBits & 0x01) != 0 && accessTokenOffset >= 0) {
            accessToken = msg.readString();
        }
        if ((nullBits & 0x02) != 0 && serverAuthGrantOffset >= 0) {
            serverAuthGrant = msg.readString();
        }

        System.out.println("[Hytale] Received AuthToken from " + session.getUsername()
                + " (hasAccessToken=" + (accessToken != null)
                + ", hasServerAuthGrant=" + (serverAuthGrant != null) + ")");

        // Exchange the server auth grant for an access token
        sendServerAuthToken(ctx, serverAuthGrant);
    }

    /**
     * Send ServerAuthToken (ID 13) to complete mutual authentication.
     *
     * ServerAuthToken wire format:
     *   [0]     byte     nullBits (bit0=serverAccessToken, bit1=passwordChallenge)
     *   [1]     int32LE  serverAccessToken offset
     *   [5]     int32LE  passwordChallenge offset
     *   [9+]    variable block
     */
    private void sendServerAuthToken(ChannelHandlerContext ctx, String serverAuthGrant) {
        HytaleAuthManager auth = HytaleAuthManager.getInstance();
        String serverAccessToken;

        if (auth.isAuthenticated() && serverAuthGrant != null) {
            serverAccessToken = auth.exchangeAuthGrant(serverAuthGrant);
            if (serverAccessToken == null) {
                System.err.println("[Hytale] Failed to exchange server auth grant");
                session.disconnect("Server authentication failed");
                return;
            }
            System.out.println("[Hytale] Exchanged auth grant for server access token");
        } else {
            serverAccessToken = "rdforward-server-access-token";
        }

        HytalePacketBuffer pkt = HytalePacketBuffer.create(PACKET_SERVER_AUTH_TOKEN, ctx.alloc());

        // Only serverAccessToken present, no password challenge
        pkt.writeByte(0x01); // nullBits: bit0 only

        int serverTokenOffsetSlot = pkt.markWriterIndex();
        pkt.writeIntLE(0); // placeholder
        pkt.writeIntLE(-1); // passwordChallenge not present

        int varBlockStart = pkt.markWriterIndex();

        // Write server access token
        pkt.setIntLE(serverTokenOffsetSlot, pkt.markWriterIndex() - varBlockStart);
        pkt.writeString(serverAccessToken);

        session.sendPacket(pkt);
        System.out.println("[Hytale] Sent ServerAuthToken to " + session.getUsername());

        // The client validates the server access token asynchronously (Ed25519 + cert binding).
        // On localhost, packets arrive in the same QUIC frame before validation completes.
        // Delay to let the client finish auth, then send WorldSettings + ServerInfo
        // (NOT ConnectAccept — that's development-flow only).
        session.setState(HytaleSession.State.SENDING_REGISTRIES);
        ctx.executor().schedule(() -> sendWorldSettingsAndServerInfo(ctx),
                500, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Send WorldSettings (ID 20) + ServerInfo (ID 223) to transition client
     * from auth phase to setup phase. The client will respond with RequestAssets (ID 23).
     */
    private void sendWorldSettingsAndServerInfo(ChannelHandlerContext ctx) {
        // WorldSettings (ID 20): nullBits(1) + worldHeight(int32LE) + [optional requiredAssets]
        // requiredAssets tells the client which assets it will need. The client's
        // "Handle RequiredAssets" logic uses this list to wait for and integrate
        // delivered assets into its texture system. Without it, the client skips
        // asset integration entirely ("No assets to process").
        HytalePacketBuffer ws = HytalePacketBuffer.create(PACKET_WORLD_SETTINGS, ctx.alloc(), 1024);
        ws.writeByte(0x01); // nullBits: bit0=requiredAssets present
        ws.writeIntLE(320); // worldHeight — real server uses ChunkUtil.HEIGHT=320 (10 sections of 32)
        // Asset[] array: VarInt count + [64-char hash + VarString name] per entry
        ws.writeVarInt(COMMON_ASSETS.length);
        for (String[] asset : COMMON_ASSETS) {
            ws.writeFixedAscii(asset[0], 64); // SHA-256 hash
            ws.writeString(asset[1]);          // asset path/name
        }
        session.sendPacket(ws);

        // ServerInfo (ID 223): variable-block with serverName, motd, maxPlayers
        HytalePacketBuffer si = HytalePacketBuffer.create(PACKET_SERVER_INFO, ctx.alloc());
        si.writeByte(0x03); // nullBits: bit0=serverName, bit1=motd, no fallbackServer
        si.writeIntLE(128);  // maxPlayers

        // 3 offset slots (serverName, motd, fallbackServer)
        int nameOffsetSlot = si.markWriterIndex();
        si.writeIntLE(0);
        int motdOffsetSlot = si.markWriterIndex();
        si.writeIntLE(0);
        si.writeIntLE(-1); // fallbackServer: not present

        // Variable block starts at 17
        int varBlockStart = si.markWriterIndex();

        // serverName
        si.setIntLE(nameOffsetSlot, si.markWriterIndex() - varBlockStart);
        si.writeString("RDForward");

        // motd
        si.setIntLE(motdOffsetSlot, si.markWriterIndex() - varBlockStart);
        si.writeString("A Minecraft Server");

        session.sendPacket(si);

        System.out.println("[Hytale] Sent WorldSettings + ServerInfo to " + session.getUsername()
                + " (awaiting RequestAssets)");
    }

    /**
     * Handle RequestAssets (ID 23) from client.
     * The client sends this after receiving WorldSettings + ServerInfo.
     * We respond with asset data, then WorldLoadProgress + WorldLoadFinished.
     */
    private void handleRequestAssets(ChannelHandlerContext ctx, HytalePacketBuffer msg) {
        System.out.println("[Hytale] Received RequestAssets from " + session.getUsername());

        // Send common assets FIRST — before ANY other packets.
        // The client's "Handle RequiredAssets" logic determines asset availability
        // based on the first non-asset packet it receives. If we send SetClientId
        // before assets, the client concludes "no assets to process" and skips
        // integrating delivered assets into its texture system.
        // Real server sends assets → registries → WorldLoadFinished → (PlayerOptions) → SetClientId.
        sendCommonAssets(ctx);

        // The client requires ALL asset registries before WorldLoadFinished.
        // BlockTypes (40) MUST be sent first — other registries reference it.
        // Two standard formats:
        // FBS=6: nullBits(1) + type(1) + maxId(4) — entries nullable via nullBits
        // FBS=2: nullBits(1) + type(1) — no maxId, entries nullable via nullBits
        // type=0 means FULL (Init) update.

        // UpdateBlockTypes (ID 40): maxId=2, 2 entries (Empty + Unknown).
        // Each BlockType entry is exactly: 4 nullBits + 160 fixed + 100 offset slots = 264 bytes minimum.
        // Variable data follows the offset slots (referenced by offsets relative to byte 264).
        //
        // BlockType.serialize() format (from decompiled code):
        //   nullBits[4] → unknown(1) → drawType(1) → material(1) → opacity(1) → hitbox(4LE)
        //   → interactionHitbox(4LE) → modelScale(4fLE) → looping(1) → maxSupportDistance(4LE)
        //   → blockSupportsRequiredFor(1) → requiresAlphaBlending(1) → cubeShadingMode(1)
        //   → randomRotation(1) → variantRotation(1) → rotationYawPlacementOffset(1)
        //   → blockSoundSetIndex(4LE) → ambientSoundEventIndex(4LE) → particleColor(3)
        //   → light(4) → tint(24) → biomeTint(24) → group(4LE) → movementSettings(42)
        //   → flags(2) → placementSettings(17) → ignoreSupportWhenPlaced(1)
        //   → transitionToTag(4LE) → 25×offsetSlot(4LE) → variableData
        // UpdateBlockTypes (ID 40) Init: allocate the block type array.
        // Outer fixed block is 10 bytes:
        //   [0] nullBits, [1] type, [2-5] maxId, [6-9] update flags
        // Entries for IDs 0-2 must NOT have name field (client validates
        // Reserved block type IDs from decompiled server (BlockType.java):
        //   ID 0 = "Empty"      drawType=0(Empty)  material=0(Empty)  opacity=2(Transparent)
        //   ID 1 = "Unknown"    drawType=2(Cube)   material=1(Solid)  unknown=true
        //   ID 2 = "Debug_Cube" drawType=2(Cube)   material=1(Solid)
        //   ID 3 = "Debug_Model" drawType=3(Model) material=0(Empty)
        // Server sets packet.name = this.id for ALL block types.
        // Client validates: drawType=Empty + name present (but not "Empty") = crash.
        // Must send name via variable field slot 1 (nullBits[1] & 1).
        {
            // All 4 reserved block types from decompiled server BlockType.java
            String[] names    = {"Empty",  "Unknown", "Debug_Cube", "Debug_Model"};
            int[]    draws    = {0,        2,         2,            2};  // Empty, Cube, Cube, Cube
            int[]    mats     = {0,        1,         1,            0};  // Empty, Solid, Solid, Empty
            int[]    opacs    = {2,        0,         0,            2};  // Transparent, Solid, Solid, Transparent
            boolean[] unknowns = {false,   true,      false,        false};
            int maxId = 4; // NEXT FREE index, not last used — array allocated with size maxId

            HytalePacketBuffer pkt = HytalePacketBuffer.create(40, ctx.alloc(), (maxId + 1) * 600 + 20);
            io.netty.buffer.ByteBuf buf = pkt.getBuf();

            buf.writeByte(0x01);    // nullBits: bit0 = blockTypes map present
            buf.writeByte(0);       // type = 0 (Init)
            buf.writeIntLE(maxId);  // maxId
            buf.writeByte(1);       // updateBlockTextures
            buf.writeByte(1);       // updateModelTextures
            buf.writeByte(1);       // updateModels
            buf.writeByte(1);       // updateMapGeometry

            pkt.writeVarInt(names.length); // entry count

            // Texture references must be PATH strings, not hashes.
            // The client resolves paths via CommonAssetsIndex.hashes (path→hash mapping
            // for built-in assets from Assets.zip). "BlockTextures/Unknown.png" is built-in.
            // Hashes can't be resolved as paths.
            String unknownTex = "BlockTextures/Unknown.png";

            for (int id = 0; id < maxId; id++) {
                buf.writeIntLE(id);            // key (block ID)
                int entryStart = buf.writerIndex();
                // BlockType: NBFS=4, FBS=164, VFC=25, VBS=264
                // nullBits: [1].0=name, [1].1=shaderEffect, [1].3=modelTexture, [1].7=cubeTextures
                // nullBits[0]: tint+biomeTint+movementSettings+flags MUST be present
                // (client render loop accesses these without null-checking)
                buf.writeByte(0x04 | 0x08 | 0x10 | 0x20 | 0x80); // nullBits[0]: tint+biomeTint+movementSettings+flags+item
                buf.writeByte(0x01 | 0x02 | 0x08 | 0x80); // nullBits[1]: name+shaderEffect+modelTexture+cubeTextures
                buf.writeByte(0);              // nullBits[2]
                buf.writeByte(0x08);           // nullBits[3]: bit3=interactions (server ALWAYS sets this)
                buf.writeByte(unknowns[id] ? 1 : 0); // unknown
                buf.writeByte(draws[id]);      // drawType
                buf.writeByte(mats[id]);       // material
                buf.writeByte(opacs[id]);      // opacity
                buf.writeIntLE(0);             // hitbox
                buf.writeIntLE(0);             // interactionHitbox
                buf.writeFloatLE(1.0f);        // modelScale
                buf.writeByte(0);              // looping
                buf.writeIntLE(0);             // maxSupportDistance
                buf.writeByte(0);              // blockSupportsRequiredFor
                buf.writeByte(0);              // requiresAlphaBlending
                buf.writeByte(0);              // cubeShadingMode
                buf.writeByte(0);              // randomRotation
                buf.writeByte(0);              // variantRotation
                buf.writeByte(0);              // rotationYawPlacementOffset
                buf.writeIntLE(0);             // blockSoundSetIndex
                buf.writeIntLE(0);             // ambientSoundEventIndex
                buf.writeZero(3);              // particleColor
                buf.writeZero(4);              // light
                // tint: 6 x int32 = 24 bytes, server default = -1 (0xFFFFFFFF = no tint)
                for (int t = 0; t < 6; t++) buf.writeIntLE(-1);
                // biomeTint: 6 x int32 = 24 bytes, server default = -1
                for (int t = 0; t < 6; t++) buf.writeIntLE(-1);
                buf.writeIntLE(0);             // group (index 0 = "Air" group)
                buf.writeZero(42);             // movementSettings
                buf.writeZero(2);              // flags
                buf.writeZero(17);             // placementSettings
                buf.writeByte(0);              // ignoreSupportWhenPlaced
                buf.writeIntLE(Integer.MIN_VALUE); // transitionToTag (-2147483648 = no tag, matches server)
                // 25 offset slots — use backpatching for present fields
                int slot0Pos = buf.writerIndex(); buf.writeIntLE(0);   // slot 0: item (backpatch)
                int slot1Pos = buf.writerIndex(); buf.writeIntLE(0);   // slot 1: name (backpatch)
                int slot2Pos = buf.writerIndex(); buf.writeIntLE(0);   // slot 2: shaderEffect (backpatch)
                buf.writeIntLE(-1);  // slot 3: model (null)
                int slot4Pos = buf.writerIndex(); buf.writeIntLE(0);   // slot 4: modelTexture (backpatch)
                buf.writeIntLE(-1);  // slot 5: modelAnimation (null)
                buf.writeIntLE(-1);  // slot 6: support (null)
                buf.writeIntLE(-1);  // slot 7: supporting (null)
                int slot8Pos = buf.writerIndex(); buf.writeIntLE(0);   // slot 8: cubeTextures (backpatch)
                for (int s = 9; s < 20; s++) buf.writeIntLE(-1); // slots 9-19: null
                int slot20Pos = buf.writerIndex(); buf.writeIntLE(0);  // slot 20: interactions (backpatch)
                for (int s = 21; s < 25; s++) buf.writeIntLE(-1); // slots 21-24: null
                // Variable block starts here (offset 264 from entryStart)
                int vbs = buf.writerIndex();

                // Slot 0: item VarString — links block type to its item in the Items dict
                buf.setIntLE(slot0Pos, buf.writerIndex() - vbs);
                pkt.writeString(names[id].toLowerCase()); // item key = lowercase block name

                // Slot 1: name VarString
                buf.setIntLE(slot1Pos, buf.writerIndex() - vbs);
                pkt.writeString(names[id]);

                // Slot 2: shaderEffect — VarInt(1) + byte(0) = ShaderType.None
                buf.setIntLE(slot2Pos, buf.writerIndex() - vbs);
                pkt.writeVarInt(1);
                buf.writeByte(0); // ShaderType.None

                // Slot 4: modelTexture — VarInt(1) + ModelTexture[1]
                // ModelTexture: nullBits(1) + weight(float4) + [texture VarString]
                // Real server uses "Blocks/_Debug/Texture.png" (UNKNOWN_CUSTOM_MODEL_TEXTURE)
                buf.setIntLE(slot4Pos, buf.writerIndex() - vbs);
                pkt.writeVarInt(1); // 1 entry
                buf.writeByte(0x01);         // ModelTexture nullBits: bit0=texture present
                buf.writeFloatLE(1.0f);      // weight
                pkt.writeString("Blocks/_Debug/Texture.png"); // texture VarString

                // Slot 8: cubeTextures — VarInt(1) + BlockTextures[1]
                // BlockTextures: nullBits(1) + weight(float4) + 6 offset slots(24) + variable(6 face VarStrings)
                buf.setIntLE(slot8Pos, buf.writerIndex() - vbs);
                pkt.writeVarInt(1); // 1 entry
                int btStart = buf.writerIndex();
                buf.writeByte(0x3F);         // BlockTextures nullBits: bits 0-5 = all 6 faces present
                buf.writeFloatLE(1.0f);      // weight
                // 6 face offset slots (backpatch)
                int[] faceSlots = new int[6];
                for (int f = 0; f < 6; f++) {
                    faceSlots[f] = buf.writerIndex();
                    buf.writeIntLE(0);
                }
                int btVbs = buf.writerIndex(); // VBS=29 from btStart
                // Write all 6 faces as unknownTex
                for (int f = 0; f < 6; f++) {
                    buf.setIntLE(faceSlots[f], buf.writerIndex() - btVbs);
                    pkt.writeString(unknownTex);
                }

                // Slot 20: interactions — empty Map<InteractionType, Integer>
                // Server ALWAYS sets this (even empty). Client NullRefs without it.
                buf.setIntLE(slot20Pos, buf.writerIndex() - vbs);
                pkt.writeVarInt(0); // 0 entries
            }

            session.sendPacket(pkt);
        }

        // All registry packets must send non-null (but empty) data fields.
        // Sending null fields (nullBits=0) causes client NullReferenceException.

        // BlockHitboxes (ID 41): "Full" unit box at hitbox type index 0.
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(41, ctx.alloc(), 40);
            pkt.writeByte(0x01); // nullBits: entries present
            pkt.writeByte(0);    // type: Init
            pkt.writeIntLE(1);   // maxId = 1
            pkt.writeVarInt(1);  // 1 entry
            pkt.writeIntLE(0);   // key = 0
            pkt.writeVarInt(1);  // 1 hitbox in the array
            pkt.writeFloatLE(0.0f); pkt.writeFloatLE(0.0f); pkt.writeFloatLE(0.0f); // min
            pkt.writeFloatLE(1.0f); pkt.writeFloatLE(1.0f); pkt.writeFloatLE(1.0f); // max
            session.sendPacket(pkt);
        }

        // BlockSoundSets (ID 42): EMPTY at index 0.
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(42, ctx.alloc(), 50);
            pkt.writeByte(0x01); // nullBits: entries present
            pkt.writeByte(0);    // type: Init
            pkt.writeIntLE(1);   // maxId = 1
            pkt.writeVarInt(1);  // 1 entry
            pkt.writeIntLE(0);   // key = 0
            pkt.writeByte(0x02); // entry nullBits: bit1=id present
            pkt.writeZeroes(8);  // moveInRepeatRange
            pkt.writeIntLE(0);   // idOffset: 0
            pkt.writeIntLE(-1);  // soundEventIndicesOffset: null
            pkt.writeVarInt(5);
            pkt.getBuf().writeBytes("EMPTY".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            session.sendPacket(pkt);
        }

        // FBS=6 registries that had no preloaded assets.
        // Init (type=0) requires explicit entries — null array slots cause NullRef.
        // Each entry format depends on the asset type's FBS/VFC/VBS.

        // ItemSoundSet (43): FBS=1, VFC=2, VBS=9. id: nullBits bit0
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(43, ctx.alloc(), 25);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x01);     // nullBits: bit0=id
            pkt.writeIntLE(0);       // slot 0: id → var offset 0
            pkt.writeIntLE(-1);      // slot 1: sounds (null)
            pkt.writeString("Unknown");
            session.sendPacket(pkt);
        }
        // EntityEffect (51): FBS=25, VFC=6, VBS=49. id: nullBits bit0
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(51, ctx.alloc(), 80);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x01);     // nullBits: bit0=id
            pkt.writeZeroes(24);     // 24 fixed bytes
            pkt.writeIntLE(0);       // slot 0: id → var offset 0
            for (int i = 1; i < 6; i++) pkt.writeIntLE(-1);
            pkt.writeString("Unknown");
            session.sendPacket(pkt);
        }
        // ModelVFXs (53): entry=ModelVFX, FBS=49, VFC=1, VBS=49 (inline). id: bit5 (0x20)
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(53, ctx.alloc(), 65);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x20);     // nullBits: bit5=id
            pkt.writeZeroes(48);     // 48 fixed bytes (switchTo through postColorOpacity)
            pkt.writeString("Unknown"); // id (inline, no offset slots)
            session.sendPacket(pkt);
        }
        // Interactions (66): entry=Interaction (polymorphic). SimpleInteraction (typeId=1)
        // SimpleInteraction: FBS=19, VFC=5, VBS=39
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(66, ctx.alloc(), 60);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeVarInt(1);      // typeId = 1 (SimpleInteraction)
            pkt.writeByte(0x00);     // nullBits: no optional fields
            pkt.writeByte(0);        // waitForDataFrom = Client
            pkt.writeFloatLE(0.0f);  // horizontalSpeedMultiplier
            pkt.writeFloatLE(0.0f);  // runTime
            pkt.writeByte(0);        // cancelOnItemChange = false
            pkt.writeIntLE(0x80000000); // next = MIN_VALUE (none)
            pkt.writeIntLE(0x80000000); // failed = MIN_VALUE (none)
            for (int i = 0; i < 5; i++) pkt.writeIntLE(-1); // 5 offset slots (null)
            session.sendPacket(pkt);
        }
        // RootInteractions (67): entry=RootInteraction, FBS=6, VFC=6, VBS=30. id: bit0
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(67, ctx.alloc(), 50);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x01);     // nullBits: bit0=id
            pkt.writeZeroes(5);      // 5 fixed bytes
            pkt.writeIntLE(0);       // slot 0: id → var offset 0
            for (int i = 1; i < 6; i++) pkt.writeIntLE(-1);
            pkt.writeString("Unknown");
            session.sendPacket(pkt);
        }
        // EntityStatTypes (72): entry=EntityStatType, FBS=15, VFC=3, VBS=27. id: bit0
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(72, ctx.alloc(), 55);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x01);     // nullBits: bit0=id
            pkt.writeZeroes(14);     // 14 fixed bytes
            pkt.writeIntLE(0);       // slot 0: id → var offset 0
            pkt.writeIntLE(-1);      // slot 1 (null)
            pkt.writeIntLE(-1);      // slot 2 (null)
            pkt.writeString("Unknown");
            session.sendPacket(pkt);
        }
        // EntityUIComponents (73): entry=EntityUIComponent, FBS=51, VFC=1, VBS=51 (inline). NO id
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(73, ctx.alloc(), 60);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x00);     // nullBits: no id/inline vars
            pkt.writeZeroes(50);     // 50 fixed bytes
            session.sendPacket(pkt);
        }
        // HitboxCollisionConfig (74): entry=HitboxCollisionConfig, FBS=5, VFC=0, VBS=5. NO id, NO vars
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(74, ctx.alloc(), 15);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x00);     // nullBits
            pkt.writeZeroes(4);      // 4 fixed bytes
            session.sendPacket(pkt);
        }
        // RepulsionConfig (75): entry=RepulsionConfig, FBS=12, VFC=0, VBS=12. NO id, NO vars
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(75, ctx.alloc(), 25);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x00);     // nullBits
            pkt.writeZeroes(11);     // 11 fixed bytes
            session.sendPacket(pkt);
        }
        // TagPatterns (84): entry=TagPattern, FBS=6, VFC=2, VBS=14. NO id
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(84, ctx.alloc(), 25);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x00);     // nullBits: no id
            pkt.writeZeroes(5);      // 5 fixed bytes
            pkt.writeIntLE(-1);      // slot 0 (null)
            pkt.writeIntLE(-1);      // slot 1 (null)
            session.sendPacket(pkt);
        }
        // FBS=6 registries WITH preloaded defaults.
        // These use IndexedLookupTableAssetMap and preload a default at index 0.
        // Init (type=0) clears preloaded entries, so we MUST send an explicit entry
        // at key=0 with a non-null id (used as dictionary key by the client).

        // Weather (47): FBS=30 (4 nullBits + NearFar fog(8) + FogOptions(18)), VFC=24, VBS=126
        // REQUIRED — client errors "We have not received the asset types of Weather" without it.
        // Fog MUST be non-null — renderer accesses weather.fog.near/far unconditionally.
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(47, ctx.alloc(), 160);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x05);     // nullBits[0]: bit0=fog + bit2=id
            pkt.writeZeroes(3);      // nullBits[1-3]
            pkt.writeFloatLE(10.0f); // fog.near (NearFar offset 4)
            pkt.writeFloatLE(512.0f);// fog.far  (NearFar offset 8)
            pkt.writeZeroes(18);     // fogOptions (offset 12-29, not present)
            pkt.writeIntLE(0);       // slot 0: id → var offset 0
            for (int i = 1; i < 24; i++) pkt.writeIntLE(-1);
            pkt.writeString("Unknown");
            session.sendPacket(pkt);
        }
        // ItemQuality (55): FBS=7 (1 nullBits + 6 fixed), VFC=7, VBS=35
        // id: nullBits bit1, offset slot 0 (position 7)
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(55, ctx.alloc(), 55);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x02);     // nullBits: bit1=id
            pkt.writeZeroes(6);      // color(3) + rarity(1) + quality(1) + isGlowing(1)
            pkt.writeIntLE(0);       // slot 0: id → var offset 0
            for (int i = 1; i < 7; i++) pkt.writeIntLE(-1);
            pkt.writeString("Default");
            session.sendPacket(pkt);
        }
        // ItemReticleConfig (57): FBS=1 (1 nullBits), VFC=4, VBS=17
        // id: nullBits bit0, offset slot 0 (position 1)
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(57, ctx.alloc(), 35);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x01);     // nullBits: bit0=id
            pkt.writeIntLE(0);       // slot 0: id → var offset 0
            for (int i = 1; i < 4; i++) pkt.writeIntLE(-1);
            pkt.writeString("Default");
            session.sendPacket(pkt);
        }
        // SoundEvent (65): FBS=38 (1 nullBits + 37 fixed), VFC=2, VBS=46
        // id: nullBits bit0, offset slot 0 (position 38)
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(65, ctx.alloc(), 65);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x01);     // nullBits: bit0=id
            pkt.writeFloatLE(1.0f);  // volume
            pkt.writeFloatLE(1.0f);  // pitch
            pkt.writeZeroes(8);      // musicDuckingVolume + ambientDuckingVolume
            pkt.writeIntLE(1);       // maxInstance
            pkt.writeByte(0);        // preventSoundInterruption
            pkt.writeZeroes(8);      // startAttenuationDistance + maxDistance
            pkt.writeFloatLE(1.0f);  // spatialBlend
            pkt.writeIntLE(0);       // audioCategory index
            pkt.writeIntLE(0);       // slot 0: id → var offset 0
            pkt.writeIntLE(-1);      // slot 1: layers (null)
            pkt.writeString("Empty");
            session.sendPacket(pkt);
        }
        // SoundSet (79): FBS=2 (1 nullBits + 1 soundCategory), VFC=2, VBS=10
        // id: nullBits bit0, offset slot 0 (position 2)
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(79, ctx.alloc(), 25);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x01);     // nullBits: bit0=id
            pkt.writeByte(0);        // soundCategory = 0
            pkt.writeIntLE(0);       // slot 0: id → var offset 0
            pkt.writeIntLE(-1);      // slot 1: sounds (null)
            pkt.writeString("Empty");
            session.sendPacket(pkt);
        }
        // AudioCategory (80): FBS=5 (1 nullBits + 4 volume), VFC=1, VBS=5 (inline, no offset slots)
        // id: nullBits bit0, inline at position 5
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(80, ctx.alloc(), 20);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x01);     // nullBits: bit0=id
            pkt.writeFloatLE(1.0f);  // volume
            pkt.writeString("Empty"); // id (inline, no offset slot)
            session.sendPacket(pkt);
        }
        // ReverbEffect (81): FBS=54 (1 nullBits + 52 floats + 1 bool), VFC=1, VBS=54 (inline)
        // id: nullBits bit0, inline at position 54
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(81, ctx.alloc(), 75);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x01);     // nullBits: bit0=id
            pkt.writeZeroes(53);     // 13 floats + 1 bool
            pkt.writeString("Empty"); // id (inline)
            session.sendPacket(pkt);
        }
        // EqualizerEffect (82): FBS=41 (1 nullBits + 10 floats), VFC=1, VBS=41 (inline)
        // id: nullBits bit0, inline at position 41
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(82, ctx.alloc(), 60);
            pkt.writeByte(0x01); pkt.writeByte(0); pkt.writeIntLE(1);
            pkt.writeVarInt(1); pkt.writeIntLE(0);
            pkt.writeByte(0x01);     // nullBits: bit0=id
            pkt.writeZeroes(40);     // 10 floats
            pkt.writeString("Empty"); // id (inline)
            session.sendPacket(pkt);
        }

        // UpdateFluidFX (ID 63): EMPTY at index 0. FBS=61, VFC=2
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(63, ctx.alloc(), 90);
            pkt.writeByte(0x01); // nullBits: entries present
            pkt.writeByte(0);    // type: Init
            pkt.writeIntLE(1);   // maxId = 1
            pkt.writeVarInt(1);  // 1 entry
            pkt.writeIntLE(0);   // key = 0
            pkt.writeByte(0x10);     // nullBits: bit4=id present
            pkt.writeByte(0);        // shader: ShaderType.None
            pkt.writeByte(0);        // fogMode: FluidFog.Color
            pkt.writeZeroes(3);      // fogColor (null)
            pkt.writeZeroes(8);      // fogDistance NearFar (null)
            pkt.writeFloatLE(40.0f); // fogDepthStart
            pkt.writeFloatLE(10.0f); // fogDepthFalloff
            pkt.writeZeroes(3);      // colorFilter (null)
            pkt.writeFloatLE(1.0f);  // colorSaturation
            pkt.writeFloatLE(0.0f);  // distortionAmplitude
            pkt.writeFloatLE(0.0f);  // distortionFrequency
            pkt.writeZeroes(24);     // movementSettings (null)
            pkt.writeIntLE(0);       // id offset → var offset 0
            pkt.writeIntLE(-1);      // particle offset (null)
            pkt.writeString("Empty");
            session.sendPacket(pkt);
        }

        // UpdateFluids (ID 83): EMPTY + UNKNOWN. FBS=23, VFC=6
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(83, ctx.alloc(), 160);
            pkt.writeByte(0x01); // nullBits: entries present
            pkt.writeByte(0);    // type: Init
            pkt.writeIntLE(2);   // maxId = 2
            pkt.writeVarInt(2);  // 2 entries

            // Entry 0: Fluid.EMPTY
            pkt.writeIntLE(0);
            pkt.writeByte(0x14);     // nullBits: bit2=id, bit4=shaderEffect
            pkt.writeIntLE(0);       // maxFluidLevel
            pkt.writeByte(0);        // requiresAlphaBlending
            pkt.writeByte(0);        // opacity = Solid
            pkt.writeZeroes(4);      // light (null)
            pkt.writeByte(0);        // drawType = Empty
            pkt.writeIntLE(0);       // fluidFXIndex
            pkt.writeIntLE(0);       // blockSoundSetIndex
            pkt.writeZeroes(3);      // particleColor (null)
            byte[] emptyId = "Empty".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            int emptyIdLen = varIntSize(emptyId.length) + emptyId.length;
            pkt.writeIntLE(0);           // id offset
            pkt.writeIntLE(-1);          // cubeTextures (null)
            pkt.writeIntLE(emptyIdLen);  // shaderEffect offset
            pkt.writeIntLE(-1);          // particles (null)
            pkt.writeIntLE(-1);          // blockParticleSetId (null)
            pkt.writeIntLE(-1);          // tagIndexes (null)
            pkt.writeString("Empty");
            pkt.writeVarInt(1);          // shaderEffect array count
            pkt.writeByte(0);            // ShaderType.None

            // Entry 1: Fluid.UNKNOWN
            pkt.writeIntLE(1);
            pkt.writeByte(0x14);
            pkt.writeIntLE(0);
            pkt.writeByte(0);
            pkt.writeByte(0);
            pkt.writeZeroes(4);
            pkt.writeByte(1);        // drawType = Cube
            pkt.writeIntLE(0);
            pkt.writeIntLE(0);
            pkt.writeZeroes(3);
            byte[] unknownId = "Unknown".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            int unknownIdLen = varIntSize(unknownId.length) + unknownId.length;
            pkt.writeIntLE(0);
            pkt.writeIntLE(-1);
            pkt.writeIntLE(unknownIdLen);
            pkt.writeIntLE(-1);
            pkt.writeIntLE(-1);
            pkt.writeIntLE(-1);
            pkt.writeString("Unknown");
            pkt.writeVarInt(1);
            pkt.writeByte(0);
            session.sendPacket(pkt);
        }

        // UpdateAmbienceFX (ID 62): EMPTY at index 0. FBS=18, VFC=6
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(62, ctx.alloc(), 60);
            pkt.writeByte(0x01); // nullBits: entries present
            pkt.writeByte(0);    // type: Init
            pkt.writeIntLE(1);   // maxId = 1
            pkt.writeVarInt(1);  // 1 entry
            pkt.writeIntLE(0);   // key = 0
            pkt.writeByte(0x02);     // nullBits: bit1=id present
            pkt.writeZeroes(9);      // soundEffect (null)
            pkt.writeIntLE(0);       // priority
            pkt.writeIntLE(0);       // audioCategoryIndex
            pkt.writeIntLE(0);       // id offset
            pkt.writeIntLE(-1);      // conditions (null)
            pkt.writeIntLE(-1);      // sounds (null)
            pkt.writeIntLE(-1);      // music (null)
            pkt.writeIntLE(-1);      // ambientBed (null)
            pkt.writeIntLE(-1);      // blockedAmbienceFxIndices (null)
            pkt.writeString("Empty");
            session.sendPacket(pkt);
        }

        // FBS=2 VFC=1 packets: nullBits(1)=1 + type(1)=0 + VarInt(0)
        // Excludes VFC=2 packets (49, 50, 85), packet 45 (BlockBreakingDecals), packet 78 (BlockGroups)
        int[] fbs2vfc1 = {44, 46, 48, 52, 56, 58, 59, 68};
        for (int id : fbs2vfc1) {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(id, ctx.alloc(), 3);
            pkt.writeByte(0x01); // nullBits: bit0=entries present (non-null)
            pkt.writeByte(0);    // type: Init
            pkt.writeVarInt(0);  // 0 entries
            session.sendPacket(pkt);
        }

        // BlockGroups (78) moved to after Items+Recipes — see below

        // UpdateBlockBreakingDecals (ID 45): FBS=2, VFC=1, String-keyed.
        {
            String[] crackTextures = {
                "BlockTextures/Cracks/T_Crack_Generic_01.png",
                "BlockTextures/Cracks/T_Crack_Generic_02.png",
                "BlockTextures/Cracks/T_Crack_Generic_03.png",
                "BlockTextures/Cracks/T_Crack_Generic_04.png",
                "BlockTextures/Cracks/T_Crack_Generic_05.png",
                "BlockTextures/Cracks/T_Crack_Generic_06.png",
                "BlockTextures/Cracks/T_Crack_Generic_07.png",
            };
            HytalePacketBuffer pkt = HytalePacketBuffer.create(45, ctx.alloc(), 400);
            pkt.writeByte(0x01); // nullBits: bit0=entries present
            pkt.writeByte(0);    // type: Init
            pkt.writeVarInt(1);  // 1 entry
            pkt.writeString("Unknown");
            pkt.writeByte(0x01); // nullBits: bit0=stageTextures present
            pkt.writeVarInt(crackTextures.length);
            for (String tex : crackTextures) {
                pkt.writeString(tex);
            }
            session.sendPacket(pkt);
        }

        // ViewBobbing (76), CameraShake (77), Emotes (86) are NOT sent by the
        // real server's AssetRegistryLoader — omitted intentionally.

        // FBS=2 VFC=2 packets (49 ParticleSystems, 50 ParticleSpawners, 85 ProjectileConfigs):
        // nullBits(1) + type(1) + offset1(4) + offset2(4) + VarInt(0) + VarInt(0)
        // bit0=entries present, bit1=removed present (both empty)
        for (int id : new int[]{49, 50, 85}) {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(id, ctx.alloc(), 12);
            pkt.writeByte(0x03); // nullBits: bit0+bit1 present
            pkt.writeByte(0);    // type: Init
            pkt.writeIntLE(0);   // offset1: 0 (data at varBlockStart)
            pkt.writeIntLE(1);   // offset2: 1 (after first VarInt(0))
            pkt.writeVarInt(0);  // entries: 0
            pkt.writeVarInt(0);  // removed: 0
            session.sendPacket(pkt);
        }

        // UpdateItems (ID 54): NBFS=1, FBS=4, VFC=2, VBS=12
        // Map<String, ItemBase> items — STRING keys, not int!
        // ItemBase: NBFS=5, FBS=148, VFC=28, VBS=260
        // Each block type gets a corresponding item (cross-processing requires this).
        {
            String[] itemKeys = {"empty", "unknown", "debug_cube", "debug_model"};
            int[] blockIds    = {0,       1,         2,            3};

            HytalePacketBuffer items = HytalePacketBuffer.create(54, ctx.alloc(), 1600);
            io.netty.buffer.ByteBuf ibuf = items.getBuf();
            ibuf.writeByte(0x03);    // nullBits: bit0=items, bit1=removed
            ibuf.writeByte(0);       // type: Init
            ibuf.writeByte(1);       // updateModels = true
            ibuf.writeByte(1);       // updateIcons = true
            int itemsSlot = ibuf.writerIndex();
            ibuf.writeIntLE(0);      // items offset (backpatch)
            int removedSlot = ibuf.writerIndex();
            ibuf.writeIntLE(0);      // removedItems offset (backpatch)
            int outerVBS = ibuf.writerIndex(); // VBS=12 from packet start

            // items dict
            ibuf.setIntLE(itemsSlot, ibuf.writerIndex() - outerVBS);
            items.writeVarInt(itemKeys.length);

            for (int idx = 0; idx < itemKeys.length; idx++) {
                items.writeString(itemKeys[idx]); // STRING key

                // ItemBase entry (NBFS=5, FBS=148, VFC=28, VBS=260)
                ibuf.writeByte(0x20);    // nullBits[0]: bit5=id
                ibuf.writeByte(0);       // nullBits[1]
                ibuf.writeByte(0x04);    // nullBits[2]: bit2=itemEntity (slot 13)
                ibuf.writeByte(0x02 | 0x04 | 0x08); // nullBits[3]: interactions+interactionVars+interactionConfig
                ibuf.writeByte(0);       // nullBits[4]
                ibuf.writeFloatLE(1.0f); // scale
                ibuf.writeByte(0);       // usePlayerAnimations
                ibuf.writeIntLE(1);      // maxStack
                ibuf.writeIntLE(0);      // reticleIndex
                ibuf.writeZero(25);      // iconProperties inline
                ibuf.writeIntLE(0);      // itemLevel
                ibuf.writeIntLE(0);      // qualityIndex
                ibuf.writeByte(0);       // consumable
                ibuf.writeByte(0);       // variant
                ibuf.writeIntLE(blockIds[idx]); // blockId — links to block type
                ibuf.writeZero(16);      // gliderConfig
                ibuf.writeZero(4);       // blockSelectorTool
                ibuf.writeZero(4);       // light
                ibuf.writeDoubleLE(0.0); // durability
                ibuf.writeIntLE(0);      // soundEventIndex
                ibuf.writeIntLE(0);      // itemSoundSetIndex
                ibuf.writeZero(49);      // pullbackConfig
                ibuf.writeByte(0);       // clipsGeometry
                ibuf.writeByte(0);       // renderDeployablePreview
                // 28 offset slots
                int islot0 = ibuf.writerIndex();
                ibuf.writeIntLE(0);      // slot 0: id (backpatch)
                for (int s = 1; s < 13; s++) ibuf.writeIntLE(-1);
                int islot13 = ibuf.writerIndex();
                ibuf.writeIntLE(0);      // slot 13: itemEntity (backpatch)
                for (int s = 14; s < 20; s++) ibuf.writeIntLE(-1);
                int islot20 = ibuf.writerIndex();
                ibuf.writeIntLE(0);      // slot 20: interactions (backpatch)
                int islot21 = ibuf.writerIndex();
                ibuf.writeIntLE(0);      // slot 21: interactionVars (backpatch)
                int islot22 = ibuf.writerIndex();
                ibuf.writeIntLE(0);      // slot 22: interactionConfig (backpatch)
                for (int s = 23; s < 28; s++) ibuf.writeIntLE(-1);
                int itemVBS = ibuf.writerIndex();

                // Slot 0: id
                ibuf.setIntLE(islot0, ibuf.writerIndex() - itemVBS);
                items.writeString(itemKeys[idx]);
                // Slot 13: itemEntity
                ibuf.setIntLE(islot13, ibuf.writerIndex() - itemVBS);
                ibuf.writeByte(0); ibuf.writeZero(3); ibuf.writeByte(0);
                // Slot 20: interactions
                ibuf.setIntLE(islot20, ibuf.writerIndex() - itemVBS);
                items.writeVarInt(0);
                // Slot 21: interactionVars
                ibuf.setIntLE(islot21, ibuf.writerIndex() - itemVBS);
                items.writeVarInt(0);
                // Slot 22: interactionConfig
                ibuf.setIntLE(islot22, ibuf.writerIndex() - itemVBS);
                ibuf.writeByte(0); ibuf.writeByte(0); ibuf.writeByte(0); ibuf.writeByte(0);
                ibuf.writeIntLE(-1); ibuf.writeIntLE(-1);
            }

            // removedItems array
            ibuf.setIntLE(removedSlot, ibuf.writerIndex() - outerVBS);
            items.writeVarInt(0);
            session.sendPacket(items);
        }

        // UpdateRecipes (ID 60): FBS=2 VFC=2 VBS=10
        // nullBits(1) + type(1) + offset1(4) + offset2(4) + VarInt(0) + VarInt(0)
        HytalePacketBuffer recipes = HytalePacketBuffer.create(60, ctx.alloc(), 12);
        recipes.writeByte(0x03); // nullBits: bit0=recipes, bit1=removed
        recipes.writeByte(0);    // type: Init
        recipes.writeIntLE(0);   // recipes offset: 0
        recipes.writeIntLE(1);   // removedRecipes offset: 1
        recipes.writeVarInt(0);  // recipes: 0 entries
        recipes.writeVarInt(0);  // removedRecipes: 0 entries
        session.sendPacket(recipes);

        // UpdateEnvironments (ID 61): FBS=7 VFC=1
        // MUST be sent BEFORE BlockGroups — the "BlockTypes, Items" cross-processing
        // triggered by BlockGroups needs Environment to already be loaded.
        // Real server sends Environment at position 11, BlockGroups at position 32.
        {
            HytalePacketBuffer envs = HytalePacketBuffer.create(61, ctx.alloc(), 60);
            envs.writeByte(0x01);    // nullBits: bit0=entries present
            envs.writeByte(0);       // type: Init
            envs.writeIntLE(1);      // maxId = 1 (next free index)
            envs.writeByte(1);       // rebuildMapGeometry = true
            envs.writeVarInt(1);     // 1 entry

            // Entry key = 0 (UNKNOWN_ID)
            envs.writeIntLE(0);

            // WorldEnvironment: FBS=4 (nullBits(1) + waterTint(3)) + 3 offset slots(12) = VBS=16
            envs.writeByte(0x03);    // nullBits: waterTint + id present
            // waterTint = Color(10, 51, 85)
            envs.writeByte(10);
            envs.writeByte(51);
            envs.writeByte(85);
            // 3 offset slots
            envs.writeIntLE(0);      // idOffset → var offset 0
            envs.writeIntLE(-1);     // fluidParticlesOffset: null
            envs.writeIntLE(-1);     // tagIndexesOffset: null
            // Variable data: id = "Unknown"
            envs.writeString("Unknown");

            session.sendPacket(envs);
        }

        // BlockGroups (78): Must list ALL block type names in their groups.
        // Triggers "BlockTypes, Items" cross-processing on the client.
        {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(78, ctx.alloc(), 80);
            pkt.writeByte(0x01); // nullBits: groups present
            pkt.writeByte(0);    // type: Init
            pkt.writeVarInt(1);  // 1 group entry
            pkt.writeString("Air"); // STRING key
            // BlockGroup: VBS=1 (inline, no offset slots). Format: nullBits(1) + names[]
            pkt.writeByte(0x01); // nullBits: bit0=names present
            pkt.writeVarInt(4);  // ALL 4 block type names in this group
            pkt.writeString("Empty");
            pkt.writeString("Unknown");
            pkt.writeString("Debug_Cube");
            pkt.writeString("Debug_Model");
            session.sendPacket(pkt);
        }

        // UpdateTranslations (ID 64): FBS=2 VFC=1
        HytalePacketBuffer translations = HytalePacketBuffer.create(64, ctx.alloc(), 3);
        translations.writeByte(0x01); // nullBits: bit0=entries present
        translations.writeByte(0);    // type: Init
        translations.writeVarInt(0);  // 0 entries
        session.sendPacket(translations);

        // Send WorldLoadProgress (ID 21)
        HytalePacketBuffer progress = HytalePacketBuffer.create(PACKET_WORLD_LOAD_PROGRESS, ctx.alloc(), 9);
        progress.writeByte(0);    // nullBits: no status message
        progress.writeIntLE(0);   // percentComplete
        progress.writeIntLE(0);   // percentCompleteSubitem
        session.sendPacket(progress);

        // WorldLoadFinished (ID 22): triggers SettingUp → Playing transition.
        // Must be sent BEFORE JoinWorld — client rejects JoinWorld during SettingUp
        // ("Received JoinWorld at SettingUp connection stage but expected it only during Playing").
        HytalePacketBuffer loadFinished = HytalePacketBuffer.create(PACKET_WORLD_LOAD_FINISHED, ctx.alloc());
        session.sendPacket(loadFinished);

        // Send the FULL join sequence immediately after WorldLoadFinished.
        // The render loop needs: world (JoinWorld), weather, sun, time dilation,
        // features, post-FX, update rate, and entity data — ALL before the first frame.
        // Deferring any of these causes render loop crashes.
        sendPreJoinSequence(ctx);

        // JoinWorld (104)
        {
            HytalePacketBuffer joinWorld = HytalePacketBuffer.create(PACKET_JOIN_WORLD, ctx.alloc(), 18);
            joinWorld.writeBoolean(true);  // clearWorld
            joinWorld.writeBoolean(false); // fadeInOut
            joinWorld.writeUUID(UUID.nameUUIDFromBytes("rdforward-world".getBytes()));
            session.sendPacket(joinWorld);
        }
        joinWorldSent = true;

        // UpdateWeather MUST immediately follow JoinWorld
        {
            HytalePacketBuffer weather = HytalePacketBuffer.create(149, ctx.alloc(), 8);
            weather.writeIntLE(0);        // weatherIndex: 0
            weather.writeFloatLE(0.0f);   // transitionSeconds: instant
            session.sendPacket(weather);
        }
        // SunSettings
        {
            HytalePacketBuffer sun = HytalePacketBuffer.create(PACKET_UPDATE_SUN_SETTINGS, ctx.alloc(), 8);
            sun.writeFloatLE(0.5f);
            sun.writeFloatLE(0.0f);
            session.sendPacket(sun);
        }

        // Send ALL Phase 6 state immediately — don't wait for ClientReady.
        sendGameState(ctx);

        System.out.println("[Hytale] Sent full join sequence + game state to "
                + session.getUsername());
    }

    /**
     * Send pre-JoinWorld packets after WorldLoadFinished.
     * Real server sends these during PlayerConnectEvent (Universe.addPlayer).
     * JoinWorld is deferred to handlePlayerOptions() to match real server timing.
     */
    private void sendPreJoinSequence(ChannelHandlerContext ctx) {
        // VoiceConfig (452): FBS=17, no nullBits
        {
            HytalePacketBuffer vc = HytalePacketBuffer.create(PACKET_VOICE_CONFIG, ctx.alloc(), 17);
            vc.writeByte(1);           // voiceEnabled = true
            vc.writeByte(0);           // codec = Opus
            vc.writeIntLE(48000);      // sampleRate
            vc.writeByte(1);           // channels = 1
            vc.writeFloatLE(32.0f);    // maxHearingDistance
            vc.writeFloatLE(4.0f);     // referenceDistance
            vc.writeByte(1);           // supportsVoiceStream = true
            vc.writeByte(60);          // maxPacketsPerSecond
            session.sendPacket(vc);
        }

        // AddToServerPlayerList (224)
        {
            UUID playerUuid = session.getPlayerUUID();
            UUID worldUuid = UUID.nameUUIDFromBytes("rdforward-world".getBytes());

            HytalePacketBuffer pl = HytalePacketBuffer.create(PACKET_ADD_TO_PLAYER_LIST, ctx.alloc(), 60);
            pl.writeByte(0x01);  // nullBits: bit0=players present
            pl.writeVarInt(1);   // 1 player entry
            pl.writeByte(0x03);  // entry nullBits: bit0=worldUuid, bit1=username
            pl.writeUUID(playerUuid);
            pl.writeUUID(worldUuid);
            pl.writeIntLE(0);          // ping = 0ms
            pl.writeString(session.getUsername());
            session.sendPacket(pl);
        }

        // ServerTags (34): empty tags map
        {
            HytalePacketBuffer tags = HytalePacketBuffer.create(34, ctx.alloc(), 3);
            tags.writeByte(0x01); // nullBits: tags present
            tags.writeVarInt(0);  // 0 tags
            session.sendPacket(tags);
        }
    }

    /**
     * Full join sequence: pre-join packets + JoinWorld + Weather + Sun.
     * Called from handleViewRadius/handlePlayerOptions — AFTER the client has
     * finished its Prepare steps (PrepareEntitiesAtlas etc.) and sent ViewRadius.
     * EntityUpdates are deferred to handleClientReady(readyForChunks=true).
     */
    private void sendJoinSequence(ChannelHandlerContext ctx) {
        sendPreJoinSequence(ctx);

        // JoinWorld (104): FBS=18, no nullBits
        {
            HytalePacketBuffer joinWorld = HytalePacketBuffer.create(PACKET_JOIN_WORLD, ctx.alloc(), 18);
            joinWorld.writeBoolean(true);  // clearWorld
            joinWorld.writeBoolean(false); // fadeInOut
            joinWorld.writeUUID(UUID.nameUUIDFromBytes("rdforward-world".getBytes()));
            session.sendPacket(joinWorld);
        }
        // UpdateWeather + SunSettings already sent after WorldLoadFinished.

        System.out.println("[Hytale] Sent JoinWorld to " + session.getUsername()
                + " (awaiting ClientReady)");
    }

    /**
     * Send game state after ClientReady (readyForChunks). Matches real server Phase 6→7:
     * ViewRadius + SetEntitySeed + SetClientId + EntityUpdates.
     */
    private void sendGameState(ChannelHandlerContext ctx) {
        if (gameStateSent) return;
        gameStateSent = true;
        // ViewRadius (32): FBS=4, no nullBits
        {
            HytalePacketBuffer vr = HytalePacketBuffer.create(PACKET_VIEW_RADIUS, ctx.alloc(), 4);
            vr.writeIntLE(4); // view radius in super-chunks
            session.sendPacket(vr);
        }
        // SetEntitySeed (160): FBS=4, no nullBits
        {
            HytalePacketBuffer seed = HytalePacketBuffer.create(160, ctx.alloc(), 4);
            seed.writeIntLE(12345);
            session.sendPacket(seed);
        }
        // SetClientId (100)
        {
            HytalePacketBuffer clientId = HytalePacketBuffer.create(PACKET_SET_CLIENT_ID, ctx.alloc(), 4);
            clientId.writeIntLE(session.getEntityNetworkId());
            session.sendPacket(clientId);
        }
        // EntityUpdates (161): create player entity with all required components.
        {
            int entityId = session.getEntityNetworkId();
            int spawnX = world.getSpawnX();
            int spawnZ = world.getSpawnZ();
            int[] safe = world.findSafePosition(spawnX, world.getHeight() * 2 / 3, spawnZ, 50);
            double px = safe[0] + 0.5, py = safe[1], pz = safe[2] + 0.5;

            HytalePacketBuffer eu = HytalePacketBuffer.create(PACKET_ENTITY_UPDATES, ctx.alloc(), 256);
            io.netty.buffer.ByteBuf buf = eu.getBuf();

            // EntityUpdates fixed block (9 bytes)
            buf.writeByte(0x02);    // nullBits: bit1=updates present
            buf.writeIntLE(-1);     // removed offset = -1 (null)
            int euUpdatesSlot = buf.writerIndex();
            buf.writeIntLE(0);      // updates offset (backpatch)
            int euVarStart = buf.writerIndex();

            buf.setIntLE(euUpdatesSlot, buf.writerIndex() - euVarStart);
            eu.writeVarInt(1);      // 1 entity update

            // EntityUpdate fixed block (13 bytes)
            buf.writeByte(0x02);    // nullBits: bit1=updates present
            buf.writeIntLE(entityId);
            buf.writeIntLE(-1);     // removed offset = -1 (null)
            int entUpdatesSlot = buf.writerIndex();
            buf.writeIntLE(0);      // updates offset (backpatch)
            int entVarStart = buf.writerIndex();

            buf.setIntLE(entUpdatesSlot, buf.writerIndex() - entVarStart);
            eu.writeVarInt(4);      // 4 component updates: Model + PlayerSkin + Equipment + Transform

            // ComponentUpdate 1: type 3 = ModelUpdate
            eu.writeVarInt(3);
            eu.writeByte(0x01);         // ModelUpdate nullBits: bit0 = model present
            eu.writeFloatLE(1.0f);      // entityScale
            // Model struct: NBFS=2, FBS=51, VFC=12, VBS=99
            // Server ALWAYS sets texture (never null) — NullRef without it
            eu.writeByte(0x04 | 0x10);  // Model nullBits[0]: bit2=assetId, bit4=texture
            eu.writeByte(0x00);         // Model nullBits[1]: nothing
            eu.writeFloatLE(1.0f);      // scale
            eu.writeFloatLE(1.62f);     // eyeHeight
            eu.writeFloatLE(0.0f);      // crouchOffset
            eu.writeFloatLE(0.0f);      // sittingOffset
            eu.writeFloatLE(0.0f);      // sleepingOffset
            eu.writeZeroes(24);         // hitbox (null, zeroed)
            eu.writeZeroes(4);          // light (null, zeroed)
            eu.writeByte(0);            // phobia = None
            // 12 offset slots
            eu.writeIntLE(0);           // offset[0]: assetId → varBlockStart+0
            eu.writeIntLE(-1);          // offset[1]: path (null)
            eu.writeIntLE(-1);          // offset[2]: texture (backpatch below)
            for (int i = 3; i < 12; i++) eu.writeIntLE(-1); // remaining: null
            int modelVarStart = buf.writerIndex();
            // Slot 0: assetId
            buf.setIntLE(modelVarStart - 48, 0); // slot[0] already set to 0 above
            eu.writeString("Player");
            // Slot 2: texture — backpatch offset[2]
            int textureOffset = buf.writerIndex() - modelVarStart;
            buf.setIntLE(modelVarStart - 40, textureOffset); // slot[2] is at -40 from varStart
            eu.writeString("Characters/_Debug/Texture.png");

            // ComponentUpdate 2: type 4 = PlayerSkinUpdate
            // FBS=1, VBS=1. Null skin (no inline PlayerSkin struct).
            eu.writeVarInt(4);
            eu.writeByte(0x00);         // nullBits: no skin

            // ComponentUpdate 3: type 7 = EquipmentUpdate
            // FBS=1, VFC=3, VBS=13. All null (no armor, no hands).
            eu.writeVarInt(7);
            eu.writeByte(0x00);         // nullBits: no armorIds, no rightHand, no leftHand
            eu.writeIntLE(-1);          // slot 0: armorIds (null)
            eu.writeIntLE(-1);          // slot 1: rightHandItemId (null)
            eu.writeIntLE(-1);          // slot 2: leftHandItemId (null)

            // ComponentUpdate 4: type 9 = TransformUpdate
            // FBS=49, VBS=49. ModelTransform inline.
            eu.writeVarInt(9);
            eu.writeByte(0x07);     // nullBits: position + bodyOrientation + lookOrientation
            eu.writeDoubleLE(px);
            eu.writeDoubleLE(py);
            eu.writeDoubleLE(pz);
            eu.writeFloatLE(0.0f);  // body yaw
            eu.writeFloatLE(0.0f);  // body pitch
            eu.writeFloatLE(0.0f);  // body roll
            eu.writeFloatLE(0.0f);  // look yaw
            eu.writeFloatLE(0.0f);  // look pitch
            eu.writeFloatLE(0.0f);  // look roll

            session.sendPacket(eu);
        }
        // SetClientId already sent above (before EntityUpdates)

        // ClientTeleport (109): send spawn position.
        sendSpawnTeleport(ctx);

        // SetTimeDilation (30): FBS=4, no nullBits
        {
            HytalePacketBuffer td = HytalePacketBuffer.create(30, ctx.alloc(), 4);
            td.writeFloatLE(1.0f);
            session.sendPacket(td);
        }
        // UpdateFeatures (31): FBS=1, nullBits(1)
        {
            HytalePacketBuffer feat = HytalePacketBuffer.create(31, ctx.alloc(), 3);
            feat.writeByte(0x01); // nullBits: features present
            feat.writeVarInt(0);  // empty features map
            session.sendPacket(feat);
        }
        // UpdateSunSettings (360): FBS=8, no nullBits
        {
            HytalePacketBuffer sun = HytalePacketBuffer.create(PACKET_UPDATE_SUN_SETTINGS, ctx.alloc(), 8);
            sun.writeFloatLE(0.5f);  // heightPercentage (midday sun position)
            sun.writeFloatLE(0.0f);  // angleRadians
            session.sendPacket(sun);
        }
        // UpdatePostFxSettings (361): FBS=20, no nullBits
        {
            HytalePacketBuffer pfx = HytalePacketBuffer.create(PACKET_UPDATE_POST_FX_SETTINGS, ctx.alloc(), 20);
            pfx.writeFloatLE(1.0f);  // globalIntensity
            pfx.writeFloatLE(1.0f);  // power
            pfx.writeFloatLE(1.0f);  // sunshaftScale
            pfx.writeFloatLE(1.0f);  // sunIntensity
            pfx.writeFloatLE(0.5f);  // sunshaftIntensity
            session.sendPacket(pfx);
        }
        // SetUpdateRate (29): FBS=4, no nullBits
        {
            HytalePacketBuffer rate = HytalePacketBuffer.create(29, ctx.alloc(), 4);
            rate.writeIntLE(20); // 20 TPS
            session.sendPacket(rate);
        }
        // UpdateTimeSettings (145): FBS=10, no nullBits
        {
            HytalePacketBuffer ts = HytalePacketBuffer.create(145, ctx.alloc(), 10);
            ts.writeIntLE(600);     // daytimeDurationSeconds (10 min)
            ts.writeIntLE(600);     // nighttimeDurationSeconds (10 min)
            ts.writeByte(8);        // totalMoonPhases
            ts.writeByte(0);        // timePaused = false
            session.sendPacket(ts);
        }
        // UpdateTime (146): FBS=13, nullBits(1) + InstantData(long seconds + int nanos)
        {
            HytalePacketBuffer time = HytalePacketBuffer.create(146, ctx.alloc(), 13);
            time.writeByte(0x01);       // nullBits: gameTime present
            time.writeLongLE(300L);     // seconds (midday)
            time.writeIntLE(0);         // nanos
            session.sendPacket(time);
        }
        // UpdateWorldMapSettings (240): FBS=20, nullBits(1)
        {
            HytalePacketBuffer wms = HytalePacketBuffer.create(PACKET_UPDATE_WORLD_MAP_SETTINGS, ctx.alloc(), 20);
            wms.writeByte(0x00);         // nullBits: no biomeDataMap (bit0=0)
            wms.writeByte(1);            // enabled = true
            wms.writeByte(0);            // allowTeleportToCoordinates = false
            wms.writeByte(0);            // allowTeleportToMarkers = false
            wms.writeByte(1);            // allowShowOnMapToggle = true
            wms.writeByte(1);            // allowCompassTrackingToggle = true
            wms.writeByte(0);            // allowCreatingMapMarkers = false
            wms.writeByte(0);            // allowRemovingOtherPlayersMarkers = false
            wms.writeFloatLE(32.0f);     // defaultScale
            wms.writeFloatLE(2.0f);      // minScale
            wms.writeFloatLE(256.0f);    // maxScale
            session.sendPacket(wms);
        }
        // SetGameMode (101): FBS=1, no nullBits
        {
            HytalePacketBuffer gm = HytalePacketBuffer.create(PACKET_SET_GAME_MODE, ctx.alloc(), 1);
            gm.writeByte(1); // Creative
            session.sendPacket(gm);
        }
        // UpdateWeather (149): FBS=8, Default channel, not compressed.
        // Sets the active weather to index 0 in our Weather registry (sent during registry phase).
        {
            HytalePacketBuffer weather = HytalePacketBuffer.create(149, ctx.alloc(), 8);
            weather.writeIntLE(0);        // weatherIndex: 0 (our Weather registry entry)
            weather.writeFloatLE(0.0f);   // transitionSeconds: instant transition
            session.sendPacket(weather);
        }

        System.out.println("[Hytale] Sent game state to " + session.getUsername()
                + " (SetClientId + EntityUpdates, awaiting ClientReady readyForGameplay)");
    }

    /**
     * Handle PlayerOptions (ID 33) from client.
     * Client sends this after Prepare phases complete. Now safe to send JoinWorld.
     */
    private void handlePlayerOptions(ChannelHandlerContext ctx, HytalePacketBuffer msg) {
        System.out.println("[Hytale] Received PlayerOptions from " + session.getUsername());
        if (!joinWorldSent) {
            joinWorldSent = true;
            sendPreJoinSequence(ctx);
            {
                HytalePacketBuffer joinWorld = HytalePacketBuffer.create(PACKET_JOIN_WORLD, ctx.alloc(), 18);
                joinWorld.writeBoolean(true);  // clearWorld
                joinWorld.writeBoolean(false); // fadeInOut
                joinWorld.writeUUID(UUID.nameUUIDFromBytes("rdforward-world".getBytes()));
                session.sendPacket(joinWorld);
            }
            System.out.println("[Hytale] Sent JoinWorld to " + session.getUsername()
                    + " (awaiting ClientReady)");
        }
    }

    /** Send ConnectAccept (development flow only — release clients use auth flow). */
    private void sendConnectAcceptAndRegistries(ChannelHandlerContext ctx) {
        // Send ConnectAccept (ID 14): nullBits(1) + no password challenge
        HytalePacketBuffer accept = HytalePacketBuffer.create(PACKET_CONNECT_ACCEPT, ctx.alloc());
        accept.writeByte(0); // nullBits: no password challenge
        session.sendPacket(accept);
        // Dev flow incomplete — release clients use auth + RequestAssets flow
    }

    /**
     * Handle ClientReady (ID 105) from client.
     * Fixed: byte readyForChunks + byte readyForGameplay (2 bytes total).
     *
     * Real server flow (World.onFinishPlayerJoining): after ClientReady, sends
     * ViewRadius + SetEntitySeed + SetClientId + SetTimeDilation + UpdateFeatures
     * + SunSettings + PostFxSettings + SetUpdateRate, then starts chunk tracker.
     */
    private void handleClientReady(ChannelHandlerContext ctx, HytalePacketBuffer msg) {
        boolean readyForChunks = msg.readBoolean();
        boolean readyForGameplay = msg.readBoolean();

        System.out.println("[Hytale] ClientReady from " + session.getUsername()
                + " (readyForChunks=" + readyForChunks + ", readyForGameplay=" + readyForGameplay + ")");

        // JoinWorld already sent after WorldLoadFinished.
        if (readyForChunks && !readyForGameplay) {
            // First ClientReady: client processed JoinWorld. Now send game state
            // (SetClientId + EntityUpdates) matching real server Phase 6→7.
            sendGameState(ctx);
            // Now safe to send chunks on the Chunks QUIC stream.
            ensureChunksStreamAndSendChunks(ctx);
        }
    }

    // sendEntityData removed — replaced by sendGameState() which handles the full
    // Phase 6→7 flow (ViewRadius + SetEntitySeed + SetClientId + EntityUpdates).

    /**
     * Send ClientTeleport (ID 109) with spawn position.
     * Fixed: nullBits(1) + teleportId(1) + ModelTransform(49) + resetVelocity(1) = 52 bytes.
     *
     * ModelTransform: Position(24) + Direction bodyOrientation(12) + Direction lookOrientation(12) + byte(1).
     */
    private void sendSpawnTeleport(ChannelHandlerContext ctx) {
        int spawnX = world.getSpawnX();
        int spawnZ = world.getSpawnZ();
        int[] safe = world.findSafePosition(spawnX, world.getHeight() * 2 / 3, spawnZ, 50);

        HytalePacketBuffer pkt = HytalePacketBuffer.create(PACKET_CLIENT_TELEPORT, ctx.alloc(), 52);
        pkt.writeByte(0x01); // nullBits: bit0 = modelTransform present
        pkt.writeByte(0);    // teleportId

        // ModelTransform (49 bytes): nullBits(1) + Position(24) + bodyOrientation(12) + lookOrientation(12)
        pkt.writeByte(0x07); // ModelTransform nullBits: position + bodyOrientation + lookOrientation all present

        // Position (3x double LE = 24 bytes)
        pkt.writeDoubleLE(safe[0] + 0.5); // center of block
        pkt.writeDoubleLE(safe[1]);        // feet level
        pkt.writeDoubleLE(safe[2] + 0.5);

        // Direction: bodyOrientation (yaw, pitch, roll as 3x float LE = 12 bytes)
        pkt.writeFloatLE(0.0f); // yaw (radians, 0 = North)
        pkt.writeFloatLE(0.0f); // pitch
        pkt.writeFloatLE(0.0f); // roll

        // Direction: lookOrientation (3x float LE = 12 bytes)
        pkt.writeFloatLE(0.0f);
        pkt.writeFloatLE(0.0f);
        pkt.writeFloatLE(0.0f);

        pkt.writeBoolean(true); // resetVelocity

        session.sendPacket(pkt);
    }

    /**
     * Ensure the Chunks channel stream exists, then send initial chunks and complete login.
     * If the client already opened a Chunks stream (QUIC stream 4), use it directly.
     * Otherwise, create a server-initiated stream for the Chunks channel.
     */
    private void ensureChunksStreamAndSendChunks(ChannelHandlerContext ctx) {
        QuicStreamChannel chunksStream = session.getChannelStream(STREAM_CHUNKS);
        if (chunksStream != null && chunksStream.isActive()) {
            System.out.println("[Hytale] Using client-opened Chunks stream (QUIC ID "
                    + chunksStream.streamId() + ")");
            sendInitialChunks(ctx);
            completeLogin(ctx);
            return;
        }

        // Create server-initiated stream for the Chunks channel
        session.getQuicChannel().createStream(QuicStreamType.BIDIRECTIONAL,
                new ChannelInitializer<QuicStreamChannel>() {
                    @Override
                    protected void initChannel(QuicStreamChannel ch) {
                        ch.pipeline().addLast("frameCodec", new HytaleFrameCodec());
                    }
                }
        ).addListener(f -> {
            if (f.isSuccess()) {
                QuicStreamChannel newStream = (QuicStreamChannel) ((io.netty.util.concurrent.Future<?>) f).getNow();
                session.setChannelStream(STREAM_CHUNKS, newStream);
                session.registerStream(newStream.streamId(), newStream);
                System.out.println("[Hytale] Created server Chunks stream (QUIC ID "
                        + newStream.streamId() + ")");
            } else {
                System.err.println("[Hytale] Failed to create Chunks stream, using Default: "
                        + ((io.netty.util.concurrent.Future<?>) f).cause());
            }
            sendInitialChunks(ctx);
            completeLogin(ctx);
        });
    }

    /** Send initial chunks around spawn. */
    private void sendInitialChunks(ChannelHandlerContext ctx) {
        int spawnX = world.getWidth() / 2;
        int spawnZ = world.getDepth() / 2;
        int viewRadius = 4; // super-chunks (each 32 blocks)

        // Hytale chunks are 3D: 32x32x32 blocks each
        int spawnChunkX = spawnX >> 5; // divide by 32
        int spawnChunkZ = spawnZ >> 5;
        int maxChunkY = (world.getHeight() + 31) >> 5; // ceil(height/32)

        int chunksSent = 0;
        for (int cx = spawnChunkX - viewRadius; cx <= spawnChunkX + viewRadius; cx++) {
            for (int cz = spawnChunkZ - viewRadius; cz <= spawnChunkZ + viewRadius; cz++) {
                // Per-column packets: heightmap, tintmap, environments
                // Real server sends these before section data for each chunk column.
                sendChunkColumnData(ctx, cx, cz, maxChunkY);

                for (int cy = 0; cy < maxChunkY; cy++) {
                    HytalePacketBuffer chunkPkt = chunkConverter.convertChunk(
                            world, cx, cy, cz, ctx.alloc());
                    if (chunkPkt != null) {
                        session.sendPacketOnChannel(chunkPkt, STREAM_CHUNKS);
                        chunksSent++;
                    }
                }
            }
        }

        System.out.println("[Hytale] Sent " + chunksSent + " initial chunks to " + session.getUsername());
    }

    /**
     * Send per-column chunk data: heightmap (132), tintmap (133), environments (134).
     * The real server sends all three for every chunk column alongside section data.
     */
    private void sendChunkColumnData(ChannelHandlerContext ctx, int cx, int cz, int maxChunkY) {
        // SetChunkHeightmap (132): nullBits(1) + x(4LE) + z(4LE) + [ShortBytePalette data]
        // Data format is ShortBytePalette: short count + short[] keys + int dataLen + byte[] bitfield
        // The heightmap field is @Nullable — send it with proper palettized data.
        {
            // Build height values for this 32x32 chunk column
            short[] heights = new short[1024];
            int worldBaseX = cx << 5;
            int worldBaseZ = cz << 5;
            for (int bx = 0; bx < 32; bx++) {
                for (int bz = 0; bz < 32; bz++) {
                    int wx = worldBaseX + bx;
                    int wz = worldBaseZ + bz;
                    short h = 0;
                    if (wx >= 0 && wx < world.getWidth() && wz >= 0 && wz < world.getDepth()) {
                        for (int wy = world.getHeight() - 1; wy >= 0; wy--) {
                            if (world.getBlock(wx, wy, wz) != 0) {
                                h = (short) (wy + 1);
                                break;
                            }
                        }
                    }
                    heights[bx * 32 + bz] = h;
                }
            }
            byte[] hmData = serializeShortBytePalette(heights);

            HytalePacketBuffer hm = HytalePacketBuffer.create(132, ctx.alloc(), 9 + 3 + hmData.length);
            hm.writeByte(0x01);  // nullBits: bit0=heightmap present
            hm.writeIntLE(cx);
            hm.writeIntLE(cz);
            hm.writeVarInt(hmData.length);
            hm.getBuf().writeBytes(hmData);
            session.sendPacketOnChannel(hm, STREAM_CHUNKS);
        }

        // SetChunkTintmap (133): nullBits(1) + x(4LE) + z(4LE) + [tintmap data]
        // Client accesses tintmap buffer unconditionally — MUST be non-null.
        // Send ShortBytePalette with all zeros (no tint).
        {
            short[] tints = new short[1024]; // all zeros = no tint
            byte[] tmData = serializeShortBytePalette(tints);

            HytalePacketBuffer tm = HytalePacketBuffer.create(133, ctx.alloc(), 9 + 5 + tmData.length);
            tm.writeByte(0x01);  // nullBits: bit0=tintmap present
            tm.writeIntLE(cx);
            tm.writeIntLE(cz);
            tm.writeVarInt(tmData.length);
            tm.getBuf().writeBytes(tmData);
            session.sendPacketOnChannel(tm, STREAM_CHUNKS);
        }

        // SetChunkEnvironments (134): nullBits(1) + x(4LE) + z(4LE) + [EnvironmentChunk data]
        // Data is 1024 EnvironmentColumns serialized via serializeProtocol():
        //   per column: short LE rangeCount + (rangeCount × (short minY + short envId))
        // Simplest case: single range per column with env ID 0.
        {
            // Build environment data: 1024 columns, each with 1 range (env 0 for all Y)
            // Per column: writeShortLE(1) + writeShortLE(0) + writeShortLE(0) = 6 bytes
            byte[] envData = new byte[1024 * 6];
            for (int i = 0; i < 1024; i++) {
                int off = i * 6;
                envData[off] = 1; envData[off + 1] = 0;     // rangeCount=1 (LE)
                envData[off + 2] = 0; envData[off + 3] = 0; // minY=0 (Integer.MIN_VALUE & 0xFFFF = 0)
                envData[off + 4] = 0; envData[off + 5] = 0; // envId=0
            }

            HytalePacketBuffer env = HytalePacketBuffer.create(134, ctx.alloc(), 9 + 5 + envData.length);
            env.writeByte(0x01);  // nullBits: bit0=environments present
            env.writeIntLE(cx);
            env.writeIntLE(cz);
            env.writeVarInt(envData.length);
            env.getBuf().writeBytes(envData);
            session.sendPacketOnChannel(env, STREAM_CHUNKS);
        }
    }

    /** Complete login: create player, register with PlayerManager, switch to gameplay handler. */
    private void completeLogin(ChannelHandlerContext ctx) {
        if (session.getState() == HytaleSession.State.CONNECTED) return;
        session.setState(HytaleSession.State.CONNECTED);

        String username = session.getUsername();
        String uuid = session.getPlayerUUID() != null
                ? session.getPlayerUUID().toString() : null;

        // Register with PlayerManager (assigns player ID, triggers broadcasts)
        // Channel is null for Hytale clients (they use QUIC, not TCP)
        ConnectedPlayer player = playerManager.addPlayer(
                username, uuid, null, ProtocolVersion.HYTALE);
        if (player == null) {
            session.disconnect("Server is full!");
            return;
        }

        // Set spawn position
        int spawnX = world.getSpawnX();
        int spawnZ = world.getSpawnZ();
        int[] safe = world.findSafePosition(spawnX, world.getHeight() * 2 / 3, spawnZ, 50);
        player.updatePosition(
                (short) ((safe[0] * 32) + 16), // fixed-point, center of block
                (short) ((safe[1] * 32) + 51), // eye level in fixed-point
                (short) ((safe[2] * 32) + 16),
                (byte) 0, (byte) 0
        );

        // Create session wrapper for broadcast routing
        HytaleSessionWrapper wrapper = new HytaleSessionWrapper(
                session, new ClassicToHytaleTranslator(blockMapper), chunkConverter);
        player.setHytaleSession(wrapper);

        // Fire join event
        ServerEvents.PLAYER_JOIN.invoker().onPlayerJoin(username, ProtocolVersion.HYTALE);

        System.out.println("[Hytale] " + username + " logged in (entityId="
                + session.getEntityNetworkId() + ")");

        // Replace this handler with gameplay handler
        ctx.pipeline().replace(this, "handler",
                new HytaleGameplayHandler(session, player, world, playerManager,
                        chunkManager, blockMapper, chunkConverter));
    }

    private void handlePong(HytalePacketBuffer msg) {
        // Pong (ID 3): nullBits(1) + id(4) + time(12) + type(1) + packetQueueSize(2) = 20 bytes
        msg.readUnsignedByte(); // nullBits
        int pingId = msg.readIntLE();
        session.setLastPongTime(System.currentTimeMillis());
    }

    private volatile boolean joinWorldSent = false;
    private volatile boolean gameStateSent = false;

    private void handleViewRadius(ChannelHandlerContext ctx, HytalePacketBuffer msg) {
        int radius = msg.readIntLE();
        System.out.println("[Hytale] Client requested view radius: " + radius);
        // Real server sends JoinWorld after receiving ViewRadius + PlayerOptions.
        if (!joinWorldSent) {
            joinWorldSent = true;
            sendPreJoinSequence(ctx);
            // JoinWorld (104)
            {
                HytalePacketBuffer joinWorld = HytalePacketBuffer.create(PACKET_JOIN_WORLD, ctx.alloc(), 18);
                joinWorld.writeBoolean(true);  // clearWorld
                joinWorld.writeBoolean(false); // fadeInOut
                joinWorld.writeUUID(UUID.nameUUIDFromBytes("rdforward-world".getBytes()));
                session.sendPacket(joinWorld);
            }
            System.out.println("[Hytale] Sent JoinWorld to " + session.getUsername()
                    + " (awaiting ClientReady)");
        }
    }

    /**
     * Write a minimal 264-byte BlockType entry: all null bits cleared, all fixed fields zeroed,
     * all 25 offset slots set to -1 (no variable data). The client's deserializer will read
     * exactly 264 bytes and treat all optional/variable fields as absent.
     */
    private void writeMinimalBlockTypeEntry(HytalePacketBuffer buf) {
        // nullBits[0..3] = all zero (no optional fields present)
        buf.writeZeroes(4);
        // Fixed fields (160 bytes total, offsets 4-163):
        //   unknown(1) + drawType(1) + material(1) + opacity(1) + hitbox(4) + interactionHitbox(4)
        //   + modelScale(4) + looping(1) + maxSupportDistance(4) + blockSupportsRequiredFor(1)
        //   + requiresAlphaBlending(1) + cubeShadingMode(1) + randomRotation(1) + variantRotation(1)
        //   + rotationYawPlacementOffset(1) + blockSoundSetIndex(4) + ambientSoundEventIndex(4)
        //   + particleColor(3) + light(4) + tint(24) + biomeTint(24) + group(4)
        //   + movementSettings(42) + flags(2) + placementSettings(17)
        //   + ignoreSupportWhenPlaced(1) + transitionToTag(4)
        // All zeros except transitionToTag = 0x80000000 (Integer.MIN_VALUE = "no tag")
        buf.writeZeroes(156);  // offsets 4-159 (everything before transitionToTag)
        buf.writeIntLE(0x80000000);  // transitionToTag at offset 160-163
        // 25 variable field offset slots (100 bytes): all -1 (null)
        for (int i = 0; i < 25; i++) {
            buf.writeIntLE(-1);
        }
        // No variable data — total exactly 264 bytes
    }

    /**
     * Write a BlockType entry with shaderEffect + cubeTextures.
     * Uses backpatching (same technique as real server's serialize()) to
     * fill in offset slots after writing variable data.
     */
    private void writeBlockTypeWithShaderEffectOnly(HytalePacketBuffer buf) {
        // nullBits: only shaderEffect (no cubeTextures — avoids texture atlas NullRef)
        buf.writeByte(0x00);          // nullBits[0]
        buf.writeByte(0x02);          // nullBits[1]: bit1=shaderEffect only
        buf.writeByte(0x00);          // nullBits[2]
        buf.writeByte(0x00);          // nullBits[3]

        // Fixed fields (160 bytes)
        buf.writeByte(0);             // unknown (offset 4)
        buf.writeByte(0);             // drawType = Empty (offset 5) — invisible, avoids texture lookup
        buf.writeByte(0);             // material = Empty (offset 6)
        buf.writeByte(0);             // opacity = Solid (offset 7)
        buf.writeZeroes(152);         // offsets 8-159 (hitbox through ignoreSupportWhenPlaced)
        buf.writeIntLE(0x80000000);   // transitionToTag at offset 160-163

        // 25 offset slots — write placeholders, backpatch later
        int slot0Pos = buf.writerIndex(); // remember position of first offset slot
        for (int i = 0; i < 25; i++) {
            buf.writeIntLE(0); // placeholder
        }
        int varBlockStart = buf.writerIndex(); // position 264 relative to entry start

        // Set all unused slots to -1
        for (int i = 0; i < 25; i++) {
            buf.setIntLE(slot0Pos + i * 4, -1);
        }

        // slot 2: shaderEffect
        buf.setIntLE(slot0Pos + 2 * 4, buf.writerIndex() - varBlockStart);
        buf.writeVarInt(1);  // array count = 1
        buf.writeByte(0);    // ShaderType.None = 0
    }

    /**
     * Write a BlockType entry with drawType=Cube and cubeTextures pointing to "Unknown.png".
     * This ensures the client's block texture atlas is non-empty, preventing the
     * IndexOutOfRangeException crash during the first render frame.
     *
     * Format verified against decompiled BlockType.serialize():
     *   4 nullBits + 160 fixed fields + 25×4 offset slots + variable data
     */
    private void writeBlockTypeWithTextures(HytalePacketBuffer buf) {
        // Texture path that exists in the client's Assets.zip
        byte[] texBytes = "BlockTextures/Unknown.png".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int texVarIntLen = varIntSize(texBytes.length);
        int faceSize = texVarIntLen + texBytes.length; // size of one VarString face

        // === nullBits (4 bytes) ===
        // byte0: bit1=shaderEffect(0x02) in protocol, but shaderEffect is byte1 bit1
        //   Actually: byte0 bits: 0=particleColor, 1=light, 2=tint, 3=biomeTint,
        //   4=movementSettings, 5=flags, 6=placementSettings, 7=item
        // byte1 bits: 0=name, 1=shaderEffect, 2=model, 3=modelTexture,
        //   4=modelAnimation, 5=support, 6=supporting, 7=cubeTextures
        buf.writeByte(0x00);          // nullBits[0]: no optional fixed fields
        buf.writeByte((byte) 0x82);   // nullBits[1]: bit1=shaderEffect, bit7=cubeTextures
        buf.writeByte(0x00);          // nullBits[2]: none
        buf.writeByte(0x00);          // nullBits[3]: none

        // === Fixed fields (160 bytes, offsets 4-163) ===
        buf.writeByte(1);            // unknown = true (offset 4)
        buf.writeByte(2);            // drawType = Cube (offset 5)
        buf.writeByte(1);            // material = Solid (offset 6)
        buf.writeByte(0);            // opacity = Solid (offset 7)
        buf.writeIntLE(0);           // hitbox = 0 (offset 8)
        buf.writeIntLE(0);           // interactionHitbox = 0 (offset 12)
        buf.writeFloatLE(0.0f);      // modelScale (offset 16)
        buf.writeByte(0);            // looping (offset 20)
        buf.writeIntLE(0);           // maxSupportDistance (offset 21)
        buf.writeByte(0);            // blockSupportsRequiredFor (offset 25)
        buf.writeByte(0);            // requiresAlphaBlending (offset 26)
        buf.writeByte(0);            // cubeShadingMode (offset 27)
        buf.writeByte(0);            // randomRotation (offset 28)
        buf.writeByte(0);            // variantRotation (offset 29)
        buf.writeByte(0);            // rotationYawPlacementOffset (offset 30)
        buf.writeIntLE(0);           // blockSoundSetIndex (offset 31)
        buf.writeIntLE(0);           // ambientSoundEventIndex (offset 35)
        buf.writeZeroes(3);          // particleColor (offset 39-41)
        buf.writeZeroes(4);          // light (offset 42-45)
        buf.writeZeroes(24);         // tint (offset 46-69)
        buf.writeZeroes(24);         // biomeTint (offset 70-93)
        buf.writeIntLE(0);           // group (offset 94)
        buf.writeZeroes(42);         // movementSettings (offset 98-139)
        buf.writeZeroes(2);          // flags (offset 140-141)
        buf.writeZeroes(17);         // placementSettings (offset 142-158)
        buf.writeByte(0);            // ignoreSupportWhenPlaced (offset 159)
        buf.writeIntLE(0x80000000);  // transitionToTag (offset 160)

        // === 25 offset slots (offsets 164-263) ===
        // shaderEffect at slot 2, cubeTextures at slot 8; rest = -1
        // Variable data layout: shaderEffect(2 bytes) + cubeTextures(...)
        int shaderEffectSize = 1 + 1; // VarInt(1) + byte(0) = 2 bytes
        int cubeTexturesOffset = shaderEffectSize;

        buf.writeIntLE(-1);                  // slot 0: item (null)
        buf.writeIntLE(-1);                  // slot 1: name (null)
        buf.writeIntLE(0);                   // slot 2: shaderEffect → var offset 0
        buf.writeIntLE(-1);                  // slot 3: model (null)
        buf.writeIntLE(-1);                  // slot 4: modelTexture (null)
        buf.writeIntLE(-1);                  // slot 5: modelAnimation (null)
        buf.writeIntLE(-1);                  // slot 6: support (null)
        buf.writeIntLE(-1);                  // slot 7: supporting (null)
        buf.writeIntLE(cubeTexturesOffset);  // slot 8: cubeTextures → var offset 2
        for (int i = 9; i < 25; i++) {
            buf.writeIntLE(-1);              // slots 9-24: null
        }

        // === Variable data ===
        // @0: shaderEffect = [ShaderType.None]
        buf.writeVarInt(1);   // array count = 1
        buf.writeByte(0);     // ShaderType.None = 0

        // @2: cubeTextures = [BlockTextures(all 6 faces = Unknown.png)]
        // BlockTextures: nullBits(1) + weight(4) + 6×offsetSlot(4) + 6×VarString
        buf.writeVarInt(1);          // array count = 1
        buf.writeByte(0x3F);         // nullBits: all 6 faces present (bits 0-5)
        buf.writeFloatLE(1.0f);      // weight
        // 6 offset slots (relative to BlockTextures varBlockStart at byte 29)
        for (int face = 0; face < 6; face++) {
            buf.writeIntLE(face * faceSize);
        }
        // 6 face VarStrings
        for (int face = 0; face < 6; face++) {
            buf.writeVarInt(texBytes.length);
            buf.getBuf().writeBytes(texBytes);
        }
    }

    /**
     * Write a single BlockType entry into the buffer.
     *
     * @param buf             target buffer (already has packet header written)
     * @param isUnknown       true for UNKNOWN block type
     * @param drawType        0=Empty, 2=Cube, 3=Model
     * @param material        0=Empty, 1=Solid
     * @param opacity         0=Solid, 2=Transparent
     * @param shaderEffectOnly if true, only write shaderEffect in variable data (for Empty blocks)
     * @param texturePath     texture path for cubeTextures/modelTexture (null = shaderEffectOnly)
     */
    private void writeBlockTypeEntry(HytalePacketBuffer buf, boolean isUnknown, int drawType,
                                      int material, int opacity, boolean shaderEffectOnly,
                                      String texturePath) {
        // === nullBits (4 bytes) ===
        // byte0: bit2=tint(0x04), bit3=biomeTint(0x08), bit4=movementSettings(0x10), bit5=flags(0x20)
        // byte1: bit0=name(0x01), bit1=shaderEffect(0x02), bit3=modelTexture(0x08), bit7=cubeTextures(0x80)
        if (shaderEffectOnly) {
            buf.writeByte(0x3C);   // nullBits[0]: tint + biomeTint + movementSettings + flags
            buf.writeByte(0x02);   // nullBits[1]: shaderEffect only
        } else {
            buf.writeByte(0x3C);          // nullBits[0]: tint + biomeTint + movementSettings + flags
            buf.writeByte((byte) 0x8A);   // nullBits[1]: shaderEffect + modelTexture + cubeTextures
        }
        buf.writeByte(0);   // nullBits[2]
        buf.writeByte(0);   // nullBits[3]

        // === Fixed fields (160 bytes, offsets 4-163) ===
        buf.writeByte(isUnknown ? 1 : 0); // unknown (offset 4)
        buf.writeByte(drawType);         // drawType (offset 5)
        buf.writeByte(material);         // material (offset 6)
        buf.writeByte(opacity);          // opacity (offset 7)
        buf.writeIntLE(0);               // hitbox (offset 8): type index 0 ("Full")
        buf.writeIntLE(0);               // interactionHitbox (offset 12): index 0
        buf.writeFloatLE(0.0f);          // modelScale (offset 16)
        buf.writeByte(0);                // looping (offset 20)
        buf.writeIntLE(0);               // maxSupportDistance (offset 21)
        buf.writeByte(0);                // blockSupportsRequiredFor (offset 25): Any=0
        buf.writeByte(0);                // requiresAlphaBlending (offset 26)
        buf.writeByte(0);                // cubeShadingMode (offset 27): Standard=0
        buf.writeByte(0);                // randomRotation (offset 28): None=0
        buf.writeByte(0);                // variantRotation (offset 29): None=0
        buf.writeByte(0);                // rotationYawPlacementOffset (offset 30): None=0
        buf.writeIntLE(0);               // blockSoundSetIndex (offset 31): index 0
        buf.writeIntLE(-1);              // ambientSoundEventIndex (offset 35): none
        buf.writeZeroes(3);              // particleColor (offset 39-41): null
        buf.writeZeroes(4);              // light (offset 42-45): null
        for (int i = 0; i < 6; i++) buf.writeIntLE(-1); // tint (offset 46-69): no tint
        buf.writeZeroes(24);             // biomeTint (offset 70-93): zeros
        buf.writeIntLE(0);               // group (offset 94): index 0 ("Air")
        buf.writeZeroes(42);             // movementSettings (offset 98-139): defaults
        buf.writeZeroes(2);              // flags (offset 140-141)
        buf.writeZeroes(17);             // placementSettings (offset 142-158): null
        buf.writeByte(0);                // ignoreSupportWhenPlaced (offset 159)
        buf.writeIntLE(0x80000000);      // transitionToTag (offset 160): no tag

        // === 25 variable field offset slots (offsets 164-263) ===
        // Slot order: item(0), name(1), shaderEffect(2), model(3), modelTexture(4),
        //   modelAnimation(5), support(6), supporting(7), cubeTextures(8),
        //   cubeSideMaskTexture(9), conditionalSounds(10), particles(11),
        //   blockParticleSetId(12), blockBreakingDecalId(13), transitionTexture(14),
        //   transitionToGroups(15), interactionHint(16), gathering(17), display(18),
        //   rail(19), interactions(20), states(21), tagIndexes(22), bench(23),
        //   connectedBlockRuleSet(24)
        if (shaderEffectOnly) {
            // Only shaderEffect at var offset 0
            buf.writeIntLE(-1);  // slot 0: item (null)
            buf.writeIntLE(-1);  // slot 1: name (null)
            buf.writeIntLE(0);   // slot 2: shaderEffect → var offset 0
            for (int i = 3; i < 25; i++) buf.writeIntLE(-1); // slots 3-24: null
        } else {
            // shaderEffect@0, modelTexture@2, cubeTextures@(2+modelTextureSize)
            byte[] texBytes = texturePath.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            // ModelTexture array: VarInt(1) + nullBits(1) + weight(4) + VarInt(len) + bytes
            int modelTextureSize = 1 + 1 + 4 + varIntSize(texBytes.length) + texBytes.length;
            int cubeTexturesVarOffset = 2 + modelTextureSize;

            buf.writeIntLE(-1);                   // slot 0: item (null)
            buf.writeIntLE(-1);                   // slot 1: name (null)
            buf.writeIntLE(0);                    // slot 2: shaderEffect → var offset 0
            buf.writeIntLE(-1);                   // slot 3: model (null)
            buf.writeIntLE(2);                    // slot 4: modelTexture → var offset 2
            buf.writeIntLE(-1);                   // slot 5: modelAnimation (null)
            buf.writeIntLE(-1);                   // slot 6: support (null)
            buf.writeIntLE(-1);                   // slot 7: supporting (null)
            buf.writeIntLE(cubeTexturesVarOffset); // slot 8: cubeTextures
            for (int i = 9; i < 25; i++) buf.writeIntLE(-1); // slots 9-24: null
        }

        // === Variable data ===
        // @0: shaderEffect = [ShaderType.None]
        buf.writeVarInt(1);
        buf.writeByte(0);  // ShaderType.None

        if (!shaderEffectOnly) {
            byte[] texBytes = texturePath.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            int texVarIntLen = varIntSize(texBytes.length);

            // @2: modelTexture = [ModelTexture(texturePath, weight=1.0)]
            // ModelTexture format: nullBits(1) + weight(4) + [VarString if present]
            buf.writeVarInt(1);      // array count
            buf.writeByte(0x01);     // ModelTexture nullBits: texture present
            buf.writeFloatLE(1.0f);  // weight
            buf.writeVarInt(texBytes.length);
            buf.getBuf().writeBytes(texBytes);

            // @cubeTexturesVarOffset: cubeTextures = [BlockTextures(all faces = texturePath)]
            // BlockTextures format: nullBits(1) + weight(4) + 6×offsetSlot(4) + varStrings
            // VARIABLE_BLOCK_START = 29 within BlockTextures
            // Each face string: VarInt(len) + bytes = texVarIntLen + texBytes.length
            int faceSize = texVarIntLen + texBytes.length;
            buf.writeVarInt(1);       // array count
            buf.writeByte(0x3F);      // BlockTextures nullBits: all 6 faces present
            buf.writeFloatLE(1.0f);   // weight
            // 6 offset slots (relative to BlockTextures varBlockStart at +29)
            for (int face = 0; face < 6; face++) {
                buf.writeIntLE(face * faceSize); // offset for face N
            }
            // 6 face strings
            for (int face = 0; face < 6; face++) {
                buf.writeVarInt(texBytes.length);
                buf.getBuf().writeBytes(texBytes);
            }
        }
    }

    /** Compute VarInt encoded size for a non-negative value. */
    private static int varIntSize(int value) {
        if (value < 0) throw new IllegalArgumentException("Negative VarInt: " + value);
        if (value < 128) return 1;
        if (value < 16384) return 2;
        if (value < 2097152) return 3;
        if (value < 268435456) return 4;
        return 5;
    }

    /**
     * Serialize height values as a ShortBytePalette (matching the server's format).
     * Format: short LE count + short LE[] keys + int LE dataLen + byte[] bitfield (10 bits/entry).
     */
    private static byte[] serializeShortBytePalette(short[] values) {
        // Build palette: map unique values to indices
        java.util.LinkedHashMap<Short, Integer> palette = new java.util.LinkedHashMap<>();
        int[] indices = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            Integer idx = palette.get(values[i]);
            if (idx == null) {
                idx = palette.size();
                palette.put(values[i], idx);
            }
            indices[i] = idx;
        }

        int count = palette.size();

        // Build 10-bit packed bitfield (same as BitFieldArr(10, 1024))
        int bits = 10;
        int byteLen = (values.length * bits + 7) / 8;
        byte[] bitfield = new byte[byteLen];
        for (int i = 0; i < values.length; i++) {
            int value = indices[i];
            int bitIndex = i * bits;
            int byteIndex = bitIndex / 8;
            int bitOffset = bitIndex % 8;
            // Write value across byte boundaries
            int remaining = bits;
            int currentValue = value;
            while (remaining > 0) {
                int bitsInByte = Math.min(8 - bitOffset, remaining);
                int mask = (1 << bitsInByte) - 1;
                int clearMask = ~(mask << bitOffset);
                bitfield[byteIndex] = (byte) (bitfield[byteIndex] & clearMask | (currentValue & mask) << bitOffset);
                currentValue >>>= bitsInByte;
                remaining -= bitsInByte;
                byteIndex++;
                bitOffset = 0;
            }
        }

        // Serialize: count(2) + keys(count*2) + dataLen(4) + data
        int totalSize = 2 + count * 2 + 4 + byteLen;
        byte[] result = new byte[totalSize];
        int pos = 0;
        // count (short LE)
        result[pos++] = (byte) (count & 0xFF);
        result[pos++] = (byte) ((count >> 8) & 0xFF);
        // keys (short LE each)
        for (java.util.Map.Entry<Short, Integer> e : palette.entrySet()) {
            short key = e.getKey();
            result[pos++] = (byte) (key & 0xFF);
            result[pos++] = (byte) ((key >> 8) & 0xFF);
        }
        // data length (int LE)
        result[pos++] = (byte) (byteLen & 0xFF);
        result[pos++] = (byte) ((byteLen >> 8) & 0xFF);
        result[pos++] = (byte) ((byteLen >> 16) & 0xFF);
        result[pos++] = (byte) ((byteLen >> 24) & 0xFF);
        // data
        System.arraycopy(bitfield, 0, result, pos, byteLen);
        return result;
    }

    /**
     * Send common assets to the client via AssetInitialize/AssetPart/AssetFinalize.
     * The client won't load textures from Assets.zip without this.
     */
    private void sendCommonAssets(ChannelHandlerContext ctx) {
        for (String[] asset : COMMON_ASSETS) {
            String hash = asset[0];
            String name = asset[1];
            String resourcePath = asset[2];

            // Load binary data from JAR resources
            byte[] data;
            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    System.err.println("[Hytale] Asset resource not found: " + resourcePath);
                    continue;
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
                data = bos.toByteArray();
            } catch (Exception e) {
                System.err.println("[Hytale] Failed to read asset: " + resourcePath + ": " + e.getMessage());
                continue;
            }

            // AssetInitialize (ID 24): FBS=4 (size:int), VFC=1 (asset:Asset)
            // Asset: hash (64 fixed ASCII) + name (VarString)
            HytalePacketBuffer init = HytalePacketBuffer.create(24, ctx.alloc(), 80 + name.length());
            init.writeIntLE(data.length);    // size of the asset data
            init.writeFixedAscii(hash, 64);  // 64-char hash
            init.writeString(name);          // asset name/path
            session.sendPacket(init);

            // AssetPart (ID 25): nullBits(1) + VarInt(len) + byte[]
            HytalePacketBuffer part = HytalePacketBuffer.create(25, ctx.alloc(), data.length + 10);
            part.writeByte(0x01);            // nullBits: bit0=part present
            part.writeVarInt(data.length);
            part.getBuf().writeBytes(data);
            session.sendPacket(part);

            // AssetFinalize (ID 26): empty packet
            HytalePacketBuffer fin = HytalePacketBuffer.create(26, ctx.alloc(), 0);
            session.sendPacket(fin);
        }

        System.out.println("[Hytale] Sent " + COMMON_ASSETS.length + " common assets to " + session.getUsername());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[Hytale] Login error for " + session.getUsername()
                + ": " + cause.getMessage());
        cause.printStackTrace();
        session.disconnect("Login error: " + cause.getMessage());
    }
}
