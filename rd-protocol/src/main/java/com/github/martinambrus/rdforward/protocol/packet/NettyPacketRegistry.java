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

    /** Forward map: (state, direction, packetId) -> factory */
    private static final Map<String, PacketFactory> REGISTRY = new HashMap<String, PacketFactory>();

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

    public static Packet createPacket(ConnectionState state, PacketDirection direction, int packetId) {
        PacketFactory factory = REGISTRY.get(key(state, direction, packetId));
        return factory != null ? factory.create() : null;
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
