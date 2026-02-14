package com.github.martinambrus.rdforward.protocol.packet;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
// Explicit imports resolve classic/alpha name collisions (explicit beats wildcard)
import com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket;

import java.util.HashMap;
import java.util.Map;

/**
 * Version-aware packet registry.
 *
 * Maps (protocol version, direction, packet ID) to packet factories.
 * This is necessary because Classic and Alpha reuse the same packet IDs
 * with different meanings (e.g., 0x00 = Player Identification in Classic
 * but Keep Alive in Alpha).
 *
 * The registry follows the same pattern as ViaVersion's Protocol classes:
 * each version registers its packet mappings, and the decoder uses the
 * connection's protocol version to resolve the correct packet class.
 */
public class PacketRegistry {

    /**
     * Simple packet factory interface (Java 8 compatible).
     */
    public interface PacketFactory {
        Packet create();
    }

    private static final Map<String, PacketFactory> REGISTRY = new HashMap<String, PacketFactory>();

    static {
        registerClassicPackets();
        registerAlphaPackets();
    }

    /**
     * Register all Classic protocol packets for both RubyDung and Classic versions.
     * RubyDung is a pre-Classic prototype, so it uses the same packet format.
     */
    private static void registerClassicPackets() {
        ProtocolVersion[] classicVersions = {ProtocolVersion.RUBYDUNG, ProtocolVersion.CLASSIC};

        for (ProtocolVersion v : classicVersions) {
            // Client -> Server packets
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x00, new PacketFactory() {
                public Packet create() { return new PlayerIdentificationPacket(); }
            });
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x05, new PacketFactory() {
                public Packet create() { return new SetBlockClientPacket(); }
            });
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x08, new PacketFactory() {
                public Packet create() { return new PlayerTeleportPacket(); }
            });
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x0D, new PacketFactory() {
                public Packet create() { return new MessagePacket(); }
            });

            // Server -> Client packets
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x00, new PacketFactory() {
                public Packet create() { return new ServerIdentificationPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x01, new PacketFactory() {
                public Packet create() { return new PingPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x02, new PacketFactory() {
                public Packet create() { return new LevelInitializePacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x03, new PacketFactory() {
                public Packet create() { return new LevelDataChunkPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x04, new PacketFactory() {
                public Packet create() { return new LevelFinalizePacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x06, new PacketFactory() {
                public Packet create() { return new SetBlockServerPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x07, new PacketFactory() {
                public Packet create() { return new SpawnPlayerPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x08, new PacketFactory() {
                public Packet create() { return new PlayerTeleportPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x09, new PacketFactory() {
                public Packet create() { return new PositionOrientationUpdatePacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x0A, new PacketFactory() {
                public Packet create() { return new PositionUpdatePacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x0B, new PacketFactory() {
                public Packet create() { return new OrientationUpdatePacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x0C, new PacketFactory() {
                public Packet create() { return new DespawnPlayerPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x0D, new PacketFactory() {
                public Packet create() { return new MessagePacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x0E, new PacketFactory() {
                public Packet create() { return new DisconnectPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x0F, new PacketFactory() {
                public Packet create() { return new UpdateUserTypePacket(); }
            });
        }
    }

    /**
     * Register all Alpha protocol packets for both Alpha versions.
     * Alpha uses different packet IDs from Classic (e.g., 0x00 = KeepAlive,
     * not PlayerIdentification; 0x03 = Chat, not LevelDataChunk).
     */
    private static void registerAlphaPackets() {
        ProtocolVersion[] alphaVersions = {ProtocolVersion.ALPHA_1_2_3, ProtocolVersion.ALPHA_1_2_5, ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.ALPHA_1_0_16};

        for (ProtocolVersion v : alphaVersions) {
            // === Bidirectional packets ===
            // 0x00 Keep Alive
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x00, new PacketFactory() {
                public Packet create() { return new KeepAlivePacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x00, new PacketFactory() {
                public Packet create() { return new KeepAlivePacket(); }
            });
            // 0x04 C2S: zero-payload tick/ack (client sends after inventory update)
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x04, new PacketFactory() {
                public Packet create() { return new KeepAlivePacket(); }
            });
            // 0x05 C2S: Player Inventory sync (client sends inventory contents)
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x05, new PacketFactory() {
                public Packet create() { return new PlayerInventoryPacket(); }
            });
            // 0x03 Chat
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x03, new PacketFactory() {
                public Packet create() { return new ChatPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x03, new PacketFactory() {
                public Packet create() { return new ChatPacket(); }
            });
            // 0xFF Disconnect (FQN to avoid collision with classic.DisconnectPacket)
            register(v, PacketDirection.CLIENT_TO_SERVER, 0xFF, new PacketFactory() {
                public Packet create() { return new com.github.martinambrus.rdforward.protocol.packet.alpha.DisconnectPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0xFF, new PacketFactory() {
                public Packet create() { return new com.github.martinambrus.rdforward.protocol.packet.alpha.DisconnectPacket(); }
            });

            // === Client -> Server packets ===
            // Login flow
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x01, new PacketFactory() {
                public Packet create() { return new LoginC2SPacket(); }
            });
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x02, new PacketFactory() {
                public Packet create() { return new HandshakeC2SPacket(); }
            });
            // Player movement
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x0A, new PacketFactory() {
                public Packet create() { return new PlayerOnGroundPacket(); }
            });
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x0B, new PacketFactory() {
                public Packet create() { return new PlayerPositionPacket(); }
            });
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x0C, new PacketFactory() {
                public Packet create() { return new PlayerLookPacket(); }
            });
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x0D, new PacketFactory() {
                public Packet create() { return new PlayerPositionAndLookC2SPacket(); }
            });
            // Player actions
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x0E, new PacketFactory() {
                public Packet create() { return new PlayerDiggingPacket(); }
            });
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x0F, new PacketFactory() {
                public Packet create() { return new PlayerBlockPlacementPacket(); }
            });
            // 0x10 Holding Change (hotbar slot switch)
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x10, new PacketFactory() {
                public Packet create() { return new HoldingChangePacket(); }
            });
            // 0x12 Animation (arm swing)
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x12, new PacketFactory() {
                public Packet create() { return new AnimationPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x12, new PacketFactory() {
                public Packet create() { return new AnimationPacket(); }
            });
            // 0x15 Pickup Spawn (C2S: client dropping an item)
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x15, new PacketFactory() {
                public Packet create() { return new PickupSpawnPacket(); }
            });

            // === Server -> Client packets ===
            // Login flow
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x01, new PacketFactory() {
                public Packet create() { return new LoginS2CPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x02, new PacketFactory() {
                public Packet create() { return new HandshakeS2CPacket(); }
            });
            // Game state
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x04, new PacketFactory() {
                public Packet create() { return new TimeUpdatePacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x06, new PacketFactory() {
                public Packet create() { return new SpawnPositionPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x08, new PacketFactory() {
                public Packet create() { return new UpdateHealthPacket(); }
            });
            // Player position (S->C uses swapped y/stance field order)
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x0D, new PacketFactory() {
                public Packet create() { return new PlayerPositionAndLookS2CPacket(); }
            });
            // Entity packets (FQN to avoid collision with classic.SpawnPlayerPacket)
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x14, new PacketFactory() {
                public Packet create() { return new com.github.martinambrus.rdforward.protocol.packet.alpha.SpawnPlayerPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x15, new PacketFactory() {
                public Packet create() { return new PickupSpawnPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x16, new PacketFactory() {
                public Packet create() { return new CollectItemPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x1D, new PacketFactory() {
                public Packet create() { return new DestroyEntityPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x1F, new PacketFactory() {
                public Packet create() { return new EntityRelativeMovePacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x20, new PacketFactory() {
                public Packet create() { return new EntityLookPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x21, new PacketFactory() {
                public Packet create() { return new EntityLookAndMovePacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x22, new PacketFactory() {
                public Packet create() { return new EntityTeleportPacket(); }
            });
            // World packets
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x32, new PacketFactory() {
                public Packet create() { return new PreChunkPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x33, new PacketFactory() {
                public Packet create() { return new MapChunkPacket(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x35, new PacketFactory() {
                public Packet create() { return new BlockChangePacket(); }
            });
            // Inventory
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x11, new PacketFactory() {
                public Packet create() { return new AddToInventoryPacket(); }
            });
        }

        // Override version-specific packets for pre-rewrite Alpha (v10-v14).
        // These versions use different wire formats for 0x0F and 0x15.
        ProtocolVersion[] preRewriteVersions = {ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.ALPHA_1_0_16};
        for (ProtocolVersion v : preRewriteVersions) {
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x0F, new PacketFactory() {
                public Packet create() { return new PlayerBlockPlacementPacketV14(); }
            });
            register(v, PacketDirection.CLIENT_TO_SERVER, 0x15, new PacketFactory() {
                public Packet create() { return new PickupSpawnPacketV14(); }
            });
            register(v, PacketDirection.SERVER_TO_CLIENT, 0x15, new PacketFactory() {
                public Packet create() { return new PickupSpawnPacketV14(); }
            });
        }
    }

    /**
     * Register a packet factory for a specific version, direction, and ID.
     */
    public static void register(ProtocolVersion version, PacketDirection direction, int packetId, PacketFactory factory) {
        REGISTRY.put(registryKey(version, direction, packetId), factory);
    }

    /**
     * Create a packet instance for the given version, direction, and ID.
     * Returns null if no packet is registered for this combination.
     */
    public static Packet createPacket(ProtocolVersion version, PacketDirection direction, int packetId) {
        PacketFactory factory = REGISTRY.get(registryKey(version, direction, packetId));
        return factory != null ? factory.create() : null;
    }

    /**
     * Check if a packet is registered for the given combination.
     */
    public static boolean hasPacket(ProtocolVersion version, PacketDirection direction, int packetId) {
        return REGISTRY.containsKey(registryKey(version, direction, packetId));
    }

    private static String registryKey(ProtocolVersion version, PacketDirection direction, int packetId) {
        return version.name() + ":" + direction.name() + ":" + packetId;
    }
}
