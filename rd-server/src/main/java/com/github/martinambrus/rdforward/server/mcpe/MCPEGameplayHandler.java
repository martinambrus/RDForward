package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.server.ChunkManager;
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

    public void handlePacket(ChannelHandlerContext ctx, int packetId, ByteBuf payload) {
        if (disconnected) return;

        switch (packetId) {
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
                        + " (" + payload.readableBytes() + " bytes)");
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
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();

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
        confirm.writeByte(MCPEConstants.UPDATE_BLOCK);
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
        int face = buf.readInt();
        int itemId = buf.readShort();
        int meta = buf.readUnsignedByte();
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
        confirm.writeByte(MCPEConstants.UPDATE_BLOCK);
        confirm.writeInt(targetX);
        confirm.writeInt(targetZ);
        confirm.writeByte(targetY);
        confirm.writeByte(blockId);
        confirm.writeByte(0);
        server.sendGamePacket(session, confirm.getBuf());
    }

    private void handleMessage(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        // Protocol 11: message only (no source field)
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

            // Broadcast to other MCPE sessions
            for (java.util.Map.Entry<java.net.InetSocketAddress, LegacyRakNetSession> entry
                    : server.getSessions().entrySet()) {
                LegacyRakNetSession other = entry.getValue();
                if (other == session || other.getState() == LegacyRakNetSession.State.DISCONNECTED) continue;
                if (other.getGameplayHandler() == null) continue;

                MCPEPacketBuffer pkt = new MCPEPacketBuffer();
                pkt.writeByte(MCPEConstants.UPDATE_BLOCK);
                pkt.writeInt(x);
                pkt.writeInt(z);
                pkt.writeByte(y);
                pkt.writeByte(0); // air
                pkt.writeByte(0); // meta
                server.sendGamePacket(other, pkt.getBuf());
            }

            // Broadcast as Classic packet for Java clients
            com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket classicPkt =
                    new com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket(
                            (short) x, (short) y, (short) z, (byte) 0);
            playerManager.broadcastPacketExcept(classicPkt, player);

            // Confirm back to the breaking player
            MCPEPacketBuffer confirm = new MCPEPacketBuffer();
            confirm.writeByte(MCPEConstants.UPDATE_BLOCK);
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

            // Broadcast to other MCPE sessions
            for (java.util.Map.Entry<java.net.InetSocketAddress, LegacyRakNetSession> entry
                    : server.getSessions().entrySet()) {
                LegacyRakNetSession other = entry.getValue();
                if (other == session || other.getState() == LegacyRakNetSession.State.DISCONNECTED) continue;
                if (other.getGameplayHandler() == null) continue;

                MCPEPacketBuffer pkt = new MCPEPacketBuffer();
                pkt.writeByte(MCPEConstants.UPDATE_BLOCK);
                pkt.writeInt(bx);
                pkt.writeInt(bz);
                pkt.writeByte(by);
                pkt.writeByte(0); // air
                pkt.writeByte(0); // meta
                server.sendGamePacket(other, pkt.getBuf());
            }

            // Broadcast as Classic packet for Java clients
            com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket classicPkt =
                    new com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket(
                            (short) bx, (short) by, (short) bz, (byte) 0);
            playerManager.broadcastPacketExcept(classicPkt, player);

            // Confirm back to the breaking player
            MCPEPacketBuffer confirm = new MCPEPacketBuffer();
            confirm.writeByte(MCPEConstants.UPDATE_BLOCK);
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

            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(MCPEConstants.MOVE_PLAYER);
            pkt.writeInt(entityId);
            pkt.writeFloat(x);
            pkt.writeFloat(y);
            pkt.writeFloat(z);
            pkt.writeFloat(yaw);
            pkt.writeFloat(pitch);
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
            pkt.writeByte(MCPEConstants.UPDATE_BLOCK);
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
            pkt.writeByte(MCPEConstants.ANIMATE);
            pkt.writeByte(action);
            pkt.writeInt(entityId);
            server.sendGamePacket(other, pkt.getBuf());
        }
    }

    // ========== Chunk Sending ==========

    private void sendChunkData(int chunkX, int chunkZ) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        // One packet per Y-section (PocketMine Alpha_1.3 style)
        for (int section = 0; section < 8; section++) {
            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(MCPEConstants.CHUNK_DATA);
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
        playerManager.broadcastPlayerDespawn(player);
        playerManager.removePlayerById(player.getPlayerId());
        pongUpdater.run();
    }
}
