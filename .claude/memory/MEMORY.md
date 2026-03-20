# RDForward Project Memory

## Backwards Compatibility Principle
- ALL supported protocol versions must remain compatible when adding new versions. Check:
  1. **Server list ping**: Must mirror the client's protocol version (via MC|PingHost parsing for 1.6+). Never hardcode a single version.
  2. **Packet registrations**: New version-specific overrides must not break older versions (use `>= N` thresholds in the `betaVersions` loop).
  3. **Packet format changes**: Always create new packet classes for changed wire formats, never modify existing ones.
  4. **S2C packets**: New versions may REQUIRE packets that older versions didn't need. Send conditionally.
  5. **UpdateTags**: EVERY new version can add/rename/remove required tags. Client shows "Incomplete set of tags" if ANY required tag name is missing. ALWAYS check tag changes (block, item, fluid, entity_type, game_event) when adding a new protocol version. Use ViaVersion's TagRewriter + Minecraft wiki changelog as sources.
  6. **Built-in registry cross-references**: When sending registries as built-in (hasData=false), the client's built-in data may reference entries in OTHER registries. ALL referenced entries must exist. E.g., wolf_variant built-in data references biomes — those biomes must be in the biome registry or the client errors "Unbound values in registry". Check MC client logs (`latest.log`) for "Unbound values" errors.

## Feedback
- [feedback_backwards_compat.md](feedback_backwards_compat.md): Always test ALL supported protocol versions after changes — new version support must not break older versions

## Data File Protection
- [feedback_no_delete_world.md](feedback_no_delete_world.md): NEVER delete world/player save files without explicit permission

## Workflow Preferences
- [feedback_restart_server.md](feedback_restart_server.md): Rebuild and restart the server proactively after code changes

## E2E Test Rules
- NEVER run two Gradle test suites in parallel. They share the Gradle daemon and will conflict/kill each other. Always run sequentially.
- Do NOT re-run already-completed cross-version tests. Only run missing/untested pairs. Use temporary test classes that check for existing baselines and skip completed pairs. Only re-run cross tests when the user explicitly asks.

## Architecture
- Multi-module Gradle project: rd-protocol, rd-world, rd-server, rd-client, rd-game, rd-e2e-agent (Java 8), rd-e2e (Java 21)
- Server uses Netty with pipeline-based protocol handling
- Four client types: Nati-framed (4-byte length prefix), MC Alpha (raw TCP), MC 1.7.2+ Netty (VarInt-framed), Bedrock (UDP/RakNet)
- `ProtocolDetectionHandler` auto-detects TCP client type on first byte (0x02 = Alpha, 0xFE = server list ping, firstByte>0x02 && secondByte==0x00 = Netty 1.7.2+)
- Internal coordinate convention: eye-level Y (feet + 1.62), Classic yaw (0 = North)
- Build command: `./gradlew buildAll`

## Protocol Versions
- Enum order (chronological): RUBYDUNG(0), CLASSIC(7), ALPHA_1_0_15(13), ALPHA_1_0_16(14), ALPHA_1_0_17(1), ALPHA_1_1_0(2), ALPHA_1_2_0(3), ALPHA_1_2_2(4), ALPHA_1_2_3(5), ALPHA_1_2_5(6), BETA_1_0(7), BETA_1_2(8), BETA_1_3(9), BETA_1_4(10), BETA_1_5(11), BETA_1_6(12), BETA_1_7(13), BETA_1_7_3(14), BETA_1_8(17), BETA_1_9_PRE5(21), RELEASE_1_0(22), RELEASE_1_1(23), RELEASE_1_2_1(28), RELEASE_1_2_4(29), RELEASE_1_3_1(39), RELEASE_1_4_2(47), RELEASE_1_4_4(49), RELEASE_1_4_6(51), RELEASE_1_5(60), RELEASE_1_5_2(61), RELEASE_1_6_1(73), RELEASE_1_6_2(74), RELEASE_1_6_4(78), RELEASE_1_7_2(4), RELEASE_1_7_6(5), RELEASE_1_8(47), RELEASE_1_9(107), RELEASE_1_9_1(108), RELEASE_1_9_2(109), RELEASE_1_9_4(110), RELEASE_1_12(335), RELEASE_1_12_1(338), RELEASE_1_12_2(340), RELEASE_1_13(393), RELEASE_1_14(477), RELEASE_1_15(573), RELEASE_1_16(735), RELEASE_1_16_2(751), RELEASE_1_17(755), RELEASE_1_17_1(756), RELEASE_1_18(757), RELEASE_1_18_2(758), RELEASE_1_19_1(760), RELEASE_1_19_3(761), RELEASE_1_19_4(762), RELEASE_1_20(763), RELEASE_1_20_2(764), RELEASE_1_20_3(765), RELEASE_1_20_5(766), RELEASE_1_21(767), RELEASE_1_21_2(768), RELEASE_1_21_4(769), RELEASE_1_21_5(770), RELEASE_1_21_6(771), RELEASE_1_21_7(772), RELEASE_1_21_9(773), BEDROCK(924)
- Protocol numbers are NON-MONOTONIC. `isAtLeast()` uses `sortOrder` field (chronological), not `versionNumber`.
- Complete Alpha protocol map: v14=1.0.16, v13=1.0.15, v12=1.0.13-14, v10=1.0.4-11, v1=1.0.17, v2=1.1.x, v3=1.2.0-1.2.1_01, v4=1.2.2, v5=1.2.3, v6=1.2.3-1.2.6

