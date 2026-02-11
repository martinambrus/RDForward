package com.github.martinambrus.rdforward.protocol.translation;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.PacketRegistry;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Netty channel handler that translates packets between protocol versions.
 *
 * This sits in the Netty pipeline between the codec layer and the
 * game handler. It intercepts outbound packets (server -> client)
 * and translates them to match the client's protocol version.
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

        // Check if this packet ID exists in the client's version
        if (!PacketRegistry.hasPacket(clientVersion, PacketDirection.SERVER_TO_CLIENT, packet.getPacketId())) {
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
        // Classic 0x06: Set Block (Server -> Client)
        if (packet instanceof SetBlockServerPacket) {
            return translateSetBlock((SetBlockServerPacket) packet);
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

    public ProtocolVersion getServerVersion() {
        return serverVersion;
    }

    public ProtocolVersion getClientVersion() {
        return clientVersion;
    }
}
