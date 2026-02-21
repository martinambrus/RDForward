package com.github.martinambrus.rdforward.bot;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.bedrock.BedrockProtocolConstants;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.BedrockClientSession;
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm;
import org.cloudburstmc.protocol.bedrock.data.auth.AuthType;
import org.cloudburstmc.protocol.bedrock.data.auth.CertificateChainPayload;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Handles the Bedrock login flow and S2C gameplay packets for bot clients.
 *
 * Login sequence:
 * 1. Send RequestNetworkSettings
 * 2. Receive NetworkSettings -> enable compression, send LoginPacket
 * 3. Receive PlayStatus(LOGIN_SUCCESS)
 * 4. Receive ResourcePacksInfo -> send HAVE_ALL_PACKS
 * 5. Receive ResourcePackStack -> send COMPLETED
 * 6. Receive StartGamePacket -> record entityId + position
 * 7. Receive PlayStatus(PLAYER_SPAWN) -> send SetLocalPlayerAsInitialized, mark login complete
 *
 * Gameplay packets handled: MovePlayerPacket, AddPlayerPacket, RemoveEntityPacket,
 * UpdateBlockPacket, TextPacket.
 */
class BotBedrockPacketHandler implements BedrockPacketHandler {

    private final String username;
    private volatile BedrockClientSession session;
    private volatile BotSession botSession;
    private final CountDownLatch sessionReady = new CountDownLatch(1);
    private final CountDownLatch initLatch = new CountDownLatch(1);

    BotBedrockPacketHandler(String username) {
        this.username = username;
    }

    void init(BedrockClientSession session) {
        this.session = session;
        initLatch.countDown();
    }

