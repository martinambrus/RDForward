package com.github.martinambrus.rdforward.protocol.translation;

import com.github.martinambrus.rdforward.protocol.Capability;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.PacketRegistry;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.protocol.packet.classic.DespawnPlayerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.DespawnPlayerPacketV015a;
import com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.MessagePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.OrientationUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PositionOrientationUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PositionUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.ServerIdentificationPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.ServerIdentificationPacketV015a;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.UpdateUserTypePacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.BlockChangePacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.DestroyEntityPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.EntityLookAndMovePacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.EntityLookPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.EntityRelativeMovePacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.EntityTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.TimeUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.UpdateHealthPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.HashMap;
import java.util.Map;

/**
 * Netty outbound channel handler that translates packets between protocol versions.
 *
 * Sits in the outbound pipeline between the game handler and the encoder.
 * Intercepts server-to-client packets and translates/filters them to match
 * the client's protocol version before encoding.
 *
 * Translation layers (applied in order):
 *   1. Version-specific packet remapping (e.g., 0.0.15a delta→absolute conversion)
 *   2. Packet existence — drop packets whose ID doesn't exist in the client version
 *   3. Capability filter — drop packets requiring capabilities the client lacks
 *   4. Content translation — rewrite packet fields (e.g. block IDs)
 *
 * For 0.0.15a clients:
 *   - Delta position packets (0x09/0x0A/0x0B) → absolute position (0x08)
 *   - DespawnPlayer 0x0C → 0x09
 *   - ServerIdentification → shorter v015a format
 *   - Drop Message, Disconnect, UpdateUserType
 */
public class VersionTranslator extends ChannelOutboundHandlerAdapter {

    private static final boolean DEBUG_LOGGING = Boolean.getBoolean("rdforward.debug.packets");

    private final ProtocolVersion serverVersion;
    private final ProtocolVersion clientVersion;
    private final boolean isClassic015a;

    /**
     * Per-entity position tracking for 0.0.15a clients.
     * Stores the last known absolute position (fixed-point shorts) and orientation
     * for each entity, needed to convert delta updates to absolute.
     * Populated by SpawnPlayer and PlayerTeleport packets.
     */
    private final Map<Integer, short[]> entityPositions;

