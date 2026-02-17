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

    /** V109 overlay: C2S packets with remapped IDs for 1.9+. Checked first for v107+ clients. */
    private static final Map<String, PacketFactory> REGISTRY_V109 = new HashMap<String, PacketFactory>();

    /** V315 overlay: C2S packets with changed wire formats for 1.11+. */
    private static final Map<String, PacketFactory> REGISTRY_V315 = new HashMap<String, PacketFactory>();

    /** V335 overlay: C2S packets with remapped IDs for 1.12 (complete — all IDs reshuffled). */
    private static final Map<String, PacketFactory> REGISTRY_V335 = new HashMap<String, PacketFactory>();

    /** V338 overlay: C2S packets that differ between 1.12 and 1.12.1+ (IDs 0x01-0x10). */
    private static final Map<String, PacketFactory> REGISTRY_V338 = new HashMap<String, PacketFactory>();

    /** V340 overlay: C2S KeepAlive changed from VarInt to Long in 1.12.2. */
    private static final Map<String, PacketFactory> REGISTRY_V340 = new HashMap<String, PacketFactory>();

    /** V109 S2C reverse map: packet class -> v109 packet ID. Checked first for v107+ encoder. */
    private static final Map<String, Integer> REVERSE_V109 = new HashMap<String, Integer>();

    /** V110 S2C reverse map overlay: packets shifted by UPDATE_SIGN removal at 0x46 in v110. */
    private static final Map<String, Integer> REVERSE_V110 = new HashMap<String, Integer>();

    /** V335 S2C reverse map overlay: 3 new S2C packets shift entity/spawn IDs in 1.12. */
    private static final Map<String, Integer> REVERSE_V335 = new HashMap<String, Integer>();

    /** V338 S2C reverse map overlay: PlaceGhostRecipe at 0x2B shifts later packets in 1.12.1+. */
    private static final Map<String, Integer> REVERSE_V338 = new HashMap<String, Integer>();

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

        // === V47 (1.8) LOGIN state S2C overrides ===
        registerV47S2C(ConnectionState.LOGIN, 0x01, new PacketFactory() {
            public Packet create() { return new NettyEncryptionRequestPacketV47(); }
        });

        // === V47 (1.8) PLAY state S2C overrides ===
        registerV47S2C(ConnectionState.PLAY, 0x00, new PacketFactory() {
            public Packet create() { return new KeepAlivePacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x01, new PacketFactory() {
            public Packet create() { return new JoinGamePacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x02, new PacketFactory() {
            public Packet create() { return new NettyChatS2CPacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x05, new PacketFactory() {
            public Packet create() { return new SpawnPositionPacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x08, new PacketFactory() {
            public Packet create() { return new NettyPlayerPositionS2CPacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x0C, new PacketFactory() {
            public Packet create() { return new NettySpawnPlayerPacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x13, new PacketFactory() {
            public Packet create() { return new NettyDestroyEntitiesPacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x15, new PacketFactory() {
            public Packet create() { return new EntityRelativeMovePacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x16, new PacketFactory() {
            public Packet create() { return new EntityLookPacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x17, new PacketFactory() {
            public Packet create() { return new EntityLookAndMovePacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x18, new PacketFactory() {
            public Packet create() { return new EntityTeleportPacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x20, new PacketFactory() {
            public Packet create() { return new NettyEntityPropertiesPacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x21, new PacketFactory() {
            public Packet create() { return new MapChunkPacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x23, new PacketFactory() {
            public Packet create() { return new NettyBlockChangePacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x2F, new PacketFactory() {
            public Packet create() { return new NettySetSlotPacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x30, new PacketFactory() {
            public Packet create() { return new NettyWindowItemsPacketV47(); }
        });
        registerV47S2C(ConnectionState.PLAY, 0x38, new PacketFactory() {
            public Packet create() { return new NettyPlayerListItemPacketV47(); }
        });

        // === V47 (1.8) LOGIN state C2S overrides ===
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

        // === V47 C2S reverse map entries (for bot encoder lookup) ===
        REVERSE.put(reverseKey(ConnectionState.LOGIN, PacketDirection.CLIENT_TO_SERVER,
                NettyEncryptionResponsePacketV47.class), 0x01);
        registerC2SReverse(KeepAlivePacketV47.class, 0x00);
        registerC2SReverse(NettyBlockPlacementPacketV47.class, 0x08);
        registerC2SReverse(PlayerDiggingPacketV47.class, 0x07);

        // === V109 (1.9) PLAY state C2S overrides (ALL IDs remapped) ===
        registerV109C2S(0x00, new PacketFactory() {
            public Packet create() { return new TeleportConfirmPacketV109(); }
        });
        registerV109C2S(0x01, new PacketFactory() {
            public Packet create() { return new NettyTabCompletePacketV109(); }
        });
        registerV109C2S(0x02, new PacketFactory() {
            public Packet create() { return new NettyChatC2SPacket(); }
        });
        registerV109C2S(0x03, new PacketFactory() {
            public Packet create() { return new ClientCommandPacket(); }
        });
        registerV109C2S(0x04, new PacketFactory() {
            public Packet create() { return new NettyClientSettingsPacketV109(); }
        });
        registerV109C2S(0x05, new PacketFactory() {
            public Packet create() { return new ConfirmTransactionPacket(); }
        });
        registerV109C2S(0x06, new PacketFactory() {
            public Packet create() { return new EnchantItemPacket(); }
        });
        registerV109C2S(0x07, new PacketFactory() {
            public Packet create() { return new NettyWindowClickPacketV47(); }
        });
        registerV109C2S(0x08, new PacketFactory() {
            public Packet create() { return new CloseWindowPacket(); }
        });
        registerV109C2S(0x09, new PacketFactory() {
            public Packet create() { return new NettyPluginMessagePacketV47(); }
        });
        registerV109C2S(0x0A, new PacketFactory() {
            public Packet create() { return new NettyUseEntityPacketV47(); }
        });
        registerV109C2S(0x0B, new PacketFactory() {
            public Packet create() { return new KeepAlivePacketV47(); }
        });
        registerV109C2S(0x0C, new PacketFactory() {
            public Packet create() { return new PlayerPositionPacketV47(); }
        });
        registerV109C2S(0x0D, new PacketFactory() {
            public Packet create() { return new PlayerPositionAndLookC2SPacketV47(); }
        });
        registerV109C2S(0x0E, new PacketFactory() {
            public Packet create() { return new PlayerLookPacket(); }
        });
        registerV109C2S(0x0F, new PacketFactory() {
            public Packet create() { return new PlayerOnGroundPacket(); }
        });
        registerV109C2S(0x12, new PacketFactory() {
            public Packet create() { return new PlayerAbilitiesPacketV73(); }
        });
        registerV109C2S(0x13, new PacketFactory() {
            public Packet create() { return new PlayerDiggingPacketV47(); }
        });
        registerV109C2S(0x14, new PacketFactory() {
            public Packet create() { return new NettyEntityActionPacketV47(); }
        });
        registerV109C2S(0x15, new PacketFactory() {
            public Packet create() { return new NettySteerVehiclePacketV47(); }
        });
        registerV109C2S(0x17, new PacketFactory() {
            public Packet create() { return new HoldingChangePacketBeta(); }
        });
        registerV109C2S(0x18, new PacketFactory() {
            public Packet create() { return new NettyCreativeSlotPacketV47(); }
        });
        registerV109C2S(0x19, new PacketFactory() {
            public Packet create() { return new NettyUpdateSignPacketV47(); }
        });
        registerV109C2S(0x1A, new PacketFactory() {
            public Packet create() { return new AnimationPacketV109(); }
        });
        registerV109C2S(0x1C, new PacketFactory() {
            public Packet create() { return new NettyBlockPlacementPacketV109(); }
        });
        registerV109C2S(0x1D, new PacketFactory() {
            public Packet create() { return new UseItemPacketV109(); }
        });

        // === V109 S2C reverse map entries (encoder lookup for v107+ clients) ===
        // V109-specific packet classes (new wire formats)
        registerV109S2CReverse(NettySpawnPlayerPacketV109.class, 0x05);
        registerV109S2CReverse(EntityRelativeMovePacketV109.class, 0x25);
        registerV109S2CReverse(EntityLookAndMovePacketV109.class, 0x26);
        registerV109S2CReverse(NettyPlayerPositionS2CPacketV109.class, 0x2E);
        registerV109S2CReverse(MapChunkPacketV109.class, 0x20);
        registerV109S2CReverse(UnloadChunkPacketV109.class, 0x1D);
        registerV109S2CReverse(EntityTeleportPacketV109.class, 0x4A);
        // Reused V47 packet classes with remapped IDs
        registerV109S2CReverse(NettyBlockChangePacketV47.class, 0x0B);
        registerV109S2CReverse(NettyChatS2CPacketV47.class, 0x0F);
        registerV109S2CReverse(NettyWindowItemsPacketV47.class, 0x14);
        registerV109S2CReverse(NettySetSlotPacketV47.class, 0x16);
        registerV109S2CReverse(NettyDisconnectPacket.class, 0x1A);
        registerV109S2CReverse(KeepAlivePacketV47.class, 0x1F);
        registerV109S2CReverse(JoinGamePacketV47.class, 0x23);
        registerV109S2CReverse(JoinGamePacketV108.class, 0x23);
        registerV109S2CReverse(EntityLookPacketV47.class, 0x27);
        registerV109S2CReverse(PlayerAbilitiesPacketV73.class, 0x2B);
        registerV109S2CReverse(NettyPlayerListItemPacketV47.class, 0x2D);
        registerV109S2CReverse(NettyDestroyEntitiesPacketV47.class, 0x30);
        registerV109S2CReverse(SpawnPositionPacketV47.class, 0x43);
        registerV109S2CReverse(NettyEntityPropertiesPacketV47.class, 0x4B);

        // === V110 (1.9.4) S2C reverse map overlay ===
        // V110 removed UPDATE_SIGN at 0x46, shifting all packets >= 0x46 down by 1.
        // Only 2 of our S2C packets are >= 0x46:
        registerV110S2CReverse(EntityTeleportPacketV109.class, 0x49);  // was 0x4A
        registerV110S2CReverse(NettyEntityPropertiesPacketV47.class, 0x4A);  // was 0x4B

        // === V315 (1.11) C2S overlay ===
        // Block Placement cursor fields changed from unsigned bytes to floats.
        registerV315C2S(0x1C, new PacketFactory() {
            public Packet create() { return new NettyBlockPlacementPacketV315(); }
        });

        // === V335 (1.12) PLAY state C2S (complete — ALL IDs reshuffled) ===
        final PacketFactory noOpFactory = new PacketFactory() {
            public Packet create() { return new NoOpPacket(); }
        };
        registerV335C2S(0x00, new PacketFactory() { public Packet create() { return new TeleportConfirmPacketV109(); } });
        registerV335C2S(0x01, noOpFactory); // CraftingRecipePlacement (new)
        registerV335C2S(0x02, new PacketFactory() { public Packet create() { return new NettyTabCompletePacketV109(); } });
        registerV335C2S(0x03, new PacketFactory() { public Packet create() { return new NettyChatC2SPacket(); } });
        registerV335C2S(0x04, new PacketFactory() { public Packet create() { return new ClientCommandPacket(); } });
        registerV335C2S(0x05, new PacketFactory() { public Packet create() { return new NettyClientSettingsPacketV109(); } });
        registerV335C2S(0x06, new PacketFactory() { public Packet create() { return new ConfirmTransactionPacket(); } });
        registerV335C2S(0x07, new PacketFactory() { public Packet create() { return new EnchantItemPacket(); } });
        registerV335C2S(0x08, new PacketFactory() { public Packet create() { return new NettyWindowClickPacketV47(); } });
        registerV335C2S(0x09, new PacketFactory() { public Packet create() { return new CloseWindowPacket(); } });
        registerV335C2S(0x0A, new PacketFactory() { public Packet create() { return new NettyPluginMessagePacketV47(); } });
        registerV335C2S(0x0B, new PacketFactory() { public Packet create() { return new NettyUseEntityPacketV47(); } });
        registerV335C2S(0x0C, new PacketFactory() { public Packet create() { return new KeepAlivePacketV47(); } });
        registerV335C2S(0x0D, new PacketFactory() { public Packet create() { return new PlayerOnGroundPacket(); } });
        registerV335C2S(0x0E, new PacketFactory() { public Packet create() { return new PlayerPositionPacketV47(); } });
        registerV335C2S(0x0F, new PacketFactory() { public Packet create() { return new PlayerPositionAndLookC2SPacketV47(); } });
        registerV335C2S(0x10, new PacketFactory() { public Packet create() { return new PlayerLookPacket(); } });
        registerV335C2S(0x11, noOpFactory); // VehicleMove
        registerV335C2S(0x12, noOpFactory); // PaddleBoat
        registerV335C2S(0x13, new PacketFactory() { public Packet create() { return new PlayerAbilitiesPacketV73(); } });
        registerV335C2S(0x14, new PacketFactory() { public Packet create() { return new PlayerDiggingPacketV47(); } });
        registerV335C2S(0x15, new PacketFactory() { public Packet create() { return new NettyEntityActionPacketV47(); } });
        registerV335C2S(0x16, new PacketFactory() { public Packet create() { return new NettySteerVehiclePacketV47(); } });
        registerV335C2S(0x17, noOpFactory); // RecipeBookUpdate (new)
        registerV335C2S(0x18, noOpFactory); // ResourcePack
        registerV335C2S(0x19, noOpFactory); // SeenAdvancements (new)
        registerV335C2S(0x1A, new PacketFactory() { public Packet create() { return new HoldingChangePacketBeta(); } });
        registerV335C2S(0x1B, new PacketFactory() { public Packet create() { return new NettyCreativeSlotPacketV47(); } });
        registerV335C2S(0x1C, new PacketFactory() { public Packet create() { return new NettyUpdateSignPacketV47(); } });
        registerV335C2S(0x1D, new PacketFactory() { public Packet create() { return new AnimationPacketV109(); } });
        registerV335C2S(0x1E, noOpFactory); // Spectate
        registerV335C2S(0x1F, new PacketFactory() { public Packet create() { return new NettyBlockPlacementPacketV315(); } });
        registerV335C2S(0x20, new PacketFactory() { public Packet create() { return new UseItemPacketV109(); } });

        // === V338 (1.12.1) C2S overlay (IDs 0x01-0x10 differ from V335) ===
        // CraftingRecipePlacement removed; IDs shift back toward V109 values.
        registerV338C2S(0x01, new PacketFactory() { public Packet create() { return new NettyTabCompletePacketV109(); } });
        registerV338C2S(0x02, new PacketFactory() { public Packet create() { return new NettyChatC2SPacket(); } });
        registerV338C2S(0x03, new PacketFactory() { public Packet create() { return new ClientCommandPacket(); } });
        registerV338C2S(0x04, new PacketFactory() { public Packet create() { return new NettyClientSettingsPacketV109(); } });
        registerV338C2S(0x05, new PacketFactory() { public Packet create() { return new ConfirmTransactionPacket(); } });
        registerV338C2S(0x06, new PacketFactory() { public Packet create() { return new EnchantItemPacket(); } });
        registerV338C2S(0x07, new PacketFactory() { public Packet create() { return new NettyWindowClickPacketV47(); } });
        registerV338C2S(0x08, new PacketFactory() { public Packet create() { return new CloseWindowPacket(); } });
        registerV338C2S(0x09, new PacketFactory() { public Packet create() { return new NettyPluginMessagePacketV47(); } });
        registerV338C2S(0x0A, new PacketFactory() { public Packet create() { return new NettyUseEntityPacketV47(); } });
        registerV338C2S(0x0B, new PacketFactory() { public Packet create() { return new KeepAlivePacketV47(); } });
        registerV338C2S(0x0C, new PacketFactory() { public Packet create() { return new PlayerOnGroundPacket(); } });
        registerV338C2S(0x0D, new PacketFactory() { public Packet create() { return new PlayerPositionPacketV47(); } });
        registerV338C2S(0x0E, new PacketFactory() { public Packet create() { return new PlayerPositionAndLookC2SPacketV47(); } });
        registerV338C2S(0x0F, new PacketFactory() { public Packet create() { return new PlayerLookPacket(); } });
        registerV338C2S(0x10, noOpFactory); // VehicleMove
        // 0x11+ fall through to V335 (same packets at same IDs)

        // === V340 (1.12.2) C2S overlay — KeepAlive changed from VarInt to Long ===
        registerV340C2S(0x0B, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });

        // === V335 (1.12) S2C reverse map overlay (delta from V110) ===
        // 3 new S2C packets (Recipe 0x30, SelectAdvancementsTab 0x36, UpdateAdvancements 0x4C)
        // shift entity movement +1, DestroyEntities +1, SpawnPosition +2, EntityTeleport +2, EntityProperties +3.
        registerV335S2CReverse(EntityRelativeMovePacketV109.class, 0x26);  // was 0x25
        registerV335S2CReverse(EntityLookAndMovePacketV109.class, 0x27);   // was 0x26
        registerV335S2CReverse(EntityLookPacketV47.class, 0x28);           // was 0x27
        registerV335S2CReverse(NettyDestroyEntitiesPacketV47.class, 0x31); // was 0x30
        registerV335S2CReverse(SpawnPositionPacketV47.class, 0x45);        // was 0x43
        registerV335S2CReverse(EntityTeleportPacketV109.class, 0x4B);      // was 0x49
        registerV335S2CReverse(NettyEntityPropertiesPacketV47.class, 0x4D); // was 0x4A

        // === V338 (1.12.1) S2C reverse map overlay (delta from V335) ===
        // New PlaceGhostRecipe at 0x2B shifts PlayerAbilities and later packets +1.
        registerV338S2CReverse(NettyPlayerPositionS2CPacketV109.class, 0x2F); // was 0x2E
        registerV338S2CReverse(PlayerAbilitiesPacketV73.class, 0x2C);          // was 0x2B
        registerV338S2CReverse(NettyPlayerListItemPacketV47.class, 0x2E);      // was 0x2D
        registerV338S2CReverse(NettyDestroyEntitiesPacketV47.class, 0x32);     // was 0x31
        registerV338S2CReverse(SpawnPositionPacketV47.class, 0x46);            // was 0x45
        registerV338S2CReverse(EntityTeleportPacketV109.class, 0x4C);          // was 0x4B
        registerV338S2CReverse(NettyEntityPropertiesPacketV47.class, 0x4E);    // was 0x4D

        // KeepAlivePacketV340 S2C: same ID 0x1F as V109, but different class.
        registerV109S2CReverse(KeepAlivePacketV340.class, 0x1F);
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

    private static void registerV47S2C(ConnectionState state, int packetId, PacketFactory factory) {
        REGISTRY_V47.put(key(state, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV109C2S(int packetId, PacketFactory factory) {
        REGISTRY_V109.put(key(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
    }

    private static void registerV315C2S(int packetId, PacketFactory factory) {
        REGISTRY_V315.put(key(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
    }

    private static void registerV335C2S(int packetId, PacketFactory factory) {
        REGISTRY_V335.put(key(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
    }

    private static void registerV338C2S(int packetId, PacketFactory factory) {
        REGISTRY_V338.put(key(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
    }

    private static void registerV340C2S(int packetId, PacketFactory factory) {
        REGISTRY_V340.put(key(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
    }

    private static void registerV109S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V109.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV110S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V110.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV335S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V335.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV338S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V338.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerS2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerC2SReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE.put(reverseKey(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, clazz), packetId);
    }

    private static void registerS2CLoginReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE.put(reverseKey(ConnectionState.LOGIN, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    public static Packet createPacket(ConnectionState state, PacketDirection direction, int packetId) {
        PacketFactory factory = REGISTRY.get(key(state, direction, packetId));
        return factory != null ? factory.create() : null;
    }

    /**
     * Version-aware packet creation. Checks version-specific overlays first
     * (V109 for v107+, V47 for v47+), then falls back to the base registry.
     */
    public static Packet createPacket(ConnectionState state, PacketDirection direction,
                                       int packetId, int protocolVersion) {
        if (protocolVersion >= 340) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V340.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        if (protocolVersion >= 338) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V338.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        if (protocolVersion >= 335) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V335.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        if (protocolVersion >= 315) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V315.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        if (protocolVersion >= 107) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V109.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
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

    /**
     * Version-aware packet ID lookup for the encoder.
     * For v110+ clients, checks V110 overlay first (UPDATE_SIGN removed at 0x46).
     * For v107+ clients, checks V109 reverse map (all S2C IDs remapped).
     * Falls back to the base REVERSE map for older versions.
     */
    public static int getPacketId(ConnectionState state, PacketDirection direction,
                                   Class<? extends Packet> clazz, int protocolVersion) {
        if (protocolVersion >= 338) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V338.get(rk);
            if (id != null) {
                return id;
            }
        }
        if (protocolVersion >= 335) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V335.get(rk);
            if (id != null) {
                return id;
            }
        }
        if (protocolVersion >= 110) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V110.get(rk);
            if (id != null) {
                return id;
            }
        }
        if (protocolVersion >= 107) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V109.get(rk);
            if (id != null) {
                return id;
            }
        }
        return getPacketId(state, direction, clazz);
    }

    private static String key(ConnectionState state, PacketDirection direction, int packetId) {
        return state.name() + ":" + direction.name() + ":" + packetId;
    }

    private static String reverseKey(ConnectionState state, PacketDirection direction,
                                      Class<? extends Packet> clazz) {
        return state.name() + ":" + direction.name() + ":" + clazz.getName();
    }
}
