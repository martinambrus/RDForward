package com.github.martinambrus.rdforward.protocol.packet;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;

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
        // TODO: registerAlphaPackets() — will be added when Alpha packet classes are implemented
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
