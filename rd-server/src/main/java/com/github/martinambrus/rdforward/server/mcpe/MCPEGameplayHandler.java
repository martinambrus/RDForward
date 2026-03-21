package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import com.github.martinambrus.rdforward.world.BlockRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Handles MCPE 0.7.0 gameplay packets after login.
 * Movement, block breaking/placement, chat, inventory, etc.
 */
public class MCPEGameplayHandler {

    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;
    /** Minimum interval between creative block breaks via arm swing (ms). */
    private static final long BREAK_COOLDOWN_MS = 500;
    /** Creative mode reach distance (blocks). */
    private static final double CREATIVE_REACH = 5.0;

    private final LegacyRakNetSession session;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final LegacyRakNetServer server;
    private final ConnectedPlayer player;
    private final Runnable pongUpdater;
    private final MCPEPacketCodec codec;

    private boolean disconnected = false;
    private long lastBreakTime = 0;

    public MCPEGameplayHandler(LegacyRakNetSession session, ServerWorld world,
                               PlayerManager playerManager,
                               LegacyRakNetServer server, ConnectedPlayer player,
                               Runnable pongUpdater) {
        this.session = session;
        this.world = world;
        this.playerManager = playerManager;
        this.server = server;
        this.player = player;
        this.pongUpdater = pongUpdater;
        this.codec = MCPEPacketCodec.forVersion(session.getMcpeProtocolVersion());
    }

    public MCPEPacketCodec getCodec() {
        return codec;
    }

    public void handlePacket(ChannelHandlerContext ctx, int packetId, ByteBuf payload) {
        if (disconnected) return;

        int id = MCPEConstants.toCanonicalId(packetId, session.getMcpeProtocolVersion());

        switch (id) {
            case -1: // Version-specific packet not mapped to canonical (e.g. RotateHead v14-v20)
                return;
            case 0x94: // MOVE_PLAYER
                handleMovePlayer(payload);
                break;
            case 0x96: // REMOVE_BLOCK
                handleRemoveBlock(payload);
                break;
            case 0xA2: // USE_ITEM (block placement)
                handleUseItem(payload);
                break;
            case 0x85: // MESSAGE (chat)
                handleMessage(payload);
                break;
            case 0x9D: // REQUEST_CHUNK
                handleRequestChunk(payload);
                break;
            case 0x9F: // PLAYER_EQUIPMENT
                handlePlayerEquipment(payload);
                break;
            case 0x97: // UPDATE_BLOCK (C2S) — v17-v20 uses this for creative block breaking
                handleUpdateBlock(payload);
                break;
            case 0xA3: // PLAYER_ACTION
                handlePlayerAction(payload);
                break;
            case 0xAB: // ANIMATE
                handleAnimate(payload);
                break;
            case 0xA1: // INTERACT
                handleInteract(payload);
                break;
            case 0x95: // v9 PLACE_BLOCK (C2S block placement)
                handlePlaceBlock(payload);
                break;
            case 0xB5: // v9 CLIENT_MESSAGE (C2S chat — canonical 0xB5 via +1 shift from wire 0xB4)
                handleMessage(payload);
                break;
            case 0x84: // READY (status=2 = chunk loaded)
                // Ignore — already spawned
                break;
            case 0xB9: // PLAYER_INPUT
                // Vehicle input — ignore for now
                break;
            default:
                System.out.println("[MCPE] Unknown packet 0x" + Integer.toHexString(packetId)
                        + " (canonical 0x" + Integer.toHexString(id)
                        + ", " + payload.readableBytes() + " bytes)");
                break;
        }
    }

