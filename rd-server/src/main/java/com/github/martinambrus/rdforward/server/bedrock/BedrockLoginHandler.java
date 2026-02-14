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
     * Extract the username from the login packet's JWT data.
     * Tries multiple sources in order:
     * 1. Certificate chain (CertificateChainPayload) — standard Xbox auth
     * 2. Single token (TokenPayload) — self-signed/offline auth
     * 3. Client data JWT — fallback, contains skin + device info
     */
    private String extractUsernameFromChain(LoginPacket packet) {
        try {
            org.cloudburstmc.protocol.bedrock.data.auth.AuthPayload authPayload = packet.getAuthPayload();

            // Try 1: Certificate chain (standard Xbox-authenticated login)
            if (authPayload instanceof org.cloudburstmc.protocol.bedrock.data.auth.CertificateChainPayload) {
                List<String> chain =
                        ((org.cloudburstmc.protocol.bedrock.data.auth.CertificateChainPayload) authPayload).getChain();
                if (chain != null) {
                    String name = findDisplayNameInTokens(chain);
                    if (name != null) return name;
                }
            }

            // Try 2: Single token (self-signed/offline mode)
            if (authPayload instanceof org.cloudburstmc.protocol.bedrock.data.auth.TokenPayload) {
                String token =
                        ((org.cloudburstmc.protocol.bedrock.data.auth.TokenPayload) authPayload).getToken();
                if (token != null) {
                    String name = extractUsernameFromJwt(token);
                    if (name != null) return name;
                }
            }

            // Try 3: Client data JWT (contains device info and sometimes username)
            String clientJwt = packet.getClientJwt();
            if (clientJwt != null) {
                String name = extractUsernameFromJwt(clientJwt);
                if (name != null) return name;
            }
        } catch (Exception e) {
            System.err.println("Failed to extract username from Bedrock login: " + e.getMessage());
        }
        return null;
    }

    /**
     * Search a list of JWT tokens for the username.
     */
    private String findDisplayNameInTokens(List<String> tokens) {
        for (String token : tokens) {
            String name = extractUsernameFromJwt(token);
            if (name != null) return name;
        }
        return null;
    }

    /** Fields that may contain the player's username in a JWT payload. */
    private static final String[] USERNAME_FIELDS = {
        "displayName", "ThirdPartyName"
    };

    /**
     * Extract the player username from a single JWT token's payload.
     * Checks multiple fields: "displayName" (Xbox auth chain) and
     * "ThirdPartyName" (self-signed/offline mode client data).
     */
    private String extractUsernameFromJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            for (String field : USERNAME_FIELDS) {
                String value = extractJsonStringValue(payload, field);
                if (value != null && !value.isEmpty()) return value;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Extract a JSON string value by key from a JSON string.
     * Simple parser — finds "key":"value" pattern.
     */
    private String extractJsonStringValue(String json, String key) {
        int nameIdx = json.indexOf("\"" + key + "\"");
        if (nameIdx < 0) return null;
        int colonIdx = json.indexOf(':', nameIdx);
        if (colonIdx < 0) return null;
        int quoteStart = json.indexOf('"', colonIdx + 1);
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteStart >= 0 && quoteEnd > quoteStart) {
            return json.substring(quoteStart + 1, quoteEnd);
        }
        return null;
    }
}
