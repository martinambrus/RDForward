package com.github.martinambrus.rdforward.protocol.translation;

import com.github.martinambrus.rdforward.protocol.Capability;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.PacketRegistry;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.BlockChangePacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.DestroyEntityPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.EntityLookAndMovePacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.EntityLookPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.EntityRelativeMovePacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.EntityTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.SpawnPlayerPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.TimeUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.UpdateHealthPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Netty channel handler that translates packets between protocol versions.
 *
 * This sits in the Netty pipeline between the codec layer and the
 * game handler. It intercepts outbound packets (server -> client)
 * and translates them to match the client's protocol version.
 *
 * Translation layers (applied in order):
 *   1. Packet existence — drop packets whose ID doesn't exist in the client version
 *   2. Capability filter — drop packets requiring capabilities the client lacks
 *   3. Content translation — rewrite packet fields (e.g. block IDs)
 *
 * Architecture (inspired by ViaVersion):
 * - One translator handles one version step (e.g., Classic -> RubyDung)
 * - For multi-version gaps, translators are chained in the pipeline
 * - Each translator only needs to know about its adjacent versions
 *
 * Pipeline for a RubyDung client on a Classic server:
 *   Inbound:  [PacketDecoder] -> [VersionTranslator] -> [GameHandler]
 *   Outbound: [GameHandler] -> [PacketEncoder]
 *
 * The translator filters and transforms inbound packets (from server
 * perspective, these are the packets being sent TO the client).
 */
public class VersionTranslator extends ChannelInboundHandlerAdapter {

    private static final boolean DEBUG_LOGGING = Boolean.getBoolean("rdforward.debug.packets");

    private final ProtocolVersion serverVersion;
    private final ProtocolVersion clientVersion;

    public VersionTranslator(ProtocolVersion serverVersion, ProtocolVersion clientVersion) {
        this.serverVersion = serverVersion;
        this.clientVersion = clientVersion;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Packet)) {
            ctx.fireChannelRead(msg);
            return;
        }

        Packet packet = (Packet) msg;

        // Layer 1: Check if this packet ID exists in the client's version
        if (!PacketRegistry.hasPacket(clientVersion, PacketDirection.SERVER_TO_CLIENT, packet.getPacketId())) {
            logFiltered(packet, "no packet ID in client version");
            return;
        }

        // Layer 2: Check capability-based filtering
        if (!passesCapabilityFilter(packet)) {
            logFiltered(packet, "client lacks required capability");
            return;
        }

        // Layer 3: Translate packet contents based on type
        Packet translated = translatePacket(packet);
        if (translated != null) {
            ctx.fireChannelRead(translated);
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
        return packet instanceof SpawnPlayerPacket
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