    private void handleMovePlayer(ByteBuf payload) {
        MCPEPacketData.MovePlayerData data = codec.readMovePlayer(new MCPEPacketBuffer(payload));

        // MCPE yaw 0=South, internal/Classic yaw 0=North → add 180°
        // Eye-level codecs (v27+) match internal convention directly;
        // feet-level codecs (v9-v20) need eye height added
        double storeY = data.isEyeLevel ? data.y : data.y + PLAYER_EYE_HEIGHT;
        player.updatePositionDouble(data.x, storeY, data.z, data.yaw + 180.0f, data.pitch);

        // Broadcast to ALL clients (Alpha via Classic teleport, MCPE via SessionWrapper translation)
        com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket teleport =
                new com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket(
                        player.getPlayerId(),
                        player.getX(), player.getY(), player.getZ(),
                        player.getYaw() & 0xFF, player.getPitch() & 0xFF);
        playerManager.broadcastPacketExcept(teleport, player);
    }

    private void handleRemoveBlock(ByteBuf payload) {
        MCPEPacketData.RemoveBlockData data = codec.readRemoveBlock(new MCPEPacketBuffer(payload));
        int x = data.x, y = data.y, z = data.z;

        if (!world.inBounds(x, y, z)) return;

        // Set block to air
        world.setBlock(x, y, z, (byte) 0);

        // Broadcast block change
        broadcastUpdateBlock(x, y, z, 0, 0);

        sendUpdateBlockConfirm(x, y, z, 0, 0);

        // Some codecs require a chunk resend after UpdateBlock (v27+)
        if (codec.requiresChunkResendForBlockUpdate()) {
            sendChunkData(x >> 4, z >> 4);
        }
    }

    /**
     * Handle C2S UpdateBlock — v17-v20 clients send this as client-side prediction.
     * Breaking is handled server-side by arm-swing raycast (handleAnimate),
     * placement by handleUseItem. If the client predicted a block removal that we
     * didn't authorize, send a correction back with the actual block state.
     * Format: x(int) + z(int) + y(byte) + blockId(byte) + meta(byte).
     */
    private void handleUpdateBlock(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int x = buf.readInt();
        int z = buf.readInt();
        int y = buf.readUnsignedByte();
        int blockId = buf.readUnsignedByte();
        int meta = buf.readUnsignedByte();

        if (!world.inBounds(x, y, z)) return;

        // Client predicted a block removal — correct it if the block is still there
        if (blockId == 0) {
            int actual = world.getBlock(x, y, z);
            if (actual != 0) {
                // Block is still there on the server — send correction
                sendUpdateBlockConfirm(x, y, z, actual, 0);
            }
        }
    }

    private void handleUseItem(ByteBuf payload) {
        MCPEPacketData.UseItemData data = codec.readUseItem(new MCPEPacketBuffer(payload));
        if (data == null) return;

        int blockX = data.blockX, blockY = data.blockY, blockZ = data.blockZ;
        int face = data.face;

        // Calculate target position based on face
        int targetX = blockX, targetY = blockY, targetZ = blockZ;
        switch (face) {
            case 0: targetY--; break; // bottom
            case 1: targetY++; break; // top
            case 2: targetZ--; break; // north
            case 3: targetZ++; break; // south
            case 4: targetX--; break; // west
            case 5: targetX++; break; // east
            default: return;
        }

        if (!world.inBounds(targetX, targetY, targetZ)) return;
        if (world.getBlock(targetX, targetY, targetZ) != 0) return;

        // Body overlap check
        int playerFeetY = (int) Math.floor(player.getDoubleY() - PLAYER_EYE_HEIGHT);
        int playerBlockX = (int) Math.floor(player.getDoubleX());
        int playerBlockZ = (int) Math.floor(player.getDoubleZ());
        if (targetX == playerBlockX && targetZ == playerBlockZ
                && (targetY == playerFeetY || targetY == playerFeetY + 1)) {
            return;
        }

        // RubyDung palette: always place cobblestone (grass at surface layer)
        int surfaceY = world.getHeight() * 2 / 3;
        int blockId = (targetY == surfaceY)
                ? BlockRegistry.GRASS
                : BlockRegistry.COBBLESTONE;

        world.setBlock(targetX, targetY, targetZ, (byte) blockId);

        // Suppress arm-swing raycast that accompanies placement (v17-v20 sends
        // both UseItem and Animate for the same tap)
        lastBreakTime = System.currentTimeMillis();

        // Broadcast
        broadcastUpdateBlock(targetX, targetY, targetZ, blockId, 0);

        sendUpdateBlockConfirm(targetX, targetY, targetZ, blockId, 0);

        // Some codecs require a chunk resend after UpdateBlock (v27+)
        if (codec.requiresChunkResendForBlockUpdate()) {
            sendChunkData(targetX >> 4, targetZ >> 4);
        }
    }