## Protocol Detail Files
- **Alpha protocols (v1-v6, v10-v14)**: See sections below
- **Beta protocols (v7-v17)**: See `memory/beta-protocol.md`
- **Beta E2E field mappings (v7-v14)**: See `memory/beta-e2e-mappings.md`
- **Pre-Netty Release protocols (v21-v78)**: See `memory/pre-netty-release-protocol.md`
- **Netty protocols (v4, v5, v47, v107-v110, v393, v477, v573, v735, v766, v767, v768, v769, v770)**: See `memory/netty-protocol.md`
- **Bedrock protocol**: See sections below + `memory/bedrock-chunks.md`
- **MCPE 0.7.x (protocol 11)**: See `memory/mcpe-protocol.md`

## Client JAR Sources
- **Mojang CDN**: Primary source. URLs from `https://piston-meta.mojang.com/mc/game/version_manifest_v2.json` -> version JSON -> downloads.client.url
- **Minecraft Wiki**: Each version page has client+server JAR download links. Example: `https://minecraft.wiki/w/Java_Edition_Beta_1.7.3`. Useful for versions not in MultiMC or for server JARs.
- **MultiMC local cache**: `/mnt/c/Users/Riman/Downloads/MultiMC/libraries/com/mojang/minecraft/<version>/minecraft-<version>-client.jar`
- **Decompiler**: CFR from `https://github.com/leibnitz27/cfr/releases` (download to `/tmp/cfr.jar`)

## Alpha Protocol Key Facts
- v1 (Alpha 1.0.17): Missing 0x05/0x06/0x08. Immediate per-placement replenishment (no batched tracker).
- v1-v2: `LoginC2SPacketV2`/`LoginS2CPacketV2` — no mapSeed/dimension fields.
- v3-v6: Same wire formats, share packet registrations.
- All Alpha versions share BlockPlacement wire format: `short itemId, int x, byte y, int z, byte dir` (12 bytes).
- `BlockPlacementData` interface unifies placement packet types.
- Alpha yaw 0 = South; Classic yaw 0 = North (+128 byte offset).
- Alpha C2S Y = feet; Alpha S2C Y = eyes; internal Y = eye-level.
- **Netty S2C PlayerPosition Y**: 1.7.x = eye-level (client subtracts 1.62f); 1.8+ = feet-level. Handler subtracts `PLAYER_EYE_HEIGHT` for 1.8+ clients.
- **Alpha eye height MUST use `(double) 1.62f`** (not `1.62d`).
- **Alpha S2C 0x0D field order**: x, y, stance, z (NO swap).

## Bedrock Support
- CloudburstMC Protocol library `bedrock-connection:3.0.0.Beta12-SNAPSHOT`, codec v924 (MC 1.26.0)
- `BedrockServerInitializer` + `RakChannelFactory.server(NioDatagramChannel.class)` for bootstrap
- `PlayerManager` uses `playersById` as primary storage (Bedrock players have no Netty channel)
- Block palette: 15845 entries from `block_palette.nbt`, sequential indices as runtime IDs
- **Palette varints MUST use zigzag encoding** (`VarInts.writeInt`), not unsigned.
- **RAK_HANDLE_PING must be false** for auto-response using `RAK_ADVERTISEMENT`.
- Pong format: `MCPE;motd;protocolVer;mcVer;playerCount;maxPlayers;serverGuid;subMotd;gameType;nintendoLimited;ipv4Port;ipv6Port;`
- `pongUpdater` wired via `ServerEvents.PLAYER_JOIN` and `PLAYER_LEAVE` listeners.

