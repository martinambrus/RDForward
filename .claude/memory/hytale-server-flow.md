---
name: hytale-server-flow
description: Complete Hytale server connection flow from decompiled HytaleServer.jar — packet sequence, registry order, entity creation
type: project
---

## Hytale Server Location
`/mnt/c/Users/Riman/AppData/Roaming/Hytale/install/release/package/game/latest/Server/HytaleServer.jar`
Decompile with: `java -jar /tmp/cfr.jar <class-file>`

## Protocol Constants
- Protocol CRC: `1080406952` (we send `-1356075132` which is different — verify)
- Build number: `51`
- World height: `320` (ChunkUtil.HEIGHT), 10 sections of 32 blocks each

## Connection Flow (from decompiled server)

### Phase 1: Auth (InitialPacketHandler)
1. Client sends Connect (0) with protocolCrc, buildNumber, uuid, username, clientType, identityToken
2. Server validates protocolCrc == 1080406952
3. Auth mode: Server sends AuthGrant (11) -> Client sends AuthToken (12) -> Server sends ServerAuthToken (13)
4. Dev mode: Server sends ConnectAccept (14) directly
5. Password check if configured: PasswordResponse (15) -> PasswordAccepted (16)

### Phase 2: Setup (SetupPacketHandler.registered0)
6. Server fires PlayerSetupConnectEvent
7. Server sends WorldSettings (20): worldHeight=320 + requiredAssets from CommonAssetModule
8. Server sends ServerInfo (223): serverName + motd + maxPlayers + fallbackServer

### Phase 3: Asset Loading (SetupPacketHandler.handle(RequestAssets))
9. Client sends RequestAssets (23) with list of cached assets
10. Server fires SendCommonAssetsEvent async (common assets via AssetInit/Part/Finalize 24/25/26)
11. Server sends ALL registry packets via AssetRegistryLoader.sendAssets():
    - Iterates `AssetRegistry.getStoreMap().values()` (LinkedHashMap order)
    - Each HytaleAssetStore with a packetGenerator calls `generateInitPacket()` -> raw packet
    - Registries are sent as DIRECT raw packets (UpdateBlockTypes, UpdateItems, etc.) NOT wrapped in AssetInit/Part/Finalize

**Registry send order** (from AssetRegistryLoader registration order):
1. AmbienceFX
2. BlockBoundingBoxes (hitboxes, ID 41)
3. BlockSet (ID 46)
4. BlockSoundSet (ID 42)
5. ItemSoundSet (ID 43)
6. BlockParticleSet
7. BlockBreakingDecal (ID 45)
8. BlockType (ID 40)
9. Fluid
10. ItemPlayerAnimations
11. Environment (ID 61)
12. FluidFX
13. ItemCategory
14. FieldcraftCategory
15. ItemReticleConfig
16. Item (ID 54)
17. CraftingRecipe (ID 60)
18. ParticleSpawner
19. ParticleSystem
20. Trail
21. EntityEffect
22. ModelVFX
23. ResourceType
24. Weather (ID 47)
25. SoundEvent (ID 65)
26. SoundSet
27. AudioCategory
28. ReverbEffect
29. EqualizerEffect
30. ItemQuality
31. ProjectileConfig
32. BlockGroup (ID 78) — loaded AFTER BlockType+Item, uses string-keyed DefaultAssetMap
33. TagPattern

Plus module-registered:
- EntityStatType, EntityUIComponent, Interactions, RootInteractions, UnarmedInteractions
- HitboxCollisionConfig, RepulsionConfig

12. Server sends UpdateTranslations (64)
13. Server sends WorldLoadProgress (21): "Loading world...", 0/0
14. Server sends WorldLoadFinished (22) — triggers SettingUp -> Playing

### Phase 4: PlayerOptions -> Universe.addPlayer (SetupPacketHandler)
15. Client sends ViewRadius (32) — desired view radius
16. Client sends PlayerOptions (33) — player skin
17. Server validates skin, calls Universe.addPlayer():
    - Creates GamePacketHandler (replaces SetupPacketHandler)
    - Opens QUIC aux streams for Chunks + WorldMap
    - Creates player entity components
    - Model assigned from "Player" ModelAsset or custom skin
    - Fires PlayerConnectEvent
    - Server sends ServerTags (34) — AssetRegistry.getClientTags()
    - Calls World.addPlayer()