    /**
     * Handle v9 PlaceBlock (0x95): direct block placement — v11+ uses UseItem instead.
     */
    private void handlePlaceBlock(ByteBuf payload) {
        MCPEPacketData.PlaceBlockData data = codec.readPlaceBlock(new MCPEPacketBuffer(payload));
        if (data == null) return; // only v9 supports PlaceBlock

        int blockX = data.x, blockY = data.y, blockZ = data.z;
        int block = data.blockId;
        int face = data.face;

        // Calculate target position based on face
        int targetX = blockX, targetY = blockY, targetZ = blockZ;
        switch (face) {
            case 0: targetY--; break;
            case 1: targetY++; break;
            case 2: targetZ--; break;
            case 3: targetZ++; break;
            case 4: targetX--; break;
            case 5: targetX++; break;
            default: return;
        }

        if (!world.inBounds(targetX, targetY, targetZ)) return;
        if (world.getBlock(targetX, targetY, targetZ) != 0) return;

        // Body overlap check
        int playerFeetY = (int) Math.floor(player.getDoubleY() - PLAYER_EYE_HEIGHT);
        int playerBlockX = (int) Math.floor(player.getDoubleX());
        int playerBlockZ = (int) Math.floor(player.getDoubleZ());
        if (targetX == playerBlockX && targetZ == playerBlockZ
                && (targetY == playerFeetY || targetY == playerFeetY + 1)) {
            return;
        }

        // Use the block ID from the packet if valid, else fallback
        int blockId = (block >= 1 && block <= 255) ? block : BlockRegistry.COBBLESTONE;

        world.setBlock(targetX, targetY, targetZ, (byte) blockId);

        lastBreakTime = System.currentTimeMillis();

        broadcastUpdateBlock(targetX, targetY, targetZ, blockId, 0);
        sendUpdateBlockConfirm(targetX, targetY, targetZ, blockId, 0);
    }

    private void handleMessage(ByteBuf payload) {
        String message = codec.readMessage(new MCPEPacketBuffer(payload));

        // MCPE 0.7.0 client pre-formats chat as "<Username> text" — strip the prefix
        // to avoid double-wrapping when broadcastChat formats it again.
        String prefix = "<" + player.getUsername() + "> ";
        if (message.startsWith(prefix)) {
            message = message.substring(prefix.length());
        }

        if (message.startsWith("/")) {
            String command = message.substring(1);
            boolean handled = com.github.martinambrus.rdforward.server.api.CommandRegistry.dispatch(
                    command, player.getUsername(), false,
                    reply -> playerManager.sendChat(player, reply));
            if (!handled) {
                playerManager.sendChat(player, "Unknown command: " + command.split("\\s+")[0]);
            }
            return;
        }

        System.out.println("[MCPE Chat] " + player.getUsername() + ": " + message);

        // Broadcast using the same format as Alpha handler: "Username: message"
        playerManager.broadcastChat(player.getPlayerId(), player.getUsername() + ": " + message);
    }