## Bedrock Position & Rotation
- MovePlayerPacket position = eye/head level. AddPlayerPacket/StartGamePacket/PlayerAuthInputPacket = feet level.
- MovePlayerPacket/AddPlayerPacket rotation = (yaw, pitch, headYaw). PlayerAuthInputPacket = (pitch, yaw, headYaw).
- Pitch must be signed degrees: -90 to 90.
- Creative block breaking: `PlayerActionType.DIMENSION_CHANGE_REQUEST_OR_CREATIVE_DESTROY_BLOCK`.
- AddPlayerPacket: Always set `motion=Vector3f.ZERO` and `hand=ItemData.AIR` (null NPEs).

## Alpha Inventory & Block Placement
- **AddToInventory (0x11)** for Alpha v1-v6. Beta 1.0+ uses SetSlot (0x67).
- Batched cobblestone replenishment with 1-second debounce timer. `trackedCobblestone` corrected by PlayerInventoryPacket (0x05).
- **BlockChangePacket confirmation to placer required** (Alpha client reverts after 80 ticks without it).
- **Body overlap check on placement** in all handlers (Alpha, Classic, Bedrock).
- Block placement bypasses tick queue to avoid double-broadcast.

## Alpha Chunk Skylight
- **MUST compute from height map** (`chunk.generateSkylightMap()`). Incorrect skyLight=15 everywhere causes StackOverflowError on Alpha client.

## Alpha Pre-1.2.0 Client Bugs
- **TimSort crash**: `-Djava.util.Arrays.useLegacyMergeSort=true`. Server kicks first-time pre-1.2.0 clients with this message.
- Decompiled JARs at `/mnt/c/Users/Riman/Downloads/mc-alpha-beta-craftbukkit-spigot/Alpha/` and `/mnt/c/Users/Riman/Downloads/MultiMC/libraries/com/mojang/minecraft/`. CFR at `/tmp/cfr.jar`.

## 1.13 Protocol Quirks
- **Global palette VarInt**: In 1.9-1.12, global palette mode writes `VarInt(0)` (palette length) which the client reads. In 1.13, the global palette `read(ByteBuf)` is a **no-op** — do NOT write the palette VarInt. Extra byte misaligns all section data reads.
- **Brand plugin message required**: 1.13 client NPEs without S2C `minecraft:brand` plugin message (packet 0x19). Channel name changed from `MC|Brand` to `minecraft:brand`.
- **Chunk bit-packing**: 1.13 uses spanning packing (same as 1.9-1.12). Non-spanning was introduced in 1.16. `bitsPerBlock=14` for global palette (registry >8192 states).

## 1.14 Protocol Quirks
- **Position encoding changed**: 1.8-1.13 = `x<<38 | y<<26 | z`. 1.14+ = `x<<38 | z<<12 | y`. Y and Z bit positions swapped. ALL packets using Position packed longs need V477 variants with `readPositionV477`/`writePositionV477`.
- **Block placement wire format changed**: Hand moved to first field (before Location), IsInsideBlock boolean added at end. Requires `NettyBlockPlacementPacketV477`.
- **Light data moved out of chunks**: Chunk sections no longer contain block/sky light. Light sent via separate `UpdateLightPacketV477`. Light bitmasks use 18 bits: bit 0 = section -1, bits 1-16 = sections 0-15, bit 17 = section 16.
- **Heightmaps NBT in chunk packet**: `MapChunkPacketV477` includes NBT compound with MOTION_BLOCKING + WORLD_SURFACE long arrays. 9 bits per entry, spanning-packed into 36 longs.
- **Section blockCount**: Each chunk section starts with `short blockCount` (non-air blocks) before bitsPerBlock.
- **JoinGame changed**: Difficulty removed, `VarInt viewDistance` added after levelType.
- **New mandatory packets**: SetChunkCacheCenter (0x40), SetChunkCacheRadius (0x41) must be sent during join before chunks.
- **Global palette**: Same as 1.13 (14-bit, no palette VarInt, spanning packing).
- **Biomes**: Still int[256] (same as 1.13). int[1024] is 1.15+.
- **Slot format**: Same as v404/1.13.2 (boolean+VarInt).
- **`NettyPluginMessageS2CPacketV393` has no no-arg constructor**: Use noOpFactory for bot decoder S2C forward entry.

