package com.github.martinambrus.rdforward.server.hytale;

import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import com.github.martinambrus.rdforward.protocol.event.EventResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import static com.github.martinambrus.rdforward.server.hytale.HytaleProtocolConstants.*;

/**
 * Handles gameplay packets from a connected Hytale client.
 *
 * Installed after login completes, replacing HytaleLoginHandler.
 * Processes movement, block interaction, chat, and view radius updates.
 */
public class HytaleGameplayHandler extends SimpleChannelInboundHandler<HytalePacketBuffer> {

    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;

    private final HytaleSession session;
    private final ConnectedPlayer player;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;
    private final HytaleBlockMapper blockMapper;
    private final HytaleChunkConverter chunkConverter;

    public HytaleGameplayHandler(HytaleSession session, ConnectedPlayer player,
                                  ServerWorld world, PlayerManager playerManager,
                                  ChunkManager chunkManager, HytaleBlockMapper blockMapper,
                                  HytaleChunkConverter chunkConverter) {
        this.session = session;
        this.player = player;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
        this.blockMapper = blockMapper;
        this.chunkConverter = chunkConverter;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HytalePacketBuffer msg) throws Exception {
        int packetId = msg.getPacketId();

        switch (packetId) {
            case PACKET_CLIENT_MOVEMENT:
                handleMovement(msg);
                break;
            case PACKET_MOUSE_INTERACTION:
                handleMouseInteraction(msg);
                break;
            case PACKET_CLIENT_PLACE_BLOCK:
                handlePlaceBlock(msg);
                break;
            case PACKET_CHAT_MESSAGE:
                handleChat(msg);
                break;
            case PACKET_VIEW_RADIUS:
                handleViewRadius(msg);
                break;
            case PACKET_PONG:
                handlePong(msg);
                break;
            case PACKET_CLIENT_READY:
                // Ignore during gameplay
                break;
            default:
                // Silently ignore unknown packets
                break;
        }
    }

    /**
     * Handle ClientMovement (ID 108) — 155 bytes fixed.
     *
     * Layout:
     *   [0-1]   byte[2]  nullBits
     *   [2-24]  MovementStates(23)  [null bit 0.0]
     *   [25-30] HalfFloatPosition(6) relativePosition [null bit 0.1]
     *   [31-54] Position(24) absolutePosition [null bit 0.2] — 3x double LE
     *   [55-66] Direction(12) bodyOrientation [null bit 0.3] — yaw, pitch, roll as float LE
     *   [67-78] Direction(12) lookOrientation [null bit 0.4]
     *   ...
     *
     * Coordinate conversion:
     *   Hytale: doubles, feet-level Y, yaw in radians (0=North)
     *   Internal: fixed-point (x32), eye-level Y, yaw byte (0=North for Classic)
     */
    private void handleMovement(HytalePacketBuffer msg) {
        int nullBits0 = msg.readUnsignedByte();
        int nullBits1 = msg.readUnsignedByte();

        // Skip MovementStates (23 bytes) — we don't use ground/swim/crouch state
        msg.readBytes(23);

        // Skip HalfFloatPosition (6 bytes) — relative position delta
        msg.readBytes(6);

        boolean hasAbsolutePos = (nullBits0 & 0x04) != 0; // bit 0.2
        boolean hasBodyOrientation = (nullBits0 & 0x08) != 0; // bit 0.3
        boolean hasLookOrientation = (nullBits0 & 0x10) != 0; // bit 0.4

        double x = msg.readDoubleLE();
        double y = msg.readDoubleLE();
        double z = msg.readDoubleLE();

        float bodyYaw = msg.readFloatLE();   // radians
        float bodyPitch = msg.readFloatLE();  // radians
        float bodyRoll = msg.readFloatLE();   // radians (unused)

        float lookYaw = msg.readFloatLE();
        float lookPitch = msg.readFloatLE();
        float lookRoll = msg.readFloatLE();

        if (!hasAbsolutePos) return;

        // Convert Hytale position to internal fixed-point (x32)
        // Hytale Y = feet level; internal Y = eye level
        short fixedX = (short) (x * 32);
        short fixedY = (short) ((y + PLAYER_EYE_HEIGHT) * 32);
        short fixedZ = (short) (z * 32);

        // Convert Hytale yaw (radians, 0=North) to Classic byte (0=North, 0-255)
        // Classic yaw = hytaleYawDegrees * 256 / 360
        double hytaleYawDegrees = Math.toDegrees(lookYaw);
        byte classicYaw = (byte) ((int) (hytaleYawDegrees * 256.0 / 360.0) & 0xFF);

        // Convert Hytale pitch (radians, positive=up) to Classic byte
        // Classic pitch: 0=level, 64=up, 192=down (0-255 maps to 0-360 degrees)
        // Hytale pitch: positive=up, negative=down
        double hytalePitchDegrees = Math.toDegrees(lookPitch);
        byte classicPitch = (byte) ((int) (-hytalePitchDegrees * 256.0 / 360.0) & 0xFF);

        player.updatePosition(fixedX, fixedY, fixedZ, classicYaw, classicPitch);

        // Broadcast to other players
        playerManager.broadcastPositionUpdate(player, fixedX, fixedY, fixedZ, classicYaw, classicPitch);
    }

