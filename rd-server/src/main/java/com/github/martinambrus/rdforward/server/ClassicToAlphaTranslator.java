package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Outbound handler that translates Classic broadcast packets to their
 * Alpha equivalents for real MC Alpha clients.
 *
 * The server's internal language is Classic packets. When PlayerManager
 * broadcasts a Classic packet, this translator intercepts it in the
 * outbound pipeline and converts it to the corresponding Alpha packet
 * before the RawPacketEncoder writes it to the wire.
 *
 * Packets that are already Alpha packets (sent directly by
 * AlphaConnectionHandler) pass through unchanged.
 *
 * Entity ID mapping: Classic uses byte playerId (0-127).
 * Alpha uses int entityId. We map playerId -> playerId + 1
 * (entity 0 is sometimes special in Alpha).
 */
public class ClassicToAlphaTranslator extends ChannelOutboundHandlerAdapter {

    /** Eye-height offset in fixed-point units. Internal Y is eye-level; Alpha expects feet. */
    private static final int EYE_HEIGHT_FIXED = AlphaConnectionHandler.PLAYER_EYE_HEIGHT_FIXED;

    /**
     * When true, Classic PingPackets are dropped instead of translated to
     * zero-payload KeepAlive. Beta 1.8+ (v17) uses KeepAlivePacketV17 with
     * an int ID — a zero-payload KeepAlive would cause stream misalignment
     * because the client reads 4 extra bytes as the keepAliveId.
     * Set by AlphaConnectionHandler after determining the client version.
     */
    private volatile boolean dropPing = false;

    /**
     * Client protocol version, used to select correct packet variants for
     * v39+ (BlockChangePacketV39, DestroyEntityPacketV39, SpawnPlayerPacketV39).
     * Set by AlphaConnectionHandler after determining the client version.
     */
    private volatile ProtocolVersion clientVersion;

    public void setDropPing(boolean dropPing) {
        this.dropPing = dropPing;
    }

