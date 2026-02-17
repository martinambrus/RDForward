package com.github.martinambrus.rdforward.protocol.packet;

import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
import com.github.martinambrus.rdforward.protocol.packet.netty.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Packet registry for the 1.7.2+ Netty protocol.
 *
 * Maps (state, direction, packetId) to packet factories for decoding,
 * and (state, direction, packetClass) to packetId for encoding.
 *
 * Unlike the pre-Netty PacketRegistry which is version-aware, this
 * registry is state-aware. All 1.7.2 connections use the same mappings,
 * with the active state determining which ID space is used.
 */
public class NettyPacketRegistry {

    public interface PacketFactory {
        Packet create();
    }

    /** Forward map: (state, direction, packetId) -> factory (v4/v5 base) */
    private static final Map<String, PacketFactory> REGISTRY = new HashMap<String, PacketFactory>();

    /** V47 overlay: C2S packets that differ in 1.8. Checked first for v47+ clients. */
    private static final Map<String, PacketFactory> REGISTRY_V47 = new HashMap<String, PacketFactory>();

    /** Reverse map: (state, direction, className) -> packetId */
    private static final Map<String, Integer> REVERSE = new HashMap<String, Integer>();

    static {
        // === HANDSHAKING state ===
        registerC2S(ConnectionState.HANDSHAKING, 0x00, new PacketFactory() {
            public Packet create() { return new NettyHandshakePacket(); }
        }, NettyHandshakePacket.class);

        // === STATUS state ===
        registerC2S(ConnectionState.STATUS, 0x00, new PacketFactory() {
            public Packet create() { return new StatusRequestPacket(); }
        }, StatusRequestPacket.class);
        registerC2S(ConnectionState.STATUS, 0x01, new PacketFactory() {
            public Packet create() { return new StatusPingPacket(); }
        }, StatusPingPacket.class);
        registerS2C(ConnectionState.STATUS, 0x00, new PacketFactory() {
            public Packet create() { return new StatusResponsePacket(); }
        }, StatusResponsePacket.class);
        registerS2C(ConnectionState.STATUS, 0x01, new PacketFactory() {
            public Packet create() { return new StatusPingPacket(); }
        }, StatusPingPacket.class);

        // === LOGIN state ===
        registerC2S(ConnectionState.LOGIN, 0x00, new PacketFactory() {
            public Packet create() { return new LoginStartPacket(); }
        }, LoginStartPacket.class);
        registerC2S(ConnectionState.LOGIN, 0x01, new PacketFactory() {
            public Packet create() { return new NettyEncryptionResponsePacket(); }
        }, NettyEncryptionResponsePacket.class);
        registerS2C(ConnectionState.LOGIN, 0x00, new PacketFactory() {
            public Packet create() { return new LoginDisconnectPacket(); }
        }, LoginDisconnectPacket.class);
        registerS2C(ConnectionState.LOGIN, 0x01, new PacketFactory() {
            public Packet create() { return new NettyEncryptionRequestPacket(); }
        }, NettyEncryptionRequestPacket.class);
        registerS2C(ConnectionState.LOGIN, 0x02, new PacketFactory() {
            public Packet create() { return new LoginSuccessPacket(); }
        }, LoginSuccessPacket.class);

        // === PLAY state: S2C ===
        registerS2C(ConnectionState.PLAY, 0x00, new PacketFactory() {
            public Packet create() { return new KeepAlivePacketV17(); }
        }, KeepAlivePacketV17.class);
        registerS2C(ConnectionState.PLAY, 0x01, new PacketFactory() {
            public Packet create() { return new JoinGamePacket(); }
        }, JoinGamePacket.class);
        registerS2C(ConnectionState.PLAY, 0x02, new PacketFactory() {
            public Packet create() { return new NettyChatS2CPacket(); }
        }, NettyChatS2CPacket.class);
        registerS2C(ConnectionState.PLAY, 0x05, new PacketFactory() {
            public Packet create() { return new SpawnPositionPacket(); }
        }, SpawnPositionPacket.class);
        registerS2C(ConnectionState.PLAY, 0x08, new PacketFactory() {
            public Packet create() { return new NettyPlayerPositionS2CPacket(); }
        }, NettyPlayerPositionS2CPacket.class);
        registerS2C(ConnectionState.PLAY, 0x0C, new PacketFactory() {
            public Packet create() { return new NettySpawnPlayerPacket(); }
        }, NettySpawnPlayerPacket.class);
        // V5 variant — only register reverse map so encoder can look up packet ID.
        // Forward map stays on v4's NettySpawnPlayerPacket (irrelevant: S2C only).
        REVERSE.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT,
                NettySpawnPlayerPacketV5.class), 0x0C);
        registerS2C(ConnectionState.PLAY, 0x13, new PacketFactory() {
            public Packet create() { return new NettyDestroyEntitiesPacket(); }
        }, NettyDestroyEntitiesPacket.class);
        registerS2C(ConnectionState.PLAY, 0x15, new PacketFactory() {
            public Packet create() { return new EntityRelativeMovePacket(); }
        }, EntityRelativeMovePacket.class);
        registerS2C(ConnectionState.PLAY, 0x16, new PacketFactory() {
            public Packet create() { return new EntityLookPacket(); }
        }, EntityLookPacket.class);
        registerS2C(ConnectionState.PLAY, 0x17, new PacketFactory() {
            public Packet create() { return new EntityLookAndMovePacket(); }
        }, EntityLookAndMovePacket.class);
        registerS2C(ConnectionState.PLAY, 0x18, new PacketFactory() {
            public Packet create() { return new EntityTeleportPacket(); }
        }, EntityTeleportPacket.class);
        registerS2C(ConnectionState.PLAY, 0x20, new PacketFactory() {
            public Packet create() { return new NettyEntityPropertiesPacket(); }
        }, NettyEntityPropertiesPacket.class);
        registerS2C(ConnectionState.PLAY, 0x21, new PacketFactory() {
            public Packet create() { return new MapChunkPacketV39(); }
        }, MapChunkPacketV39.class);
        registerS2C(ConnectionState.PLAY, 0x23, new PacketFactory() {
            public Packet create() { return new NettyBlockChangePacket(); }
        }, NettyBlockChangePacket.class);
        registerS2C(ConnectionState.PLAY, 0x2F, new PacketFactory() {
            public Packet create() { return new NettySetSlotPacket(); }
        }, NettySetSlotPacket.class);
        registerS2C(ConnectionState.PLAY, 0x30, new PacketFactory() {
            public Packet create() { return new NettyWindowItemsPacket(); }
        }, NettyWindowItemsPacket.class);
        registerS2C(ConnectionState.PLAY, 0x38, new PacketFactory() {
            public Packet create() { return new NettyPlayerListItemPacket(); }
        }, NettyPlayerListItemPacket.class);
        registerS2C(ConnectionState.PLAY, 0x39, new PacketFactory() {
            public Packet create() { return new PlayerAbilitiesPacketV73(); }
        }, PlayerAbilitiesPacketV73.class);
        registerS2C(ConnectionState.PLAY, 0x40, new PacketFactory() {
            public Packet create() { return new NettyDisconnectPacket(); }
        }, NettyDisconnectPacket.class);

        // === PLAY state: C2S ===
        registerC2S(ConnectionState.PLAY, 0x00, new PacketFactory() {
            public Packet create() { return new KeepAlivePacketV17(); }
        }, KeepAlivePacketV17.class);
        registerC2S(ConnectionState.PLAY, 0x01, new PacketFactory() {
            public Packet create() { return new NettyChatC2SPacket(); }
        }, NettyChatC2SPacket.class);
        registerC2S(ConnectionState.PLAY, 0x02, new PacketFactory() {
            public Packet create() { return new NettyUseEntityPacket(); }
        }, NettyUseEntityPacket.class);
        registerC2S(ConnectionState.PLAY, 0x03, new PacketFactory() {
            public Packet create() { return new PlayerOnGroundPacket(); }
        }, PlayerOnGroundPacket.class);
        registerC2S(ConnectionState.PLAY, 0x04, new PacketFactory() {
            public Packet create() { return new PlayerPositionPacket(); }
        }, PlayerPositionPacket.class);
        registerC2S(ConnectionState.PLAY, 0x05, new PacketFactory() {
            public Packet create() { return new PlayerLookPacket(); }
        }, PlayerLookPacket.class);
        registerC2S(ConnectionState.PLAY, 0x06, new PacketFactory() {
            public Packet create() { return new PlayerPositionAndLookC2SPacket(); }
        }, PlayerPositionAndLookC2SPacket.class);
        registerC2S(ConnectionState.PLAY, 0x07, new PacketFactory() {
            public Packet create() { return new PlayerDiggingPacket(); }
        }, PlayerDiggingPacket.class);
        registerC2S(ConnectionState.PLAY, 0x08, new PacketFactory() {
            public Packet create() { return new NettyBlockPlacementPacket(); }
        }, NettyBlockPlacementPacket.class);
        registerC2S(ConnectionState.PLAY, 0x09, new PacketFactory() {
            public Packet create() { return new HoldingChangePacketBeta(); }
        }, HoldingChangePacketBeta.class);
        registerC2S(ConnectionState.PLAY, 0x0A, new PacketFactory() {
            public Packet create() { return new AnimationPacket(); }
        }, AnimationPacket.class);
        registerC2S(ConnectionState.PLAY, 0x0B, new PacketFactory() {
            public Packet create() { return new NettyEntityActionPacket(); }
        }, NettyEntityActionPacket.class);
        registerC2S(ConnectionState.PLAY, 0x0C, new PacketFactory() {
            public Packet create() { return new NettySteerVehiclePacket(); }
        }, NettySteerVehiclePacket.class);
        registerC2S(ConnectionState.PLAY, 0x0D, new PacketFactory() {
            public Packet create() { return new CloseWindowPacket(); }
        }, CloseWindowPacket.class);
        registerC2S(ConnectionState.PLAY, 0x0E, new PacketFactory() {
            public Packet create() { return new NettyWindowClickPacket(); }
        }, NettyWindowClickPacket.class);
        registerC2S(ConnectionState.PLAY, 0x0F, new PacketFactory() {
            public Packet create() { return new ConfirmTransactionPacket(); }
        }, ConfirmTransactionPacket.class);
        registerC2S(ConnectionState.PLAY, 0x10, new PacketFactory() {
            public Packet create() { return new NettyCreativeSlotPacket(); }
        }, NettyCreativeSlotPacket.class);
        registerC2S(ConnectionState.PLAY, 0x11, new PacketFactory() {
            public Packet create() { return new EnchantItemPacket(); }
        }, EnchantItemPacket.class);
        registerC2S(ConnectionState.PLAY, 0x12, new PacketFactory() {
            public Packet create() { return new NettyUpdateSignPacket(); }
        }, NettyUpdateSignPacket.class);
        registerC2S(ConnectionState.PLAY, 0x13, new PacketFactory() {
            public Packet create() { return new PlayerAbilitiesPacketV73(); }
        }, PlayerAbilitiesPacketV73.class);
        registerC2S(ConnectionState.PLAY, 0x14, new PacketFactory() {
            public Packet create() { return new NettyTabCompletePacket(); }
        }, NettyTabCompletePacket.class);
        registerC2S(ConnectionState.PLAY, 0x15, new PacketFactory() {
            public Packet create() { return new NettyClientSettingsPacket(); }
        }, NettyClientSettingsPacket.class);
        registerC2S(ConnectionState.PLAY, 0x16, new PacketFactory() {
            public Packet create() { return new ClientCommandPacket(); }
        }, ClientCommandPacket.class);
        registerC2S(ConnectionState.PLAY, 0x17, new PacketFactory() {
            public Packet create() { return new NettyPluginMessagePacket(); }
        }, NettyPluginMessagePacket.class);

        // === V47 (1.8) LOGIN state overrides ===
        registerV47C2S(ConnectionState.LOGIN, 0x01, new PacketFactory() {
            public Packet create() { return new NettyEncryptionResponsePacketV47(); }
        });

        // === V47 (1.8) PLAY state C2S overrides ===
        // Only packets with changed wire formats — same packet IDs as v4/v5.
        registerV47C2S(ConnectionState.PLAY, 0x00, new PacketFactory() {
            public Packet create() { return new KeepAlivePacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x02, new PacketFactory() {
            public Packet create() { return new NettyUseEntityPacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x04, new PacketFactory() {
            public Packet create() { return new PlayerPositionPacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x06, new PacketFactory() {
            public Packet create() { return new PlayerPositionAndLookC2SPacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x07, new PacketFactory() {
            public Packet create() { return new PlayerDiggingPacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x08, new PacketFactory() {
            public Packet create() { return new NettyBlockPlacementPacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x0A, new PacketFactory() {
            public Packet create() { return new AnimationPacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x0B, new PacketFactory() {
            public Packet create() { return new NettyEntityActionPacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x0C, new PacketFactory() {
            public Packet create() { return new NettySteerVehiclePacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x0E, new PacketFactory() {
            public Packet create() { return new NettyWindowClickPacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x10, new PacketFactory() {
            public Packet create() { return new NettyCreativeSlotPacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x12, new PacketFactory() {
            public Packet create() { return new NettyUpdateSignPacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x14, new PacketFactory() {
            public Packet create() { return new NettyTabCompletePacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x15, new PacketFactory() {
            public Packet create() { return new NettyClientSettingsPacketV47(); }
        });
        registerV47C2S(ConnectionState.PLAY, 0x17, new PacketFactory() {
            public Packet create() { return new NettyPluginMessagePacketV47(); }
        });

        // === V47 S2C reverse map entries (for encoder lookup) ===
        registerS2CReverse(KeepAlivePacketV47.class, 0x00);
        registerS2CReverse(JoinGamePacketV47.class, 0x01);
        registerS2CReverse(NettyChatS2CPacketV47.class, 0x02);
        registerS2CReverse(SpawnPositionPacketV47.class, 0x05);
        registerS2CReverse(NettyPlayerPositionS2CPacketV47.class, 0x08);
        registerS2CReverse(NettySpawnPlayerPacketV47.class, 0x0C);
        registerS2CReverse(NettyDestroyEntitiesPacketV47.class, 0x13);
        registerS2CReverse(EntityRelativeMovePacketV47.class, 0x15);
        registerS2CReverse(EntityLookPacketV47.class, 0x16);
        registerS2CReverse(EntityLookAndMovePacketV47.class, 0x17);
        registerS2CReverse(EntityTeleportPacketV47.class, 0x18);
        registerS2CReverse(NettyEntityPropertiesPacketV47.class, 0x20);
        registerS2CReverse(MapChunkPacketV47.class, 0x21);
        registerS2CReverse(NettyBlockChangePacketV47.class, 0x23);
        registerS2CReverse(NettySetSlotPacketV47.class, 0x2F);
        registerS2CReverse(NettyWindowItemsPacketV47.class, 0x30);
        registerS2CReverse(NettyPlayerListItemPacketV47.class, 0x38);
        // V47 LOGIN S2C reverse entry
        registerS2CLoginReverse(NettyEncryptionRequestPacketV47.class, 0x01);
    }

    private static void registerC2S(ConnectionState state, int packetId,
                                     PacketFactory factory, Class<? extends Packet> clazz) {
        REGISTRY.put(key(state, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
        REVERSE.put(reverseKey(state, PacketDirection.CLIENT_TO_SERVER, clazz), packetId);
    }

    private static void registerS2C(ConnectionState state, int packetId,
                                     PacketFactory factory, Class<? extends Packet> clazz) {
        REGISTRY.put(key(state, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
        REVERSE.put(reverseKey(state, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV47C2S(ConnectionState state, int packetId, PacketFactory factory) {
        REGISTRY_V47.put(key(state, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
    }

    private static void registerS2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerS2CLoginReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE.put(reverseKey(ConnectionState.LOGIN, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    public static Packet createPacket(ConnectionState state, PacketDirection direction, int packetId) {
        PacketFactory factory = REGISTRY.get(key(state, direction, packetId));
        return factory != null ? factory.create() : null;
    }

    /**
     * Version-aware packet creation. For v47+ clients, checks V47 overlay first
     * for C2S packets with changed wire formats, then falls back to the base registry.
     */
    public static Packet createPacket(ConnectionState state, PacketDirection direction,
                                       int packetId, int protocolVersion) {
        if (protocolVersion >= 47) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V47.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        return createPacket(state, direction, packetId);
    }

    public static int getPacketId(ConnectionState state, PacketDirection direction,
                                   Class<? extends Packet> clazz) {
        Integer id = REVERSE.get(reverseKey(state, direction, clazz));
        if (id == null) {
            throw new IllegalArgumentException("No packet ID for " + clazz.getSimpleName()
                    + " in " + state + " " + direction);
        }
        return id;
    }

    private static String key(ConnectionState state, PacketDirection direction, int packetId) {
        return state.name() + ":" + direction.name() + ":" + packetId;
    }

    private static String reverseKey(ConnectionState state, PacketDirection direction,
                                      Class<? extends Packet> clazz) {
        return state.name() + ":" + direction.name() + ":" + clazz.getName();
    }
}
