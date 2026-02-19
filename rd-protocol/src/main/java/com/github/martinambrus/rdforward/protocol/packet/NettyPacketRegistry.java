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

    /** V735 overlay: C2S/S2C packets for 1.16 (ADD_GLOBAL_ENTITY removed, SpawnPosition relocated, GENERATE_JIGSAW inserted). */
    private static final Map<String, PacketFactory> REGISTRY_V735 = new HashMap<String, PacketFactory>();

    /** V735 S2C reverse map overlay: shifted S2C/C2S packet IDs for 1.16. */
    private static final Map<String, Integer> REVERSE_V735 = new HashMap<String, Integer>();

    /** V751 overlay: C2S/S2C packets for 1.16.2 (CHUNK_BLOCKS_UPDATE removed at 0x0F, SECTION_BLOCKS_UPDATE at 0x3B, C2S recipe split). */
    private static final Map<String, PacketFactory> REGISTRY_V751 = new HashMap<String, PacketFactory>();

    /** V751 S2C/C2S reverse map overlay: shifted packet IDs for 1.16.2. */
    private static final Map<String, Integer> REVERSE_V751 = new HashMap<String, Integer>();

    /** V755 overlay: C2S/S2C packets for 1.17 (many S2C splits/insertions, CONTAINER_ACK removed from C2S). */
    private static final Map<String, PacketFactory> REGISTRY_V755 = new HashMap<String, PacketFactory>();

    /** V755 S2C/C2S reverse map overlay: shifted packet IDs for 1.17. */
    private static final Map<String, Integer> REVERSE_V755 = new HashMap<String, Integer>();

    /** V756 overlay: S2C DestroyEntities reverted from single-entity to multi-entity format. */
    private static final Map<String, PacketFactory> REGISTRY_V756 = new HashMap<String, PacketFactory>();

    /** V756 S2C reverse map overlay: NettyDestroyEntitiesPacketV47 at 0x3A instead of RemoveEntityPacketV755. */
    private static final Map<String, Integer> REVERSE_V756 = new HashMap<String, Integer>();

    /** V757 overlay: S2C MapChunk+UpdateLight combined, JoinGame gained simulationDistance. */
    private static final Map<String, PacketFactory> REGISTRY_V757 = new HashMap<String, PacketFactory>();

    /** V757 S2C reverse map overlay: MapChunkPacketV757 at 0x22, JoinGamePacketV757 at 0x26. */
    private static final Map<String, Integer> REVERSE_V757 = new HashMap<String, Integer>();

    /** V758 overlay: JoinGame infiniburn gains '#' prefix, UpdateTags gains fall_damage_resetting. */
    private static final Map<String, PacketFactory> REGISTRY_V758 = new HashMap<String, PacketFactory>();

    /** V758 S2C reverse map overlay: JoinGamePacketV758 at 0x26, UpdateTagsPacketV758 at 0x67. */
    private static final Map<String, Integer> REVERSE_V758 = new HashMap<String, Integer>();

    /** V759 overlay: C2S/S2C packets for 1.19 (chat system rewrite, packet ID reshuffle, BlockChangedAck). */
    private static final Map<String, PacketFactory> REGISTRY_V759 = new HashMap<String, PacketFactory>();

    /** V759 S2C/C2S reverse map overlay: shifted packet IDs for 1.19. */
    private static final Map<String, Integer> REVERSE_V759 = new HashMap<String, Integer>();

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

        // === V735 (1.16) S2C reverse map overlay (delta from V573) ===
        // ADD_GLOBAL_ENTITY removed (was 0x02 in V573) -> all S2C shift -1 from V573.
        // SpawnPosition relocated from after UpdateScore to after SetChunkCacheRadius.
        registerV735S2CReverse(NettySpawnPlayerPacketV573.class, 0x04);
        registerV735S2CReverse(AcknowledgePlayerDiggingPacketV573.class, 0x07);
        registerV735S2CReverse(NettyBlockChangePacketV477.class, 0x0B);
        registerV735S2CReverse(NettyChatS2CPacketV735.class, 0x0E);
        registerV735S2CReverse(DeclareCommandsPacketV393.class, 0x11);
        registerV735S2CReverse(ConfirmTransactionPacket.class, 0x12);
        registerV735S2CReverse(NettyWindowItemsPacketV47.class, 0x14);
        registerV735S2CReverse(NettySetSlotPacketV393.class, 0x16);
        registerV735S2CReverse(NettySetSlotPacketV404.class, 0x16);
        registerV735S2CReverse(NettyPluginMessageS2CPacketV393.class, 0x18);
        registerV735S2CReverse(NettyDisconnectPacket.class, 0x1A);
        registerV735S2CReverse(UnloadChunkPacketV109.class, 0x1D);
        registerV735S2CReverse(NettyChangeGameStatePacket.class, 0x1E);
        registerV735S2CReverse(KeepAlivePacketV340.class, 0x20);
        registerV735S2CReverse(MapChunkPacketV735.class, 0x21);
        registerV735S2CReverse(UpdateLightPacketV735.class, 0x24);
        registerV735S2CReverse(JoinGamePacketV735.class, 0x25);
        registerV735S2CReverse(EntityRelativeMovePacketV109.class, 0x28);
        registerV735S2CReverse(EntityLookAndMovePacketV109.class, 0x29);
        registerV735S2CReverse(EntityLookPacketV47.class, 0x2A);
        registerV735S2CReverse(PlayerAbilitiesPacketV73.class, 0x31);
        registerV735S2CReverse(NettyPlayerListItemPacketV47.class, 0x33);
        registerV735S2CReverse(NettyPlayerPositionS2CPacketV109.class, 0x35);
        registerV735S2CReverse(NettyDestroyEntitiesPacketV47.class, 0x37);
        registerV735S2CReverse(SetChunkCacheCenterPacketV477.class, 0x40);
        registerV735S2CReverse(SetChunkCacheRadiusPacketV477.class, 0x41);
        registerV735S2CReverse(SpawnPositionPacketV477.class, 0x42);
        registerV735S2CReverse(NettyTimeUpdatePacket.class, 0x4E);
        registerV735S2CReverse(EntityTeleportPacketV109.class, 0x56);
        registerV735S2CReverse(NettyEntityPropertiesPacketV47.class, 0x58);
        registerV735S2CReverse(UpdateRecipesPacketV393.class, 0x5A);
        registerV735S2CReverse(UpdateTagsPacketV735.class, 0x5B);

        // V735 LOGIN state: UUID changed from String to binary (2 longs)
        REVERSE_V735.put(reverseKey(ConnectionState.LOGIN, PacketDirection.SERVER_TO_CLIENT,
                LoginSuccessPacketV735.class), 0x02);

        // === V735 (1.16) S2C forward entries (for bot decoder) ===
        // Shadow stale V573 IDs that shifted in V735 due to ADD_GLOBAL_ENTITY removal.
        registerV735S2C(0x05, noOpFactory);  // V573: SpawnPlayer; V735: SpawnExperienceOrb
        registerV735S2C(0x08, noOpFactory);  // V573: AcknowledgeDigging; V735: BlockBreakAnimation
        registerV735S2C(0x0C, noOpFactory);  // V573: BlockChange; V735: BossBar
        registerV735S2C(0x0F, noOpFactory);  // V573: Chat; V735: TabComplete
        registerV735S2C(0x12, noOpFactory);  // V573: DeclareCommands; V735: WindowConfirmation
        registerV735S2C(0x13, noOpFactory);  // V573: ConfirmTransaction; V735: CloseWindow
        registerV735S2C(0x15, noOpFactory);  // V573: WindowItems; V735: ContainerSetData
        registerV735S2C(0x17, noOpFactory);  // V573: SetSlot; V735: Cooldown
        registerV735S2C(0x19, noOpFactory);  // V573: PluginMessage; V735: CustomSound
        registerV735S2C(0x1B, noOpFactory);  // V573: Disconnect; V735: EntityEvent
        registerV735S2C(0x1E, noOpFactory);  // V573: UnloadChunk; V735: Explosion
        registerV735S2C(0x1F, noOpFactory);  // V573: ChangeGameState; V735: HorseScreenOpen
        registerV735S2C(0x21, noOpFactory);  // V573: KeepAlive; V735: KeepAlive(new ID)
        registerV735S2C(0x22, noOpFactory);  // V573: ChunkData; V735: LevelEvent
        registerV735S2C(0x25, noOpFactory);  // V573: UpdateLight; V735: UpdateLight(new pos)
        registerV735S2C(0x26, noOpFactory);  // V573: JoinGame; V735: TradeMerchant
        registerV735S2C(0x29, noOpFactory);  // V573: EntityRelMove; V735: EntityRelMove(same but check)
        registerV735S2C(0x2A, noOpFactory);  // V573: EntityLookMove; V735: EntityLookMove(same)
        registerV735S2C(0x2B, noOpFactory);  // V573: EntityLook; V735: EntityLook(same)
        registerV735S2C(0x32, noOpFactory);  // V573: Abilities; V735: PlayerCombat
        registerV735S2C(0x34, noOpFactory);  // V573: PlayerInfo; V735: FacePlayer
        registerV735S2C(0x36, noOpFactory);  // V573: PlayerPos; V735: UnlockRecipes
        registerV735S2C(0x38, noOpFactory);  // V573: DestroyEntities; V735: RecipeBook
        registerV735S2C(0x41, noOpFactory);  // V573: ChunkCacheCenter; V735: Respawn
        registerV735S2C(0x42, noOpFactory);  // V573: ChunkCacheRadius; V735: EntityHeadRotation
        registerV735S2C(0x4E, noOpFactory);  // V573: SpawnPosition; V735: SetActionBarText
        registerV735S2C(0x4F, noOpFactory);  // V573: TimeUpdate; V735: SpawnPosition(new)
        registerV735S2C(0x57, noOpFactory);  // V573: EntityTeleport; V735: StopSound
        registerV735S2C(0x59, noOpFactory);  // V573: EntityProperties; V735: EntityProperties(new)
        // Register active V735 S2C forward entries for bot decoder
        registerV735S2C(0x04, new PacketFactory() { public Packet create() { return new NettySpawnPlayerPacketV573(); } });
        registerV735S2C(0x0B, new PacketFactory() { public Packet create() { return new NettyBlockChangePacketV477(); } });
        registerV735S2C(0x0E, new PacketFactory() { public Packet create() { return new NettyChatS2CPacketV735(); } });
        registerV735S2C(0x11, new PacketFactory() { public Packet create() { return new DeclareCommandsPacketV393(); } });
        registerV735S2C(0x14, new PacketFactory() { public Packet create() { return new NettyWindowItemsPacketV47(); } });
        registerV735S2C(0x16, new PacketFactory() { public Packet create() { return new NettySetSlotPacketV393(); } });
        registerV735S2C(0x18, noOpFactory); // PluginMessage (bot doesn't need to decode)
        registerV735S2C(0x1A, new PacketFactory() { public Packet create() { return new NettyDisconnectPacket(); } });
        registerV735S2C(0x1D, new PacketFactory() { public Packet create() { return new UnloadChunkPacketV109(); } });
        registerV735S2C(0x20, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });
        registerV735S2C(0x24, new PacketFactory() { public Packet create() { return new UpdateLightPacketV735(); } });
        registerV735S2C(0x28, new PacketFactory() { public Packet create() { return new EntityRelativeMovePacketV109(); } });
        registerV735S2C(0x31, new PacketFactory() { public Packet create() { return new PlayerAbilitiesPacketV73(); } });
        registerV735S2C(0x33, new PacketFactory() { public Packet create() { return new NettyPlayerListItemPacketV47(); } });
        registerV735S2C(0x35, new PacketFactory() { public Packet create() { return new NettyPlayerPositionS2CPacketV109(); } });
        registerV735S2C(0x37, new PacketFactory() { public Packet create() { return new NettyDestroyEntitiesPacketV47(); } });
        registerV735S2C(0x40, new PacketFactory() { public Packet create() { return new SetChunkCacheCenterPacketV477(); } });
        registerV735S2C(0x56, new PacketFactory() { public Packet create() { return new EntityTeleportPacketV109(); } });
        registerV735S2C(0x4E, new PacketFactory() { public Packet create() { return new NettyTimeUpdatePacket(); } });

        // === V735 (1.16) C2S entries (GENERATE_JIGSAW inserted at 0x0F, shifting >= 0x0F by +1) ===
        registerV735C2S(0x00, new PacketFactory() { public Packet create() { return new TeleportConfirmPacketV109(); } });
        registerV735C2S(0x01, noOpFactory); // BlockEntityTagQuery
        registerV735C2S(0x02, noOpFactory); // SetDifficulty
        registerV735C2S(0x03, new PacketFactory() { public Packet create() { return new NettyChatC2SPacket(); } });
        registerV735C2S(0x04, new PacketFactory() { public Packet create() { return new ClientCommandPacket(); } });
        registerV735C2S(0x05, new PacketFactory() { public Packet create() { return new NettyClientSettingsPacketV109(); } });
        registerV735C2S(0x06, noOpFactory); // CommandSuggestion
        registerV735C2S(0x07, new PacketFactory() { public Packet create() { return new ConfirmTransactionPacket(); } });
        registerV735C2S(0x08, noOpFactory); // ClickWindowButton
        registerV735C2S(0x09, noOpFactory); // WindowClick (slot format changed)
        registerV735C2S(0x0A, new PacketFactory() { public Packet create() { return new CloseWindowPacket(); } });
        registerV735C2S(0x0B, new PacketFactory() { public Packet create() { return new NettyPluginMessagePacketV47(); } });
        registerV735C2S(0x0C, noOpFactory); // EditBook
        registerV735C2S(0x0D, noOpFactory); // EntityTagQuery
        registerV735C2S(0x0E, new PacketFactory() { public Packet create() { return new NettyUseEntityPacketV47(); } });
        registerV735C2S(0x0F, noOpFactory); // GenerateJigsaw (NEW in 1.16)
        registerV735C2S(0x10, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });
        registerV735C2S(0x11, noOpFactory); // LockDifficulty
        registerV735C2S(0x12, new PacketFactory() { public Packet create() { return new PlayerPositionPacketV47(); } });
        registerV735C2S(0x13, new PacketFactory() { public Packet create() { return new PlayerPositionAndLookC2SPacketV47(); } });
        registerV735C2S(0x14, new PacketFactory() { public Packet create() { return new PlayerLookPacket(); } });
        registerV735C2S(0x15, new PacketFactory() { public Packet create() { return new PlayerOnGroundPacket(); } });
        registerV735C2S(0x16, noOpFactory); // VehicleMove
        registerV735C2S(0x17, noOpFactory); // PaddleBoat
        registerV735C2S(0x18, noOpFactory); // PickItem
        registerV735C2S(0x19, noOpFactory); // PlaceRecipe
        registerV735C2S(0x1A, new PacketFactory() { public Packet create() { return new PlayerAbilitiesPacketV735(); } });
        registerV735C2S(0x1B, new PacketFactory() { public Packet create() { return new PlayerDiggingPacketV477(); } });
        registerV735C2S(0x1C, new PacketFactory() { public Packet create() { return new NettyEntityActionPacketV47(); } });
        registerV735C2S(0x1D, new PacketFactory() { public Packet create() { return new NettySteerVehiclePacketV47(); } });
        registerV735C2S(0x1E, noOpFactory); // RecipeBookUpdate
        registerV735C2S(0x1F, noOpFactory); // RenameItem
        registerV735C2S(0x20, noOpFactory); // ResourcePack
        registerV735C2S(0x21, noOpFactory); // SeenAdvancements
        registerV735C2S(0x22, noOpFactory); // SelectTrade
        registerV735C2S(0x23, noOpFactory); // SetBeacon
        registerV735C2S(0x24, new PacketFactory() { public Packet create() { return new HoldingChangePacketBeta(); } });
        registerV735C2S(0x25, noOpFactory); // SetCommandBlock
        registerV735C2S(0x26, noOpFactory); // SetCommandMinecart
        registerV735C2S(0x27, noOpFactory); // CreativeSlot (slot format changed)
        registerV735C2S(0x28, noOpFactory); // SetJigsawBlock
        registerV735C2S(0x29, noOpFactory); // SetStructureBlock
        registerV735C2S(0x2A, new PacketFactory() { public Packet create() { return new NettyUpdateSignPacketV47(); } });
        registerV735C2S(0x2B, new PacketFactory() { public Packet create() { return new AnimationPacketV109(); } });
        registerV735C2S(0x2C, noOpFactory); // Spectate
        registerV735C2S(0x2D, new PacketFactory() { public Packet create() { return new NettyBlockPlacementPacketV477(); } });
        registerV735C2S(0x2E, new PacketFactory() { public Packet create() { return new UseItemPacketV109(); } });

        // === V735 (1.16) C2S reverse map entries (for bot encoder) ===
        registerV735C2SReverse(NettyChatC2SPacket.class, 0x03);
        registerV735C2SReverse(KeepAlivePacketV340.class, 0x10);
        registerV735C2SReverse(PlayerDiggingPacketV477.class, 0x1B);
        registerV735C2SReverse(NettyBlockPlacementPacketV477.class, 0x2D);

        // === V751 (1.16.2) S2C reverse map overlay (delta from V735) ===
        // CHUNK_BLOCKS_UPDATE removed at V735 0x0F. IDs 0x10-0x3B shift -1. IDs >= 0x3C unchanged.
        // IDs 0x00-0x0E unchanged from V735.
        registerV751S2CReverse(NettySpawnPlayerPacketV573.class, 0x04);
        registerV751S2CReverse(AcknowledgePlayerDiggingPacketV573.class, 0x07);
        registerV751S2CReverse(NettyBlockChangePacketV477.class, 0x0B);
        registerV751S2CReverse(NettyChatS2CPacketV735.class, 0x0E);
        registerV751S2CReverse(DeclareCommandsPacketV393.class, 0x10);
        registerV751S2CReverse(ConfirmTransactionPacket.class, 0x11);
        registerV751S2CReverse(NettyWindowItemsPacketV47.class, 0x13);
        registerV751S2CReverse(NettySetSlotPacketV393.class, 0x15);
        registerV751S2CReverse(NettySetSlotPacketV404.class, 0x15);
        registerV751S2CReverse(NettyPluginMessageS2CPacketV393.class, 0x17);
        registerV751S2CReverse(NettyDisconnectPacket.class, 0x19);
        registerV751S2CReverse(UnloadChunkPacketV109.class, 0x1C);
        registerV751S2CReverse(NettyChangeGameStatePacket.class, 0x1D);
        registerV751S2CReverse(KeepAlivePacketV340.class, 0x1F);
        registerV751S2CReverse(MapChunkPacketV751.class, 0x20);
        registerV751S2CReverse(UpdateLightPacketV735.class, 0x23);
        registerV751S2CReverse(JoinGamePacketV751.class, 0x24);
        registerV751S2CReverse(EntityRelativeMovePacketV109.class, 0x27);
        registerV751S2CReverse(EntityLookAndMovePacketV109.class, 0x28);
        registerV751S2CReverse(EntityLookPacketV47.class, 0x29);
        registerV751S2CReverse(PlayerAbilitiesPacketV73.class, 0x30);
        registerV751S2CReverse(NettyPlayerListItemPacketV47.class, 0x32);
        registerV751S2CReverse(NettyPlayerPositionS2CPacketV109.class, 0x34);
        registerV751S2CReverse(NettyDestroyEntitiesPacketV47.class, 0x36);
        // IDs >= 0x3C unchanged from V735
        registerV751S2CReverse(SetChunkCacheCenterPacketV477.class, 0x40);
        registerV751S2CReverse(SetChunkCacheRadiusPacketV477.class, 0x41);
        registerV751S2CReverse(SpawnPositionPacketV477.class, 0x42);
        registerV751S2CReverse(NettyTimeUpdatePacket.class, 0x4E);
        registerV751S2CReverse(EntityTeleportPacketV109.class, 0x56);
        registerV751S2CReverse(NettyEntityPropertiesPacketV47.class, 0x58);
        registerV751S2CReverse(UpdateRecipesPacketV393.class, 0x5A);
        registerV751S2CReverse(UpdateTagsPacketV751.class, 0x5B);

        // V751 LOGIN state: same as V735 (binary UUID)
        REVERSE_V751.put(reverseKey(ConnectionState.LOGIN, PacketDirection.SERVER_TO_CLIENT,
                LoginSuccessPacketV735.class), 0x02);

        // === V751 (1.16.2) S2C forward entries (for bot decoder) ===
        // Shadow stale V735 IDs that shifted in V751 due to CHUNK_BLOCKS_UPDATE removal at 0x0F.
        // V735 IDs 0x10-0x3B now map to different packets; shadow old positions with noOp.
        registerV751S2C(0x10, noOpFactory);  // V735: DeclareCommands -> V751: TabComplete
        registerV751S2C(0x11, noOpFactory);  // V735: ConfirmTransaction -> V751: DeclareCommands
        registerV751S2C(0x13, noOpFactory);  // V735: WindowItems -> V751: WindowConfirmation
        registerV751S2C(0x14, noOpFactory);  // V735: (unused) -> V751: ContainerSetContent
        registerV751S2C(0x15, noOpFactory);  // V735: SetSlot -> V751: ContainerSetData
        registerV751S2C(0x16, noOpFactory);  // V735: (unused) -> V751: SetSlot
        registerV751S2C(0x18, noOpFactory);  // V735: PluginMessage -> V751: CustomSound
        registerV751S2C(0x19, noOpFactory);  // V735: (unused) -> V751: Disconnect
        registerV751S2C(0x1A, noOpFactory);  // V735: Disconnect -> V751: EntityEvent
        registerV751S2C(0x1C, noOpFactory);  // V735: (unused) -> V751: UnloadChunk
        registerV751S2C(0x1D, noOpFactory);  // V735: UnloadChunk -> V751: ChangeGameState
        registerV751S2C(0x1E, noOpFactory);  // V735: ChangeGameState -> V751: HorseScreenOpen
        registerV751S2C(0x1F, noOpFactory);  // V735: (unused) -> V751: KeepAlive
        registerV751S2C(0x20, noOpFactory);  // V735: KeepAlive -> V751: ChunkData
        registerV751S2C(0x23, noOpFactory);  // V735: UpdateLight -> V751: UpdateLight(new pos)
        registerV751S2C(0x24, noOpFactory);  // V735: (unused) -> V751: JoinGame
        registerV751S2C(0x25, noOpFactory);  // V735: JoinGame -> V751: MapData
        registerV751S2C(0x27, noOpFactory);  // V735: (unused) -> V751: EntityRelMove
        registerV751S2C(0x28, noOpFactory);  // V735: EntityRelMove -> V751: EntityLookMove
        registerV751S2C(0x29, noOpFactory);  // V735: EntityLookMove -> V751: EntityLook
        registerV751S2C(0x2A, noOpFactory);  // V735: EntityLook -> V751: (other)
        registerV751S2C(0x30, noOpFactory);  // V735: (unused) -> V751: Abilities
        registerV751S2C(0x31, noOpFactory);  // V735: Abilities -> V751: EndCombat
        registerV751S2C(0x32, noOpFactory);  // V735: (unused) -> V751: PlayerInfo
        registerV751S2C(0x33, noOpFactory);  // V735: PlayerInfo -> V751: FacePlayer
        registerV751S2C(0x34, noOpFactory);  // V735: (unused) -> V751: PlayerPos
        registerV751S2C(0x35, noOpFactory);  // V735: PlayerPos -> V751: UnlockRecipes
        registerV751S2C(0x36, noOpFactory);  // V735: (unused) -> V751: DestroyEntities
        registerV751S2C(0x37, noOpFactory);  // V735: DestroyEntities -> V751: (other)
        // Register active V751 S2C forward entries for bot decoder
        registerV751S2C(0x04, new PacketFactory() { public Packet create() { return new NettySpawnPlayerPacketV573(); } });
        registerV751S2C(0x0B, new PacketFactory() { public Packet create() { return new NettyBlockChangePacketV477(); } });
        registerV751S2C(0x0E, new PacketFactory() { public Packet create() { return new NettyChatS2CPacketV735(); } });
        registerV751S2C(0x17, noOpFactory); // PluginMessage (bot doesn't need to decode)
        registerV751S2C(0x1F, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });
        registerV751S2C(0x27, new PacketFactory() { public Packet create() { return new EntityRelativeMovePacketV109(); } });
        registerV751S2C(0x28, new PacketFactory() { public Packet create() { return new EntityLookAndMovePacketV109(); } });
        registerV751S2C(0x29, new PacketFactory() { public Packet create() { return new EntityLookPacketV47(); } });
        registerV751S2C(0x30, new PacketFactory() { public Packet create() { return new PlayerAbilitiesPacketV73(); } });
        registerV751S2C(0x32, new PacketFactory() { public Packet create() { return new NettyPlayerListItemPacketV47(); } });
        registerV751S2C(0x34, new PacketFactory() { public Packet create() { return new NettyPlayerPositionS2CPacketV109(); } });
        registerV751S2C(0x36, new PacketFactory() { public Packet create() { return new NettyDestroyEntitiesPacketV47(); } });
        registerV751S2C(0x40, new PacketFactory() { public Packet create() { return new SetChunkCacheCenterPacketV477(); } });
        registerV751S2C(0x4E, new PacketFactory() { public Packet create() { return new NettyTimeUpdatePacket(); } });
        registerV751S2C(0x56, new PacketFactory() { public Packet create() { return new EntityTeleportPacketV109(); } });

        // === V751 (1.16.2) C2S entries (RECIPE_BOOK_UPDATE split at 0x1E, shifting >= 0x1F by +1) ===
        // IDs 0x00-0x1E unchanged from V735. Only need to register shifted packets and new 0x1F.
        registerV751C2S(0x1F, noOpFactory); // RecipeBookSeenRecipe (NEW in 1.16.2)
        registerV751C2S(0x20, noOpFactory); // RenameItem (was 0x1F)
        registerV751C2S(0x21, noOpFactory); // ResourcePack (was 0x20)
        registerV751C2S(0x22, noOpFactory); // SeenAdvancements (was 0x21)
        registerV751C2S(0x23, noOpFactory); // SelectTrade (was 0x22)
        registerV751C2S(0x24, noOpFactory); // SetBeacon (was 0x23)
        registerV751C2S(0x25, new PacketFactory() { public Packet create() { return new HoldingChangePacketBeta(); } });
        registerV751C2S(0x26, noOpFactory); // SetCommandBlock (was 0x25)
        registerV751C2S(0x27, noOpFactory); // SetCommandMinecart (was 0x26)
        registerV751C2S(0x28, noOpFactory); // CreativeSlot (was 0x27)
        registerV751C2S(0x29, noOpFactory); // SetJigsawBlock (was 0x28)
        registerV751C2S(0x2A, noOpFactory); // SetStructureBlock (was 0x29)
        registerV751C2S(0x2B, new PacketFactory() { public Packet create() { return new NettyUpdateSignPacketV47(); } });
        registerV751C2S(0x2C, new PacketFactory() { public Packet create() { return new AnimationPacketV109(); } });
        registerV751C2S(0x2D, noOpFactory); // Spectate (was 0x2C)
        registerV751C2S(0x2E, new PacketFactory() { public Packet create() { return new NettyBlockPlacementPacketV477(); } });
        registerV751C2S(0x2F, new PacketFactory() { public Packet create() { return new UseItemPacketV109(); } });

        // === V751 (1.16.2) C2S reverse map entries (for bot encoder) ===
        registerV751C2SReverse(NettyChatC2SPacket.class, 0x03);
        registerV751C2SReverse(KeepAlivePacketV340.class, 0x10);
        registerV751C2SReverse(PlayerDiggingPacketV477.class, 0x1B);
        registerV751C2SReverse(NettyBlockPlacementPacketV477.class, 0x2E);

        // === V755 (1.17) S2C reverse map overlay (delta from V751) ===
        // Many new S2C packets inserted: ADD_VIBRATION_SIGNAL at 0x05, CLEAR_TITLES at 0x10,
        // INITIALIZE_BORDER at 0x20, PING at 0x30, PLAYER_COMBAT split (3 packets at 0x33-0x35),
        // SET_ACTION_BAR_TEXT at 0x41, SET_BORDER split (6 packets at 0x42-0x46),
        // SET_SUBTITLE_TEXT at 0x57, SET_TITLE_TEXT at 0x59, SET_TITLES_ANIMATION at 0x5A.
        // ConfirmTransaction removed from both S2C and C2S.
        registerV755S2CReverse(NettySpawnPlayerPacketV573.class, 0x04);
        registerV755S2CReverse(AcknowledgePlayerDiggingPacketV573.class, 0x08);
        registerV755S2CReverse(NettyBlockChangePacketV477.class, 0x0C);
        registerV755S2CReverse(NettyChatS2CPacketV735.class, 0x0F);
        registerV755S2CReverse(DeclareCommandsPacketV393.class, 0x12);
        registerV755S2CReverse(NettyWindowItemsPacketV47.class, 0x14);
        registerV755S2CReverse(NettySetSlotPacketV393.class, 0x16);
        registerV755S2CReverse(NettySetSlotPacketV404.class, 0x16);
        registerV755S2CReverse(NettyPluginMessageS2CPacketV393.class, 0x18);
        registerV755S2CReverse(NettyDisconnectPacket.class, 0x1A);
        registerV755S2CReverse(UnloadChunkPacketV109.class, 0x1D);
        registerV755S2CReverse(NettyChangeGameStatePacket.class, 0x1E);
        registerV755S2CReverse(KeepAlivePacketV340.class, 0x21);
        registerV755S2CReverse(MapChunkPacketV755.class, 0x22);
        registerV755S2CReverse(UpdateLightPacketV755.class, 0x25);
        registerV755S2CReverse(JoinGamePacketV755.class, 0x26);
        registerV755S2CReverse(EntityRelativeMovePacketV109.class, 0x29);
        registerV755S2CReverse(EntityLookAndMovePacketV109.class, 0x2A);
        registerV755S2CReverse(EntityLookPacketV47.class, 0x2B);
        registerV755S2CReverse(PlayerAbilitiesPacketV73.class, 0x32);
        registerV755S2CReverse(NettyPlayerListItemPacketV47.class, 0x36);
        registerV755S2CReverse(NettyPlayerPositionS2CPacketV755.class, 0x38);
        registerV755S2CReverse(RemoveEntityPacketV755.class, 0x3A);
        registerV755S2CReverse(SetChunkCacheCenterPacketV477.class, 0x49);
        registerV755S2CReverse(SetChunkCacheRadiusPacketV477.class, 0x4A);
        registerV755S2CReverse(SpawnPositionPacketV755.class, 0x4B);
        registerV755S2CReverse(NettyTimeUpdatePacket.class, 0x58);
        registerV755S2CReverse(EntityTeleportPacketV109.class, 0x61);
        registerV755S2CReverse(NettyEntityPropertiesPacketV755.class, 0x63);
        registerV755S2CReverse(UpdateRecipesPacketV393.class, 0x65);
        registerV755S2CReverse(UpdateTagsPacketV755.class, 0x66);

        // V755 LOGIN state: same as V735/V751 (binary UUID)
        REVERSE_V755.put(reverseKey(ConnectionState.LOGIN, PacketDirection.SERVER_TO_CLIENT,
                LoginSuccessPacketV735.class), 0x02);

        // === V755 (1.17) S2C forward entries (for bot decoder) ===
        // Shadow stale V751 IDs that shifted in V755 due to new packets.
        registerV755S2C(0x04, new PacketFactory() { public Packet create() { return new NettySpawnPlayerPacketV573(); } });
        registerV755S2C(0x08, noOpFactory);  // V751: AcknowledgeDigging; V755: BlockBreakAnimation
        registerV755S2C(0x0B, noOpFactory);  // V751: BlockChange; V755: BlockEntity
        registerV755S2C(0x0C, new PacketFactory() { public Packet create() { return new NettyBlockChangePacketV477(); } });
        registerV755S2C(0x0E, noOpFactory);  // V751: Chat; V755: TabComplete
        registerV755S2C(0x0F, new PacketFactory() { public Packet create() { return new NettyChatS2CPacketV735(); } });
        registerV755S2C(0x10, noOpFactory);  // V751: DeclareCommands; V755: ClearTitles
        registerV755S2C(0x11, noOpFactory);  // V751: ConfirmTransaction; V755: CommandSuggestions
        registerV755S2C(0x12, noOpFactory);  // shadow: DeclareCommands new pos
        registerV755S2C(0x13, noOpFactory);  // V751: WindowItems; V755: CloseWindow
        registerV755S2C(0x14, new PacketFactory() { public Packet create() { return new NettyWindowItemsPacketV47(); } });
        registerV755S2C(0x15, noOpFactory);  // V751: SetSlot; V755: SetSlot(new pos)
        registerV755S2C(0x16, new PacketFactory() { public Packet create() { return new NettySetSlotPacketV393(); } });
        registerV755S2C(0x17, noOpFactory);  // V751: PluginMessage; V755: Cooldown
        registerV755S2C(0x18, noOpFactory); // PluginMessage (bot doesn't need to decode)
        registerV755S2C(0x19, noOpFactory);  // V751: Disconnect; V755: CustomSound
        registerV755S2C(0x1A, new PacketFactory() { public Packet create() { return new NettyDisconnectPacket(); } });
        registerV755S2C(0x1C, noOpFactory);  // V751: UnloadChunk; V755: EntityEvent
        registerV755S2C(0x1D, new PacketFactory() { public Packet create() { return new UnloadChunkPacketV109(); } });
        registerV755S2C(0x1E, new PacketFactory() { public Packet create() { return new NettyChangeGameStatePacket(); } });
        registerV755S2C(0x1F, noOpFactory);  // V751: KeepAlive; V755: HorseScreenOpen
        registerV755S2C(0x20, noOpFactory);  // V751: ChunkData; V755: InitializeBorder
        registerV755S2C(0x21, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });
        registerV755S2C(0x22, new PacketFactory() { public Packet create() { return new MapChunkPacketV755(); } });
        registerV755S2C(0x23, noOpFactory);  // V751: UpdateLight; V755: LevelEvent
        registerV755S2C(0x24, noOpFactory);  // V751: JoinGame; V755: Particle
        registerV755S2C(0x25, new PacketFactory() { public Packet create() { return new UpdateLightPacketV755(); } });
        registerV755S2C(0x26, new PacketFactory() { public Packet create() { return new JoinGamePacketV755(); } });
        registerV755S2C(0x27, noOpFactory);  // V751: EntityRelMove; V755: MapData
        registerV755S2C(0x28, noOpFactory);  // V751: EntityLookMove; V755: Merchant Offers
        registerV755S2C(0x29, new PacketFactory() { public Packet create() { return new EntityRelativeMovePacketV109(); } });
        registerV755S2C(0x2A, new PacketFactory() { public Packet create() { return new EntityLookAndMovePacketV109(); } });
        registerV755S2C(0x2B, new PacketFactory() { public Packet create() { return new EntityLookPacketV47(); } });
        registerV755S2C(0x30, noOpFactory);  // V751: Abilities; V755: Ping
        registerV755S2C(0x32, noOpFactory);  // V751: PlayerInfo; V755: Abilities
        registerV755S2C(0x34, noOpFactory);  // V751: PlayerPos; V755: EndCombat
        registerV755S2C(0x36, noOpFactory);  // V751: DestroyEntities; V755: PlayerInfo
        registerV755S2C(0x38, new PacketFactory() { public Packet create() { return new NettyPlayerPositionS2CPacketV755(); } });
        registerV755S2C(0x3A, new PacketFactory() { public Packet create() { return new RemoveEntityPacketV755(); } });
        registerV755S2C(0x40, noOpFactory);  // V751: ChunkCacheCenter; V755: SelectAdv
        registerV755S2C(0x49, new PacketFactory() { public Packet create() { return new SetChunkCacheCenterPacketV477(); } });
        registerV755S2C(0x4E, noOpFactory);  // V751: TimeUpdate; V755: SetTitleSubText
        registerV755S2C(0x56, noOpFactory);  // V751: EntityTeleport; V755: SoundEntity
        registerV755S2C(0x58, new PacketFactory() { public Packet create() { return new NettyTimeUpdatePacket(); } });
        registerV755S2C(0x61, new PacketFactory() { public Packet create() { return new EntityTeleportPacketV109(); } });

        // === V755 (1.17) C2S entries (CONTAINER_ACK removed at 0x07, shifting 0x08-0x1D by -1; PONG at 0x1D) ===
        registerV755C2S(0x00, new PacketFactory() { public Packet create() { return new TeleportConfirmPacketV109(); } });
        registerV755C2S(0x01, noOpFactory); // BlockEntityTagQuery
        registerV755C2S(0x02, noOpFactory); // SetDifficulty
        registerV755C2S(0x03, new PacketFactory() { public Packet create() { return new NettyChatC2SPacket(); } });
        registerV755C2S(0x04, new PacketFactory() { public Packet create() { return new ClientCommandPacket(); } });
        registerV755C2S(0x05, new PacketFactory() { public Packet create() { return new NettyClientSettingsPacketV109(); } });
        registerV755C2S(0x06, noOpFactory); // CommandSuggestion
        // 0x07: ConfirmTransaction REMOVED — ClickWindowButton now at 0x07
        registerV755C2S(0x07, noOpFactory); // ClickWindowButton (was 0x08)
        registerV755C2S(0x08, noOpFactory); // WindowClick (was 0x09)
        registerV755C2S(0x09, new PacketFactory() { public Packet create() { return new CloseWindowPacket(); } });
        registerV755C2S(0x0A, new PacketFactory() { public Packet create() { return new NettyPluginMessagePacketV47(); } });
        registerV755C2S(0x0B, noOpFactory); // EditBook (was 0x0C)
        registerV755C2S(0x0C, noOpFactory); // EntityTagQuery (was 0x0D)
        registerV755C2S(0x0D, new PacketFactory() { public Packet create() { return new NettyUseEntityPacketV47(); } });
        registerV755C2S(0x0E, noOpFactory); // GenerateJigsaw (was 0x0F)
        registerV755C2S(0x0F, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });
        registerV755C2S(0x10, noOpFactory); // LockDifficulty (was 0x11)
        registerV755C2S(0x11, new PacketFactory() { public Packet create() { return new PlayerPositionPacketV47(); } });
        registerV755C2S(0x12, new PacketFactory() { public Packet create() { return new PlayerPositionAndLookC2SPacketV47(); } });
        registerV755C2S(0x13, new PacketFactory() { public Packet create() { return new PlayerLookPacket(); } });
        registerV755C2S(0x14, new PacketFactory() { public Packet create() { return new PlayerOnGroundPacket(); } });
        registerV755C2S(0x15, noOpFactory); // VehicleMove
        registerV755C2S(0x16, noOpFactory); // PaddleBoat
        registerV755C2S(0x17, noOpFactory); // PickItem
        registerV755C2S(0x18, noOpFactory); // PlaceRecipe
        registerV755C2S(0x19, new PacketFactory() { public Packet create() { return new PlayerAbilitiesPacketV735(); } });
        registerV755C2S(0x1A, new PacketFactory() { public Packet create() { return new PlayerDiggingPacketV477(); } });
        registerV755C2S(0x1B, new PacketFactory() { public Packet create() { return new NettyEntityActionPacketV47(); } });
        registerV755C2S(0x1C, new PacketFactory() { public Packet create() { return new NettySteerVehiclePacketV47(); } });
        registerV755C2S(0x1D, noOpFactory); // Pong (NEW in 1.17)
        registerV755C2S(0x1E, noOpFactory); // RecipeBookChangeSettings (was 0x1E)
        registerV755C2S(0x1F, noOpFactory); // RecipeBookSeenRecipe (was 0x1F)
        registerV755C2S(0x20, noOpFactory); // RenameItem (was 0x20)
        registerV755C2S(0x21, noOpFactory); // ResourcePack (was 0x21)
        registerV755C2S(0x22, noOpFactory); // SeenAdvancements (was 0x22)
        registerV755C2S(0x23, noOpFactory); // SelectTrade (was 0x23)
        registerV755C2S(0x24, noOpFactory); // SetBeacon (was 0x24)
        registerV755C2S(0x25, new PacketFactory() { public Packet create() { return new HoldingChangePacketBeta(); } });
        registerV755C2S(0x26, noOpFactory); // SetCommandBlock
        registerV755C2S(0x27, noOpFactory); // SetCommandMinecart
        registerV755C2S(0x28, noOpFactory); // CreativeSlot
        registerV755C2S(0x29, noOpFactory); // SetJigsawBlock
        registerV755C2S(0x2A, noOpFactory); // SetStructureBlock
        registerV755C2S(0x2B, new PacketFactory() { public Packet create() { return new NettyUpdateSignPacketV47(); } });
        registerV755C2S(0x2C, new PacketFactory() { public Packet create() { return new AnimationPacketV109(); } });
        registerV755C2S(0x2D, noOpFactory); // Spectate
        registerV755C2S(0x2E, new PacketFactory() { public Packet create() { return new NettyBlockPlacementPacketV477(); } });
        registerV755C2S(0x2F, new PacketFactory() { public Packet create() { return new UseItemPacketV109(); } });

        // === V755 (1.17) C2S reverse map entries (for bot encoder) ===
        registerV755C2SReverse(NettyChatC2SPacket.class, 0x03);
        registerV755C2SReverse(KeepAlivePacketV340.class, 0x0F);
        registerV755C2SReverse(PlayerDiggingPacketV477.class, 0x1A);
        registerV755C2SReverse(NettyBlockPlacementPacketV477.class, 0x2E);

        // === V756 (1.17.1) S2C overlay ===
        // SetSlot gained VarInt stateId before windowId.
        registerV756S2C(0x16, new PacketFactory() { public Packet create() { return new NettySetSlotPacketV756(); } });
        registerV756S2CReverse(NettySetSlotPacketV756.class, 0x16);
        // DestroyEntities reverted from single-entity to multi-entity format.
        registerV756S2C(0x3A, new PacketFactory() { public Packet create() { return new NettyDestroyEntitiesPacketV47(); } });
        registerV756S2CReverse(NettyDestroyEntitiesPacketV47.class, 0x3A);

        // === V757 (1.18) S2C overlay ===
        // MapChunk + UpdateLight combined into one packet.
        registerV757S2C(0x22, new PacketFactory() { public Packet create() { return new MapChunkPacketV757(); } });
        registerV757S2CReverse(MapChunkPacketV757.class, 0x22);
        // JoinGame gained simulationDistance.
        registerV757S2C(0x26, new PacketFactory() { public Packet create() { return new JoinGamePacketV757(); } });
        registerV757S2CReverse(JoinGamePacketV757.class, 0x26);

        // SET_SIMULATION_DISTANCE inserted at 0x57 shifts all S2C >= 0x57 by +1.
        // Forward registrations (bot decoder):
        registerV757S2C(0x59, new PacketFactory() { public Packet create() { return new NettyTimeUpdatePacket(); } });
        registerV757S2C(0x62, new PacketFactory() { public Packet create() { return new EntityTeleportPacketV109(); } });
        // Reverse registrations (encoder for sending to 1.18 clients):
        registerV757S2CReverse(NettyTimeUpdatePacket.class, 0x59);
        registerV757S2CReverse(EntityTeleportPacketV109.class, 0x62);
        registerV757S2CReverse(NettyEntityPropertiesPacketV755.class, 0x64);
        registerV757S2CReverse(UpdateRecipesPacketV393.class, 0x66);
        registerV757S2CReverse(UpdateTagsPacketV757.class, 0x67);

        // === V758 (1.18.2) S2C overlay ===
        // JoinGame: infiniburn fields gain '#' prefix.
        registerV758S2C(0x26, new PacketFactory() { public Packet create() { return new JoinGamePacketV758(); } });
        registerV758S2CReverse(JoinGamePacketV758.class, 0x26);
        // UpdateTags: new fall_damage_resetting block tag.
        registerV758S2CReverse(UpdateTagsPacketV758.class, 0x67);

        // === V759 (1.19) S2C reverse map overlay ===
        // Major reshuffle: 4 packets removed, 5 added. Chat system split (SystemChat + PlayerChat).
        registerV759S2CReverse(NettySpawnPlayerPacketV573.class, 0x02);
        registerV759S2CReverse(BlockChangedAckPacketV759.class, 0x05);
        registerV759S2CReverse(NettyBlockChangePacketV477.class, 0x09);
        registerV759S2CReverse(DeclareCommandsPacketV393.class, 0x0F);
        registerV759S2CReverse(NettyWindowItemsPacketV47.class, 0x11);
        registerV759S2CReverse(NettySetSlotPacketV756.class, 0x13);
        registerV759S2CReverse(NettyPluginMessageS2CPacketV393.class, 0x15);
        registerV759S2CReverse(NettyDisconnectPacket.class, 0x17);
        registerV759S2CReverse(UnloadChunkPacketV109.class, 0x1A);
        registerV759S2CReverse(NettyChangeGameStatePacket.class, 0x1B);
        registerV759S2CReverse(KeepAlivePacketV340.class, 0x1E);
        registerV759S2CReverse(MapChunkPacketV757.class, 0x1F);
        registerV759S2CReverse(JoinGamePacketV759.class, 0x23);
        registerV759S2CReverse(EntityRelativeMovePacketV109.class, 0x26);
        registerV759S2CReverse(EntityLookAndMovePacketV109.class, 0x27);
        registerV759S2CReverse(EntityLookPacketV47.class, 0x28);
        registerV759S2CReverse(PlayerAbilitiesPacketV73.class, 0x2F);
        registerV759S2CReverse(NettyPlayerListItemPacketV759.class, 0x34);
        registerV759S2CReverse(NettyPlayerPositionS2CPacketV755.class, 0x36);
        registerV759S2CReverse(NettyDestroyEntitiesPacketV47.class, 0x38);
        registerV759S2CReverse(SetChunkCacheCenterPacketV477.class, 0x48);
        registerV759S2CReverse(SetChunkCacheRadiusPacketV477.class, 0x49);
        registerV759S2CReverse(SpawnPositionPacketV755.class, 0x4A);
        registerV759S2CReverse(NettyTimeUpdatePacket.class, 0x59);
        registerV759S2CReverse(SystemChatPacketV759.class, 0x5F);
        registerV759S2CReverse(EntityTeleportPacketV109.class, 0x63);
        registerV759S2CReverse(NettyEntityPropertiesPacketV755.class, 0x65);
        registerV759S2CReverse(UpdateRecipesPacketV393.class, 0x67);
        registerV759S2CReverse(UpdateTagsPacketV759.class, 0x68);

        // V759 LOGIN state: gains empty property array
        REVERSE_V759.put(reverseKey(ConnectionState.LOGIN, PacketDirection.SERVER_TO_CLIENT,
                LoginSuccessPacketV759.class), 0x02);

        // V759 LOGIN state C2S: EncryptionResponse gains boolean hasVerifyToken
        REGISTRY_V759.put(key(ConnectionState.LOGIN, PacketDirection.CLIENT_TO_SERVER, 0x01),
                new PacketFactory() { public Packet create() { return new NettyEncryptionResponsePacketV759(); } });

        // === V759 (1.19) S2C forward entries (for bot decoder) ===
        // Shadow stale V755-V758 entries at IDs that changed meaning in V759.
        registerV759S2C(0x04, noOpFactory);  // V755: SpawnPlayer; V759: Statistics
        registerV759S2C(0x0C, noOpFactory);  // V755: BlockChange; V759: ChatPreview
        registerV759S2C(0x0F, noOpFactory);  // V755: Chat; V759: DeclareCommands
        registerV759S2C(0x14, noOpFactory);  // V755: WindowItems; V759: Cooldown
        registerV759S2C(0x16, noOpFactory);  // V756: SetSlot; V759: NamedSound
        registerV759S2C(0x1D, noOpFactory);  // V755: UnloadChunk; V759: WorldBorderInit
        registerV759S2C(0x21, noOpFactory);  // V755: KeepAlive; V759: SpawnParticle
        registerV759S2C(0x22, noOpFactory);  // V757: MapChunk; V759: UpdateLight
        registerV759S2C(0x25, noOpFactory);  // V755: UpdateLight; V759: TradeList
        registerV759S2C(0x29, noOpFactory);  // V755: EntityRelMove; V759: VehicleMove
        registerV759S2C(0x2A, noOpFactory);  // V755: EntityLookMove; V759: OpenBook
        registerV759S2C(0x2B, noOpFactory);  // V755: EntityLook; V759: OpenWindow
        registerV759S2C(0x3A, noOpFactory);  // V756: DestroyEntities; V759: ResourcePack
        registerV759S2C(0x49, noOpFactory);  // V755: ChunkCacheCenter; V759: ChunkCacheRadius
        registerV759S2C(0x62, noOpFactory);  // V757: EntityTeleport(0x62); V759: Collect
        registerV759S2C(0x64, noOpFactory);  // V757: EntityProperties(0x64); V759: Advancements
        registerV759S2C(0x66, noOpFactory);  // V757: UpdateRecipes(0x66); V759: EntityEffect
        // Register active V759 S2C forward entries for bot decoder
        registerV759S2C(0x02, new PacketFactory() { public Packet create() { return new NettySpawnPlayerPacketV573(); } });
        registerV759S2C(0x09, new PacketFactory() { public Packet create() { return new NettyBlockChangePacketV477(); } });
        registerV759S2C(0x11, new PacketFactory() { public Packet create() { return new NettyWindowItemsPacketV47(); } });
        registerV759S2C(0x13, new PacketFactory() { public Packet create() { return new NettySetSlotPacketV756(); } });
        registerV759S2C(0x15, noOpFactory); // PluginMessage (bot doesn't need to decode)
        registerV759S2C(0x17, new PacketFactory() { public Packet create() { return new NettyDisconnectPacket(); } });
        registerV759S2C(0x1A, new PacketFactory() { public Packet create() { return new UnloadChunkPacketV109(); } });
        registerV759S2C(0x1B, new PacketFactory() { public Packet create() { return new NettyChangeGameStatePacket(); } });
        registerV759S2C(0x1E, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });
        registerV759S2C(0x1F, new PacketFactory() { public Packet create() { return new MapChunkPacketV757(); } });
        registerV759S2C(0x23, new PacketFactory() { public Packet create() { return new JoinGamePacketV759(); } });
        registerV759S2C(0x26, new PacketFactory() { public Packet create() { return new EntityRelativeMovePacketV109(); } });
        registerV759S2C(0x27, new PacketFactory() { public Packet create() { return new EntityLookAndMovePacketV109(); } });
        registerV759S2C(0x28, new PacketFactory() { public Packet create() { return new EntityLookPacketV47(); } });
        registerV759S2C(0x2F, new PacketFactory() { public Packet create() { return new PlayerAbilitiesPacketV73(); } });
        registerV759S2C(0x36, new PacketFactory() { public Packet create() { return new NettyPlayerPositionS2CPacketV755(); } });
        registerV759S2C(0x38, new PacketFactory() { public Packet create() { return new NettyDestroyEntitiesPacketV47(); } });
        registerV759S2C(0x48, new PacketFactory() { public Packet create() { return new SetChunkCacheCenterPacketV477(); } });
        registerV759S2C(0x59, new PacketFactory() { public Packet create() { return new NettyTimeUpdatePacket(); } });
        registerV759S2C(0x63, new PacketFactory() { public Packet create() { return new EntityTeleportPacketV109(); } });
        registerV759S2C(0x65, new PacketFactory() { public Packet create() { return new NettyEntityPropertiesPacketV755(); } });
        registerV759S2C(0x67, new PacketFactory() { public Packet create() { return new UpdateRecipesPacketV393(); } });
        registerV759S2C(0x5F, noOpFactory); // SystemChat (bot doesn't need special decoding)
        registerV759S2C(0x61, noOpFactory);  // V755: EntityTeleport(0x61); V759: NbtQueryResponse

        // === V759 (1.19) C2S entries (complete — CHAT_COMMAND at 0x03 and CHAT_PREVIEW at 0x05 inserted) ===
        // 0x00-0x02 unchanged from V755, 0x03 new, 0x04 = V755 0x03, 0x05 new, 0x06+ = V755 0x04+ shifted +2.
        registerV759C2S(0x00, new PacketFactory() { public Packet create() { return new TeleportConfirmPacketV109(); } });
        registerV759C2S(0x01, noOpFactory); // BlockEntityTagQuery
        registerV759C2S(0x02, noOpFactory); // SetDifficulty
        registerV759C2S(0x03, new PacketFactory() { public Packet create() { return new ChatCommandC2SPacketV759(); } }); // NEW
        registerV759C2S(0x04, new PacketFactory() { public Packet create() { return new NettyChatC2SPacket(); } });
        registerV759C2S(0x05, noOpFactory); // ChatPreview (NEW)
        registerV759C2S(0x06, new PacketFactory() { public Packet create() { return new ClientCommandPacket(); } });
        registerV759C2S(0x07, new PacketFactory() { public Packet create() { return new NettyClientSettingsPacketV109(); } });
        registerV759C2S(0x08, noOpFactory); // CommandSuggestion
        registerV759C2S(0x09, noOpFactory); // ClickWindowButton
        registerV759C2S(0x0A, noOpFactory); // WindowClick
        registerV759C2S(0x0B, new PacketFactory() { public Packet create() { return new CloseWindowPacket(); } });
        registerV759C2S(0x0C, new PacketFactory() { public Packet create() { return new NettyPluginMessagePacketV47(); } });
        registerV759C2S(0x0D, noOpFactory); // EditBook
        registerV759C2S(0x0E, noOpFactory); // EntityTagQuery
        registerV759C2S(0x0F, new PacketFactory() { public Packet create() { return new NettyUseEntityPacketV47(); } });
        registerV759C2S(0x10, noOpFactory); // GenerateJigsaw
        registerV759C2S(0x11, new PacketFactory() { public Packet create() { return new KeepAlivePacketV340(); } });
        registerV759C2S(0x12, noOpFactory); // LockDifficulty
        registerV759C2S(0x13, new PacketFactory() { public Packet create() { return new PlayerPositionPacketV47(); } });
        registerV759C2S(0x14, new PacketFactory() { public Packet create() { return new PlayerPositionAndLookC2SPacketV47(); } });
        registerV759C2S(0x15, new PacketFactory() { public Packet create() { return new PlayerLookPacket(); } });
        registerV759C2S(0x16, new PacketFactory() { public Packet create() { return new PlayerOnGroundPacket(); } });
        registerV759C2S(0x17, noOpFactory); // VehicleMove
        registerV759C2S(0x18, noOpFactory); // PaddleBoat
        registerV759C2S(0x19, noOpFactory); // PickItem
        registerV759C2S(0x1A, noOpFactory); // PlaceRecipe
        registerV759C2S(0x1B, new PacketFactory() { public Packet create() { return new PlayerAbilitiesPacketV735(); } });
        registerV759C2S(0x1C, new PacketFactory() { public Packet create() { return new PlayerDiggingPacketV759(); } });
        registerV759C2S(0x1D, new PacketFactory() { public Packet create() { return new NettyEntityActionPacketV47(); } });
        registerV759C2S(0x1E, new PacketFactory() { public Packet create() { return new NettySteerVehiclePacketV47(); } });
        registerV759C2S(0x1F, noOpFactory); // Pong
        registerV759C2S(0x20, noOpFactory); // RecipeBookChangeSettings
        registerV759C2S(0x21, noOpFactory); // RecipeBookSeenRecipe
        registerV759C2S(0x22, noOpFactory); // RenameItem
        registerV759C2S(0x23, noOpFactory); // ResourcePack
        registerV759C2S(0x24, noOpFactory); // SeenAdvancements
        registerV759C2S(0x25, noOpFactory); // SelectTrade
        registerV759C2S(0x26, noOpFactory); // SetBeacon
        registerV759C2S(0x27, new PacketFactory() { public Packet create() { return new HoldingChangePacketBeta(); } });
        registerV759C2S(0x28, noOpFactory); // SetCommandBlock
        registerV759C2S(0x29, noOpFactory); // SetCommandMinecart
        registerV759C2S(0x2A, noOpFactory); // CreativeSlot
        registerV759C2S(0x2B, noOpFactory); // SetJigsawBlock
        registerV759C2S(0x2C, noOpFactory); // SetStructureBlock
        registerV759C2S(0x2D, new PacketFactory() { public Packet create() { return new NettyUpdateSignPacketV47(); } });
        registerV759C2S(0x2E, new PacketFactory() { public Packet create() { return new AnimationPacketV109(); } });
        registerV759C2S(0x2F, noOpFactory); // Spectate
        registerV759C2S(0x30, new PacketFactory() { public Packet create() { return new NettyBlockPlacementPacketV759(); } });
        registerV759C2S(0x31, new PacketFactory() { public Packet create() { return new UseItemPacketV109(); } });

        // === V759 (1.19) C2S reverse map entries (for bot encoder) ===
        registerV759C2SReverse(NettyChatC2SPacket.class, 0x04);
        registerV759C2SReverse(KeepAlivePacketV340.class, 0x11);
        registerV759C2SReverse(PlayerDiggingPacketV759.class, 0x1C);
        registerV759C2SReverse(NettyBlockPlacementPacketV759.class, 0x30);
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

    private static void registerV735S2C(int packetId, PacketFactory factory) {
        REGISTRY_V735.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV735S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V735.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV735C2S(int packetId, PacketFactory factory) {
        REGISTRY_V735.put(key(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
    }

    private static void registerV735C2SReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V735.put(reverseKey(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, clazz), packetId);
    }

    private static void registerV751S2C(int packetId, PacketFactory factory) {
        REGISTRY_V751.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV751S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V751.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV751C2S(int packetId, PacketFactory factory) {
        REGISTRY_V751.put(key(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
    }

    private static void registerV751C2SReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V751.put(reverseKey(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, clazz), packetId);
    }

    private static void registerV755S2C(int packetId, PacketFactory factory) {
        REGISTRY_V755.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV755S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V755.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV755C2S(int packetId, PacketFactory factory) {
        REGISTRY_V755.put(key(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
    }

    private static void registerV755C2SReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V755.put(reverseKey(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, clazz), packetId);
    }

    private static void registerV756S2C(int packetId, PacketFactory factory) {
        REGISTRY_V756.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV756S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V756.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV757S2C(int packetId, PacketFactory factory) {
        REGISTRY_V757.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV757S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V757.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV758S2C(int packetId, PacketFactory factory) {
        REGISTRY_V758.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV758S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V758.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV759S2C(int packetId, PacketFactory factory) {
        REGISTRY_V759.put(key(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, packetId), factory);
    }

    private static void registerV759S2CReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V759.put(reverseKey(ConnectionState.PLAY, PacketDirection.SERVER_TO_CLIENT, clazz), packetId);
    }

    private static void registerV759C2S(int packetId, PacketFactory factory) {
        REGISTRY_V759.put(key(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, packetId), factory);
    }

    private static void registerV759C2SReverse(Class<? extends Packet> clazz, int packetId) {
        REVERSE_V759.put(reverseKey(ConnectionState.PLAY, PacketDirection.CLIENT_TO_SERVER, clazz), packetId);
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
        if (protocolVersion >= 759) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V759.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        if (protocolVersion >= 758) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V758.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        if (protocolVersion >= 757) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V757.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        if (protocolVersion >= 756) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V756.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        if (protocolVersion >= 755) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V755.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        if (protocolVersion >= 751) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V751.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
        if (protocolVersion >= 735) {
            String k = key(state, direction, packetId);
            PacketFactory factory = REGISTRY_V735.get(k);
            if (factory != null) {
                return factory.create();
            }
        }
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
        if (protocolVersion >= 759) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V759.get(rk);
            if (id != null) {
                return id;
            }
        }
        if (protocolVersion >= 758) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V758.get(rk);
            if (id != null) {
                return id;
            }
        }
        if (protocolVersion >= 757) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V757.get(rk);
            if (id != null) {
                return id;
            }
        }
        if (protocolVersion >= 756) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V756.get(rk);
            if (id != null) {
                return id;
            }
        }
        if (protocolVersion >= 755) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V755.get(rk);
            if (id != null) {
                return id;
            }
        }
        if (protocolVersion >= 751) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V751.get(rk);
            if (id != null) {
                return id;
            }
        }
        if (protocolVersion >= 735) {
            String rk = reverseKey(state, direction, clazz);
            Integer id = REVERSE_V735.get(rk);
            if (id != null) {
                return id;
            }
        }
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