    void startLogin() throws InterruptedException {
        // Wait for initSession() callback (async on RakNet handshake completion)
        if (!initLatch.await(5000, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException("Bedrock session init timed out for " + username);
        }
        RequestNetworkSettingsPacket request = new RequestNetworkSettingsPacket();
        request.setProtocolVersion(BedrockProtocolConstants.CODEC.getProtocolVersion());
        session.sendPacketImmediately(request);
    }

    // ---- Login flow handlers ----

    @Override
    public PacketSignal handle(NetworkSettingsPacket packet) {
        session.setCompression(PacketCompressionAlgorithm.ZLIB);

        // Construct a minimal self-signed JWT with the bot's username
        String jwt = createOfflineJwt(username);

        LoginPacket login = new LoginPacket();
        login.setProtocolVersion(BedrockProtocolConstants.CODEC.getProtocolVersion());
        login.setAuthPayload(new CertificateChainPayload(Collections.singletonList(jwt), AuthType.SELF_SIGNED));
        login.setClientJwt(jwt);
        session.sendPacketImmediately(login);

        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(PlayStatusPacket packet) {
        if (packet.getStatus() == PlayStatusPacket.Status.PLAYER_SPAWN) {
            // Spawn sequence complete — send initialization acknowledgement
            SetLocalPlayerAsInitializedPacket init = new SetLocalPlayerAsInitializedPacket();
            init.setRuntimeEntityId(botSession != null ? botSession.getEntityId() : 0);
            session.sendPacket(init);

            if (botSession != null && !botSession.isLoginComplete()) {
                botSession.markLoginComplete();
            }
        }
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(ResourcePacksInfoPacket packet) {
        ResourcePackClientResponsePacket response = new ResourcePackClientResponsePacket();
        response.setStatus(ResourcePackClientResponsePacket.Status.HAVE_ALL_PACKS);
        session.sendPacket(response);
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(ResourcePackStackPacket packet) {
        // Set block/item definitions on codec helper before gameplay packets arrive
        session.getPeer().getCodecHelper().setBlockDefinitions(
                BedrockProtocolConstants.getBlockDefinitions());
        session.getPeer().getCodecHelper().setItemDefinitions(
                BedrockProtocolConstants.getItemDefinitions());

        ResourcePackClientResponsePacket response = new ResourcePackClientResponsePacket();
        response.setStatus(ResourcePackClientResponsePacket.Status.COMPLETED);
        session.sendPacket(response);
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(StartGamePacket packet) {
        // Create BotSession now that we have entity ID and position
        botSession = new BotSession(session, ProtocolVersion.BEDROCK);
        botSession.recordLogin((int) packet.getRuntimeEntityId());

        Vector3f pos = packet.getPlayerPosition();
        // StartGamePacket position is eye-level
        botSession.recordPosition(pos.getX(), pos.getY(), pos.getZ(), 0, 0);

        sessionReady.countDown();
        return PacketSignal.HANDLED;
    }

    // ---- Gameplay packet handlers ----

    @Override
    public PacketSignal handle(MovePlayerPacket packet) {
        if (botSession == null) return PacketSignal.HANDLED;
        Vector3f pos = packet.getPosition();
        Vector3f rot = packet.getRotation();
        // MovePlayerPacket position is eye-level; rot = (pitch, yaw, headYaw)
        botSession.recordPosition(pos.getX(), pos.getY(), pos.getZ(), rot.getY(), rot.getX());
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(AddPlayerPacket packet) {
        if (botSession == null) return PacketSignal.HANDLED;
        botSession.recordSpawnPlayer((int) packet.getRuntimeEntityId(), packet.getUsername());
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(RemoveEntityPacket packet) {
        if (botSession == null) return PacketSignal.HANDLED;
        botSession.recordDespawn((int) packet.getUniqueEntityId());
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(UpdateBlockPacket packet) {
        if (botSession == null) return PacketSignal.HANDLED;
        org.cloudburstmc.math.vector.Vector3i pos = packet.getBlockPosition();
        int runtimeId = packet.getDefinition().getRuntimeId();
        botSession.recordBlockChange(pos.getX(), pos.getY(), pos.getZ(), runtimeId);
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(TextPacket packet) {
        if (botSession == null) return PacketSignal.HANDLED;
        botSession.recordChat(packet.getMessage());
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(LevelChunkPacket packet) {
        if (botSession == null) return PacketSignal.HANDLED;
        // Record that chunk data was received (we don't parse Bedrock sub-chunks yet)
        botSession.recordBedrockChunk(packet.getChunkX(), packet.getChunkZ());
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(ChunkRadiusUpdatedPacket packet) {
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(NetworkChunkPublisherUpdatePacket packet) {
        return PacketSignal.HANDLED;
    }

    // Silently consume packets the bot doesn't need
    @Override
    public PacketSignal handle(ItemComponentPacket packet) { return PacketSignal.HANDLED; }
    @Override
    public PacketSignal handle(AvailableEntityIdentifiersPacket packet) { return PacketSignal.HANDLED; }
    @Override
    public PacketSignal handle(CameraPresetsPacket packet) { return PacketSignal.HANDLED; }
    @Override
    public PacketSignal handle(CreativeContentPacket packet) { return PacketSignal.HANDLED; }
    @Override
    public PacketSignal handle(UpdateAbilitiesPacket packet) { return PacketSignal.HANDLED; }
    @Override
    public PacketSignal handle(SetEntityDataPacket packet) { return PacketSignal.HANDLED; }
    @Override
    public PacketSignal handle(UpdateAttributesPacket packet) { return PacketSignal.HANDLED; }
    @Override
    public PacketSignal handle(GameRulesChangedPacket packet) { return PacketSignal.HANDLED; }
    @Override
    public PacketSignal handle(SetTimePacket packet) {
        if (botSession != null) {
            botSession.recordTimeUpdate(packet.getTime());
        }
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(LevelEventPacket packet) {
        if (botSession != null) {
            org.cloudburstmc.protocol.bedrock.data.LevelEventType type = packet.getType();
            if (type == org.cloudburstmc.protocol.bedrock.data.LevelEvent.START_RAINING) {
                botSession.recordWeatherChange(1); // rain started
            } else if (type == org.cloudburstmc.protocol.bedrock.data.LevelEvent.STOP_RAINING) {
                botSession.recordWeatherChange(2); // rain stopped
            } else if (type == org.cloudburstmc.protocol.bedrock.data.LevelEvent.START_THUNDERSTORM) {
                botSession.recordWeatherChange(3); // thunder started
            } else if (type == org.cloudburstmc.protocol.bedrock.data.LevelEvent.STOP_THUNDERSTORM) {
                botSession.recordWeatherChange(4); // thunder stopped
            }
        }
        return PacketSignal.HANDLED;
    }
    @Override
    public PacketSignal handle(PlayerListPacket packet) { return PacketSignal.HANDLED; }
    @Override
    public PacketSignal handle(TickSyncPacket packet) { return PacketSignal.HANDLED; }

    @Override
    public PacketSignal handle(DisconnectPacket packet) {
        if (botSession != null) {
            botSession.markDisconnected();
        }
        return PacketSignal.HANDLED;
    }

    @Override
    public void onDisconnect(CharSequence reason) {
        if (botSession != null) {
            botSession.markDisconnected();
        }
    }

    // ---- Session access ----

    BotSession awaitSession(long timeoutMs) throws InterruptedException {
        sessionReady.await(timeoutMs, TimeUnit.MILLISECONDS);
        return botSession;
    }

    BotSession getSession() {
        return botSession;
    }

    void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect("Bot disconnecting");
        }
    }

    // ---- JWT helper ----

    /**
     * Create a minimal unsigned JWT containing the bot's display name.
     * The server extracts the username from the payload's "displayName" field
     * without verifying the signature (offline mode).
     */
    private static String createOfflineJwt(String displayName) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"displayName\":\"" + displayName + "\"}").getBytes());
        return header + "." + payload + ".";
    }
}
