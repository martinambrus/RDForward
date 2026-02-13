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
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Netty outbound channel handler that translates packets between protocol versions.
 *
 * Sits in the outbound pipeline between the game handler and the encoder.
 * Intercepts server-to-client packets and translates/filters them to match
 * the client's protocol version before encoding.
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
 *   Inbound:  [PacketDecoder] -> [GameHandler]
 *   Outbound: [GameHandler] -> [VersionTranslator] -> [PacketEncoder]
 */
public class VersionTranslator extends ChannelOutboundHandlerAdapter {

    private static final boolean DEBUG_LOGGING = Boolean.getBoolean("rdforward.debug.packets");

    private final ProtocolVersion serverVersion;
    private final ProtocolVersion clientVersion;

    public VersionTranslator(ProtocolVersion serverVersion, ProtocolVersion clientVersion) {
        this.serverVersion = serverVersion;
        this.clientVersion = clientVersion;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Packet)) {
            ctx.write(msg, promise);
            return;
        }

        Packet packet = (Packet) msg;

        // Layer 1: Check if this packet ID exists in the client's version
        if (!PacketRegistry.hasPacket(clientVersion, PacketDirection.SERVER_TO_CLIENT, packet.getPacketId())) {
            logFiltered(packet, "no packet ID in client version");
            promise.setSuccess();
            return;
        }

        // Layer 2: Check capability-based filtering
        if (!passesCapabilityFilter(packet)) {
            logFiltered(packet, "client lacks required capability");
            promise.setSuccess();
            return;
        }

        // Layer 3: Translate packet contents based on type
        Packet translated = translatePacket(packet);
        if (translated != null) {
            ctx.write(translated, promise);
        } else {
            promise.setSuccess();
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
