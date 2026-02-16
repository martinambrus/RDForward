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
        ProtocolVersion[] alphaVersions = {ProtocolVersion.ALPHA_1_0_17, ProtocolVersion.ALPHA_1_1_0, ProtocolVersion.ALPHA_1_2_0, ProtocolVersion.ALPHA_1_2_2, ProtocolVersion.ALPHA_1_2_3, ProtocolVersion.ALPHA_1_2_5, ProtocolVersion.ALPHA_1_0_15, ProtocolVersion.ALPHA_1_0_16, ProtocolVersion.BETA_1_0, ProtocolVersion.BETA_1_2, ProtocolVersion.BETA_1_3, ProtocolVersion.BETA_1_4, ProtocolVersion.BETA_1_5, ProtocolVersion.BETA_1_6, ProtocolVersion.BETA_1_7, ProtocolVersion.BETA_1_7_3, ProtocolVersion.BETA_1_8, ProtocolVersion.RELEASE_1_0, ProtocolVersion.RELEASE_1_1, ProtocolVersion.RELEASE_1_2_1, ProtocolVersion.RELEASE_1_2_4};

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

        // Override version-specific packets for Alpha v1/v2 (1.0.17-1.1.2_01).
        // These versions use a shorter Login packet (no mapSeed/dimension fields).
        register(ProtocolVersion.ALPHA_1_0_17, PacketDirection.CLIENT_TO_SERVER, 0x01, new PacketFactory() {
            public Packet create() { return new LoginC2SPacketV2(); }
        });
        register(ProtocolVersion.ALPHA_1_0_17, PacketDirection.SERVER_TO_CLIENT, 0x01, new PacketFactory() {
            public Packet create() { return new LoginS2CPacketV2(); }
        });
        register(ProtocolVersion.ALPHA_1_1_0, PacketDirection.CLIENT_TO_SERVER, 0x01, new PacketFactory() {
            public Packet create() { return new LoginC2SPacketV2(); }
        });
        register(ProtocolVersion.ALPHA_1_1_0, PacketDirection.SERVER_TO_CLIENT, 0x01, new PacketFactory() {
            public Packet create() { return new LoginS2CPacketV2(); }
        });

        // Pre-rewrite Alpha (v10-v14) uses identical wire formats to post-rewrite
        // (v1-v6) for all packets including 0x0F (BlockPlacement) and 0x15
        // (PickupSpawn). No version-specific overrides needed.

        // === Beta + Release overrides (v7+) ===
        // Beta changed several packet wire formats and added new packets.
        // Release 1.0.0 (v22) shares the same base registrations as Beta, with
        // version-specific overrides for item slot format (NBT tags).
        ProtocolVersion[] betaVersions = {ProtocolVersion.BETA_1_0, ProtocolVersion.BETA_1_2, ProtocolVersion.BETA_1_3, ProtocolVersion.BETA_1_4, ProtocolVersion.BETA_1_5, ProtocolVersion.BETA_1_6, ProtocolVersion.BETA_1_7, ProtocolVersion.BETA_1_7_3, ProtocolVersion.BETA_1_8, ProtocolVersion.RELEASE_1_0, ProtocolVersion.RELEASE_1_1, ProtocolVersion.RELEASE_1_2_1, ProtocolVersion.RELEASE_1_2_4};
        for (ProtocolVersion betaV : betaVersions) {
            // Override C2S packets that changed format:
            register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x0F, new PacketFactory() {
                public Packet create() { return new PlayerBlockPlacementPacketBeta(); }
            });
            register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x10, new PacketFactory() {
                public Packet create() { return new HoldingChangePacketBeta(); }
            });
            // 0x05 C2S: Beta replaced PlayerInventory with EntityEquipment (S2C only).
            // Unregister Alpha's C2S PlayerInventory for Beta — clients don't send it.
            REGISTRY.remove(registryKey(betaV, PacketDirection.CLIENT_TO_SERVER, 0x05));
            // 0x11 S2C: Beta removed AddToInventory — uses SetSlot (0x67) instead.
            REGISTRY.remove(registryKey(betaV, PacketDirection.SERVER_TO_CLIENT, 0x11));
            // 0x15 C2S: Beta clients use PlayerDigging status=4 for Q-drops, not
            // PickupSpawn. Remove C2S registration because PickupSpawn has damage
            // in the MIDDLE of the packet (not at the end), so the phantom KeepAlive
            // trick doesn't work for the byte→short format change.
            REGISTRY.remove(registryKey(betaV, PacketDirection.CLIENT_TO_SERVER, 0x15));

            // New C2S packets in Beta
            register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x07, new PacketFactory() {
                public Packet create() { return new UseEntityPacket(); }
            });
            register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x09, new PacketFactory() {
                public Packet create() { return new RespawnPacket(); }
            });
            register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x65, new PacketFactory() {
                public Packet create() { return new CloseWindowPacket(); }
            });
            register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x66, new PacketFactory() {
                public Packet create() { return new WindowClickPacket(); }
            });
            register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x6A, new PacketFactory() {
                public Packet create() { return new ConfirmTransactionPacket(); }
            });
            register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x82, new PacketFactory() {
                public Packet create() { return new UpdateSignPacket(); }
            });
            // 0x13 Entity Action (crouch/uncrouch/leave bed)
            register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x13, new PacketFactory() {
                public Packet create() { return new EntityActionPacket(); }
            });
            // 0x1B Input/Steer Vehicle (silently consumed)
            register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x1B, new PacketFactory() {
                public Packet create() { return new InputPacket(); }
            });

            // New S2C packets in Beta
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x05, new PacketFactory() {
                public Packet create() { return new EntityEquipmentPacket(); }
            });
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x18, new PacketFactory() {
                public Packet create() { return new SpawnMobPacket(); }
            });
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x1C, new PacketFactory() {
                public Packet create() { return new EntityVelocityPacket(); }
            });
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x26, new PacketFactory() {
                public Packet create() { return new EntityStatusPacket(); }
            });
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x27, new PacketFactory() {
                public Packet create() { return new AttachEntityPacket(); }
            });
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x3C, new PacketFactory() {
                public Packet create() { return new ExplosionPacket(); }
            });
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x64, new PacketFactory() {
                public Packet create() { return new OpenWindowPacket(); }
            });
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x67, new PacketFactory() {
                public Packet create() { return new SetSlotPacket(); }
            });
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x68, new PacketFactory() {
                public Packet create() { return new WindowItemsPacket(); }
            });
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x69, new PacketFactory() {
                public Packet create() { return new WindowPropertyPacket(); }
            });
            // Bidirectional Beta packets
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x6A, new PacketFactory() {
                public Packet create() { return new ConfirmTransactionPacket(); }
            });
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x82, new PacketFactory() {
                public Packet create() { return new UpdateSignPacket(); }
            });
            register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x67, new PacketFactory() {
                public Packet create() { return new SetSlotPacket(); }
            });
            register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x65, new PacketFactory() {
                public Packet create() { return new CloseWindowPacket(); }
            });

            // Beta v10+ (Beta 1.4+): protocol number 10 clashes with pre-rewrite Alpha v10.
            // Override 0x01 C2S with forceMapSeed=true so Login reads mapSeed/dimension
            // instead of treating v10 as a pre-rewrite version with no mapSeed.
            if (betaV.getVersionNumber() >= 10) {
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x01, new PacketFactory() {
                    public Packet create() { return new LoginC2SPacket(true); }
                });
            }

            // Beta v11+ (Beta 1.5+): WindowClick added a 'shift' byte for shift-click
            // support and changed item damage from byte to short (no phantom KeepAlive
            // needed since v11 has a distinct protocol version with String16 encoding).
            if (betaV.getVersionNumber() >= 11) {
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x66, new PacketFactory() {
                    public Packet create() { return new WindowClickPacketBeta15(); }
                });
            }

            // Beta v12+ (Beta 1.6+): Respawn (0x09) C2S gained a dimension byte.
            // Earlier Beta versions used an empty payload.
            if (betaV.getVersionNumber() >= 12) {
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x09, new PacketFactory() {
                    public Packet create() { return new RespawnPacketV12(); }
                });
            }

            // Beta v17+ (Beta 1.8+): Major wire format changes.
            // BlockPlacement changed damage from byte to short (no phantom KeepAlive needed).
            if (betaV.getVersionNumber() >= 17) {
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x0F, new PacketFactory() {
                    public Packet create() { return new PlayerBlockPlacementPacketV17(); }
                });
                // KeepAlive changed from zero-payload to int keepAliveId
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x00, new PacketFactory() {
                    public Packet create() { return new KeepAlivePacketV17(); }
                });
                register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x00, new PacketFactory() {
                    public Packet create() { return new KeepAlivePacketV17(); }
                });
                // Login S2C gained gameMode + difficulty + worldHeight + maxPlayers
                register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x01, new PacketFactory() {
                    public Packet create() { return new LoginS2CPacketV17(); }
                });
                // Respawn expanded from dimension byte to dimension + difficulty +
                // gameMode + worldHeight + seed
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x09, new PacketFactory() {
                    public Packet create() { return new RespawnPacketV17(); }
                });
                // Creative Inventory Action (creative mode item selection).
                // Beta 1.8 uses 4 unconditional shorts (slot, itemId, count, damage).
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x6B, new PacketFactory() {
                    public Packet create() { return new CreativeSlotPacket(); }
                });
                // Note: 0xCA PlayerAbilities does not exist as a packet in Beta 1.8.
                // Abilities are derived client-side from the gameMode in Login/Respawn.

                // Player List Item (Tab list). Same format in Beta 1.8 and Release 1.0.0.
                register(betaV, PacketDirection.SERVER_TO_CLIENT, 0xC9, new PacketFactory() {
                    public Packet create() { return new PlayerListItemPacket(); }
                });
            }

            // Release v22+ (Release 1.0.0+): Item slots gained NBT tag data after
            // damage. All packets containing item data need v22 variants. CreativeSlot
            // changed from unconditional 4-short format to conditional item with NBT.
            // New C2S packets: EnchantItem (0x6C), PlayerAbilities (0xCA, now a real packet).
            if (betaV.getVersionNumber() >= 22) {
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x0F, new PacketFactory() {
                    public Packet create() { return new PlayerBlockPlacementPacketV22(); }
                });
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x66, new PacketFactory() {
                    public Packet create() { return new WindowClickPacketV22(); }
                });
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x6B, new PacketFactory() {
                    public Packet create() { return new CreativeSlotPacketV22(); }
                });
                register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x67, new PacketFactory() {
                    public Packet create() { return new SetSlotPacketV22(); }
                });
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x67, new PacketFactory() {
                    public Packet create() { return new SetSlotPacketV22(); }
                });
                register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x68, new PacketFactory() {
                    public Packet create() { return new WindowItemsPacketV22(); }
                });
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x6C, new PacketFactory() {
                    public Packet create() { return new EnchantItemPacket(); }
                });
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0xCA, new PacketFactory() {
                    public Packet create() { return new PlayerAbilitiesPacket(); }
                });
            }

            // Release v23+ (Release 1.1+): Login S2C gained levelType String16.
            // Respawn gained levelType String16 at end. New Custom Payload (0xFA).
            if (betaV.getVersionNumber() >= 23) {
                register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x01, new PacketFactory() {
                    public Packet create() { return new LoginS2CPacketV23(); }
                });
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x09, new PacketFactory() {
                    public Packet create() { return new RespawnPacketV23(); }
                });
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0xFA, new PacketFactory() {
                    public Packet create() { return new CustomPayloadPacket(); }
                });
            }

            // Release v28+ (Release 1.2.1+): Seed removed from Login/Respawn,
            // dimension byte→int. InputPacket (0x1B) removed. MapChunk changed
            // to section-based format.
            if (betaV.getVersionNumber() >= 28) {
                register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x01, new PacketFactory() {
                    public Packet create() { return new LoginS2CPacketV28(); }
                });
                register(betaV, PacketDirection.CLIENT_TO_SERVER, 0x09, new PacketFactory() {
                    public Packet create() { return new RespawnPacketV28(); }
                });
                register(betaV, PacketDirection.SERVER_TO_CLIENT, 0x33, new PacketFactory() {
                    public Packet create() { return new MapChunkPacketV28(); }
                });
                // InputPacket (0x1B) removed in 12w01a (between v23 and v28)
                REGISTRY.remove(registryKey(betaV, PacketDirection.CLIENT_TO_SERVER, 0x1B));
            }
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
