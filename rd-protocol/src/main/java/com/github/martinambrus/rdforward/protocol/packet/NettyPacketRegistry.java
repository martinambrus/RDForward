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

    /** V108 overlay: S2C packets with changed wire formats in 1.9.1+ (dimension byte→int). */
    private static final Map<String, PacketFactory> REGISTRY_V108 = new HashMap<String, PacketFactory>();

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

    /** V393 overlay: C2S packets with remapped IDs for 1.13 (complete — all IDs reshuffled). */
    private static final Map<String, PacketFactory> REGISTRY_V393 = new HashMap<String, PacketFactory>();

    /** V477 overlay: C2S packets with remapped IDs for 1.14 (complete — all IDs reshuffled). */
    private static final Map<String, PacketFactory> REGISTRY_V477 = new HashMap<String, PacketFactory>();

    /** V109 S2C reverse map: packet class -> v109 packet ID. Checked first for v107+ encoder. */
    private static final Map<String, Integer> REVERSE_V109 = new HashMap<String, Integer>();

    /** V110 S2C reverse map overlay: packets shifted by UPDATE_SIGN removal at 0x46 in v110. */
    private static final Map<String, Integer> REVERSE_V110 = new HashMap<String, Integer>();

    /** V335 S2C reverse map overlay: 3 new S2C packets shift entity/spawn IDs in 1.12. */
    private static final Map<String, Integer> REVERSE_V335 = new HashMap<String, Integer>();

    /** V338 S2C reverse map overlay: PlaceGhostRecipe at 0x2B shifts later packets in 1.12.1+. */
    private static final Map<String, Integer> REVERSE_V338 = new HashMap<String, Integer>();

    /** V393 S2C reverse map overlay: 6 new S2C packets shift all IDs in 1.13. */
    private static final Map<String, Integer> REVERSE_V393 = new HashMap<String, Integer>();

    /** V477 S2C reverse map overlay: all S2C packet IDs reshuffled in 1.14. */
    private static final Map<String, Integer> REVERSE_V477 = new HashMap<String, Integer>();

    /** V573 overlay: S2C packets for 1.15 (new AcknowledgePlayerDigging at 0x08 shifts all S2C >= 0x08 by +1). */
    private static final Map<String, PacketFactory> REGISTRY_V573 = new HashMap<String, PacketFactory>();

    /** V573 S2C reverse map overlay: shifted S2C packet IDs for 1.15. */
    private static final Map<String, Integer> REVERSE_V573 = new HashMap<String, Integer>();

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
        registerS2C(ConnectionState.PLAY, 0x03, new PacketFactory() {
            public Packet create() { return new NettyTimeUpdatePacket(); }
        }, NettyTimeUpdatePacket.class);
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
        registerS2C(ConnectionState.PLAY, 0x2B, new PacketFactory() {
            public Packet create() { return new NettyChangeGameStatePacket(); }
        }, NettyChangeGameStatePacket.class);
        registerS2C(ConnectionState.PLAY, 0x2F, new PacketFactory() {
            public Packet create() { return new NettySetSlotPacket(); }
        }, NettySetSlotPacket.class);
        registerS2C(ConnectionState.PLAY, 0x32, new PacketFactory() {
            public Packet create() { return new ConfirmTransactionPacket(); }
        }, ConfirmTransactionPacket.class);
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
        registerC2SReverse(NettyWindowClickPacketV47.class, 0x0E);
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
        registerV109S2CReverse(NettyTimeUpdatePacket.class, 0x44);
        registerV109S2CReverse(NettyChangeGameStatePacket.class, 0x1E);

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

        // === V393 (1.13) PLAY state C2S (complete — ALL IDs reshuffled) ===
        registerV393C2S(0x00, new PacketFactory() { public Packet create() { return new TeleportConfirmPacketV109(); } });
        registerV393C2S(0x01, noOpFactory); // BlockEntityTagQuery (new)
        registerV393C2S(0x02, new PacketFactory() { public Packet create() { return new NettyChatC2SPacket(); } });
        registerV393C2S(0x03, new PacketFactory() { public Packet create() { return new ClientCommandPacket(); } });
        registerV393C2S(0x04, new PacketFactory() { public Packet create() { return new NettyClientSettingsPacketV109(); } });
        registerV393C2S(0x05, noOpFactory); // CommandSuggestion (format changed)
        registerV393C2S(0x06, new PacketFactory() { public Packet create() { return new ConfirmTransactionPacket(); } });
        registerV393C2S(0x07, new PacketFactory() { public Packet create() { return new EnchantItemPacket(); } });
        registerV393C2S(0x08, noOpFactory); // WindowClick (slot format changed)
        registerV393C2S(0x09, new PacketFactory() { public Packet create() { return new CloseWindowPacket(); } });
        registerV393C2S(0x0A, new PacketFactory() { public Packet create() { return new NettyPluginMessagePacketV47(); } });
        registerV393C2S(0x0B, noOpFactory); // EditBook (new)
        registerV393C2S(0x0C, noOpFactory); // EntityTagQuery (new)
        registerV393C2S(0x0D, new PacketFactory() { public Packet create() { return new NettyUseEntityPacketV47(); } });
        registerV393C2S(0x0E, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });
        registerV393C2S(0x0F, new PacketFactory() { public Packet create() { return new PlayerOnGroundPacket(); } });
        registerV393C2S(0x10, new PacketFactory() { public Packet create() { return new PlayerPositionPacketV47(); } });
        registerV393C2S(0x11, new PacketFactory() { public Packet create() { return new PlayerPositionAndLookC2SPacketV47(); } });
        registerV393C2S(0x12, new PacketFactory() { public Packet create() { return new PlayerLookPacket(); } });
        registerV393C2S(0x13, noOpFactory); // VehicleMove
        registerV393C2S(0x14, noOpFactory); // PaddleBoat
        registerV393C2S(0x15, noOpFactory); // PickItem (new)
        registerV393C2S(0x16, noOpFactory); // PlaceRecipe
        registerV393C2S(0x17, new PacketFactory() { public Packet create() { return new PlayerAbilitiesPacketV73(); } });
        registerV393C2S(0x18, new PacketFactory() { public Packet create() { return new PlayerDiggingPacketV47(); } });
        registerV393C2S(0x19, new PacketFactory() { public Packet create() { return new NettyEntityActionPacketV47(); } });
        registerV393C2S(0x1A, new PacketFactory() { public Packet create() { return new NettySteerVehiclePacketV47(); } });
        registerV393C2S(0x1B, noOpFactory); // RecipeBookUpdate
        registerV393C2S(0x1C, noOpFactory); // RenameItem (new)
        registerV393C2S(0x1D, noOpFactory); // ResourcePack
        registerV393C2S(0x1E, noOpFactory); // SeenAdvancements
        registerV393C2S(0x1F, noOpFactory); // SelectTrade (new)
        registerV393C2S(0x20, noOpFactory); // SetBeacon (new)
        registerV393C2S(0x21, new PacketFactory() { public Packet create() { return new HoldingChangePacketBeta(); } });
        registerV393C2S(0x22, noOpFactory); // SetCommandBlock (new)
        registerV393C2S(0x23, noOpFactory); // SetCommandMinecart (new)
        registerV393C2S(0x24, noOpFactory); // CreativeSlot (slot format changed)
        registerV393C2S(0x25, noOpFactory); // SetStructureBlock (new)
        registerV393C2S(0x26, new PacketFactory() { public Packet create() { return new NettyUpdateSignPacketV47(); } });
        registerV393C2S(0x27, new PacketFactory() { public Packet create() { return new AnimationPacketV109(); } });
        registerV393C2S(0x28, noOpFactory); // Spectate
        registerV393C2S(0x29, new PacketFactory() { public Packet create() { return new NettyBlockPlacementPacketV315(); } });
        registerV393C2S(0x2A, new PacketFactory() { public Packet create() { return new UseItemPacketV109(); } });

        // === ConfirmTransaction S2C reverse map entries ===
        // Base/V47: 0x32 (already registered above via registerS2C)
        registerV109S2CReverse(ConfirmTransactionPacket.class, 0x11);
        registerV393S2CReverse(ConfirmTransactionPacket.class, 0x12);

        // === V109 (1.9) PLAY state S2C forward entries (for bot decoder, v107+) ===
        // Active packet decoders at correct V109 IDs:
        registerV109S2C(0x05, new PacketFactory() { public Packet create() { return new NettySpawnPlayerPacketV109(); } });
        registerV109S2C(0x0B, new PacketFactory() { public Packet create() { return new NettyBlockChangePacketV47(); } });
        registerV109S2C(0x0F, new PacketFactory() { public Packet create() { return new NettyChatS2CPacketV47(); } });
        registerV109S2C(0x11, new PacketFactory() { public Packet create() { return new ConfirmTransactionPacket(); } });
        registerV109S2C(0x16, new PacketFactory() { public Packet create() { return new NettySetSlotPacketV47(); } });
        registerV109S2C(0x1A, new PacketFactory() { public Packet create() { return new NettyDisconnectPacket(); } });
        registerV109S2C(0x1D, new PacketFactory() { public Packet create() { return new UnloadChunkPacketV109(); } });
        registerV109S2C(0x1E, new PacketFactory() { public Packet create() { return new NettyChangeGameStatePacket(); } });
        registerV109S2C(0x1F, new PacketFactory() { public Packet create() { return new KeepAlivePacketV47(); } });
        registerV109S2C(0x20, new PacketFactory() { public Packet create() { return new MapChunkPacketV109(); } });
        registerV109S2C(0x23, new PacketFactory() { public Packet create() { return new JoinGamePacketV47(); } });
        registerV109S2C(0x2B, new PacketFactory() { public Packet create() { return new PlayerAbilitiesPacketV73(); } });
        registerV109S2C(0x2E, new PacketFactory() { public Packet create() { return new NettyPlayerPositionS2CPacketV109(); } });
        registerV109S2C(0x30, new PacketFactory() { public Packet create() { return new NettyDestroyEntitiesPacketV47(); } });
        registerV109S2C(0x43, new PacketFactory() { public Packet create() { return new SpawnPositionPacketV47(); } });
        registerV109S2C(0x44, new PacketFactory() { public Packet create() { return new NettyTimeUpdatePacket(); } });
        // NoOp shadows: block stale V47/base S2C entries whose IDs have different
        // meanings in 1.9+. Without these, a v107+ bot decoder falls through to V47
        // and decodes the wrong packet type (e.g., SpawnObject decoded as KeepAlive).
        registerV109S2C(0x00, noOpFactory);  // V47: KeepAlive; V109: SpawnObject
        registerV109S2C(0x01, noOpFactory);  // V47: JoinGame; V109: SpawnExperienceOrb
        registerV109S2C(0x02, noOpFactory);  // V47: Chat; V109: SpawnGlobalEntity
        registerV109S2C(0x03, noOpFactory);  // base: TimeUpdate; V109: SpawnMob
        registerV109S2C(0x08, noOpFactory);  // V47: PlayerPosition; V109: BlockBreakAnimation
        registerV109S2C(0x0C, noOpFactory);  // V47: SpawnPlayer; V109: BossBar
        registerV109S2C(0x13, noOpFactory);  // V47: DestroyEntities; V109: OpenWindow
        registerV109S2C(0x15, noOpFactory);  // V47: EntityRelativeMove; V109: WindowProperty
        registerV109S2C(0x17, noOpFactory);  // V47: EntityLookAndMove; V109: SetCooldown
        registerV109S2C(0x18, noOpFactory);  // V47: EntityTeleport; V109: PluginMessage
        registerV109S2C(0x21, noOpFactory);  // V47: MapChunk; V109: Effect
        registerV109S2C(0x2F, noOpFactory);  // V47: SetSlot; V109: UseBed
        registerV109S2C(0x32, noOpFactory);  // base: ConfirmTransaction; V109: Team
        registerV109S2C(0x38, noOpFactory);  // V47: PlayerListItem; V109: WorldBorder
        registerV109S2C(0x39, noOpFactory);  // base: PlayerAbilities; V109: EntityMetadata
        registerV109S2C(0x40, noOpFactory);  // base: Disconnect; V109: SetPassengers

        // === V108 (1.9.1) S2C forward entry — JoinGame dimension byte→int ===
        registerV108S2C(0x23, new PacketFactory() { public Packet create() { return new JoinGamePacketV108(); } });

        // === V335 (1.12) S2C forward entries (block stale V109 IDs + register shifted packets) ===
        // Unlock Recipes inserted at 0x30 (was DestroyEntities in V109).
        // SpawnPosition moved from 0x43 to 0x45. Shadow both with NoOp.
        registerV335S2C(0x30, noOpFactory);  // V109: DestroyEntities; V335: Unlock Recipes
        registerV335S2C(0x43, noOpFactory);  // V109: SpawnPosition; V335: Set Passengers
        // DestroyEntities shifted from 0x30 to 0x31 in V335.
        registerV335S2C(0x31, new PacketFactory() { public Packet create() { return new NettyDestroyEntitiesPacketV47(); } });

        // === V338 (1.12.1) S2C forward entries ===
        // PlaceGhostRecipe inserted at 0x2B (was PlayerAbilities in V109) and
        // PlayerListItem moved to 0x2E (was PlayerPosition in V109). Shadow with NoOp.
        registerV338S2C(0x2B, noOpFactory);  // V109: PlayerAbilities; V338: CraftRecipeResponse
        registerV338S2C(0x2E, noOpFactory);  // V109: PlayerPosition; V338: PlayerListItem
        // PlayerPosition shifted from 0x2E to 0x2F in V338.
        registerV338S2C(0x2F, new PacketFactory() { public Packet create() { return new NettyPlayerPositionS2CPacketV109(); } });
        // DestroyEntities shifted from 0x31 (V335) to 0x32 in V338.
        // Shadow 0x31 to block V335 DestroyEntities (V338 0x31 = Unlock Recipes).
        registerV338S2C(0x31, noOpFactory);
        registerV338S2C(0x32, new PacketFactory() { public Packet create() { return new NettyDestroyEntitiesPacketV47(); } });

        // === V340 (1.12.2) S2C forward entry — KeepAlive changed to Long ===
        registerV340S2C(0x1F, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });

        // === V393 (1.13) S2C forward entries (for bot decoder) ===
        // Shadow stale V338/V340 IDs that moved in V393 to prevent fallthrough.
        registerV393S2C(0x2F, noOpFactory);  // V338: PlayerPosition; V393: Use Bed
        registerV393S2C(0x1F, noOpFactory);  // V340: KeepAlive; V393: Unload Chunk
        // Shadow stale V109/V108 entries whose IDs shifted in 1.13.
        registerV393S2C(0x0F, noOpFactory);  // V109: Chat; V393: Tab-Complete
        registerV393S2C(0x16, noOpFactory);  // V109: SetSlot; V393: Set Cooldown
        registerV393S2C(0x1A, noOpFactory);  // V109: Disconnect; V393: Entity Status
        registerV393S2C(0x1D, new PacketFactory() { public Packet create() { return new NettyChangeGameStatePacket(); } });  // V109: UnloadChunk; V393: Change Game State
        registerV393S2C(0x1E, noOpFactory);  // V109: ChangeGameState; V393: Open Horse Window
        registerV393S2C(0x20, noOpFactory);  // V109: MapChunk; V393: Effect
        registerV393S2C(0x23, noOpFactory);  // V108: JoinGame; V393: Map
        // Register active V393 S2C forward entries for bot decoder
        registerV393S2C(0x12, new PacketFactory() { public Packet create() { return new ConfirmTransactionPacket(); } });
        registerV393S2C(0x0B, new PacketFactory() { public Packet create() { return new NettyBlockChangePacketV393(); } });
        registerV393S2C(0x0E, new PacketFactory() { public Packet create() { return new NettyChatS2CPacketV47(); } });
        registerV393S2C(0x17, new PacketFactory() { public Packet create() { return new NettySetSlotPacketV393(); } });
        registerV393S2C(0x1B, new PacketFactory() { public Packet create() { return new NettyDisconnectPacket(); } });
        registerV393S2C(0x21, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });
        registerV393S2C(0x22, new PacketFactory() { public Packet create() { return new MapChunkPacketV109(); } });
        registerV393S2C(0x25, new PacketFactory() { public Packet create() { return new JoinGamePacketV108(); } });
        registerV393S2C(0x32, new PacketFactory() { public Packet create() { return new NettyPlayerPositionS2CPacketV109(); } });
        // DestroyEntities shifted to 0x35 in V393.
        registerV393S2C(0x35, new PacketFactory() { public Packet create() { return new NettyDestroyEntitiesPacketV47(); } });
        registerV393S2C(0x44, noOpFactory);  // V109: TimeUpdate; V393: Merchant Offers
        registerV393S2C(0x4A, new PacketFactory() { public Packet create() { return new NettyTimeUpdatePacket(); } });

        // === V393 (1.13) S2C reverse map overlay (delta from V338) ===
        registerV393S2CReverse(NettyChatS2CPacketV47.class, 0x0E);
        registerV393S2CReverse(NettyWindowItemsPacketV47.class, 0x15);
        registerV393S2CReverse(NettySetSlotPacketV393.class, 0x17);
        registerV393S2CReverse(NettyDisconnectPacket.class, 0x1B);
        registerV393S2CReverse(UnloadChunkPacketV109.class, 0x1F);
        registerV393S2CReverse(KeepAlivePacketV340.class, 0x21);
        registerV393S2CReverse(MapChunkPacketV109.class, 0x22);
        registerV393S2CReverse(JoinGamePacketV108.class, 0x25);
        registerV393S2CReverse(EntityRelativeMovePacketV109.class, 0x28);
        registerV393S2CReverse(EntityLookAndMovePacketV109.class, 0x29);
        registerV393S2CReverse(EntityLookPacketV47.class, 0x2A);
        registerV393S2CReverse(PlayerAbilitiesPacketV73.class, 0x2E);
        registerV393S2CReverse(NettyPlayerListItemPacketV47.class, 0x30);
        registerV393S2CReverse(NettyPlayerPositionS2CPacketV109.class, 0x32);
        registerV393S2CReverse(NettyDestroyEntitiesPacketV47.class, 0x35);
        registerV393S2CReverse(SpawnPositionPacketV47.class, 0x49);
        registerV393S2CReverse(EntityTeleportPacketV109.class, 0x50);
        registerV393S2CReverse(NettyEntityPropertiesPacketV47.class, 0x52);
        // V393-specific packet classes
        registerV393S2CReverse(NettyBlockChangePacketV393.class, 0x0B);
        // New mandatory S2C packets
        registerV393S2CReverse(DeclareCommandsPacketV393.class, 0x11);
        registerV393S2CReverse(UpdateRecipesPacketV393.class, 0x54);
        registerV393S2CReverse(UpdateTagsPacketV393.class, 0x55);
        // SpawnPlayer 0x05 is UNCHANGED from V338
        registerV393S2CReverse(NettySpawnPlayerPacketV109.class, 0x05);
        registerV393S2CReverse(NettyPluginMessageS2CPacketV393.class, 0x19);
        registerV393S2CReverse(NettyTimeUpdatePacket.class, 0x4A);
        registerV393S2CReverse(NettyChangeGameStatePacket.class, 0x1D);
        // V404 (1.13.2) SetSlot uses boolean+VarInt slot format, same packet ID
        registerV393S2CReverse(NettySetSlotPacketV404.class, 0x17);

        // === V393 (1.13) C2S reverse map entries (for bot encoder) ===
        registerV393C2SReverse(NettyChatC2SPacket.class, 0x02);
        registerV393C2SReverse(KeepAlivePacketV340.class, 0x0E);
        registerV393C2SReverse(PlayerDiggingPacketV47.class, 0x18);
        registerV393C2SReverse(NettyBlockPlacementPacketV315.class, 0x29);

        // === V109 C2S reverse map entries (for bot encoder lookup, v107+ clients) ===
        registerV109C2SReverse(TeleportConfirmPacketV109.class, 0x00);
        registerV109C2SReverse(NettyChatC2SPacket.class, 0x02);
        registerV109C2SReverse(KeepAlivePacketV47.class, 0x0B);
        registerV109C2SReverse(PlayerDiggingPacketV47.class, 0x13);
        registerV109C2SReverse(NettyBlockPlacementPacketV109.class, 0x1C);
        registerV109C2SReverse(NettyBlockPlacementPacketV315.class, 0x1C);
        registerV109C2SReverse(KeepAlivePacketV340.class, 0x0B);

        // === V335 (1.12) C2S reverse map entries (all IDs reshuffled) ===
        registerV335C2SReverse(NettyChatC2SPacket.class, 0x03);
        registerV335C2SReverse(KeepAlivePacketV47.class, 0x0C);
        registerV335C2SReverse(PlayerDiggingPacketV47.class, 0x14);
        registerV335C2SReverse(NettyBlockPlacementPacketV315.class, 0x1F);

        // === V338 (1.12.1) C2S reverse map entries (0x01-0x10 shift back) ===
        registerV338C2SReverse(NettyChatC2SPacket.class, 0x02);
        registerV338C2SReverse(KeepAlivePacketV47.class, 0x0B);

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
        registerV335S2CReverse(NettyTimeUpdatePacket.class, 0x46);        // was 0x44

        // === V338 (1.12.1) S2C reverse map overlay (delta from V335) ===
        // New PlaceGhostRecipe at 0x2B shifts PlayerAbilities and later packets +1.
        registerV338S2CReverse(NettyPlayerPositionS2CPacketV109.class, 0x2F); // was 0x2E
        registerV338S2CReverse(PlayerAbilitiesPacketV73.class, 0x2C);          // was 0x2B
        registerV338S2CReverse(NettyPlayerListItemPacketV47.class, 0x2E);      // was 0x2D
        registerV338S2CReverse(NettyDestroyEntitiesPacketV47.class, 0x32);     // was 0x31
        registerV338S2CReverse(SpawnPositionPacketV47.class, 0x46);            // was 0x45
        registerV338S2CReverse(EntityTeleportPacketV109.class, 0x4C);          // was 0x4B
        registerV338S2CReverse(NettyEntityPropertiesPacketV47.class, 0x4E);    // was 0x4D
        registerV338S2CReverse(NettyTimeUpdatePacket.class, 0x47);           // was 0x46

        // KeepAlivePacketV340 S2C: same ID 0x1F as V109, but different class.
        registerV109S2CReverse(KeepAlivePacketV340.class, 0x1F);

        // === V477 (1.14) PLAY state C2S (complete — ALL IDs reshuffled) ===
        registerV477C2S(0x00, new PacketFactory() { public Packet create() { return new TeleportConfirmPacketV109(); } });
        registerV477C2S(0x01, noOpFactory); // BlockEntityTagQuery
        registerV477C2S(0x02, noOpFactory); // SetDifficulty (new)
        registerV477C2S(0x03, new PacketFactory() { public Packet create() { return new NettyChatC2SPacket(); } });
        registerV477C2S(0x04, new PacketFactory() { public Packet create() { return new ClientCommandPacket(); } });
        registerV477C2S(0x05, new PacketFactory() { public Packet create() { return new NettyClientSettingsPacketV109(); } });
        registerV477C2S(0x06, noOpFactory); // CommandSuggestion
        registerV477C2S(0x07, new PacketFactory() { public Packet create() { return new ConfirmTransactionPacket(); } });
        registerV477C2S(0x08, noOpFactory); // ClickWindowButton (new)
        registerV477C2S(0x09, noOpFactory); // WindowClick (slot format changed)
        registerV477C2S(0x0A, new PacketFactory() { public Packet create() { return new CloseWindowPacket(); } });
        registerV477C2S(0x0B, new PacketFactory() { public Packet create() { return new NettyPluginMessagePacketV47(); } });
        registerV477C2S(0x0C, noOpFactory); // EditBook
        registerV477C2S(0x0D, noOpFactory); // EntityTagQuery
        registerV477C2S(0x0E, new PacketFactory() { public Packet create() { return new NettyUseEntityPacketV47(); } });
        registerV477C2S(0x0F, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });
        registerV477C2S(0x10, noOpFactory); // LockDifficulty (new)
        registerV477C2S(0x11, new PacketFactory() { public Packet create() { return new PlayerPositionPacketV47(); } });
        registerV477C2S(0x12, new PacketFactory() { public Packet create() { return new PlayerPositionAndLookC2SPacketV47(); } });
        registerV477C2S(0x13, new PacketFactory() { public Packet create() { return new PlayerLookPacket(); } });
        registerV477C2S(0x14, new PacketFactory() { public Packet create() { return new PlayerOnGroundPacket(); } });
        registerV477C2S(0x15, noOpFactory); // VehicleMove
        registerV477C2S(0x16, noOpFactory); // PaddleBoat
        registerV477C2S(0x17, noOpFactory); // PickItem
        registerV477C2S(0x18, noOpFactory); // PlaceRecipe
        registerV477C2S(0x19, new PacketFactory() { public Packet create() { return new PlayerAbilitiesPacketV73(); } });
        registerV477C2S(0x1A, new PacketFactory() { public Packet create() { return new PlayerDiggingPacketV477(); } });
        registerV477C2S(0x1B, new PacketFactory() { public Packet create() { return new NettyEntityActionPacketV47(); } });
        registerV477C2S(0x1C, new PacketFactory() { public Packet create() { return new NettySteerVehiclePacketV47(); } });
        registerV477C2S(0x1D, noOpFactory); // RecipeBookUpdate
        registerV477C2S(0x1E, noOpFactory); // RenameItem
        registerV477C2S(0x1F, noOpFactory); // ResourcePack
        registerV477C2S(0x20, noOpFactory); // SeenAdvancements
        registerV477C2S(0x21, noOpFactory); // SelectTrade
        registerV477C2S(0x22, noOpFactory); // SetBeacon
        registerV477C2S(0x23, new PacketFactory() { public Packet create() { return new HoldingChangePacketBeta(); } });
        registerV477C2S(0x24, noOpFactory); // SetCommandBlock
        registerV477C2S(0x25, noOpFactory); // SetCommandMinecart
        registerV477C2S(0x26, noOpFactory); // CreativeSlot (slot format changed)
        registerV477C2S(0x27, noOpFactory); // SetJigsawBlock (new)
        registerV477C2S(0x28, noOpFactory); // SetStructureBlock
        registerV477C2S(0x29, new PacketFactory() { public Packet create() { return new NettyUpdateSignPacketV47(); } });
        registerV477C2S(0x2A, new PacketFactory() { public Packet create() { return new AnimationPacketV109(); } });
        registerV477C2S(0x2B, noOpFactory); // Spectate
        registerV477C2S(0x2C, new PacketFactory() { public Packet create() { return new NettyBlockPlacementPacketV477(); } });
        registerV477C2S(0x2D, new PacketFactory() { public Packet create() { return new UseItemPacketV109(); } });

        // === V477 (1.14) S2C reverse map overlay (delta from V393) ===
        registerV477S2CReverse(NettyWindowItemsPacketV47.class, 0x14);
        registerV477S2CReverse(NettySetSlotPacketV393.class, 0x16);
        registerV477S2CReverse(NettySetSlotPacketV404.class, 0x16);
        registerV477S2CReverse(NettyPluginMessageS2CPacketV393.class, 0x18);
        registerV477S2CReverse(NettyDisconnectPacket.class, 0x1A);
        registerV477S2CReverse(UnloadChunkPacketV109.class, 0x1D);
        registerV477S2CReverse(KeepAlivePacketV340.class, 0x20);
        registerV477S2CReverse(MapChunkPacketV477.class, 0x21);
        registerV477S2CReverse(UpdateLightPacketV477.class, 0x24);
        registerV477S2CReverse(JoinGamePacketV477.class, 0x25);
        registerV477S2CReverse(EntityRelativeMovePacketV109.class, 0x28);
        registerV477S2CReverse(EntityLookAndMovePacketV109.class, 0x29);
        registerV477S2CReverse(EntityLookPacketV47.class, 0x2A);
        registerV477S2CReverse(PlayerAbilitiesPacketV73.class, 0x31);
        registerV477S2CReverse(NettyPlayerListItemPacketV47.class, 0x33);
        registerV477S2CReverse(NettyPlayerPositionS2CPacketV109.class, 0x35);
        registerV477S2CReverse(NettyDestroyEntitiesPacketV47.class, 0x37);
        registerV477S2CReverse(SetChunkCacheCenterPacketV477.class, 0x40);
        registerV477S2CReverse(SetChunkCacheRadiusPacketV477.class, 0x41);
        registerV477S2CReverse(SpawnPositionPacketV477.class, 0x4D);
        registerV477S2CReverse(EntityTeleportPacketV109.class, 0x56);
        registerV477S2CReverse(NettyEntityPropertiesPacketV47.class, 0x58);
        registerV477S2CReverse(NettySpawnPlayerPacketV477.class, 0x05);
        registerV477S2CReverse(NettyBlockChangePacketV477.class, 0x0B);
        registerV477S2CReverse(NettyChatS2CPacketV47.class, 0x0E);
        registerV477S2CReverse(DeclareCommandsPacketV393.class, 0x11);
        registerV477S2CReverse(ConfirmTransactionPacket.class, 0x12);
        registerV477S2CReverse(UpdateRecipesPacketV393.class, 0x5A);
        registerV477S2CReverse(UpdateTagsPacketV477.class, 0x5B);
        registerV477S2CReverse(NettyTimeUpdatePacket.class, 0x4E);
        registerV477S2CReverse(NettyChangeGameStatePacket.class, 0x1E);

        // === V477 (1.14) S2C forward entries (for bot decoder) ===
        registerV477S2C(0x15, noOpFactory);  // V393: WindowItems; V477: ContainerSetData
        registerV477S2C(0x17, noOpFactory);  // V393: SetSlot; V477: Cooldown
        registerV477S2C(0x19, noOpFactory);  // V393: PluginMessage; V477: CustomSound
        registerV477S2C(0x1B, noOpFactory);  // V393: Disconnect; V477: EntityEvent
        registerV477S2C(0x1F, noOpFactory);  // V393: UnloadChunk; V477: HorseScreenOpen
        registerV477S2C(0x22, noOpFactory);  // V393: ChunkData; V477: LevelEvent
        registerV477S2C(0x2E, noOpFactory);  // V393: Abilities; V477: OpenScreen
        registerV477S2C(0x30, noOpFactory);  // V393: PlayerInfo; V477: PlaceGhostRecipe
        registerV477S2C(0x32, noOpFactory);  // V393: PlayerPos; V477: PlayerCombat
        registerV477S2C(0x49, noOpFactory);  // V393: SpawnPosition; V477: SetObjective
        registerV477S2C(0x50, noOpFactory);  // V393: EntityTeleport; V477: SoundEntity
        registerV477S2C(0x52, noOpFactory);  // V393: EntityProperties; V477: StopSound
        registerV477S2C(0x54, noOpFactory);  // V393: UpdateRecipes; V477: TagQuery
        registerV477S2C(0x55, noOpFactory);  // V393: UpdateTags; V477: TakeItemEntity
        registerV477S2C(0x14, new PacketFactory() { public Packet create() { return new NettyWindowItemsPacketV47(); } });
        registerV477S2C(0x16, new PacketFactory() { public Packet create() { return new NettySetSlotPacketV393(); } });
        registerV477S2C(0x18, noOpFactory); // PluginMessage (bot doesn't need to decode)
        registerV477S2C(0x1A, new PacketFactory() { public Packet create() { return new NettyDisconnectPacket(); } });
        registerV477S2C(0x1D, new PacketFactory() { public Packet create() { return new UnloadChunkPacketV109(); } });
        registerV477S2C(0x20, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });
        registerV477S2C(0x21, new PacketFactory() { public Packet create() { return new MapChunkPacketV477(); } });
        registerV477S2C(0x25, new PacketFactory() { public Packet create() { return new JoinGamePacketV477(); } });
        registerV477S2C(0x31, new PacketFactory() { public Packet create() { return new PlayerAbilitiesPacketV73(); } });
        registerV477S2C(0x33, new PacketFactory() { public Packet create() { return new NettyPlayerListItemPacketV47(); } });
        registerV477S2C(0x35, new PacketFactory() { public Packet create() { return new NettyPlayerPositionS2CPacketV109(); } });
        registerV477S2C(0x37, new PacketFactory() { public Packet create() { return new NettyDestroyEntitiesPacketV47(); } });
        registerV477S2C(0x4D, new PacketFactory() { public Packet create() { return new SpawnPositionPacketV47(); } });
        registerV477S2C(0x56, new PacketFactory() { public Packet create() { return new EntityTeleportPacketV109(); } });
        registerV477S2C(0x1E, new PacketFactory() { public Packet create() { return new NettyChangeGameStatePacket(); } });
        registerV477S2C(0x4A, noOpFactory);  // V393: TimeUpdate; V477: RemoveEntityEffect
        registerV477S2C(0x4E, new PacketFactory() { public Packet create() { return new NettyTimeUpdatePacket(); } });

        // === V477 (1.14) C2S reverse map entries (for bot encoder) ===
        registerV477C2SReverse(NettyChatC2SPacket.class, 0x03);
        registerV477C2SReverse(KeepAlivePacketV340.class, 0x0F);
        registerV477C2SReverse(PlayerDiggingPacketV477.class, 0x1A);
        registerV477C2SReverse(NettyBlockPlacementPacketV477.class, 0x2C);

        // === V573 (1.15) S2C reverse map overlay (delta from V477) ===
        // New AcknowledgePlayerDigging at 0x08 shifts all S2C IDs >= 0x08 by +1.
        // SpawnPlayer uses new V573 class (no metadata).
        registerV573S2CReverse(NettySpawnPlayerPacketV573.class, 0x05);
        registerV573S2CReverse(AcknowledgePlayerDiggingPacketV573.class, 0x08);
        registerV573S2CReverse(NettyBlockChangePacketV477.class, 0x0C);
        registerV573S2CReverse(NettyChatS2CPacketV47.class, 0x0F);
        registerV573S2CReverse(DeclareCommandsPacketV393.class, 0x12);
        registerV573S2CReverse(ConfirmTransactionPacket.class, 0x13);
        registerV573S2CReverse(NettyWindowItemsPacketV47.class, 0x15);
        registerV573S2CReverse(NettySetSlotPacketV393.class, 0x17);
        registerV573S2CReverse(NettySetSlotPacketV404.class, 0x17);
        registerV573S2CReverse(NettyPluginMessageS2CPacketV393.class, 0x19);
        registerV573S2CReverse(NettyDisconnectPacket.class, 0x1B);
        registerV573S2CReverse(UnloadChunkPacketV109.class, 0x1E);
        registerV573S2CReverse(NettyChangeGameStatePacket.class, 0x1F);
        registerV573S2CReverse(KeepAlivePacketV340.class, 0x21);
        registerV573S2CReverse(MapChunkPacketV573.class, 0x22);
        registerV573S2CReverse(UpdateLightPacketV477.class, 0x25);
        registerV573S2CReverse(JoinGamePacketV573.class, 0x26);
        registerV573S2CReverse(EntityRelativeMovePacketV109.class, 0x29);
        registerV573S2CReverse(EntityLookAndMovePacketV109.class, 0x2A);
        registerV573S2CReverse(EntityLookPacketV47.class, 0x2B);
        registerV573S2CReverse(PlayerAbilitiesPacketV73.class, 0x32);
        registerV573S2CReverse(NettyPlayerListItemPacketV47.class, 0x34);
        registerV573S2CReverse(NettyPlayerPositionS2CPacketV109.class, 0x36);
        registerV573S2CReverse(NettyDestroyEntitiesPacketV47.class, 0x38);
        registerV573S2CReverse(SetChunkCacheCenterPacketV477.class, 0x41);
        registerV573S2CReverse(SetChunkCacheRadiusPacketV477.class, 0x42);
        registerV573S2CReverse(SpawnPositionPacketV477.class, 0x4E);
        registerV573S2CReverse(NettyTimeUpdatePacket.class, 0x4F);
        registerV573S2CReverse(EntityTeleportPacketV109.class, 0x57);
        registerV573S2CReverse(NettyEntityPropertiesPacketV47.class, 0x59);
        registerV573S2CReverse(UpdateRecipesPacketV393.class, 0x5B);
        registerV573S2CReverse(UpdateTagsPacketV477.class, 0x5C);

        // === V573 (1.15) S2C forward entries (for bot decoder) ===
        // Shadow stale V477 IDs that shifted in V573 due to new 0x08 packet.
        registerV573S2C(0x0B, noOpFactory);  // V477: BlockChange; V573: Difficulty
        registerV573S2C(0x0E, noOpFactory);  // V477: Chat; V573: TabComplete
        registerV573S2C(0x12, noOpFactory);  // V477: ConfirmTransaction; V573: DeclareCommands
        registerV573S2C(0x14, noOpFactory);  // V477: WindowItems; V573: ContainerSetContent
        registerV573S2C(0x16, noOpFactory);  // V477: SetSlot; V573: ContainerSetData
        registerV573S2C(0x18, noOpFactory);  // V477: PluginMessage; V573: Cooldown
        registerV573S2C(0x1A, noOpFactory);  // V477: Disconnect; V573: CustomSound
        registerV573S2C(0x1D, noOpFactory);  // V477: UnloadChunk; V573: EntityEvent
        registerV573S2C(0x1E, noOpFactory);  // V477: ChangeGameState; V573: Explosion
        registerV573S2C(0x20, noOpFactory);  // V477: KeepAlive; V573: HorseScreenOpen
        registerV573S2C(0x21, noOpFactory);  // V477: ChunkData; V573: KeepAlive(new pos)
        registerV573S2C(0x25, noOpFactory);  // V477: JoinGame; V573: UpdateLight(new pos)
        registerV573S2C(0x28, noOpFactory);  // V477: EntityRelMove; V573: MapData
        registerV573S2C(0x29, noOpFactory);  // V477: EntityLookMove; V573: Merchant Offers
        registerV573S2C(0x31, noOpFactory);  // V477: Abilities; V573: PlayerCombat
        registerV573S2C(0x33, noOpFactory);  // V477: PlayerInfo; V573: FacePlayer
        registerV573S2C(0x35, noOpFactory);  // V477: PlayerPos; V573: UseBed
        registerV573S2C(0x37, noOpFactory);  // V477: DestroyEntities; V573: RecipeBook
        registerV573S2C(0x40, noOpFactory);  // V477: ChunkCacheCenter; V573: ResourcePack
        registerV573S2C(0x41, noOpFactory);  // V477: ChunkCacheRadius; V573: Respawn
        registerV573S2C(0x4D, noOpFactory);  // V477: SpawnPosition; V573: SetScore
        registerV573S2C(0x4E, noOpFactory);  // V477: TimeUpdate; V573: SpawnPosition(new pos)
        registerV573S2C(0x56, noOpFactory);  // V477: EntityTeleport; V573: StopSound
        registerV573S2C(0x58, noOpFactory);  // V477: EntityProperties; V573: EntityTeleport(new pos)
        // Register active V573 S2C forward entries for bot decoder
        registerV573S2C(0x0C, new PacketFactory() { public Packet create() { return new NettyBlockChangePacketV477(); } });
        registerV573S2C(0x0F, new PacketFactory() { public Packet create() { return new NettyChatS2CPacketV47(); } });
        registerV573S2C(0x13, new PacketFactory() { public Packet create() { return new ConfirmTransactionPacket(); } });
        registerV573S2C(0x15, new PacketFactory() { public Packet create() { return new NettyWindowItemsPacketV47(); } });
        registerV573S2C(0x17, new PacketFactory() { public Packet create() { return new NettySetSlotPacketV393(); } });
        registerV573S2C(0x19, noOpFactory); // PluginMessage (bot doesn't need to decode)
        registerV573S2C(0x1B, new PacketFactory() { public Packet create() { return new NettyDisconnectPacket(); } });
        registerV573S2C(0x1F, new PacketFactory() { public Packet create() { return new NettyChangeGameStatePacket(); } });
        registerV573S2C(0x22, new PacketFactory() { public Packet create() { return new MapChunkPacketV573(); } });
        registerV573S2C(0x26, new PacketFactory() { public Packet create() { return new JoinGamePacketV573(); } });
        registerV573S2C(0x2A, new PacketFactory() { public Packet create() { return new EntityLookAndMovePacketV109(); } });
        registerV573S2C(0x2B, new PacketFactory() { public Packet create() { return new EntityLookPacketV47(); } });
        registerV573S2C(0x32, new PacketFactory() { public Packet create() { return new PlayerAbilitiesPacketV73(); } });
        registerV573S2C(0x34, new PacketFactory() { public Packet create() { return new NettyPlayerListItemPacketV47(); } });
        registerV573S2C(0x36, new PacketFactory() { public Packet create() { return new NettyPlayerPositionS2CPacketV109(); } });
        registerV573S2C(0x38, new PacketFactory() { public Packet create() { return new NettyDestroyEntitiesPacketV47(); } });
        registerV573S2C(0x4F, new PacketFactory() { public Packet create() { return new NettyTimeUpdatePacket(); } });
        registerV573S2C(0x57, new PacketFactory() { public Packet create() { return new EntityTeleportPacketV109(); } });

        // === V573 (1.15) C2S reverse map entries (for bot encoder) ===
        // C2S is unchanged from V477, but register for completeness
        registerV573C2SReverse(NettyChatC2SPacket.class, 0x03);
        registerV573C2SReverse(KeepAlivePacketV340.class, 0x0F);
        registerV573C2SReverse(PlayerDiggingPacketV477.class, 0x1A);
        registerV573C2SReverse(NettyBlockPlacementPacketV477.class, 0x2C);
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

    private static void registerV393C2S(int packetId, PacketFactory factory) {
        REGISTRY_V393.put(key(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
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

    private static void registerV393S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V393.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV393C2SReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V393.put(reverseKey(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, clazz), packetId);
    }

    private static void registerV477C2S(int packetId, PacketFactory factory) {
        REGISTRY_V477.put(key(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
    }

    private static void registerV477S2C(int packetId, PacketFactory factory) {
        REGISTRY_V477.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV477S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V477.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV477C2SReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V477.put(reverseKey(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, clazz), packetId);
    }

    private static void registerV573S2C(int packetId, PacketFactory factory) {
        REGISTRY_V573.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV573S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V573.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV573C2SReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V573.put(reverseKey(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, clazz), packetId);
    }

    private static void registerV109S2C(int packetId, PacketFactory factory) {
        REGISTRY_V109.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV108S2C(int packetId, PacketFactory factory) {
        REGISTRY_V108.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV335S2C(int packetId, PacketFactory factory) {
        REGISTRY_V335.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV338S2C(int packetId, PacketFactory factory) {
        REGISTRY_V338.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV340S2C(int packetId, PacketFactory factory) {
        REGISTRY_V340.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV393S2C(int packetId, PacketFactory factory) {
        REGISTRY_V393.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV109C2SReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V109.put(reverseKey(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, clazz), packetId);
    }

    private static void registerV335C2SReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V335.put(reverseKey(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, clazz), packetId);
    }

    private static void registerV338C2SReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V338.put(reverseKey(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, clazz), packetId);
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
        if (protocolVersion >= 573) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V573.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        if (protocolVersion >= 477) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V477.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        if (protocolVersion >= 393) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V393.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
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
        if (protocolVersion >= 108) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V108.get(k);
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
        if (protocolVersion >= 573) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V573.get(rk);
            if (id != null) {
                return id;
            }
        }
        if (protocolVersion >= 477) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V477.get(rk);
            if (id != null) {
                return id;
            }
        }
        if (protocolVersion >= 393) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V393.get(rk);
            if (id != null) {
                return id;
            }
        }
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
