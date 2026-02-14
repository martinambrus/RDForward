package com.github.martinambrus.rdforward.server.bedrock;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;

import java.util.List;

/**
 * Bridges a BedrockServerSession to the server's ConnectedPlayer system.
 *
 * Holds the Bedrock session reference and a ClassicToBedrockTranslator.
 * When the server broadcasts Classic packets, ConnectedPlayer routes them
 * here instead of through the Netty channel pipeline.
 */
public class BedrockSessionWrapper {

    private final BedrockServerSession session;
    private final ClassicToBedrockTranslator translator;

    public BedrockSessionWrapper(BedrockServerSession session,
                                  ClassicToBedrockTranslator translator) {
        this.session = session;
        this.translator = translator;
    }

    /**
     * Translate a Classic broadcast packet to Bedrock and send it.
     * Some Classic packets may produce multiple Bedrock packets
     * (e.g. SpawnPlayer requires PlayerListPacket before AddPlayerPacket).
     * Packets that have no Bedrock equivalent are silently dropped.
     */
    public void translateAndSend(Packet classicPacket) {
        if (!session.isConnected()) {
            return;
        }

        List<org.cloudburstmc.protocol.bedrock.packet.BedrockPacket> packets =
                translator.translateAll(classicPacket);
        for (org.cloudburstmc.protocol.bedrock.packet.BedrockPacket pkt : packets) {
            session.sendPacket(pkt);
        }
    }

    /**
     * Send a Bedrock packet directly (not translated from Classic).
     */
    public void sendDirect(org.cloudburstmc.protocol.bedrock.packet.BedrockPacket packet) {
        if (session.isConnected()) {
            session.sendPacket(packet);
        }
    }

    /**
     * Disconnect the Bedrock session.
     */
    public void disconnect(String reason) {
        session.disconnect(reason);
    }

    public BedrockServerSession getSession() {
        return session;
    }

    public ClassicToBedrockTranslator getTranslator() {
        return translator;
    }
}
