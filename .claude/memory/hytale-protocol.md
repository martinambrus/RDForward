---
name: hytale-protocol
description: Hytale QUIC protocol implementation details — critical findings from debugging client crashes
type: project
---

## Hytale Protocol Critical Findings

### WorldSettings worldHeight
WorldSettings (ID 20) must send worldHeight >= 256 (real server uses 320). The client allocates rendering arrays based on worldHeight. Sending worldHeight=64 (our Classic world height) causes IndexOutOfRangeException at +0xb36284 in the render loop ~300ms after SettingUp->Playing transition. Fixed by hardcoding worldHeight=320.

**Why:** Renderer allocates Y-based arrays (light, shadow, etc.) using worldHeight/32. Too-small values underallocate, causing array OOB on first render frame.

**How to apply:** Always send worldHeight=320 in WorldSettings regardless of actual world height.

### Reserved Block Type IDs (from decompiled server)
Decompiled from `HytaleServer.jar` at `com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType`:

| ID | Key | DrawType | Material | Opacity | Notes |
|----|------|----------|----------|---------|-------|
| 0 | "Empty" | Empty (0) | Empty (0) | Transparent (2) | Air block, group="Air" |
| 1 | "Unknown" | Cube (2) | Solid (1) | Solid (0) | unknown=true |
| 2 | "Debug_Cube" | Cube (2) | Solid (1) | Solid (0) | |
| 3 | "Debug_Model" | Model (3) | Empty (0) | Solid (0) | |

Server sends `packet.name = this.id` for ALL block types (line 798 of server BlockType.java). The `name` field is variable field slot 1, controlled by `nullBits[1] & 1`.

Custom game blocks start at ID 4+ in the real server.

### DrawType Enum Values (from decompiled protocol)
From `com.hypixel.hytale.protocol.DrawType`:
- 0 = Empty (invisible)
- 1 = GizmoCube (NOT regular Cube!)
- 2 = Cube (standard solid block)
- 3 = Model (custom 3D model)
- 4 = CubeWithModel

**Critical:** We previously sent drawType=1 thinking it was Cube. It's actually GizmoCube. Real Cube = 2.

### BlockType Wire Format (from decompiled protocol)
From `com.hypixel.hytale.protocol.BlockType`:
- NULLABLE_BIT_FIELD_SIZE = 4
- FIXED_BLOCK_SIZE = 164
- VARIABLE_FIELD_COUNT = 25
- VARIABLE_BLOCK_START = 264 (4 nullBits + 160 fixed + 100 offset slots)

Fixed block layout (offsets from entry start, after nullBits):
- offset 4: unknown (byte)
- offset 5: drawType (byte, DrawType enum)
- offset 6: material (byte, BlockMaterial enum)
- offset 7: opacity (byte, Opacity enum)
- offset 8: hitbox (int LE)
- offset 12: interactionHitbox (int LE)
- offset 16: modelScale (float LE)
- offset 20: looping (byte)
- offset 21: maxSupportDistance (int LE)
- offset 25: blockSupportsRequiredFor (byte)
- offset 26: requiresAlphaBlending (byte)
- offset 27: cubeShadingMode (byte)
- offset 28: randomRotation (byte)
- offset 29: variantRotation (byte)
- offset 30: rotationYawPlacementOffset (byte)
- offset 31: blockSoundSetIndex (int LE)
- offset 35: ambientSoundEventIndex (int LE)
- offset 39: particleColor (3 bytes) — controlled by nullBits[0] & 1
- offset 42: light/ColorLight (4 bytes) — controlled by nullBits[0] & 2
- offset 46: tint (24 bytes) — controlled by nullBits[0] & 4
- offset 70: biomeTint (24 bytes) — controlled by nullBits[0] & 8
- offset 94: group (int LE)
- offset 98: movementSettings (42 bytes) — controlled by nullBits[0] & 0x10
- offset 140: flags (2 bytes) — controlled by nullBits[0] & 0x20
- offset 142: placementSettings (17 bytes) — controlled by nullBits[0] & 0x40
- offset 159: ignoreSupportWhenPlaced (byte)
- offset 160: transitionToTag (int LE)
- offset 164-263: 25 variable field offset slots (int LE each, -1 = null)

