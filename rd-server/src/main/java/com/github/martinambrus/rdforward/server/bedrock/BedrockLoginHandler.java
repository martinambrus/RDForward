package com.github.martinambrus.rdforward.server.bedrock;

import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.common.PacketSignal;

import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;

import com.github.martinambrus.rdforward.server.bedrock.BedrockProtocolConstants;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Handles the Bedrock login sequence:
 * 1. RequestNetworkSettings -> NetworkSettings (enable compression)
 * 2. Login -> PlayStatus(LOGIN_SUCCESS) + ResourcePacksInfo
 * 3. ResourcePackClientResponse(HAVE_ALL_PACKS) -> ResourcePackStack
 * 4. ResourcePackClientResponse(COMPLETED) -> transition to gameplay handler
 *
 * Encryption is skipped (offline mode).
 */
public class BedrockLoginHandler implements BedrockPacketHandler {

    private final BedrockServerSession session;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;
    private final BedrockBlockMapper blockMapper;
    private final BedrockChunkConverter chunkConverter;
    private final BedrockRegistryData registryData;
    private final Runnable pongUpdater;

    private String username;

    public BedrockLoginHandler(BedrockServerSession session, ServerWorld world,
                               PlayerManager playerManager, ChunkManager chunkManager,
                               BedrockBlockMapper blockMapper, BedrockChunkConverter chunkConverter,
                               BedrockRegistryData registryData, Runnable pongUpdater) {
        this.session = session;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
        this.blockMapper = blockMapper;
        this.chunkConverter = chunkConverter;
        this.registryData = registryData;
        this.pongUpdater = pongUpdater;
    }

    @Override
    public PacketSignal handle(RequestNetworkSettingsPacket packet) {
        int clientProtocol = packet.getProtocolVersion();
        int serverProtocol = BedrockProtocolConstants.CODEC.getProtocolVersion();
        if (clientProtocol != serverProtocol) {
            PlayStatusPacket status = new PlayStatusPacket();
            status.setStatus(clientProtocol > serverProtocol
                    ? PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD
                    : PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD);
            session.sendPacketImmediately(status);
            return PacketSignal.HANDLED;
        }

        NetworkSettingsPacket settings = new NetworkSettingsPacket();
        settings.setCompressionThreshold(BedrockProtocolConstants.COMPRESSION_THRESHOLD);
        settings.setCompressionAlgorithm(PacketCompressionAlgorithm.ZLIB);
        session.sendPacketImmediately(settings);

        session.setCompression(PacketCompressionAlgorithm.ZLIB);
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(LoginPacket packet) {
        // Extract username from JWT chain (first token's payload)
        username = extractUsernameFromChain(packet);
        if (username == null || username.isEmpty()) {
            username = "BedrockPlayer";
        }

        // Skip encryption — send LOGIN_SUCCESS immediately
        PlayStatusPacket status = new PlayStatusPacket();
        status.setStatus(PlayStatusPacket.Status.LOGIN_SUCCESS);
        session.sendPacket(status);

        // Send empty resource packs info
        ResourcePacksInfoPacket packsInfo = new ResourcePacksInfoPacket();
        packsInfo.setWorldTemplateId(UUID.randomUUID());
        packsInfo.setWorldTemplateVersion("*");
        session.sendPacket(packsInfo);

        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(ResourcePackClientResponsePacket packet) {
        switch (packet.getStatus()) {
            case HAVE_ALL_PACKS:
                ResourcePackStackPacket stack = new ResourcePackStackPacket();
                stack.setGameVersion("*");
                session.sendPacket(stack);
                break;

            case COMPLETED:
                // Set vanilla block and item definitions on the codec helper — required
                // for correct serialization of block/item-related packets
                session.getPeer().getCodecHelper().setBlockDefinitions(
                        BedrockProtocolConstants.getBlockDefinitions());
                session.getPeer().getCodecHelper().setItemDefinitions(
                        BedrockProtocolConstants.getItemDefinitions());
                // Transition to gameplay handler
                BedrockGameplayHandler gameplayHandler = new BedrockGameplayHandler(
                        session, world, playerManager, chunkManager,
                        blockMapper, chunkConverter, registryData, username, pongUpdater);
                session.setPacketHandler(gameplayHandler);
                gameplayHandler.onReady();
                break;

            default:
                break;
        }
        return PacketSignal.HANDLED;
    }

    /**
     * Extract the username from the JWT chain in the login packet.
     * The chain contains JWTs; the last one has the extraData with displayName.
     */
    private String extractUsernameFromChain(LoginPacket packet) {
        try {
            // Beta12+ API: getAuthPayload() returns AuthPayload;
            // for certificate-chain auth, cast to CertificateChainPayload
            org.cloudburstmc.protocol.bedrock.data.auth.AuthPayload authPayload = packet.getAuthPayload();
            List<String> chain = null;
            if (authPayload instanceof org.cloudburstmc.protocol.bedrock.data.auth.CertificateChainPayload) {
                chain = ((org.cloudburstmc.protocol.bedrock.data.auth.CertificateChainPayload) authPayload).getChain();
            }
            if (chain == null) {
                return null;
            }

            for (String token : chain) {
                // JWT format: header.payload.signature
                String[] parts = token.split("\\.");
                if (parts.length < 2) continue;

                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                // Simple JSON extraction for "displayName"
                int nameIdx = payload.indexOf("\"displayName\"");
                if (nameIdx >= 0) {
                    int colonIdx = payload.indexOf(':', nameIdx);
                    int quoteStart = payload.indexOf('"', colonIdx + 1);
                    int quoteEnd = payload.indexOf('"', quoteStart + 1);
                    if (quoteStart >= 0 && quoteEnd > quoteStart) {
                        return payload.substring(quoteStart + 1, quoteEnd);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to extract username from Bedrock login: " + e.getMessage());
        }
        return null;
    }
}