    private void handleRequestChunk(ByteBuf payload) {
        MCPEPacketData.RequestChunkData data = codec.readRequestChunk(new MCPEPacketBuffer(payload));

        if (data.isRadiusRequest) {
            // v91: REQUEST_CHUNK_RADIUS — respond with all chunks within radius
            int radius = data.chunkXOrRadius;
            if (radius < 1) radius = 1;
            if (radius > 8) radius = 8;

            // Send CHUNK_RADIUS_UPDATED response
            MCPEPacketBuffer resp = new MCPEPacketBuffer();
            resp.writeByte(MCPEConstants.V91_CHUNK_RADIUS_UPDATED & 0xFF);
            resp.writeSignedVarInt(radius);
            server.sendGamePacket(session, resp.getBuf());

            // Send all chunks within radius
            int centerX = (int) Math.floor(player.getDoubleX()) >> 4;
            int centerZ = (int) Math.floor(player.getDoubleZ()) >> 4;
            for (int cx = centerX - radius; cx <= centerX + radius; cx++) {
                for (int cz = centerZ - radius; cz <= centerZ + radius; cz++) {
                    sendChunkData(cx, cz);
                }
            }
        } else {
            sendChunkData(data.chunkXOrRadius, data.chunkZ);
        }
    }

    private void handlePlayerEquipment(ByteBuf payload) {
        // Read but don't process for now — creative mode doesn't need equipment tracking
        codec.readPlayerEquipment(new MCPEPacketBuffer(payload));
    }

    private void handlePlayerAction(ByteBuf payload) {
        MCPEPacketData.PlayerActionData data = codec.readPlayerAction(new MCPEPacketBuffer(payload));
        int action = data.action, x = data.x, y = data.y, z = data.z;

        // Creative mode instant block destroy — codec determines which action triggers it
        if (codec.shouldBreakOnAction(action)) {
            long now = System.currentTimeMillis();
            if (now - lastBreakTime < BREAK_COOLDOWN_MS) return;
            if (!world.inBounds(x, y, z)) return;
            int oldBlock = world.getBlock(x, y, z);
            if (oldBlock == 0) return; // already air
            lastBreakTime = now;

            world.setBlock(x, y, z, (byte) 0);

            // Broadcast block removal
            broadcastUpdateBlock(x, y, z, 0, 0);

            sendUpdateBlockConfirm(x, y, z, 0, 0);

            // Some codecs require a chunk resend after UpdateBlock (v27+)
            if (codec.requiresChunkResendForBlockUpdate()) {
                sendChunkData(x >> 4, z >> 4);
            }
        }
    }

    private void handleAnimate(ByteBuf payload) {
        MCPEPacketData.AnimateData data = codec.readAnimate(new MCPEPacketBuffer(payload));
        int action = data.action;
        int entityId = data.entityId;

        // Broadcast arm swing to other players
        broadcastAnimate(entityId, action);

        // Action 1 = arm swing — attempt server-side creative block break via raycast.
        // Codec determines whether arm-swing triggers server-side raycast breaking
        if (action == 1 && codec.usesRaycastBreaking()) {
            long now = System.currentTimeMillis();
            if (now - lastBreakTime < BREAK_COOLDOWN_MS) return;

            int[] hit = raycastBlock(
                    player.getDoubleX(), player.getDoubleY(), player.getDoubleZ(),
                    player.getFloatYaw(), player.getFloatPitch(), CREATIVE_REACH);
            if (hit == null) return;

            int bx = hit[0], by = hit[1], bz = hit[2];
            int oldBlock = world.getBlock(bx, by, bz);
            if (oldBlock == 0) return;

            lastBreakTime = now;

            world.setBlock(bx, by, bz, (byte) 0);

            // Broadcast block removal
            broadcastUpdateBlock(bx, by, bz, 0, 0);

            sendUpdateBlockConfirm(bx, by, bz, 0, 0);
        }
    }

    /**
     * Raycast from eye position along look direction, returning the first
     * non-air block coordinates within maxDistance, or null if none hit.
     * Uses Classic yaw convention (0 = North = -Z).
     */
    private int[] raycastBlock(double eyeX, double eyeY, double eyeZ,
                               float yaw, float pitch, double maxDistance) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double dx = Math.sin(yawRad) * Math.cos(pitchRad);
        double dy = -Math.sin(pitchRad);
        double dz = -Math.cos(yawRad) * Math.cos(pitchRad);

