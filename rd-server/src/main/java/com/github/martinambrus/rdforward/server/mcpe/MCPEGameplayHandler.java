package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import com.github.martinambrus.rdforward.world.BlockRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Handles MCPE 0.7.0 gameplay packets after login.
 * Movement, block breaking/placement, chat, inventory, etc.
 */
public class MCPEGameplayHandler {

    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;

    private final LegacyRakNetSession session;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;
    private final LegacyRakNetServer server;
    private final ConnectedPlayer player;
    private final MCPESessionWrapper sessionWrapper;
    private final Runnable pongUpdater;

    private boolean disconnected = false;

    public MCPEGameplayHandler(LegacyRakNetSession session, ServerWorld world,
                               PlayerManager playerManager, ChunkManager chunkManager,
                               LegacyRakNetServer server, ConnectedPlayer player,
                               MCPESessionWrapper sessionWrapper, Runnable pongUpdater) {
        this.session = session;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
        this.server = server;
        this.player = player;
        this.sessionWrapper = sessionWrapper;
        this.pongUpdater = pongUpdater;
    }

    /** Convert canonical v12 ID to wire ID for this session. */
    private int wireId(int canonicalId) {
        return MCPEConstants.toWireId(canonicalId, session.getMcpeProtocolVersion());
    }

    public void handlePacket(ChannelHandlerContext ctx, int packetId, ByteBuf payload) {
        if (disconnected) return;

        // Normalize v14 wire IDs to v12 canonical (RotateHead at 0x94 handled separately)
        int pv = session.getMcpeProtocolVersion();
        if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_14 && packetId == (MCPEConstants.ROTATE_HEAD_V14 & 0xFF)) {
            // RotateHeadPacket — ignore (entity head rotation, cosmetic only)
            return;
        }
        int id = MCPEConstants.toCanonicalId(packetId, pv);

        switch (id) {
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
            case 0xA3: // PLAYER_ACTION
                handlePlayerAction(payload);
                break;
            case 0xAB: // ANIMATE
                handleAnimate(payload);
                break;
            case 0xA1: // INTERACT
                handleInteract(payload);
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

    private int moveLogCount = 0;

    private void handleMovePlayer(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int entityId = buf.readInt();
        float x = buf.readFloat();
        float y = buf.readFloat(); // feet-level Y (MCPE C2S convention)
        float z = buf.readFloat();
        float yaw, pitch;
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
            // v14: bodyYaw, pitch, headYaw — use headYaw as the primary yaw
            float bodyYaw = buf.readFloat();
            pitch = buf.readFloat();
            yaw = buf.readFloat(); // headYaw
        } else {
            yaw = buf.readFloat();
            pitch = buf.readFloat();
        }

        // Log first few position updates from client
        if (moveLogCount < 5) {
            System.out.println("[MCPE] MovePlayer from client: x=" + x + " y(feet)=" + y + " z=" + z);
            moveLogCount++;
        }

        // Store as eye-level internally (add eye height to convert from feet)
        // MCPE yaw 0=South, internal/Classic yaw 0=North → add 180°
        player.updatePositionDouble(x, y + PLAYER_EYE_HEIGHT, z, yaw + 180.0f, pitch);

        // Broadcast to ALL clients (Alpha via Classic teleport, MCPE via SessionWrapper translation)
        com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket teleport =
                new com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket(
                        player.getPlayerId(),
                        player.getX(), player.getY(), player.getZ(),
                        player.getYaw() & 0xFF, player.getPitch() & 0xFF);
        playerManager.broadcastPacketExcept(teleport, player);
    }