### Phase 5: World Join (World.onSetupPlayerJoining)
18. Assigns entity networkId
19. Server sends JoinWorld (104): clearWorld=true, fadeInOut=true, worldUuid
20. Sets queuePackets=true — subsequent packets buffered until flush
21. Waits for ClientReady (readyForChunks=true) — 10 second timeout

### Phase 6: Client Ready -> onFinishPlayerJoining
22. Client sends ClientReady (105): readyForChunks=true, readyForGameplay=false
23. Server sends (all queued, flushed together):
    - ViewRadius (32): maxViewRadius * 32
    - SetEntitySeed (160)
    - SetClientId (100): player's networkId
    - SetTimeDilation (30)
    - UpdateFeatures (31)
    - UpdateSunSettings (360)
    - UpdatePostFxSettings (361)
    - SetUpdateRate (29) — writeNoCache
24. Player Ref added to ECS store, triggers systems:

### Phase 7: ECS Systems
**PlayerSpawnedSystem** (first):
25. EntityUpdates (161) for local player with components:
    - ModelUpdate (type 3) — from "Player" ModelAsset (server-side, no registry)
    - PlayerSkinUpdate — skin data
    - EquipmentUpdate — armor + hands
    - TransformUpdate (type 9) — position + orientation
    - EntityEffectsUpdate, EntityStatsUpdate
    - Optional: InteractableUpdate, NameplateUpdate, PredictionUpdate, etc.

**PlayerAddedSystem** (after PlayerSpawnedSystem):
26. BuilderToolsSetSoundSet (418)
27. UpdatePlayerInventory (170)
28. SetActiveSlot (177) x3 — hotbar, utility, tools
29. SetBlockPlacementOverride (103)
30. tryFlush() — sends all queued packets

Then on world thread:
31. UpdateTimeSettings (145) + UpdateTime (146)
32. UpdateWorldMapSettings (240)

### Phase 8: Chunk Streaming
- ChunkTracker detects player needs chunks, sends on Chunks QUIC stream

### Phase 9: Gameplay Ready
33. Client sends second ClientReady (105): readyForGameplay=true
34. Server fires PlayerReadyEvent

## Critical Server-Side Classes
- `InitialPacketHandler` — auth flow
- `SetupPacketHandler` — WorldSettings, registries, WorldLoadFinished
- `GamePacketHandler` — gameplay packets
- `AssetRegistryLoader` — registers ALL asset stores + packet generators
- `HytaleAssetStore.sendAssets()` — generates init packet, sends raw
- `World.onSetupPlayerJoining()` — JoinWorld + queue mode
- `World.onFinishPlayerJoining()` — state packets + entity creation
- `ModelSystems.PlayerConnect` — assigns "Player" ModelAsset or custom model
- `PlayerSpawnedSystem` — sends EntityUpdates with full player data
- `PlayerAddedSystem` — inventory, active slots, builder tools

## Reserved Block Types (BlockType.java static init)
- EMPTY (ID 0): key="Empty", drawType=Empty, material=Empty, opacity=Transparent, group="Air"
- UNKNOWN (ID 1): key="Unknown", drawType=Cube, material=Solid, unknown=true
- DEBUG_CUBE (ID 2): key="Debug_Cube", drawType=Cube, material=Solid
- DEBUG_MODEL (ID 3): key="Debug_Model", drawType=Model, material=Empty

Custom game blocks start at ID 4+. Server calls `toPacket()` which sets `packet.name = this.id` for ALL blocks.

## DrawType Enum
0=Empty, 1=GizmoCube, 2=Cube, 3=Model, 4=CubeWithModel

## Server Always Sets These on BlockType Packets
- `shaderEffect`: DEFAULT_SHADER_EFFECTS = {ShaderType.None} if null
- `cubeTextures`: UNKNOWN_BLOCK_TEXTURES if null (all 6 faces = "BlockTextures/Unknown.png")
- `modelTexture`: UNKNOWN_CUSTOM_MODEL_TEXTURE if null ("Blocks/_Debug/Texture.png")
- `name`: always set to `this.id` (block key string)

## Two ClientReady Packets
1. readyForChunks=true, readyForGameplay=false — unblocks server joining
2. readyForGameplay=true — fires PlayerReadyEvent (fully connected)

## Registries WITHOUT PacketGenerators (server-only, NOT sent to client)
BlockMigration, ItemDropList, WordList, ItemToolSpec, PortalType, ModelAsset, Projectile, GameModeType, GameplayConfig, ResponseCurve, DamageCause, CameraEffect, BlockTypeListAsset, PrefabListAsset, BuilderToolItemReferenceAsset