Variable field offset slots (relative to VBS=264):
- Slot 0: item (String) — nullBits[0] & 0x80
- Slot 1: name (String) — nullBits[1] & 1
- Slot 2: shaderEffect (ShaderType[]) — nullBits[1] & 2
- Slot 3: model (String) — nullBits[1] & 4
- Slot 4: modelTexture (ModelTexture[]) — nullBits[1] & 8
- Slot 5: modelAnimation (String) — nullBits[1] & 0x10
- (remaining slots for support, cubeTextures, etc.)

### UpdateBlockTypes Packet Wire Format
From `com.hypixel.hytale.protocol.packets.assets.UpdateBlockTypes`:
- PACKET_ID = 40
- IS_COMPRESSED = true
- FIXED_BLOCK_SIZE = 10: nullBits(1) + type(1) + maxId(4) + updateBlockTextures(1) + updateModelTextures(1) + updateModels(1) + updateMapGeometry(1)
- VARIABLE_FIELD_COUNT = 1 (the blockTypes map)
- Variable block: VarInt(count) + entries(key:int LE + BlockType struct)

### UpdateBlockTypes IS_COMPRESSED=true
Packet 40 is always Zstd-compressed. The client decompresses based on packet ID, not magic number. The frame format is: [4LE compressed_length] [4LE packet_id] [compressed_bytes].

### Client Log Path
`/mnt/c/Users/Riman/AppData/Roaming/Hytale/UserData/Logs/` — timestamped files, newest first. Check for crash details after failed connections.

### Hytale Server Location
`/mnt/c/Users/Riman/AppData/Roaming/Hytale/install/release/package/game/latest/Server/HytaleServer.jar` — decompile with CFR at `/tmp/cfr.jar` when protocol questions arise.

### Decompiled Protocol Library Location
`/tmp/hytale-decompiled/com/hypixel/hytale/protocol/` — decompiled from protocol JAR, contains packet definitions, BlockType, DrawType, etc.

### SettingUp->Playing Transition
After WorldLoadFinished (ID 22), client runs these Prepare steps:
PrepareItems -> PrepareItemAnimations -> PrepareInteractions -> PrepareWorldMap -> PrepareAmbienceFX -> PrepareRootInteractions -> PrepareParticles -> PrepareBlockOverlayAtlas -> PrepareEntitiesAtlas -> render loop starts.

Two Sentry events are consistently captured between PrepareBlockOverlayAtlas and PrepareEntitiesAtlas (purpose unknown, possibly expected).

### JoinWorld Requires Playing State
JoinWorld (ID 104) is ONLY accepted during the Playing connection stage. Sending it during SettingUp causes: "Received JoinWorld at SettingUp connection stage but expected it only during Playing." WorldLoadFinished (ID 22) must be sent BEFORE JoinWorld to trigger the SettingUp -> Playing transition first.

**How to apply:** Send order: registries -> WorldLoadFinished -> JoinWorld + state.

### Chunk Packets Must Be Deferred Until ClientReady
Chunk packets (131-134) go on the Chunks QUIC stream, which is NOT ordered relative to the Default stream. If sent immediately after JoinWorld, they can race ahead and arrive before the client processes JoinWorld (creating the world object), causing NullRef at +0x1c4e1b. Wait for ClientReady (ID 105) from the client before sending chunks -- this confirms JoinWorld was processed and the world object exists.

**How to apply:** Send chunks only in handleClientReady(), never in sendJoinSequence().

### Weather Registry and UpdateWeather Required
Weather registry (packet 47) MUST be sent with at least one entry -- client errors "We have not received the asset types of Weather" without it. The entry's fog field (NearFar) MUST be non-null -- renderer accesses weather.fog.near/far unconditionally. UpdateWeather (149, FBS=8: int LE weatherIndex + float LE transitionSeconds) sets the active weather. Tintmap (133) buffer must also be non-null -- client accesses it unconditionally.