    /**
     * Handle MouseInteraction (ID 111) — block breaking.
     *
     * Fixed block (44 bytes) + 2 offset slots (8 bytes) = VARIABLE_BLOCK_START at 52:
     *   [0]     byte    nullBits (bit0=screenPoint, bit1=mouseButton, bit2=worldInteraction, bit3=itemInHandId, bit4=mouseMotion)
     *   [1-8]   int64LE clientTimestamp
     *   [9-12]  int32LE activeSlot
     *   [13-20] Vector2f screenPoint (8 bytes, nullable bit0)
     *   [21-23] MouseButtonEvent(3) mouseButton (nullable bit1)
     *   [24-43] WorldInteraction(20) worldInteraction (nullable bit2)
     *
     * WorldInteraction (20 bytes):
     *   [0]     byte    nullBits (bit0=blockPosition, bit1=blockRotation)
     *   [1-4]   int32LE entityId
     *   [5-16]  BlockPosition(12) — x(4) + y(4) + z(4)
     *   [17-19] BlockRotation(3)
     */
    private void handleMouseInteraction(HytalePacketBuffer msg) {
        int nullBits = msg.readUnsignedByte();
        boolean hasWorldInteraction = (nullBits & 0x04) != 0; // bit 2

        msg.readLongLE(); // clientTimestamp
        msg.readIntLE();  // activeSlot

        // Skip screenPoint (8 bytes)
        msg.readBytes(8);

        // MouseButtonEvent (3 bytes): button(1) + pressed(1) + type(1)
        msg.readBytes(3);

        if (!hasWorldInteraction) return;

        // WorldInteraction (20 bytes)
        int wiNullBits = msg.readUnsignedByte(); // WorldInteraction nullBits
        msg.readIntLE(); // entityId (unused for block breaking)

        boolean hasBlockPosition = (wiNullBits & 0x01) != 0;
        if (!hasBlockPosition) return;

        // BlockPosition: x(4) + y(4) + z(4)
        int blockX = msg.readIntLE();
        int blockY = msg.readIntLE();
        int blockZ = msg.readIntLE();

        // Validate block position
        if (blockX < 0 || blockX >= world.getWidth()
                || blockY < 0 || blockY >= world.getHeight()
                || blockZ < 0 || blockZ >= world.getDepth()) {
            return;
        }

        int oldBlock = world.getBlock(blockX, blockY, blockZ) & 0xFF;
        if (oldBlock == 0 || oldBlock == 7) return; // can't break air or bedrock

        // Fire event
        EventResult result = ServerEvents.BLOCK_BREAK.invoker()
                .onBlockBreak(player.getUsername(), blockX, blockY, blockZ, oldBlock);
        if (result != EventResult.PASS) return;

        // Break the block (set to air)
        world.setBlock(blockX, blockY, blockZ, (byte) 0);

        // Broadcast to all players
        SetBlockServerPacket setBlock = new SetBlockServerPacket(
                (short) blockX, (short) blockY, (short) blockZ, (byte) 0);
        playerManager.broadcastPacket(setBlock);
    }