## 1.15 Protocol Quirks
- **S2C ID shift**: New AcknowledgePlayerDigging (0x08) inserted; all S2C IDs from 0x08 onward shift +1 vs 1.14.
- **SpawnPlayer metadata removed**: No 0xFF terminator. `NettySpawnPlayerPacketV573` has no metadata at all.
- **JoinGame new fields**: `long hashedSeed` + `boolean enableRespawnScreen` added after reducedDebugInfo. `JoinGamePacketV573`.
- **Chunk biomes moved out of data array**: In 1.14, biomes (int[256]) were inside the data byte[]. In 1.15, biomes (int[1024]) are a **separate field** between heightmaps NBT and dataSize. Requires `MapChunkPacketV573` (not reusable from V477). `serializeForV573Protocol()` returns biomes separately.
- **AcknowledgePlayerDigging required**: Without it, 1.15 client reverts broken blocks. Sent from `handleDiggingV477()`.
- **C2S unchanged**: Same packet IDs and formats as v477.

## 1.16 Protocol Quirks
- **LoginSuccess UUID changed**: String UUID -> binary UUID (2 longs). Requires `LoginSuccessPacketV735`.
- **JoinGame rewritten**: NBT dimension codec + Identifier dimension + Identifier worldName.
  - In 1.16.0 (v735), "Dimension" is a **String/Identifier**, NOT NBT compound (NBT was 1.16.2+).
  - **No isHardcore boolean in v735**. Hardcore flag stays in gameMode bit 3 (separate isHardcore was added in 1.16.2/v751).
  - **maxPlayers is UNSIGNED_BYTE** in v735 (not VarInt). VarInt was 1.16.2+.
  - wiki.vg often shows the LATEST 1.16.x format. Always verify against ViaVersion source for exact v735 format.