        // Step along ray in 0.1-block increments
        int lastBx = Integer.MIN_VALUE, lastBy = Integer.MIN_VALUE, lastBz = Integer.MIN_VALUE;
        for (double d = 0.2; d <= maxDistance; d += 0.1) {
            int bx = (int) Math.floor(eyeX + dx * d);
            int by = (int) Math.floor(eyeY + dy * d);
            int bz = (int) Math.floor(eyeZ + dz * d);

            // Skip duplicate block checks
            if (bx == lastBx && by == lastBy && bz == lastBz) continue;
            lastBx = bx; lastBy = by; lastBz = bz;

            if (!world.inBounds(bx, by, bz)) continue;
            if (world.getBlock(bx, by, bz) != 0) {
                return new int[]{bx, by, bz};
            }
        }
        return null;
    }

    private void handleInteract(ByteBuf payload) {
        codec.readInteract(new MCPEPacketBuffer(payload));
        // Interaction handling — ignore for now
    }

    // ========== Broadcasting ==========

    /** Send an UpdateBlock confirmation to this session's player. */
    private void sendUpdateBlockConfirm(int x, int y, int z, int blockId, int meta) {
        MCPEPacketBuffer confirm = new MCPEPacketBuffer();
        confirm.writeByte(codec.wireId(MCPEConstants.UPDATE_BLOCK));
        codec.writeUpdateBlock(confirm, x, y, z, blockId, meta, 0x0B);
        server.sendGamePacket(session, confirm.getBuf());
    }

    private void broadcastUpdateBlock(int x, int y, int z, int blockId, int meta) {
        for (java.util.Map.Entry<java.net.InetSocketAddress, LegacyRakNetSession> entry
                : server.getSessions().entrySet()) {
            LegacyRakNetSession other = entry.getValue();
            if (other == session || other.getState() == LegacyRakNetSession.State.DISCONNECTED) continue;
            if (other.getGameplayHandler() == null) continue;
            MCPEPacketCodec otherCodec = other.getGameplayHandler().getCodec();

            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(otherCodec.wireId(MCPEConstants.UPDATE_BLOCK));
            otherCodec.writeUpdateBlock(pkt, x, y, z, blockId, meta, 0x0B);
            server.sendGamePacket(other, pkt.getBuf());
        }

        // Also broadcast as Classic packet for Java clients
        com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket classicPkt =
                new com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket(
                        (short) x, (short) y, (short) z, (byte) blockId);
        playerManager.broadcastPacketExcept(classicPkt, player);
    }

    private void broadcastAnimate(int entityId, int action) {
        for (java.util.Map.Entry<java.net.InetSocketAddress, LegacyRakNetSession> entry
                : server.getSessions().entrySet()) {
            LegacyRakNetSession other = entry.getValue();
            if (other == session || other.getState() == LegacyRakNetSession.State.DISCONNECTED) continue;
            if (other.getGameplayHandler() == null) continue;
            MCPEPacketCodec otherCodec = other.getGameplayHandler().getCodec();

            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(otherCodec.wireId(MCPEConstants.ANIMATE));
            otherCodec.writeAnimate(pkt, entityId, action);
            server.sendGamePacket(other, pkt.getBuf());
        }
    }

    // ========== Chunk Sending ==========

    void sendChunkData(int chunkX, int chunkZ) {
        codec.sendChunkData(server, session, world, chunkX, chunkZ);
    }

    // ========== Disconnect ==========

    public void onDisconnect() {
        if (disconnected) return;
        disconnected = true;

        System.out.println("[MCPE] " + player.getUsername() + " disconnected");

        playerManager.broadcastChat((byte) 0, player.getUsername() + " left the game");
        ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave(player.getUsername());
        world.rememberPlayerPosition(player);
        playerManager.broadcastPlayerDespawn(player);
        playerManager.removePlayerById(player.getPlayerId());
        pongUpdater.run();
    }
}