    /**
     * Handle ClientPlaceBlock (ID 117) — 21 bytes fixed.
     *
     *   [0]    byte    nullBits (bit0=position, bit1=rotation)
     *   [1-12] BlockPosition(12) — 3x int32LE (nullable, zeroed if null)
     *   [13-15] BlockRotation(3) (nullable, zeroed if null)
     *   [16-19] int32LE placedBlockId
     *   [20]   byte    quickReplace
     */
    private void handlePlaceBlock(HytalePacketBuffer msg) {
        int nullBits = msg.readUnsignedByte();
        boolean hasPosition = (nullBits & 0x01) != 0;

        int blockX = msg.readIntLE();
        int blockY = msg.readIntLE();
        int blockZ = msg.readIntLE();

        // BlockRotation (3 bytes)
        msg.readBytes(3);

        int hytaleBlockId = msg.readIntLE();

        if (!hasPosition) return;

        // Map Hytale block ID back to internal
        int internalBlock = blockMapper.toInternal(hytaleBlockId);
        if (internalBlock == 0) return; // can't place air

        // Validate position
        if (blockX < 0 || blockX >= world.getWidth()
                || blockY < 0 || blockY >= world.getHeight()
                || blockZ < 0 || blockZ >= world.getDepth()) {
            return;
        }

        // Fire event
        EventResult result = ServerEvents.BLOCK_PLACE.invoker()
                .onBlockPlace(player.getUsername(), blockX, blockY, blockZ, internalBlock);
        if (result != EventResult.PASS) return;

        // Place the block
        world.setBlock(blockX, blockY, blockZ, (byte) internalBlock);

        // Broadcast to all players
        SetBlockServerPacket setBlock = new SetBlockServerPacket(
                (short) blockX, (short) blockY, (short) blockZ, (byte) internalBlock);
        playerManager.broadcastPacket(setBlock);
    }

    /**
     * Handle ChatMessage (ID 211).
     * Fixed: nullBits(1). Variable: VarString message (nullable).
     */
    private void handleChat(HytalePacketBuffer msg) {
        int nullBits = msg.readUnsignedByte();
        if ((nullBits & 0x01) == 0) return;

        String message = msg.readString();
        if (message == null || message.isEmpty()) return;

        // Check for commands
        if (message.startsWith("/")) {
            String command = message.substring(1);
            boolean handled = CommandRegistry.dispatch(command, player.getUsername(), false,
                    reply -> playerManager.sendChat(player, reply));
            if (!handled) {
                playerManager.sendChat(player, "Unknown command: " + command.split("\\s+")[0]);
            }
            return;
        }

        // Fire chat event
        EventResult result = ServerEvents.CHAT.invoker().onChat(player.getUsername(), message);
        if (result == EventResult.CANCEL) return;

        // Broadcast chat
        playerManager.broadcastChat(player.getPlayerId(), player.getUsername() + ": " + message);
    }

    private void handleViewRadius(HytalePacketBuffer msg) {
        int radius = msg.readIntLE();
        System.out.println("[Hytale] " + player.getUsername() + " set view radius to " + radius);
        // TODO: Update ChunkManager view distance for this player
    }

    private void handlePong(HytalePacketBuffer msg) {
        msg.readUnsignedByte(); // nullBits
        msg.readIntLE(); // id
        session.setLastPongTime(System.currentTimeMillis());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        handleDisconnect();
        super.channelInactive(ctx);
    }

    private void handleDisconnect() {
        if (player == null) return;

        String name = player.getUsername();
        playerManager.removePlayerById(player.getPlayerId());
        ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave(name);
        world.rememberPlayerPosition(player);
        playerManager.broadcastChat((byte) 0, name + " left the game");
        playerManager.broadcastPlayerDespawn(player);
        System.out.println("[Hytale] " + name + " disconnected");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[Hytale] Error for " + (player != null ? player.getUsername() : "unknown")
                + ": " + cause.getMessage());
        cause.printStackTrace();
        handleDisconnect();
        session.disconnect("Error: " + cause.getMessage());
    }
}
