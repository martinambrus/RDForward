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

        // Normalize wire IDs to v12 canonical (RotateHead at 0x94 is v14-v20 only)
        int pv = session.getMcpeProtocolVersion();
        if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_14
                && pv < MCPEConstants.MCPE_PROTOCOL_VERSION_27
                && packetId == (MCPEConstants.ROTATE_HEAD_V14 & 0xFF)) {
            // RotateHeadPacket (v14-v20 only) — ignore
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

    private int moveLogCount = 0;

    private void handleMovePlayer(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        boolean isV91 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91;
        boolean isV27 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27;
        long entityId;
        float x, y, z, yaw, pitch;
        if (isV91) {
            // v91: VarInt entityId, 3x LFloat pos, LFloat pitch/yaw/bodyYaw, byte mode, bool onGround
            entityId = buf.readSignedVarInt();
            x = buf.readLFloat();
            y = buf.readLFloat();
            z = buf.readLFloat();
            pitch = buf.readLFloat();
            yaw = buf.readLFloat();
            float bodyYaw = buf.readLFloat();
            int mode = buf.readUnsignedByte();
            int onGround = buf.readUnsignedByte();
        } else if (isV27) {
            entityId = buf.readLong();
            x = buf.readFloat();
            y = buf.readFloat();
            z = buf.readFloat();
            yaw = buf.readFloat();
            float headYaw = buf.readFloat();
            pitch = buf.readFloat();
            int mode = buf.readUnsignedByte();
            int onGround = buf.readUnsignedByte();
        } else {
            entityId = buf.readInt();
            x = buf.readFloat();
            y = buf.readFloat();
            z = buf.readFloat();
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
                float bodyYaw = buf.readFloat();
                pitch = buf.readFloat();
                yaw = buf.readFloat(); // headYaw
            } else {
                yaw = buf.readFloat();
                pitch = buf.readFloat();
            }
        }

        // Log first few position updates from client
        if (moveLogCount < 5) {
            System.out.println("[MCPE] MovePlayer from client: x=" + x
                    + " y(" + (isV27 ? "eye" : "feet") + ")=" + y + " z=" + z);
            moveLogCount++;
        }

        // MCPE yaw 0=South, internal/Classic yaw 0=North → add 180°
        // v27+ MovePlayer Y is eye-level (matches internal convention);
        // older versions send feet-level (need to add eye height)
        double storeY = (isV27 || isV91) ? y : y + PLAYER_EYE_HEIGHT;
        player.updatePositionDouble(x, storeY, z, yaw + 180.0f, pitch);

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
        int x, y, z;
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            // v91: just block coords (VarInt x, byte y, VarInt z) — no entity ID
            int[] coords = buf.readBlockCoords();
            x = coords[0]; y = coords[1]; z = coords[2];
        } else {
            // v27+: 64-bit entityId; v11-v20: 32-bit entityId
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
                buf.readLong();
            } else {
                buf.readInt();
            }
            x = buf.readInt();
            z = buf.readInt();
            y = buf.readUnsignedByte();
        }

        System.out.println("[MCPE] RemoveBlock at " + x + "," + y + "," + z
                + " (block=" + (world.inBounds(x, y, z) ? world.getBlock(x, y, z) : "OOB") + ")");

        if (!world.inBounds(x, y, z)) return;

        // Set block to air
        world.setBlock(x, y, z, (byte) 0);

        // Broadcast block change
        broadcastUpdateBlock(x, y, z, 0, 0);

        sendUpdateBlockConfirm(x, y, z, 0, 0);

        // v27: also resend chunk as workaround (UpdateBlock may not be applied)
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
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
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int blockX, blockY, blockZ;
        int face;
        int itemId;
        int meta;
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            // v91: blockCoords + VarInt(face) + 2×Vector3f(LFloat) + VarInt(slot) + slot
            int[] coords = buf.readBlockCoords();
            blockX = coords[0]; blockY = coords[1]; blockZ = coords[2];
            face = buf.readSignedVarInt();
            buf.readLFloat(); buf.readLFloat(); buf.readLFloat(); // fx, fy, fz
            buf.readLFloat(); buf.readLFloat(); buf.readLFloat(); // posX, posY, posZ
            buf.readSignedVarInt(); // slot index
            // v91 slot: VarInt id, if id>0: VarInt auxValue (data<<8|count), LShort nbtLen, nbt
            itemId = buf.readSignedVarInt();
            if (itemId > 0) {
                int auxValue = buf.readSignedVarInt();
                meta = auxValue >> 8;
                int nbtLen = buf.getBuf().readUnsignedShortLE(); // getLShort
                if (nbtLen > 0 && nbtLen <= buf.readableBytes()) {
                    buf.skipBytes(nbtLen);
                }
            } else {
                meta = 0;
            }
        } else {
        blockX = buf.readInt();
        blockY = buf.readInt();
        blockZ = buf.readInt();
        // v20+: face is byte; v11-v18: face is int
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_20) {
            face = buf.readUnsignedByte();
        } else {
            face = buf.readInt();
        }
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            // v34: 6 floats (fx, fy, fz, posX, posY, posZ) then slot
            buf.skipBytes(24); // 6 × 4 bytes
            // v34 slot: short id, [byte count, short damage, short nbtLen, [nbt]]
            itemId = buf.readShort();
            if (itemId != 0) {
                buf.readUnsignedByte(); // count
                meta = buf.readShort(); // damage
                int nbtLen = buf.readShort();
                if (nbtLen > 0 && nbtLen <= buf.readableBytes()) {
                    buf.skipBytes(nbtLen); // skip NBT
                }
            } else {
                meta = 0;
            }
        } else {
            itemId = buf.readShort();
            // v17+: item aux/meta is short; v11-v14: byte
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17) {
                meta = buf.readShort();
            } else {
                meta = buf.readUnsignedByte();
            }
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
                buf.readLong(); // v27: 64-bit entity ID
            } else {
                buf.readInt();  // v11-v20: 32-bit entity ID
            }
        }
        }

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

        if (!world.inBounds(targetX, targetY, targetZ)) {
            System.out.println("[MCPE] UseItem target OOB: " + targetX + "," + targetY + "," + targetZ);
            return;
        }
        if (world.getBlock(targetX, targetY, targetZ) != 0) {
            System.out.println("[MCPE] UseItem target occupied: " + targetX + "," + targetY + "," + targetZ
                    + " block=" + world.getBlock(targetX, targetY, targetZ));
            return;
        }

        // RubyDung palette: grass at the surface layer, cobblestone everywhere else
        int surfaceY = world.getHeight() * 2 / 3;
        int blockId = (targetY == surfaceY)
                ? BlockRegistry.GRASS
                : BlockRegistry.COBBLESTONE;

        world.setBlock(targetX, targetY, targetZ, (byte) blockId);

        // Suppress arm-swing raycast that accompanies placement (v17-v20 sends
        // both UseItem and Animate for the same tap)
        lastBreakTime = System.currentTimeMillis();

        System.out.println("[MCPE] Placed block " + blockId + " at " + targetX + "," + targetY + "," + targetZ);

        // Broadcast
        broadcastUpdateBlock(targetX, targetY, targetZ, blockId, 0);

        sendUpdateBlockConfirm(targetX, targetY, targetZ, blockId, 0);

        // v27: also resend chunk as workaround (UpdateBlock may not be applied)
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            sendChunkData(targetX >> 4, targetZ >> 4);
        }
    }

    /**
     * Handle v9 PlaceBlock (0x95): eid(int), x(int), z(int), y(byte), block(byte), meta(byte), face(byte).
     * Direct block placement — v11+ uses UseItem instead.
     */
    private void handlePlaceBlock(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int eid = buf.readInt();
        int blockX = buf.readInt();
        int blockZ = buf.readInt();
        int blockY = buf.readUnsignedByte();
        int block = buf.readUnsignedByte();
        int meta = buf.readUnsignedByte();
        int face = buf.readUnsignedByte();

        System.out.println("[MCPE] PlaceBlock at " + blockX + "," + blockY + "," + blockZ
                + " face=" + face + " block=" + block + " meta=" + meta);

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

        System.out.println("[MCPE] Placed block " + blockId + " at " + targetX + "," + targetY + "," + targetZ);

        broadcastUpdateBlock(targetX, targetY, targetZ, blockId, 0);
        sendUpdateBlockConfirm(targetX, targetY, targetZ, blockId, 0);
    }

    private void handleMessage(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        String message;
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            // v91: byte type + VarInt-prefixed strings
            int type = buf.readUnsignedByte();
            if (type == 1 || type == 3) { // CHAT or POPUP: source + message
                buf.readStringV91(); // source
            }
            message = buf.readStringV91();
        } else {
        // v27: type(byte) + source(string) + message(string)
        // v12+: source(string) + message(string); v11: message only
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            int type = buf.readUnsignedByte(); // text type (0=raw, 1=chat, etc.)
            buf.readString(); // source (player name)
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_12) {
            buf.readString(); // player name (already known from login)
        }
        message = buf.readString();
        }

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
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            // v91: REQUEST_CHUNK_RADIUS — VarInt radius, respond with all chunks + radius updated
            int radius = buf.readSignedVarInt();
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
            int chunkX = buf.readInt();
            int chunkZ = buf.readInt();
            sendChunkData(chunkX, chunkZ);
        }
    }

    private void handlePlayerEquipment(ByteBuf payload) {
        // Read but don't process for now — creative mode doesn't need equipment tracking
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            // v91: VarInt entityId + slot(VarInt) + byte slot + byte selectedSlot + byte unknown
            buf.readSignedVarInt(); // entity ID
            int itemId = buf.readSignedVarInt(); // slot item id
            if (itemId > 0) {
                buf.readSignedVarInt(); // auxValue (data<<8|count)
                int nbtLen = buf.getBuf().readUnsignedShortLE(); // getLShort
                if (nbtLen > 0 && nbtLen <= buf.readableBytes()) {
                    buf.skipBytes(nbtLen);
                }
            }
            buf.readByte(); // slot
            buf.readByte(); // selectedSlot
            buf.readByte(); // unknownByte
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            buf.readLong(); // v27+: 64-bit entity ID
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
                // v34: slot compound — short id, [byte count, short damage, short nbtLen, [nbt]]
                int itemId = buf.readShort();
                if (itemId != 0 && buf.readableBytes() >= 5) {
                    buf.readUnsignedByte(); // count
                    buf.readShort(); // damage
                    int nbtLen = buf.readShort();
                    if (nbtLen > 0 && nbtLen <= buf.readableBytes()) {
                        buf.skipBytes(nbtLen);
                    }
                }
                buf.readByte(); // slot
                buf.readByte(); // selectedSlot
            } else {
                buf.readShort(); // item id
                buf.readShort(); // item meta
                buf.readByte(); // slot
                buf.readByte(); // selectedSlot
            }
        } else {
            buf.readInt(); // entity ID
            buf.readShort(); // item id
            buf.readShort(); // item meta
            buf.readByte(); // slot
        }
    }

    private void handlePlayerAction(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int action, x, y, z, face;
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            // v91: VarInt entityId + VarInt action + blockCoords + VarInt face
            buf.readSignedVarInt(); // entity ID
            action = buf.readSignedVarInt();
            int[] coords = buf.readBlockCoords();
            x = coords[0]; y = coords[1]; z = coords[2];
            face = buf.readSignedVarInt();
        } else {
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            buf.readLong(); // v27: 64-bit entity ID
        } else {
            buf.readInt();
        }
        action = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        face = buf.readInt();
        }

        System.out.println("[MCPE] PlayerAction action=" + action + " at " + x + "," + y + "," + z + " face=" + face);

        // Creative mode instant block destroy
        // v91: uses RemoveBlock, not PlayerAction — action 13 is SPAWN_OVERWORLD
        // v34-v81: ACTION_CREATIVE_DESTROY (13) is used for creative block breaking
        // v14-v27: only START_BREAK triggers destroy (STOP_BREAK arrives for same block, ignore it)
        // v11-v13: only STOP_BREAK triggers destroy
        boolean shouldBreak;
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            shouldBreak = false; // v91 creative breaking uses RemoveBlock
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            shouldBreak = action == MCPEConstants.ACTION_CREATIVE_DESTROY;
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
            shouldBreak = action == MCPEConstants.ACTION_START_BREAK;
        } else {
            shouldBreak = action == MCPEConstants.ACTION_STOP_BREAK;
        }
        if (shouldBreak) {
            long now = System.currentTimeMillis();
            if (now - lastBreakTime < BREAK_COOLDOWN_MS) return;
            if (!world.inBounds(x, y, z)) return;
            int oldBlock = world.getBlock(x, y, z);
            if (oldBlock == 0) return; // already air
            lastBreakTime = now;

            System.out.println("[MCPE] Creative destroy at " + x + "," + y + "," + z + " (was " + oldBlock + ")");

            world.setBlock(x, y, z, (byte) 0);

            // Broadcast block removal
            broadcastUpdateBlock(x, y, z, 0, 0);

            sendUpdateBlockConfirm(x, y, z, 0, 0);

            // v27: also resend chunk as workaround (UpdateBlock may not be applied)
            if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
                sendChunkData(x >> 4, z >> 4);
            }
        }
    }

    /** Minimum interval between creative block breaks via arm swing (ms). */
    private static final long BREAK_COOLDOWN_MS = 500;
    /** Creative mode reach distance (blocks). */
    private static final double CREATIVE_REACH = 5.0;

    private long lastBreakTime = 0;

    private void handleAnimate(ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        // v91: byte action + VarInt entityId; v27+: byte + long; older: byte + int
        int action = buf.readUnsignedByte();
        int entityId;
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            entityId = buf.readSignedVarInt();
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            entityId = (int) buf.readLong(); // v27+: 64-bit entity ID (truncate for broadcast)
        } else {
            entityId = buf.readInt();
        }

        // Broadcast arm swing to other players
        broadcastAnimate(entityId, action);

        // Action 1 = arm swing — attempt server-side creative block break via raycast.
        // v9: sends RemoveBlock natively, so skip raycast.
        // v11-v13: only mechanism for block breaking (no RemoveBlock/PlayerAction).
        // v14: sends RemoveBlock, so skip raycast.
        // v17-v18: uses arm swing for breaking.
        // v20: sends RemoveBlock (0x01=WORLD_IMMUTABLE in v20, not set), so skip raycast.
        // v27+: sends RemoveBlock or PlayerAction, so skip raycast.
        if (action == 1
                && session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_11
                && session.getMcpeProtocolVersion() < MCPEConstants.MCPE_PROTOCOL_VERSION_27
                && session.getMcpeProtocolVersion() != MCPEConstants.MCPE_PROTOCOL_VERSION_14
                && session.getMcpeProtocolVersion() != MCPEConstants.MCPE_PROTOCOL_VERSION_20) {
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
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        int action = buf.readUnsignedByte();
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            int targetId = buf.readSignedVarInt(); // v91: VarInt entity ID
        } else if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            long targetId = buf.readLong(); // v27: just target entity (long)
        } else {
            int entityId = buf.readInt();
            int targetId = buf.readInt();
        }
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
            if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
                pkt.writeSignedVarInt(entityId);
                pkt.writeLFloat(x);
                // v91 MovePlayer Y is eye-level
                pkt.writeLFloat(y);
                pkt.writeLFloat(z);
                pkt.writeLFloat(pitch);
                pkt.writeLFloat(yaw);
                pkt.writeLFloat(yaw); // bodyYaw
                pkt.writeByte(0);     // mode = normal
                pkt.writeByte(0);     // onGround
            } else if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
                pkt.writeLong(entityId); // v27: 64-bit entity ID
                pkt.writeFloat(x);
                pkt.writeFloat(y);
                pkt.writeFloat(z);
                pkt.writeFloat(yaw);
                pkt.writeFloat(yaw);  // headYaw
                pkt.writeFloat(pitch);
                pkt.writeByte(0);     // mode = normal
                pkt.writeByte(0);     // onGround
            } else {
                pkt.writeInt(entityId);
                pkt.writeFloat(x);
                // v17+ S2C MovePlayer uses eye-level Y; v11-v13 use feet-level
                float sendY = (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_17)
                        ? y : y - (float) PLAYER_EYE_HEIGHT;
                pkt.writeFloat(sendY);
                pkt.writeFloat(z);
                if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
                    pkt.writeFloat(yaw);  // bodyYaw
                    pkt.writeFloat(pitch);
                    pkt.writeFloat(yaw);  // headYaw
                } else {
                    pkt.writeFloat(yaw);
                    pkt.writeFloat(pitch);
                }
            }
            server.sendGamePacket(other, pkt.getBuf());
        }
    }

    /** Send an UpdateBlock confirmation to this session's player. */
    private void sendUpdateBlockConfirm(int x, int y, int z, int blockId, int meta) {
        MCPEPacketBuffer confirm = new MCPEPacketBuffer();
        confirm.writeByte(wireId(MCPEConstants.UPDATE_BLOCK));
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            // v91: blockCoords + UnsignedVarInt(blockId) + UnsignedVarInt((flags<<4)|meta)
            confirm.writeBlockCoords(x, y, z);
            confirm.writeUnsignedVarInt(blockId);
            confirm.writeUnsignedVarInt((0x0B << 4) | (meta & 0x0F)); // FLAG_ALL_PRIORITY
        } else {
            confirm.writeInt(x);
            confirm.writeInt(z);
            confirm.writeByte(y);
            confirm.writeByte(blockId);
            // v27 flags: NEIGHBORS(1)|NETWORK(2)|PRIORITY(8) = 0x0B (FLAG_ALL_PRIORITY)
            int flags = (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) ? 0x0B : 0;
            confirm.writeByte((meta & 0x0F) | (flags << 4));
        }
        server.sendGamePacket(session, confirm.getBuf());
    }

    private void broadcastUpdateBlock(int x, int y, int z, int blockId, int meta) {
        for (java.util.Map.Entry<java.net.InetSocketAddress, LegacyRakNetSession> entry
                : server.getSessions().entrySet()) {
            LegacyRakNetSession other = entry.getValue();
            if (other == session || other.getState() == LegacyRakNetSession.State.DISCONNECTED) continue;
            if (other.getGameplayHandler() == null) continue;

            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            int otherPv = other.getMcpeProtocolVersion();
            pkt.writeByte(MCPEConstants.toWireId(MCPEConstants.UPDATE_BLOCK, otherPv));
            if (otherPv >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
                // v91: blockCoords + UnsignedVarInt(blockId) + UnsignedVarInt((flags<<4)|meta)
                pkt.writeBlockCoords(x, y, z);
                pkt.writeUnsignedVarInt(blockId);
                pkt.writeUnsignedVarInt((0x0B << 4) | (meta & 0x0F));
            } else {
                pkt.writeInt(x);
                pkt.writeInt(z);
                pkt.writeByte(y);
                pkt.writeByte(blockId);
                int flags = (otherPv >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) ? 0x0B : 0;
                pkt.writeByte((meta & 0x0F) | (flags << 4));
            }
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
            int pv = other.getMcpeProtocolVersion();

            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(MCPEConstants.toWireId(MCPEConstants.ANIMATE, pv));
            pkt.writeByte(action);
            if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
                pkt.writeSignedVarInt(entityId);
            } else if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
                pkt.writeLong(entityId); // v27: 64-bit entity ID
            } else {
                pkt.writeInt(entityId);
            }
            server.sendGamePacket(other, pkt.getBuf());
        }
    }

    // ========== Chunk Sending ==========

    void sendChunkData(int chunkX, int chunkZ) {
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            sendFullChunkDataV91(chunkX, chunkZ);
            return;
        }
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
        int worldHeight = world.getHeight();
        int worldWidth = world.getWidth();
        int worldDepth = world.getDepth();

        // PocketMine formula: columns per packet based on MTU
        int columnsPerPacket = Math.max(1,
                (session.getMtu() - 16 - 255) / 192);
        if (columnsPerPacket > 256) columnsPerPacket = 256;

        // Pre-build column data: 193 bytes per column (flag + 8 sections)
        byte[][] columnData = new byte[256][];
        for (int j = 0; j < 256; j++) {
            int localX = j & 0x0F;
            int localZ = (j >> 4) & 0x0F;
            int worldX = baseX + localX;
            int worldZ = baseZ + localZ;

            byte[] col = new byte[193];
            col[0] = (byte) 0xFF;
            int offset = 1;

            for (int section = 0; section < 8; section++) {
                int sectionBaseY = section * 16;
                for (int localY = 0; localY < 16; localY++) {
                    int worldY = sectionBaseY + localY;
                    if (worldX >= 0 && worldX < worldWidth
                            && worldZ >= 0 && worldZ < worldDepth
                            && worldY < worldHeight) {
                        col[offset++] = (byte) mapBlockId(world.getBlock(worldX, worldY, worldZ));
                    } else {
                        col[offset++] = 0;
                    }
                }
                offset += 8; // metadata (zero-initialized)
            }
            columnData[j] = col;
        }

        // Send columns in batches with zero-padding for prior columns
        int columnsSent = 0;
        while (columnsSent < 256) {
            int batchSize = Math.min(columnsPerPacket, 256 - columnsSent);

            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(wireId(MCPEConstants.CHUNK_DATA));
            pkt.writeInt(chunkX);
            pkt.writeInt(chunkZ);

            for (int p = 0; p < columnsSent; p++) {
                pkt.writeByte(0);
            }

            for (int c = 0; c < batchSize; c++) {
                pkt.writeBytes(columnData[columnsSent + c]);
            }

            server.sendGamePacket(session, pkt.getBuf());
            columnsSent += batchSize;
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
        // chunkX/chunkZ are Little-Endian inside the compressed blob (x86 client reads via memcpy)
        int totalSize = 4 + 4 + blockIds.length + metadata.length + skyLight.length
                + blockLight.length + biomeIds.length + biomeColors.length;
        byte[] uncompressed = new byte[totalSize];
        int pos = 0;
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

        // Note: FULL_CHUNK_DATA_V17 is already a v17 wire ID, not v12 canonical — don't use wireId()
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.FULL_CHUNK_DATA_V17);
        pkt.writeBytes(compressed);
        server.sendGamePacket(session, pkt.getBuf());
    }

    /**
     * v27 FullChunkDataPacket using v27 packet ID 0x2E.
     * [0x2E][chunkX BE][chunkZ BE][dataLen BE][raw terrain]. Batch-wrapped.
     */
    private void sendFullChunkDataV27(int chunkX, int chunkZ) {
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

        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        if (isV34) {
            int chunkPktId = (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_81)
                    ? (MCPEConstants.V81_FULL_CHUNK_DATA & 0xFF)
                    : (MCPEConstants.V34_FULL_CHUNK_DATA & 0xFF);
            pkt.writeByte(chunkPktId);
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

    /**
     * v91 FullChunkDataPacket: VarInt(chunkX) + VarInt(chunkZ) + byte(order) + string(terrain).
     * String = UnsignedVarInt length + raw terrain bytes. Same terrain content as v34.
     */
    private void sendFullChunkDataV91(int chunkX, int chunkZ) {
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

        // Terrain: blockIds + metadata + skyLight + blockLight + heightMap + biomeColors + int(0) extraData
        int terrainSize = blockIds.length + metadata.length + skyLight.length
                + blockLight.length + heightMap.length + biomeColors.length + 4;
        byte[] terrain = new byte[terrainSize];
        int pos = 0;
        System.arraycopy(blockIds, 0, terrain, pos, blockIds.length); pos += blockIds.length;
        System.arraycopy(metadata, 0, terrain, pos, metadata.length); pos += metadata.length;
        System.arraycopy(skyLight, 0, terrain, pos, skyLight.length); pos += skyLight.length;
        System.arraycopy(blockLight, 0, terrain, pos, blockLight.length); pos += blockLight.length;
        System.arraycopy(heightMap, 0, terrain, pos, heightMap.length); pos += heightMap.length;
        System.arraycopy(biomeColors, 0, terrain, pos, biomeColors.length);
        // extraData count = 0 (last 4 bytes already zeroed)

        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.V91_FULL_CHUNK_DATA & 0xFF);
        pkt.writeSignedVarInt(chunkX);
        pkt.writeSignedVarInt(chunkZ);
        pkt.writeByte(0); // ORDER_COLUMNS
        // putString = UnsignedVarInt length + raw bytes
        pkt.writeUnsignedVarInt(terrain.length);
        pkt.writeBytes(terrain);
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

        playerManager.broadcastChat((byte) 0, player.getUsername() + " left the game");
        ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave(player.getUsername());
        world.rememberPlayerPosition(player);
        playerManager.broadcastPlayerDespawn(player);
        playerManager.removePlayerById(player.getPlayerId());
        pongUpdater.run();
    }
}