    public VersionTranslator(ProtocolVersion serverVersion, ProtocolVersion clientVersion) {
        this.serverVersion = serverVersion;
        this.clientVersion = clientVersion;
        this.isClassic015a = (clientVersion == ProtocolVersion.CLASSIC_0_0_15A);
        this.entityPositions = isClassic015a ? new HashMap<Integer, short[]>() : null;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Packet)) {
            ctx.write(msg, promise);
            return;
        }

        Packet packet = (Packet) msg;

        // Layer 1: Version-specific packet remapping (before existence check)
        if (isClassic015a) {
            packet = remapForClassic015a(packet);
            if (packet == null) {
                promise.setSuccess();
                return;
            }
        }

        // Track entity positions from outgoing SpawnPlayer/PlayerTeleport
        // (after remapping, so we capture the final absolute positions)
        if (isClassic015a) {
            trackEntityPosition(packet);
        }

        // Layer 2: Check if this packet ID exists in the client's version
        if (!PacketRegistry.hasPacket(clientVersion, PacketDirection.SERVER_TO_CLIENT, packet.getPacketId())) {
            logFiltered(packet, "no packet ID in client version");
            promise.setSuccess();
            return;
        }

        // Layer 3: Check capability-based filtering
        if (!passesCapabilityFilter(packet)) {
            logFiltered(packet, "client lacks required capability");
            promise.setSuccess();
            return;
        }

        // Layer 4: Translate packet contents based on type
        Packet translated = translatePacket(packet);
        if (translated != null) {
            ctx.write(translated, promise);
        } else {
            promise.setSuccess();
        }
    }

    /**
     * Remap packets for Classic 0.0.15a clients.
     * Converts delta movement to absolute, remaps DespawnPlayer ID,
     * and drops unsupported packets.
     * Returns null to drop the packet.
     */
    private Packet remapForClassic015a(Packet packet) {
        // Convert delta position updates to absolute (0.0.15a only has absolute 0x08)
        if (packet instanceof PositionOrientationUpdatePacket) {
            return deltaToAbsolute((PositionOrientationUpdatePacket) packet);
        }
        if (packet instanceof PositionUpdatePacket) {
            return deltaToAbsolute((PositionUpdatePacket) packet);
        }
        if (packet instanceof OrientationUpdatePacket) {
            return orientationToAbsolute((OrientationUpdatePacket) packet);
        }

        // Remap DespawnPlayer from 0x0C to 0x09
        if (packet instanceof DespawnPlayerPacket) {
            DespawnPlayerPacket dp = (DespawnPlayerPacket) packet;
            if (entityPositions != null) {
                entityPositions.remove(dp.getPlayerId());
            }
            return new DespawnPlayerPacketV015a(dp.getPlayerId());
        }

        // Convert ServerIdentification to shorter v015a format
        if (packet instanceof ServerIdentificationPacket) {
            return new ServerIdentificationPacketV015a(
                    ((ServerIdentificationPacket) packet).getServerName());
        }

        // Drop packets that don't exist in 0.0.15a
        if (packet instanceof MessagePacket
                || packet instanceof DisconnectPacket
                || packet instanceof UpdateUserTypePacket) {
            logFiltered(packet, "not supported in 0.0.15a");
            return null;
        }

        return packet;
    }

    /**
     * Convert PositionOrientationUpdate (delta) to PlayerTeleport (absolute).
     */
    private Packet deltaToAbsolute(PositionOrientationUpdatePacket delta) {
        short[] pos = entityPositions != null ? entityPositions.get(delta.getPlayerId()) : null;
        if (pos == null) {
            // No tracked position — can't convert, drop
            logFiltered(delta, "no tracked position for entity " + delta.getPlayerId());
            return null;
        }
        short newX = (short) (pos[0] + delta.getChangeX());
        short newY = (short) (pos[1] + delta.getChangeY());
        short newZ = (short) (pos[2] + delta.getChangeZ());
        // Update tracked position
        pos[0] = newX;
        pos[1] = newY;
        pos[2] = newZ;
        pos[3] = (short) delta.getYaw();
        pos[4] = (short) delta.getPitch();
        return new PlayerTeleportPacket(delta.getPlayerId(), newX, newY, newZ,
                delta.getYaw(), delta.getPitch());
    }

    /**
     * Convert PositionUpdate (delta, no orientation) to PlayerTeleport (absolute).
     */
    private Packet deltaToAbsolute(PositionUpdatePacket delta) {
        short[] pos = entityPositions != null ? entityPositions.get(delta.getPlayerId()) : null;
        if (pos == null) {
            logFiltered(delta, "no tracked position for entity " + delta.getPlayerId());
            return null;
        }
        short newX = (short) (pos[0] + delta.getChangeX());
        short newY = (short) (pos[1] + delta.getChangeY());
        short newZ = (short) (pos[2] + delta.getChangeZ());
        pos[0] = newX;
        pos[1] = newY;
        pos[2] = newZ;
        // Keep last known yaw/pitch
        return new PlayerTeleportPacket(delta.getPlayerId(), newX, newY, newZ,
                pos[3], pos[4]);
    }

    /**
     * Convert OrientationUpdate (look only) to PlayerTeleport (absolute).
     */
    private Packet orientationToAbsolute(OrientationUpdatePacket orient) {
        short[] pos = entityPositions != null ? entityPositions.get(orient.getPlayerId()) : null;
        if (pos == null) {
            logFiltered(orient, "no tracked position for entity " + orient.getPlayerId());
            return null;
        }
        pos[3] = (short) orient.getYaw();
        pos[4] = (short) orient.getPitch();
        return new PlayerTeleportPacket(orient.getPlayerId(), pos[0], pos[1], pos[2],
                orient.getYaw(), orient.getPitch());
    }

    /**
     * Track entity positions from outgoing SpawnPlayer and PlayerTeleport packets.
     * This builds the position state needed for delta-to-absolute conversion.
     */
    private void trackEntityPosition(Packet packet) {
        if (entityPositions == null) return;

        if (packet instanceof SpawnPlayerPacket) {
            SpawnPlayerPacket sp = (SpawnPlayerPacket) packet;
            entityPositions.put(sp.getPlayerId(),
                    new short[]{sp.getX(), sp.getY(), sp.getZ(),
                            (short) sp.getYaw(), (short) sp.getPitch()});
        } else if (packet instanceof PlayerTeleportPacket) {
            PlayerTeleportPacket tp = (PlayerTeleportPacket) packet;
            entityPositions.put(tp.getPlayerId(),
                    new short[]{tp.getX(), tp.getY(), tp.getZ(),
                            (short) tp.getYaw(), (short) tp.getPitch()});
        }
    }

    /**
     * Check whether the client has the capabilities required by this packet.
     * Returns true if the packet should be forwarded, false if it should be dropped.
     */
    private boolean passesCapabilityFilter(Packet packet) {
        // Entity packets require ENTITY_SPAWN capability
        if (isEntityPacket(packet)) {
            return Capability.ENTITY_SPAWN.isAvailableIn(clientVersion);
        }

        // Time update requires DAY_NIGHT_CYCLE capability
        if (packet instanceof TimeUpdatePacket) {
            return Capability.DAY_NIGHT_CYCLE.isAvailableIn(clientVersion);
        }

        // Health update requires PLAYER_HEALTH capability
        if (packet instanceof UpdateHealthPacket) {
            return Capability.PLAYER_HEALTH.isAvailableIn(clientVersion);
        }

        return true;
    }

    /**
     * Check if a packet is an entity-related packet (spawn, move, look, teleport, destroy).
     */
    private boolean isEntityPacket(Packet packet) {
        return packet instanceof com.github.martinambrus.rdforward.protocol.packet.alpha.SpawnPlayerPacket
                || packet instanceof DestroyEntityPacket
                || packet instanceof EntityRelativeMovePacket
                || packet instanceof EntityLookPacket
                || packet instanceof EntityLookAndMovePacket
                || packet instanceof EntityTeleportPacket;
    }

    /**
     * Translate a single packet from server version to client version.
     * Returns null if the packet should be dropped.
     */
    private Packet translatePacket(Packet packet) {
        // Classic 0x06: Set Block (Server -> Client)
        if (packet instanceof SetBlockServerPacket) {
            return translateSetBlock((SetBlockServerPacket) packet);
        }

        // Alpha 0x35: Block Change — translate block ID
        if (packet instanceof BlockChangePacket) {
            return translateBlockChange((BlockChangePacket) packet);
        }

        // Pass through unchanged for packet types that don't need translation
        return packet;
    }

    /**
     * Translate block IDs in a SetBlockServerPacket.
     */
    private Packet translateSetBlock(SetBlockServerPacket packet) {
        int translatedBlockId = BlockTranslator.translate(
                packet.getBlockType(), serverVersion, clientVersion
        );
        return new SetBlockServerPacket(
                packet.getX(), packet.getY(), packet.getZ(),
                translatedBlockId
        );
    }

    /**
     * Translate block IDs in an Alpha BlockChangePacket.
     */
    private Packet translateBlockChange(BlockChangePacket packet) {
        int translatedBlockId = BlockTranslator.translate(
                packet.getBlockType(), serverVersion, clientVersion
        );
        return new BlockChangePacket(
                packet.getX(), packet.getY(), packet.getZ(),
                translatedBlockId, packet.getMetadata()
        );
    }

    private void logFiltered(Packet packet, String reason) {
        if (DEBUG_LOGGING) {
            System.out.printf("[VersionTranslator] Filtered %s (0x%02X) for %s client: %s%n",
                    packet.getClass().getSimpleName(), packet.getPacketId(),
                    clientVersion.name(), reason);
        }
    }

    public ProtocolVersion getServerVersion() {
        return serverVersion;
    }

    public ProtocolVersion getClientVersion() {
        return clientVersion;
    }
}
