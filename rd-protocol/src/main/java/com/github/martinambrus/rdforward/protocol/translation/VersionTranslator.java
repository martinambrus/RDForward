package com.github.martinambrus.rdforward.protocol.translation;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Netty channel handler that translates packets between protocol versions.
 *
 * This sits in the Netty pipeline between the codec layer and the
 * game handler. It intercepts packets and modifies them to match
 * the target protocol version.
 *
 * Architecture (inspired by ViaVersion):
 * - One translator handles one version step (e.g., Alpha -> RubyDung)
 * - For multi-version gaps, translators are chained in the pipeline
 * - Each translator only needs to know about its adjacent versions
 *
 * Pipeline for a RubyDung client on an Alpha server:
 *   [PacketDecoder] -> [AlphaToRubyDungTranslator] -> [GameHandler]
 *   [GameHandler] -> [RubyDungToAlphaTranslator] -> [PacketEncoder]
 */
public class VersionTranslator extends ChannelInboundHandlerAdapter {

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

        // Check if this packet type exists in the client's version
        if (!packet.getType().existsIn(clientVersion)) {
            // Drop packets the client can't understand
            return;
        }

        // Translate packet contents based on type
        Packet translated = translatePacket(packet);
        if (translated != null) {
            ctx.fireChannelRead(translated);
        }
    }

    /**
     * Translate a single packet from server version to client version.
     * Returns null if the packet should be dropped.
     */
    private Packet translatePacket(Packet packet) {
        switch (packet.getType()) {
            case BLOCK_CHANGE:
                return translateBlockChange((BlockChangePacket) packet);
            default:
                // Pass through unchanged for packet types that don't need translation
                return packet;
        }
    }

    /**
     * Translate block IDs in a BlockChangePacket.
     */
    private Packet translateBlockChange(BlockChangePacket packet) {
        int translatedBlockId = BlockTranslator.translate(
                packet.getBlockId(), serverVersion, clientVersion
        );
        return new BlockChangePacket(
                packet.getX(), packet.getY(), packet.getZ(),
                translatedBlockId,
                // Strip metadata if client doesn't support it
                clientVersion.getVersionNumber() >= ProtocolVersion.ALPHA_2.getVersionNumber()
                        ? packet.getBlockMetadata() : 0
        );
    }

    public ProtocolVersion getServerVersion() {
        return serverVersion;
    }

    public ProtocolVersion getClientVersion() {
        return clientVersion;
    }
}