### Texture References Use Hashes (Asset Delivery System)
Block type texture references (cubeTextures, modelTexture) are PATH strings (e.g. "BlockTextures/Unknown.png"), but the client's texture loader resolves paths to SHA-256 hashes via the CachedAssetsIndex. Assets are delivered via CommonAssetModule using AssetInit/AssetPart/AssetFinalize packets with SHA-256 hashes, stored in `CachedAssets/<first-2-chars>/<rest-of-hash>`. Example: "BlockTextures/Unknown.png" → hash `11c5f4d7cfa18a12e83caa3c01b64a2770bb5b45fa7fd448c1a51cebe52e3ea4`. Without asset delivery or a pre-populated CachedAssetsIndex, client logs "Block overlay texture not found: Unknown Asset () (hash: )".

**How to apply:** Either deliver texture assets via CommonAssetModule before registries, or ensure CachedAssetsIndex is populated from a previous session. Cannot just send path strings in block types without the asset data.

### Complete Registry Packet IDs (from decompiled protocol)
All Update* packets with IDs. Registries with packetGenerators (sent to client):
40 BlockTypes, 41 BlockHitboxes, 42 BlockSoundSets, 43 ItemSoundSets, 44 BlockParticleSets, 45 BlockBreakingDecals, 46 BlockSets, 47 Weathers, 48 Trails, 49 ParticleSystems, 50 ParticleSpawners, 51 EntityEffects, 52 ItemPlayerAnimations, 53 ModelVFXs, 54 Items, 55 ItemQualities, 56 ItemCategories, 57 ItemReticles, 58 FieldcraftCategories, 59 ResourceTypes, 60 Recipes, 61 Environments, 62 AmbienceFX, 63 FluidFX, 64 Translations, 65 SoundEvents, 66 Interactions, 67 RootInteractions, 68 UnarmedInteractions, 72 EntityStatTypes, 73 EntityUIComponents, 74 HitboxCollisionConfig, 75 RepulsionConfig, 76 ViewBobbing, 77 CameraShake, 78 BlockGroups, 79 SoundSets, 80 AudioCategories, 81 ReverbEffects, 82 EqualizerEffects, 83 Fluids, 84 TagPatterns, 85 ProjectileConfigs, 86 Emotes

Most FBS=2 VFC=1 registries are string-keyed dicts: nullBits(1) + type(1) + VarInt(count) + entries. UpdateEmotes (86) has FBS=6: adds maxId(4) between type and entries.

### Join Sequence Timing
Join sequence (JoinWorld + state + entities) MUST be sent immediately after WorldLoadFinished. Deferring it (even by 5s) causes client timeout: "connection handshake was cancelled due to the configured timeout of 00:00:10 seconds elapsing". The client's 10-second connection handshake timer starts at connect and expects the full flow to complete within that window.

### EntityUpdates Requirements
- Entity creation requires ModelUpdate (ComponentUpdateType 3) with assetId="player" + TransformUpdate (ComponentUpdateType 9)
- TransformUpdate alone does NOT create a usable entity (NullRef at +0x773e9d)
- Model struct: NBFS=2, FBS=51, VFC=12, VARIABLE_BLOCK_START=99
- Model nullBits[0] bit2 = assetId present; offset slot 0 points to assetId VarString
- Model nullBits[0] bit4 = texture present; offset slot 2. Server ALWAYS sets texture to "textures/Unknown.png" fallback
- EntityUpdates must be sent AFTER JoinWorld (entity system requires world to exist)
- Real server ALWAYS sends: ModelUpdate(3) + PlayerSkinUpdate(4) + EquipmentUpdate(7) + TransformUpdate(9). EntityEffectsUpdate(11) + EntityStatsUpdate(8) also sent.
- PlayerSkinUpdate(null skin): just byte 0x00. EquipmentUpdate(all null): 0x00 + 3×intLE(-1) = 13 bytes