- **S2C ID shift**: ADD_GLOBAL_ENTITY removed, SpawnPosition relocated. C2S: GENERATE_JIGSAW inserted at 0x0F.
- **Chat S2C gained UUID**: `NettyChatS2CPacketV735` adds sender UUID (2 longs). Zero UUID for system messages.
- **UpdateLight gained trustEdges**: boolean after chunkZ. `UpdateLightPacketV735`.
- **Chunk bit-packing**: Non-spanning (entries don't cross long boundaries). `serializeForV735Protocol()` + `buildHeightmapLongArrayNonSpanning()`.

## 1.16 Protocol Quirks
- **wiki.vg shows 1.16.2+ format, not 1.16.0**: Always use ViaVersion source as reference for v735 specifics.
- **JoinGame dimension codec**: 1.16.0 uses FLAT format (root compound with "dimension" ListTag), NOT 1.16.2+ registry format. No biome registry. Uses "shrunk" field instead of "coordinate_scale". Dimension field is String, not NBT compound.
- **No isHardcore boolean**: Hardcore is gameMode bit 3 (same as pre-1.16). maxPlayers is unsigned byte, not VarInt. Both changed in 1.16.2.
- **Global palette 15-bit**: 17,104 block states (exceeds 2^14). Client OVERRIDES bitsPerBlock in global mode with ceil(log2(registrySize)).
- **Non-spanning bit packing**: Both section data AND heightmaps use non-spanning in 1.16.0.
- **Block state IDs shifted**: nether_gold_ore inserted at state 72, soul_fire at 1952, plus ~5700 other new states. `BlockStateMapper.toV735BlockState()` handles this. IDs 0-71 unchanged.
- **Item IDs shifted**: Cobblestone item 12→14. `BlockStateMapper.toV735ItemId()`.
- **Attribute names namespaced**: `generic.movementSpeed` → `minecraft:generic.movement_speed`.
- **All tags must be bound**: Client crashes if ANY tag is referenced before appearing in UpdateTags. Send all 83 block, 55 item, 2 fluid, 5 entity type tags.
- **C2S PlayerAbilities simplified**: Just `byte flags` (no float speeds). `PlayerAbilitiesPacketV735`.
- **MapChunk ignoreOldLightData**: New boolean after fullChunk. `MapChunkPacketV735`.
- **UpdateLight trustEdges**: New boolean after chunkZ. `UpdateLightPacketV735`.
- **Chat S2C UUID sender**: 16-byte UUID appended. `NettyChatS2CPacketV735`.
- **LoginSuccess binary UUID**: 2 longs instead of String. `LoginSuccessPacketV735`.

## 1.16.2 Protocol Quirks
- **JoinGame rewritten again**: `isHardcore` is a separate boolean (not gameMode bit 3). `gameMode` is plain byte. `maxPlayers` is VarInt (was unsigned byte). Dimension is NBT compound (was String). Dimension codec uses REGISTRY format with `minecraft:dimension_type` + `minecraft:worldgen/biome` registries. Uses `coordinate_scale` (Double) instead of `shrunk` (Byte). Has `effects` field in dimension type.
- **MapChunk**: `ignoreOldLightData` boolean REMOVED. Biomes changed from raw `int[1024]` to VarInt-length-prefixed VarInt array.
- **S2C ID shift**: CHUNK_BLOCKS_UPDATE removed at V735 0x0F, SECTION_BLOCKS_UPDATE inserted at 0x3B. IDs 0x10-0x3B shift -1 from V735. IDs < 0x10 and >= 0x3C unchanged.
- **C2S ID shift**: RECIPE_BOOK_UPDATE split into RECIPE_BOOK_CHANGE_SETTINGS (0x1E) + RECIPE_BOOK_SEEN_RECIPE (0x1F). Old 0x1F+ shifts +1.
- **UpdateTags**: 3 new block tags added (`base_stone_nether`, `base_stone_overworld`, `mushroom_grow_block`; 83->86). `furnace_materials` removed, `stone_crafting_materials` added in item tags (55->55). Client shows "Incomplete set of tags" if ANY required tag name is missing.
- **Block state/item IDs unchanged from V735** for our basic block set.

## 1.17 Rendering / E2E Quirks
- **hasAllNeighbors BFS gate**: 1.17+ LevelRenderer BFS checks `hasAllNeighbors` for sections >24 blocks from camera. If ANY horizontal neighbor chunk is missing from ClientChunkCache, the section doesn't render. This causes diagonal/missing terrain when viewDistance is too small.
- **Spawn at chunk center**: Netty clients spawn at `((worldWidth/2) >> 4) * 16 + 8` (chunk center, not boundary). At chunk boundary (e.g. x=128.5), edge chunks are ~24.7 blocks away, triggering the neighbor check. At chunk center (x=136.5), all neighbor surface sections are within 24 blocks (bypassing the check).
- **E2E viewDistance=2**: testLWJGL3 uses `-De2e.viewDistance=2` (25 chunks). viewDistance=1 (9 chunks) leaves too little terrain visible in 1.17's shader renderer.
- **Mesa CPU contention**: Running 2 LWJGL3 clients simultaneously under Mesa llvmpipe can cause chunk meshes to not compile in time. May need single-fork or longer wait for reliable baselines.

## 1.17.1 Protocol Quirks
- **SetSlot gained VarInt stateId**: Prepended before windowId. `NettySetSlotPacketV756` (stateId=0 for server-initiated updates).
- **DestroyEntities reverted to multi-entity format**: `NettyDestroyEntitiesPacketV47` (VarInt count + VarInt[]) replaces `RemoveEntityPacketV755` (single VarInt).
- **No packet ID shifts** from 1.17.
- **Block state/item IDs unchanged from V755**.

## 1.18 Protocol Quirks
- **S2C ID shift**: SET_SIMULATION_DISTANCE inserted at 0x57; all S2C >= 0x57 shift +1 vs 1.17.1.
- **Chunk Data + Update Light merged**: Single packet at 0x22 (`MapChunkPacketV757`). No separate UpdateLight for initial chunk load.
- **No primaryBitMask**: All sections must be present (16 for height=256).
- **Biomes per-section**: Each section has its own biome PalettedContainer (not a top-level chunk field). Single-valued: `byte(0) + VarInt(1) + VarInt(0)`.
- **JoinGame gained simulationDistance**: `VarInt simulationDistance` after `viewDistance`. `JoinGamePacketV757`.
- **UpdateTags**: 14 new block tags (animals_spawnable_on, axolotls_spawnable_on, azalea_grows_on, azalea_root_replaceable, big_dripleaf_placeable, foxes_spawnable_on, goats_spawnable_on, mooshrooms_spawnable_on, parrots_spawnable_on, polar_bears_spawnable_on_in_frozen_ocean, rabbits_spawnable_on, replaceable_plants, terracotta, wolves_spawnable_on). 2 new item tags (dirt, terracotta). Renamed: `lava_pool_stone_replaceables` -> `lava_pool_stone_cannot_replace`. `UpdateTagsPacketV757`.
- **Block state/item IDs unchanged from V755**.
- **C2S unchanged**: Same packet IDs and formats as v756.
- **Login sequence ordering**: ALL Netty versions: send S2C PlayerPosition AFTER chunks. The client uses PlayerPosition as the signal to exit the loading screen and start physics. Sending before chunks causes intermittent "spawned inside ground" when chunk collision data isn't ready yet. `SetChunkCacheCenter` (sent before chunks) tells 1.14+ clients which chunk to prioritize.

## 1.19.3 Protocol Quirks
- **PlayerInfo split**: `PLAYER_INFO` (0x37) replaced by `PLAYER_INFO_REMOVE` (0x35) + `PLAYER_INFO_UPDATE` (0x36). Update uses bitmask-based actions (byte), not VarInt action enum.
- **UpdateEnabledFeatures mandatory**: New S2C packet 0x67 must be sent after JoinGame with `["minecraft:vanilla"]`.
- **4 S2C removed**: CHAT_PREVIEW, CUSTOM_SOUND, PLAYER_CHAT_HEADER, SET_DISPLAY_CHAT_PREVIEW. 2 added: DISGUISED_CHAT, UPDATE_ENABLED_FEATURES.
- **C2S**: CHAT_PREVIEW removed (0x06), CHAT_SESSION_UPDATE added (0x20). IDs 0x07-0x1F shift -1, 0x21+ unchanged.
- **Block state/item IDs unchanged from V759**.
- **JoinGame/SystemChat**: Reuse V760 classes; only packet IDs shifted. Registry handles remapping.
- **UpdateTags**: Block tags: +all_signs, +invalid_spawn_inside, -non_flammable_wood (128 total). Item tags: +creeper_igniters, +fence_gates, -overworld_natural_logs (59 total).
- **Encryption Response reverted**: 1.19.3 removed the `hasVerifyToken` boolean from EncryptionResponse, reverting to V47 format (VarInt sharedSecret + VarInt verifyToken). Use `NettyEncryptionResponsePacketV47` for V761 LOGIN state, NOT V759.
- **ChatCommandC2SPacketV759 compatible**: 1.19.3 sends fewer trailing fields but packet reads command + skips remaining.

## 1.19.4 Protocol Quirks
- **4 new S2C packets**: BUNDLE_DELIMITER(0x00), CHUNKS_BIOMES(0x0D), DAMAGE_EVENT(0x18), HURT_ANIMATION(0x21). S2C shifts: 0x00-0x0C +1, 0x0D-0x16 +2, 0x17-0x1E +3, 0x1F-0x6A +4.
- **C2S**: CHAT_SESSION_UPDATE relocated from 0x20 to 0x06. IDs 0x06-0x1F shift +1. IDs 0x00-0x05 and 0x21-0x32 unchanged.
- **PlayerPosition lost dismountVehicle**: `NettyPlayerPositionS2CPacketV762` removes the boolean. Used for 1.19.4+ clients.
- **JoinGame gained damage_type registry**: `JoinGamePacketV762` adds `minecraft:damage_type` (44 entries). Biome `precipitation` (String) replaced by `has_precipitation` (Byte).
- **UpdateTags gained damage_type**: 6 registries (was 5). Block tags: +smelts_to_glass (129). Item tags: +axes, hoes, pickaxes, shovels, swords, smelts_to_glass, tools (66). Entity type tags: +dismounts_underwater, fall_damage_immune (13). Damage type tags: 24 entries.
- **Block state/item IDs unchanged from V761**. Chunk format unchanged.
- **Encryption Response**: Same as V761 (V47 format).

## 1.20 Protocol Quirks
- **No S2C or C2S packet ID changes** from V762. All 111 S2C and all C2S IDs identical.
- **JoinGame gained portalCooldown**: `VarInt portalCooldown` appended after `lastDeathLocation` boolean. `JoinGamePacketV763` (value: 0).
- **UpdateTags changes**: Block tags: -replaceable_plants, +17 new (bamboo_blocks, cherry_logs, all_hanging_signs, ceiling_hanging_signs, wall_hanging_signs, combination_step_sound_blocks, enchantment_power_provider, enchantment_power_transmitter, maintains_farmland, replaceable, replaceable_by_trees, sniffer_diggable_block, sniffer_egg_hatch_boost, stone_buttons, sword_efficient, trail_ruins_replaceable, vibration_resonators). Net: 145. Item tags: +12 new (bamboo_blocks, cherry_logs, breaks_decorated_pots, decorated_pot_ingredients, decorated_pot_sherds, noteblock_top_instruments, sniffer_food, stone_buttons, trim_materials, trim_templates, trimmable_armor, villager_plantable_seeds). Net: 78.
- **trustEdges removed from chunk+light packet**: `MapChunkPacketV763` omits the `trustEdges` boolean. Using V757 (with trustEdges) causes "30792 bytes extra" error on 1.20 clients.
- **Block state/item IDs unchanged from V762**.
- **PlayerPosition/SetSlot/SystemChat/PlayerInfo/etc.**: All reuse V762 packet classes via version cascade.

## 1.20.2 Protocol Quirks
- **CONFIGURATION state**: New between LOGIN and PLAY. See `memory/netty-protocol.md`.
- **RegistryData (v764)**: SINGLE CompoundTag (network NBT) with ALL registries. NOT per-entry (that's v766+).
- **Network NBT (v764+)**: ALL NBT omits root name. 0x0A + children + 0x00 (NO writeShort(0)). Affects RegistryData, MapChunk heightmaps.
- **SpawnPlayer removed**: Generic SpawnEntity (entityType=122). Pitch before yaw.
- **Block state/item IDs unchanged from V763**.

## 1.20.3 Protocol Quirks
- **Text components as NBT**: SystemChat, Disconnect switched from JSON String to NBT Tag. Plain text: TAG_String (0x08 + short len + UTF-8). `SystemChatPacketV765`, `NettyDisconnectPacketV765`.
- **RegistryData SAME as v764**: Still single CompoundTag. Per-registry format is v766+, NOT v765. `RegistryDataPacketV765` exists but is unused.
- **GameEvent(13) REQUIRED**: "Start waiting for level chunks" must be sent after JoinGame. Without it, 1.20.3+ clients freeze on "Loading terrain" for ~30 seconds. `NettyChangeGameStatePacket.START_WAITING_CHUNKS`.
- **Item IDs shifted**: Tuff variants (13 new items) inserted early in registry. Cobblestone: 22→35, grass_block: 14→27, dirt: 15→28, oak_planks: 23→36. Block STATE IDs unchanged.
- **S2C shifts**: 0x42-0x44 new (RESET_SCORE, RESOURCE_PACK_POP/PUSH) -> +2 for 0x43-0x6B. 0x6E-0x6F new (TICKING_STATE/STEP) -> +4 for 0x6C+.
- **C2S shift**: CONTAINER_SLOT_STATE_CHANGED at 0x0F -> +1 for >= 0x0F.
- **Config shifts**: RESOURCE_PACK split into POP(0x06)+PUSH(0x07). UpdateEnabledFeatures 0x07->0x08, UpdateTags 0x08->0x09.
- **UpdateTags**: Entity type +6 (can_breathe_under_water, undead, zombies, can_turn_in_boats, deflects_arrows, deflects_tridents)=20. Damage type +1 (can_break_armor_stand)=25. Block/item unchanged.
- **JoinGame unchanged from V764**. Chunk format unchanged.

## 1.20.5 Protocol Quirks
- **RegistryData per-registry**: Each registry sent as separate packet: `String registryId + VarInt entryCount + [String entryId + boolean hasData + Optional<CompoundTag> data]`. `RegistryDataPacketV766`. `createBuiltIn()` = hasData=false (client uses built-in data).
- **Custom dimension data causes CONFIG hang**: Client rejects custom dimension type NBT in v766. Use `createBuiltIn()` for dimension_type and biome, then pad chunk data to 24 sections.
- **Biome registry IDs are position-based**: In v766, entry position determines numeric ID (unlike v764 where each entry has explicit `id` field in NBT). Send the_void at index 0, plains at index 1 to match chunk biome palette value 1.
- **24-section chunks for built-in overworld**: Built-in overworld = minY=-64, height=384 (24 sections). Prepend 4 empty sections (Y -64 to -1), keep 16 existing (Y 0-255), append 4 empty (Y 256-319). Heightmap values +64. Light masks shift left by 4.
- **Entity type player=128**: 5 new entities (bogged=11, breeze=12, breeze_wind_charge=13, ominous_item_spawner=61, wind_charge=117) shifted player from 122→128. `NettySpawnEntityPacketV766`.
- **Item slot format count-first**: `VarInt count + VarInt itemId` (if count>0). No boolean present. `NettySetSlotPacketV766`.
- **Entity attributes VarInt ID**: Attribute keys changed from String to VarInt registry ID. `MOVEMENT_SPEED = 17`. `NettyEntityPropertiesPacketV766`.
- **EncryptionRequest gained cookie**: boolean(false) at end. `NettyEncryptionRequestPacketV766`.
- **LoginSuccess gained strictErrorHandling**: boolean at end. `LoginSuccessPacketV766`.
- **SelectKnownPacks round-trip**: After LoginAcknowledged, server sends SelectKnownPacksS2CPacket(minecraft:core v1.20.5), client replies with SelectKnownPacksC2SPacket, then server sends RegistryData + UpdateEnabledFeatures + UpdateTags + ConfigFinish.
- **JoinGame dimensionType=VarInt(0)**: Was String. `enforcesSecureChat` boolean added. `JoinGamePacketV766`.
- **UpdateTags**: 148 block (+badlands_terracotta), 78 item (axolotl_tempt_items→axolotl_food, +enchantable/mace, -tools), 20 entity_type, 25 damage_type, 2 fluid, 5 game_event. `UpdateTagsPacketV766`.

## 1.21 Protocol Quirks
- **ALL Play packet IDs identical to v766**: No S2C or C2S shifts. Only CONFIG-phase differences.
- **3 new synchronized registries**: painting_variant (30), enchantment (42), jukebox_song (19). All sent as createBuiltIn(). Without them, client hangs in CONFIG.
- **Biome registry must include wolf variant biomes**: Built-in wolf_variant data references 6 biomes (forest, grove, old_growth_pine_taiga, old_growth_spruce_taiga, snowy_taiga, taiga). Without them: "Unbound values in registry minecraft:worldgen/biome". Add after the_void(0) and plains(1).
- **Trim patterns**: +bolt, +flow (18 total). `createTrimPatternV767()`.
- **Damage type registry**: +minecraft:campfire (45 total). `createDamageTypeV767()`.
- **Wolf variants**: 9 built-in (pale, ashen, black, chestnut, rusty, snowy, spotted, striped, woods). `createWolfVariantV767()`.
- **UpdateTags**: 7 registries (+minecraft:enchantment with 22 tags). Block +blocks_wind_charge_explosions=149. Entity +4=24. Damage +wind_charge=26. `UpdateTagsPacketV767`.
- **SelectKnownPacks**: Offers 4 versions (1.20.5, 1.20.6, 1.21, 1.21.1).
- **JoinGame/chunk format/item IDs**: Unchanged from v766.
- **MC client logs**: `/mnt/c/Users/Riman/Downloads/MultiMC/instances/<version>/.minecraft/logs/latest.log`. Check for "Unbound values" and "Failed to load registries" errors.

## Forward Compatibility & Registry Updates
- **Block IDs 1-255** valid in pre-1.13. `BlockRegistry.MAX_BLOCK_ID = 255` covers through 1.12.2.
- **1.13 (The Flattening)**: Implemented via `BlockStateMapper` — maps legacy block IDs (0-255, meta=0) to global block state IDs. Chunk palette uses 14-bit global mode. Biomes changed from byte[256] to int[256].
- **1.13.2 (v404)**: Item slot format changed to `boolean present + VarInt itemId` (was `short itemId`, -1=empty). Implemented via `NettySetSlotPacketV404`. No packet ID changes from v393.
- When adding a new protocol version, review: new block types, entity types, item types, changed packet formats.

## README References Section
- The README has a "References & Acknowledgments" section. Update it when adding new dependencies or referencing open source projects.

## Automated Testing
- **rd-bot** (protocol-level headless bots): See `memory/rd-bot-plan.md`
- **rd-e2e** (visual regression with real clients): See `memory/e2e-testing.md`
- **Client JARs**: See `memory/client-jars.md`
- Multi-client support required: 2+ simultaneous bots for player visibility, block sync, and chat testing
- Alpha 1.2.6 official JAR uses single-letter ProGuard obfuscation, NOT SRG names. See `memory/e2e-testing.md`.
- Beta 1.8.1 decompiled mappings: See `memory/beta181-decompile.md`. Key diff from Alpha: no boolean[] pressedKeys (uses KeyBinding), inventory uses Container system, creative inventory class `on`.
- E2E supports creative mode (`creative=true` agent arg, `McTestAgent.isCreativeMode`). Beta 1.8 gets 1 cobblestone (not 64).

## Future Work
- **Online mode support**: Server currently only runs in offline mode. Need to add Mojang session server authentication (`sessionserver.mojang.com`) and a toggle (`onlineMode` setting). Add bot tests for both online and offline login flows.

## Key Debugging Patterns
- **Packet misalignment**: packet `read()` consumes wrong byte count -> all subsequent packets misaligned. Symptom: "unknown packet ID 0xNN".
- **Field order bugs**: Wrong field order but matching byte count -> aligned stream, garbage values. Add hex dumps to diagnose.
- **LoginC2SPacket self-adaptive**: Four branches based on `protocolVersion >= 28/17/3` and `forceMapSeed`.
- **When code review can't find a bug**: Add diagnostic logging to the server and ask the user for server logs. Don't spend too many rounds on pure code review — get empirical data.
- **Test debugging**: Run ONLY failing test(s), not the full suite. See `memory/debugging.md`.