    public void setClientVersion(ProtocolVersion clientVersion) {
        this.clientVersion = clientVersion;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Packet)) {
            super.write(ctx, msg, promise);
            return;
        }

        Packet packet = (Packet) msg;
        Packet translated = translate(packet);

        if (translated != null) {
            super.write(ctx, translated, promise);
        } else {
            // Packet was dropped (e.g. Classic-only packets like LevelInit)
            promise.setSuccess();
        }
    }

    /**
     * Translate a Classic packet to its Alpha equivalent.
     * Returns null if the packet should be silently dropped.
     * Returns the original packet if it's already an Alpha packet.
     */
    private Packet translate(Packet packet) {
        // Already an Alpha packet — pass through unchanged
        if (isAlphaPacket(packet)) {
            return packet;
        }

        if (packet instanceof PingPacket) {
            // v17+ has its own KeepAlive heartbeat with int ID; drop tick-loop pings
            // to avoid sending a zero-payload KeepAlive that misaligns the stream.
            if (dropPing) {
                return null;
            }
            // Classic 0x01 Ping -> Alpha 0x00 KeepAlive
            return new KeepAlivePacket();
        }

        if (packet instanceof SetBlockServerPacket) {
            // Classic 0x06 SetBlock -> Alpha 0x35 BlockChange
            SetBlockServerPacket sb = (SetBlockServerPacket) packet;
            if (clientVersion != null && clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
                return new BlockChangePacketV39(sb.getX(), sb.getY(), sb.getZ(),
                        sb.getBlockType(), 0);
            }
            return new BlockChangePacket(sb.getX(), sb.getY(), sb.getZ(),
                    sb.getBlockType(), 0);
        }

        if (packet instanceof com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket) {
            // Classic 0x07 SpawnPlayer -> Alpha 0x14 SpawnPlayer
            com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket sp =
                    (com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket) packet;

            // Skip self-spawn (playerId -1) — Alpha doesn't use this concept
            if (sp.getPlayerId() == -1) {
                return null;
            }

            int entityId = sp.getPlayerId() + 1;
            // Internal Y is eye-level; Alpha SpawnPlayerPacket expects feet Y
            int feetY = (int) sp.getY() - EYE_HEIGHT_FIXED;
            // Classic yaw 0 = North; Alpha yaw 0 = South. Add 128 (180°) to convert.
            int alphaYaw = (sp.getYaw() + 128) & 0xFF;
            if (clientVersion != null && clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
                return new SpawnPlayerPacketV39(
                        entityId, sp.getPlayerName(),
                        (int) sp.getX(), feetY, (int) sp.getZ(),
                        alphaYaw, sp.getPitch(), (short) 0);
            }
            return new com.github.martinambrus.rdforward.protocol.packet.alpha.SpawnPlayerPacket(
                    entityId, sp.getPlayerName(),
                    (int) sp.getX(), feetY, (int) sp.getZ(),
                    alphaYaw, sp.getPitch(), (short) 0);
        }

        if (packet instanceof PlayerTeleportPacket) {
            // Classic 0x08 PlayerTeleport -> Alpha 0x22 EntityTeleport
            PlayerTeleportPacket pt = (PlayerTeleportPacket) packet;

            // Skip self-teleport (playerId -1)
            if (pt.getPlayerId() == -1) {
                return null;
            }

            int entityId = pt.getPlayerId() + 1;
            // Internal Y is eye-level; Alpha EntityTeleportPacket expects feet Y
            int feetY = (int) pt.getY() - EYE_HEIGHT_FIXED;
            // Classic yaw 0 = North; Alpha yaw 0 = South. Add 128 (180°) to convert.
            int alphaYaw = (pt.getYaw() + 128) & 0xFF;
            return new EntityTeleportPacket(entityId,
                    (int) pt.getX(), feetY, (int) pt.getZ(),
                    alphaYaw, pt.getPitch());
        }

        if (packet instanceof PositionOrientationUpdatePacket) {
            // Classic 0x09 -> Alpha 0x21 EntityLookAndMove
            PositionOrientationUpdatePacket pou = (PositionOrientationUpdatePacket) packet;
            int entityId = pou.getPlayerId() + 1;
            return new EntityLookAndMovePacket(entityId,
                    pou.getChangeX(), pou.getChangeY(), pou.getChangeZ(),
                    pou.getYaw(), pou.getPitch());
        }

        if (packet instanceof PositionUpdatePacket) {
            // Classic 0x0A -> Alpha 0x1F EntityRelativeMove
            PositionUpdatePacket pu = (PositionUpdatePacket) packet;
            int entityId = pu.getPlayerId() + 1;
            return new EntityRelativeMovePacket(entityId,
                    pu.getChangeX(), pu.getChangeY(), pu.getChangeZ());
        }

        if (packet instanceof OrientationUpdatePacket) {
            // Classic 0x0B -> Alpha 0x20 EntityLook
            OrientationUpdatePacket ou = (OrientationUpdatePacket) packet;
            int entityId = ou.getPlayerId() + 1;
            return new EntityLookPacket(entityId, ou.getYaw(), ou.getPitch());
        }

        if (packet instanceof DespawnPlayerPacket) {
            // Classic 0x0C -> Alpha 0x1D DestroyEntity
            DespawnPlayerPacket dp = (DespawnPlayerPacket) packet;
            int entityId = dp.getPlayerId() + 1;
            if (clientVersion != null && clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
                return new DestroyEntityPacketV39(entityId);
            }
            return new DestroyEntityPacket(entityId);
        }

        if (packet instanceof MessagePacket) {
            // Classic 0x0D Message -> Alpha 0x03 Chat
            MessagePacket mp = (MessagePacket) packet;
            return new ChatPacket(mp.getMessage());
        }

        if (packet instanceof com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket) {
            // Classic 0x0E Disconnect -> Alpha 0xFF Disconnect
            com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket dp =
                    (com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket) packet;
            return new com.github.martinambrus.rdforward.protocol.packet.alpha.DisconnectPacket(dp.getReason());
        }

        // Drop Classic-only packets that have no Alpha equivalent:
        // LevelInitialize (0x02), LevelDataChunk (0x03), LevelFinalize (0x04),
        // UpdateUserType (0x0F), ServerIdentification (0x00)
        return null;
    }

    /**
     * Check if a packet is already an Alpha packet (not a Classic one).
     * Alpha packets live in the alpha package and should pass through unchanged.
     */
    private boolean isAlphaPacket(Packet packet) {
        return packet.getClass().getPackage().getName().endsWith(".alpha");
    }
}