    private void handleRemoveBlock(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int entityId = buf.readInt();
        int x = buf.readInt();
        int z = buf.readInt();
        int y = buf.readUnsignedByte();

        System.out.println("[MCPE] RemoveBlock at " + x + "," + y + "," + z
                + " (block=" + (world.inBounds(x, y, z) ? world.getBlock(x, y, z) : "OOB") + ")");

        if (!world.inBounds(x, y, z)) return;

        // Set block to air
        world.setBlock(x, y, z, (byte) 0);

        // Broadcast block change
        broadcastUpdateBlock(x, y, z, 0, 0);

        // Confirm to placer
        MCPEPacketBuffer confirm = new MCPEPacketBuffer();
        confirm.writeByte(wireId(MCPEConstants.UPDATE_BLOCK));
        confirm.writeInt(x);
        confirm.writeInt(z);
        confirm.writeByte(y);
        confirm.writeByte(0); // air
        confirm.writeByte(0); // meta
        server.sendGamePacket(session, confirm.getBuf());
    }

    private void handleUseItem(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int blockX = buf.readInt();
        int blockY = buf.readInt();
        int blockZ = buf.readInt();
        // v17+: face is byte (UseItemPacket format changed at 0.9.0); v11-v14: int
        int face;
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17) {
            face = buf.readUnsignedByte();
        } else {
            face = buf.readInt();
        }
        int itemId = buf.readShort();
        // v17+: item aux/meta is short; v11-v14: byte
        int meta;
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17) {
            meta = buf.readShort();
        } else {
            meta = buf.readUnsignedByte();
        }
        int entityId = buf.readInt();
        // float fx, fy, fz, posX, posY, posZ — skip for now

        System.out.println("[MCPE] UseItem block=" + blockX + "," + blockY + "," + blockZ
                + " face=" + face + " item=" + itemId + " meta=" + meta);

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
        if (world.getBlock(targetX, targetY, targetZ) != 0) return; // already occupied

        // RubyDung palette: grass at the surface layer, cobblestone everywhere else
        int surfaceY = world.getHeight() * 2 / 3;
        int blockId = (targetY == surfaceY)
                ? BlockRegistry.GRASS
                : BlockRegistry.COBBLESTONE;

        world.setBlock(targetX, targetY, targetZ, (byte) blockId);

        // Broadcast
        broadcastUpdateBlock(targetX, targetY, targetZ, blockId, 0);

        // Confirm to placer
        MCPEPacketBuffer confirm = new MCPEPacketBuffer();
        confirm.writeByte(wireId(MCPEConstants.UPDATE_BLOCK));
        confirm.writeInt(targetX);
        confirm.writeInt(targetZ);
        confirm.writeByte(targetY);
        confirm.writeByte(blockId);
        confirm.writeByte(0);
        server.sendGamePacket(session, confirm.getBuf());
    }

    private void handleMessage(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        // Protocol 12+: string player + string message; Protocol 11: message only
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_12) {
            buf.readString(); // player name (already known from login)
        }
        String message = buf.readString();

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
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int chunkX = buf.readInt();
        int chunkZ = buf.readInt();
        sendChunkData(chunkX, chunkZ);
    }

    private void handlePlayerEquipment(ByteBuf payload) {
        // Read but don't process for now — creative mode doesn't need equipment tracking
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int entityId = buf.readInt();
        int itemId = buf.readShort();
        int itemMeta = buf.readShort();
        int slot = buf.readByte();
    }

    private void handlePlayerAction(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int entityId = buf.readInt();
        int action = buf.readInt();
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        int face = buf.readInt();

        System.out.println("[MCPE] PlayerAction action=" + action + " at " + x + "," + y + "," + z + " face=" + face);

        // Action 2 = creative mode instant block destroy
        if (action == MCPEConstants.ACTION_STOP_BREAK) {
            if (!world.inBounds(x, y, z)) return;
            int oldBlock = world.getBlock(x, y, z);
            if (oldBlock == 0) return; // already air

            System.out.println("[MCPE] Creative destroy at " + x + "," + y + "," + z + " (was " + oldBlock + ")");

            world.setBlock(x, y, z, (byte) 0);

            // Broadcast block removal
            broadcastUpdateBlock(x, y, z, 0, 0);

            // Confirm back to the breaking player
            MCPEPacketBuffer confirm = new MCPEPacketBuffer();
            confirm.writeByte(wireId(MCPEConstants.UPDATE_BLOCK));
            confirm.writeInt(x);
            confirm.writeInt(z);
            confirm.writeByte(y);
            confirm.writeByte(0); // air
            confirm.writeByte(0); // meta
            server.sendGamePacket(session, confirm.getBuf());
        }
    }

    /** Minimum interval between creative block breaks via arm swing (ms). */
    private static final long BREAK_COOLDOWN_MS = 300;
    /** Creative mode reach distance (blocks). */
    private static final double CREATIVE_REACH = 5.0;

    private long lastBreakTime = 0;

    private void handleAnimate(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int action = buf.readUnsignedByte();
        int entityId = buf.readInt();

        // Broadcast arm swing to other players
        broadcastAnimate(entityId, action);

        // Action 1 = arm swing — attempt server-side creative block break via raycast
        // (MCPE 0.7.0 client doesn't send RemoveBlock/PlayerAction for creative breaks)
        if (action == 1) {
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

            System.out.println("[MCPE] Raycast break at " + bx + "," + by + "," + bz + " (was " + oldBlock + ")");

            world.setBlock(bx, by, bz, (byte) 0);

            // Broadcast block removal
            broadcastUpdateBlock(bx, by, bz, 0, 0);

            // Confirm back to the breaking player
            MCPEPacketBuffer confirm = new MCPEPacketBuffer();
            confirm.writeByte(wireId(MCPEConstants.UPDATE_BLOCK));
            confirm.writeInt(bx);
            confirm.writeInt(bz);
            confirm.writeByte(by);
            confirm.writeByte(0); // air
            confirm.writeByte(0); // meta
            server.sendGamePacket(session, confirm.getBuf());
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
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int action = buf.readUnsignedByte();
        int entityId = buf.readInt();
        int targetId = buf.readInt();
        // Interaction handling — ignore for now
    }

    // ========== Broadcasting ==========

    private void broadcastMovePlayer(int entityId, float x, float y, float z, float yaw, float pitch) {
        for (java.util.Map.Entry<java.net.InetSocketAddress, LegacyRakNetSession> entry
                : server.getSessions().entrySet()) {
            LegacyRakNetSession other = entry.getValue();
            if (other == session || other.getState() == LegacyRakNetSession.State.DISCONNECTED) continue;
            if (other.getGameplayHandler() == null) continue;
            int pv = other.getMcpeProtocolVersion();

            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(MCPEConstants.toWireId(MCPEConstants.MOVE_PLAYER, pv));
            pkt.writeInt(entityId);
            pkt.writeFloat(x);
            pkt.writeFloat(y);
            pkt.writeFloat(z);
            if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
                pkt.writeFloat(yaw);  // bodyYaw
                pkt.writeFloat(pitch);
                pkt.writeFloat(yaw);  // headYaw (same as bodyYaw)
            } else {
                pkt.writeFloat(yaw);
                pkt.writeFloat(pitch);
            }
            server.sendGamePacket(other, pkt.getBuf());
        }
    }

    private void broadcastUpdateBlock(int x, int y, int z, int blockId, int meta) {
        for (java.util.Map.Entry<java.net.InetSocketAddress, LegacyRakNetSession> entry
                : server.getSessions().entrySet()) {
            LegacyRakNetSession other = entry.getValue();
            if (other == session || other.getState() == LegacyRakNetSession.State.DISCONNECTED) continue;
            if (other.getGameplayHandler() == null) continue;

            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(MCPEConstants.toWireId(MCPEConstants.UPDATE_BLOCK, other.getMcpeProtocolVersion()));
            pkt.writeInt(x);
            pkt.writeInt(z);
            pkt.writeByte(y);
            pkt.writeByte(blockId);
            pkt.writeByte(meta);
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

            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(MCPEConstants.toWireId(MCPEConstants.ANIMATE, other.getMcpeProtocolVersion()));
            pkt.writeByte(action);
            pkt.writeInt(entityId);
            server.sendGamePacket(other, pkt.getBuf());
        }
    }

    // ========== Chunk Sending ==========

    private void sendChunkData(int chunkX, int chunkZ) {
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17) {
            sendFullChunkDataV17(chunkX, chunkZ);
            return;
        }

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        // One packet per Y-section (PocketMine Alpha_1.3 style)
        for (int section = 0; section < 8; section++) {
            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(wireId(MCPEConstants.CHUNK_DATA));
            pkt.writeInt(chunkX);
            pkt.writeInt(chunkZ);

            int flag = 1 << section;
            int sectionBaseY = section * 16;

            for (int j = 0; j < 256; j++) {
                int localX = j & 0x0F;
                int localZ = (j >> 4) & 0x0F;
                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;

                pkt.writeByte(flag);

                for (int localY = 0; localY < 16; localY++) {
                    int worldY = sectionBaseY + localY;
                    if (worldX >= 0 && worldX < world.getWidth()
                            && worldZ >= 0 && worldZ < world.getDepth()
                            && worldY < world.getHeight()) {
                        pkt.writeByte(mapBlockId(world.getBlock(worldX, worldY, worldZ)));
                    } else {
                        pkt.writeByte(0);
                    }
                }

                for (int i = 0; i < 8; i++) {
                    pkt.writeByte(0);
                }
            }

            server.sendGamePacket(session, pkt.getBuf());
        }
    }

    /** v17 FullChunkDataPacket — same format as in MCPELoginHandler. */
    private void sendFullChunkDataV17(int chunkX, int chunkZ) {
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

        // Assemble: chunkX + chunkZ + terrain data (all inside compressed payload)
        // No heightmap — client recalculates it.
        int totalSize = 4 + 4 + blockIds.length + metadata.length + skyLight.length
                + blockLight.length + biomeIds.length + biomeColors.length;
        byte[] uncompressed = new byte[totalSize];
        int pos = 0;
        uncompressed[pos++] = (byte) (chunkX >> 24);
        uncompressed[pos++] = (byte) (chunkX >> 16);
        uncompressed[pos++] = (byte) (chunkX >> 8);
        uncompressed[pos++] = (byte) chunkX;
        uncompressed[pos++] = (byte) (chunkZ >> 24);
        uncompressed[pos++] = (byte) (chunkZ >> 16);
        uncompressed[pos++] = (byte) (chunkZ >> 8);
        uncompressed[pos++] = (byte) chunkZ;
        System.arraycopy(blockIds, 0, uncompressed, pos, blockIds.length); pos += blockIds.length;
        System.arraycopy(metadata, 0, uncompressed, pos, metadata.length); pos += metadata.length;
        System.arraycopy(skyLight, 0, uncompressed, pos, skyLight.length); pos += skyLight.length;
        System.arraycopy(blockLight, 0, uncompressed, pos, blockLight.length); pos += blockLight.length;
        System.arraycopy(biomeIds, 0, uncompressed, pos, biomeIds.length); pos += biomeIds.length;
        System.arraycopy(biomeColors, 0, uncompressed, pos, biomeColors.length);

        byte[] compressed;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DeflaterOutputStream dos = new DeflaterOutputStream(baos);
            dos.write(uncompressed);
            dos.finish();
            dos.close();
            compressed = baos.toByteArray();
        } catch (java.io.IOException e) {
            throw new RuntimeException("zlib compress failed", e);
        }

        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.FULL_CHUNK_DATA_V17);
        pkt.writeBytes(compressed);
        server.sendGamePacket(session, pkt.getBuf());
    }

    private int mapBlockId(int internalId) {
        if (internalId >= 0 && internalId <= 49) return internalId;
        return 1; // stone fallback
    }

    private int itemIdToBlockId(int itemId) {
        // In MCPE 0.7.0, item IDs for blocks match their block IDs (items < 256 are blocks)
        if (itemId >= 1 && itemId <= 255) return itemId;
        return 0;
    }

    // ========== Disconnect ==========

    public void onDisconnect() {
        if (disconnected) return;
        disconnected = true;

        System.out.println("[MCPE] " + player.getUsername() + " disconnected");

        ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave(player.getUsername());
        world.rememberPlayerPosition(player);
        playerManager.broadcastPlayerDespawn(player);
        playerManager.removePlayerById(player.getPlayerId());
        pongUpdater.run();
    }
}
