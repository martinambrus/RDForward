# Release 1.7.2 Protocol (Netty Rewrite, v4)

## Overview
- Release 1.7.2-1.7.5 = v4 (October 2013). Biggest wire format change in MC history.
- Protocol v4 clashes with Alpha 1.2.2 — safe because 1.7.2 detected by VarInt framing, not Handshake byte.
- `RELEASE_1_7_2(4, 33, Family.RELEASE, "Release 1.7.2 (v4)", 92)` in ProtocolVersion enum.

## Key Changes from v78 (1.6.4)
- **VarInt-framed packets**: Length-prefixed with VarInt, not raw byte stream or 4-byte Nati frames.
- **Connection states**: HANDSHAKING -> STATUS (server list) or LOGIN (encryption) -> PLAY.
- **All packet IDs remapped**: Completely new ID space per state.
- **Strings**: VarInt byte count + UTF-8 (was short char count + UTF-16BE).
- **Slot data NBT**: Same as pre-Netty: `short(-1)` for no NBT, `short(len) + byte[len]` for present. NOT `byte(0x00)` TAG_End as wiki.vg incorrectly suggests.
- **S2C Player Position**: Lost separate stance field. Y field is **eye-level** (posY), NOT feet. Client does `posY = Y`, `feetY = posY - 1.62f`. Format: x, eyeY, z, yaw, pitch, onGround.
- **Encryption**: Same RSA+AES/CFB8, but no empty S2C EncryptionKeyResponse. After cipher install, send LoginSuccessPacket directly.
- **Server list**: JSON status response instead of section-delimited string. New Status state with request/response/ping packets.
- **SpawnPlayer (0x0C) has NO properties list**: Format is `VarInt entityId, String uuid, String name, int x, int y, int z, byte yaw, byte pitch, short item, metadata`. The `VarInt propertyCount + [name, value, signature]` field was added in 1.7.6, NOT 1.7.2. Confirmed by ViaLegacy `r1_7_2_5Tor1_7_6_10` which inserts `VarInt(0)` when upconverting.

## Architecture
- Parallel Netty protocol path alongside existing pre-Netty path.
- `ProtocolDetectionHandler` detects VarInt clients: `firstByte > 0x02 && firstByte != 0xFE && secondByte == 0x00`.
- Pipeline: VarIntFrameDecoder("decoder") -> NettyPacketDecoder("packetDecoder") -> VarIntFrameEncoder("encoder") -> NettyPacketEncoder("packetEncoder") -> ClassicToNettyTranslator("nettyTranslator") -> NettyConnectionHandler("handler").
- `NettyPacketRegistry` state-based (not version-aware like PacketRegistry). Maps `(state, direction, packetId) <-> PacketFactory/Class`.
- `NettyPacketEncoder`/`NettyPacketDecoder` have `connectionState` field updated by handler via `setCodecState()` (which finds them by class, not name).
- Cipher handlers inserted as `addBefore("decoder", "decrypt", ...)` and `addBefore("encoder", "encrypt", ...)`.

## Login Flow
1. C2S NettyHandshakePacket (state=HANDSHAKING, nextState=2 for Login)
2. C2S LoginStartPacket (state=LOGIN)
3. S2C NettyEncryptionRequestPacket (serverId="", pubKey, verifyToken)
4. C2S NettyEncryptionResponsePacket (encrypted sharedSecret, encrypted verifyToken)
5. Verify token, decrypt shared secret, install AES/CFB8 ciphers
6. S2C LoginSuccessPacket (uuid, username)
7. Transition to PLAY state
8. S2C JoinGamePacket, PlayerAbilities, EntityProperties, SpawnPosition, PlayerPosition, chunks, etc.

## Packet Classes
All in `rd-protocol/.../packet/netty/` package.

### New classes (format differs from pre-Netty):
- `NettyHandshakePacket`, `StatusRequestPacket`, `StatusResponsePacket`, `StatusPingPacket`
- `LoginStartPacket`, `LoginDisconnectPacket`, `NettyEncryptionRequestPacket`, `NettyEncryptionResponsePacket`, `LoginSuccessPacket`
- `JoinGamePacket`, `NettyChatS2CPacket`, `NettyPlayerPositionS2CPacket`, `NettySpawnPlayerPacket`
- `NettyEntityPropertiesPacket`, `NettyBlockChangePacket`, `NettySetSlotPacket`, `NettyWindowItemsPacket`
- `NettyPlayerListItemPacket`, `NettyDisconnectPacket`, `NettyDestroyEntitiesPacket`
- `NettyChatC2SPacket`, `NettyUseEntityPacket`, `NettyBlockPlacementPacket`, `NettyEntityActionPacket`
- `NettyWindowClickPacket`, `NettyCreativeSlotPacket`, `NettyUpdateSignPacket`, `NettyTabCompletePacket`
- `NettyClientSettingsPacket`, `ClientCommandPacket`, `NettyPluginMessagePacket`, `NettySteerVehiclePacket`

### Reused classes (identical wire format, registered at new Netty IDs):
- S2C: KeepAlivePacketV17, SpawnPositionPacket, EntityRelativeMovePacket, EntityLookPacket, EntityLookAndMovePacket, EntityTeleportPacket, MapChunkPacketV39, PlayerAbilitiesPacketV73
- C2S: KeepAlivePacketV17, PlayerOnGroundPacket, PlayerPositionPacket, PlayerLookPacket, PlayerPositionAndLookC2SPacket, PlayerDiggingPacket, HoldingChangePacketBeta, AnimationPacket, CloseWindowPacket, ConfirmTransactionPacket, EnchantItemPacket, PlayerAbilitiesPacketV73

## Release 1.7.6 Protocol (v5)
- Release 1.7.6-1.7.10 = v5 (April 2014). Wire format nearly identical to v4.
- **SpawnPlayer (0x0C)**: Gained `VarInt propertyCount + [String name, String value, String signature]` between playerName and x. We write `VarInt(0)`.
- `NettySpawnPlayerPacketV5` class. Registered in `NettyPacketRegistry` reverse map only (S2C, never decoded).
- All C2S packets unchanged from v4. No new packet classes needed for C2S.
- `NettyConnectionHandler` stores `clientVersion` from Handshake, selects V5 SpawnPlayer for v5+ clients.
- `ClassicToNettyTranslator` has `clientVersion` field (set after login), uses V5 SpawnPlayer for v5+.
- Status response mirrors client version (protocol number + version name).
- `ProtocolDetectionHandler.pingVersionString()` maps v5 to "1.7.10".

## Server Handlers
- `NettyConnectionHandler`: manages 4 connection states, handles login with encryption, gameplay in creative mode. Version-aware: stores `clientVersion` from handshake for v4/v5 dispatch.
- `ClassicToNettyTranslator`: outbound handler converting Classic broadcast packets to Netty equivalents. Has `generateOfflineUuid()` for UUID v3 generation. Version-aware: `setClientVersion()` configures SpawnPlayer variant.

## McDataTypes Helpers
- `readVarIntString(ByteBuf)` / `writeVarIntString(ByteBuf, String)`: VarInt byte count + UTF-8
- `skipNettySlotData(ByteBuf)`: reads slot with short nbtLength (-1 = none, >0 = skip that many bytes)
- `writeEmptyNettySlot(ByteBuf)`: writes short(-1)
- `writeNettySlotItem(ByteBuf, int, int, int)`: short itemId + byte count + short damage + short(-1) no NBT
- `writePosition(ByteBuf, int x, int y, int z)`: packed long for 1.8 Position type
- `readPosition(ByteBuf)`: returns int[3] with sign extension
- `writeV47SlotItem(ByteBuf, int, int, int)`: short itemId + byte count + short damage + byte(0x00) TAG_End
- `writeEmptyV47Slot(ByteBuf)`: writes short(-1) (same as pre-1.8, but no-NBT marker is byte(0x00) not short(-1))
- `skipV47SlotData(ByteBuf)`: reads V47 slot, handles TAG_End (0x00) and TAG_Compound (0x0A) NBT

## Release 1.8 Protocol (Netty v47)

### Overview
- Release 1.8-1.8.9 = Netty v47 (September 2014). Major wire format overhaul.
- Protocol v47 clashes with pre-Netty Release 1.4.2. `NettyConnectionHandler.handleHandshake()` hardcodes `pv == 47 → RELEASE_1_8` since Netty handler never receives pre-Netty clients.
- `RELEASE_1_8(47, 35, Family.RELEASE, "Release 1.8 (v47)", 92)` in ProtocolVersion enum.

### Key Changes from v4/v5 (1.7.x)
- **Position packed long**: Separate x/y/z replaced by `((x & 0x3FFFFFF) << 38) | ((y & 0xFFF) << 26) | (z & 0x3FFFFFF)` in BlockChange, SpawnPosition, PlayerDigging, BlockPlacement, UpdateSign.
- **VarInt entity IDs**: int→VarInt in EntityRelativeMove, EntityLook, EntityLookAndMove, EntityTeleport, DestroyEntities, EntityProperties.
- **Item slot NBT**: `byte(0x00)` TAG_End for no NBT (was `short(-1)`). Affects all slot read/write.
- **Chunk format**: `ushort[4096]` blockStates per section (blockId<<4|meta, little-endian), no zlib (raw in packet, VarInt frame handles compression). `MapChunkPacketV47`. **Data ordering: ALL sections' block data, THEN all block light, THEN all sky light** — NOT interleaved per section.
- **C2S Position**: Removed stance double. `PlayerPositionPacketV47` (25 bytes), `PlayerPositionAndLookC2SPacketV47` (33 bytes).
- **S2C Position**: boolean onGround→byte flags (0=absolute). Y changed from eye-level to **feet-level**. `NettyPlayerPositionS2CPacketV47`.
- **SpawnPlayer**: Raw 128-bit UUID (2 longs), removed name+properties. `NettySpawnPlayerPacketV47`.
- **PlayerListItem**: Action-based format with UUID. `NettyPlayerListItemPacketV47`. Must send ADD_PLAYER before SpawnPlayer (client resolves name from tab list).
- **KeepAlive**: int→VarInt. `KeepAlivePacketV47`.
- **JoinGame**: Added boolean reducedDebugInfo (hardcoded false). `JoinGamePacketV47`.
- **Chat S2C**: Added byte position (0=chat). `NettyChatS2CPacketV47`.
- **Encryption**: Array length prefixes changed from short to VarInt. `NettyEncryptionRequestPacketV47`, `NettyEncryptionResponsePacketV47`.

### Version-Aware Registry
- `NettyPacketRegistry.REGISTRY_V47` overlay map for changed C2S packets.
- `createPacket(state, direction, packetId, protocolVersion)` checks V47 map first for protocolVersion >= 47.
- `NettyPacketDecoder` has `protocolVersion` field (default 4), set by handler after Handshake.

### Critical: Translator clientVersion Must Be Set Before Broadcasts
- `ClassicToNettyTranslator.setClientVersion()` MUST be called BEFORE `broadcastPlayerListAdd()` and `broadcastPlayerSpawn()` in `handleJoinGame()`. `broadcastPlayerListAdd` sends to ALL players (including the joining player). If the translator defaults to RELEASE_1_7_2, it emits v4-format `NettyPlayerListItemPacket` which the 1.8 client reads as action=username_length → AIOOBE.

### Unchanged Packets (no new classes needed)
- S2C: PlayerAbilities (0x39), Disconnect (0x40)
- C2S: Chat (0x01), PlayerOnGround (0x03), PlayerLook (0x05), HoldingChange (0x09), CloseWindow (0x0D), ConfirmTransaction (0x0F), EnchantItem (0x11), PlayerAbilities (0x13), ClientStatus (0x16)

## Release 1.9 Protocol (Netty v107-v110)

### Overview
- Release 1.9-1.9.4 = v107-v110 (February-May 2016). Biggest protocol change since the 1.7 Netty rewrite.
- ALL Play state packet IDs are renumbered (both C2S and S2C).
- Entity positions switched from fixed-point int (1/32 block) to double.
- Entity relative move deltas changed from byte (1/32 scale) to short (1/4096 scale).
- Entity metadata completely restructured (ubyte index + VarInt type, 0xFF terminator).
- Chunks use paletted block storage per section.
- New mandatory packets: TeleportConfirm (C2S), UnloadChunk (S2C), UseItem (C2S).
- LOGIN state packets unchanged from v47.

### Key Changes from v47 (1.8)
- **ALL Play packet IDs remapped**: Required version-aware encoder with `REVERSE_V109` map and `REGISTRY_V109` C2S overlay.
- **Entity coords**: int fixed-point → double in SpawnPlayer, EntityTeleport.
- **Relative move deltas**: byte (1/32 block) → short (1/4096 block). Multiply old deltas by 128.
- **SpawnPlayer**: Removed `short currentItem`. Metadata changed to 1.9 format (ubyte index + VarInt type + value, 0xFF terminator).
- **S2C PlayerPosition**: Added `VarInt teleportId` at end. Y remains **feet-level** (same as 1.8). Client MUST respond with TeleportConfirm.
- **BlockPlacement**: Removed held item slot data. Direction byte → VarInt face. Added VarInt hand. Cursor positions are **unsigned bytes** (3 × 1 byte), same as pre-1.9. Cursor positions became floats in a later version (1.11+), NOT 1.9.
- **Chunk format**: VarInt primaryBitMask (was ushort). Per-section paletted format: bitsPerBlock + palette + packed indices. Entries span across long boundaries (1.9-1.12 format). **Block entity count is version-specific**: v107-v109 have NO block entity field (packet ends after data); v110 (1.9.4+) appends VarInt blockEntityCount + NBT compounds. `MapChunkPacketV109` has `writeBlockEntityCount` flag; ChunkManager sets based on `isAtLeast(RELEASE_1_9_4)`. Confirmed by ViaVersion's ChunkType1_9_1 (no block entities) vs ChunkType1_9_3 (VarInt count + NBT array).
- **UnloadChunk**: New dedicated S2C packet. Replaces MapChunk-with-bitmask-0 for chunk unloading.
- **Animation C2S**: Gained `VarInt hand` (was empty in 1.8).
- **ClientSettings C2S**: chatMode byte → VarInt, added VarInt mainHand.
- **TabComplete C2S**: Added `boolean assumeCommand`.

### Version-Aware Encoder
- `NettyPacketEncoder` now has `protocolVersion` field (set by handler after Handshake).
- `encode()` calls `NettyPacketRegistry.getPacketId(state, direction, class, protocolVersion)`.
- For v107+, REVERSE_V109 is checked first; falls back to REVERSE for login-state packets.

### New Packet Classes (13 files)

**S2C (7):**
- `NettySpawnPlayerPacketV109` (0x05): double coords, 1.9 metadata
- `EntityTeleportPacketV109` (0x4A): double coords
- `EntityRelativeMovePacketV109` (0x25): short deltas (1/4096 scale)
- `EntityLookAndMovePacketV109` (0x26): short deltas + angles
- `NettyPlayerPositionS2CPacketV109` (0x2E): V47 + VarInt teleportId
- `MapChunkPacketV109` (0x20): paletted sections, VarInt bitmask
- `UnloadChunkPacketV109` (0x1D): int chunkX + int chunkZ

**C2S (6):**
- `TeleportConfirmPacketV109` (0x00): VarInt teleportId
- `NettyTabCompletePacketV109` (0x01): String + boolean + optional Position
- `NettyClientSettingsPacketV109` (0x04): VarInt chatMode, VarInt mainHand
- `AnimationPacketV109` (0x1A): VarInt hand
- `NettyBlockPlacementPacketV109` (0x1C): Position + VarInt face + VarInt hand + cursors
- `UseItemPacketV109` (0x1D): VarInt hand

### Reused V47 Packets (new IDs via REVERSE_V109)
- S2C: NettyBlockChangePacketV47 (0x0B), NettyChatS2CPacketV47 (0x0F), NettyWindowItemsPacketV47 (0x14), NettySetSlotPacketV47 (0x16), NettyDisconnectPacket (0x1A), KeepAlivePacketV47 (0x1F), JoinGamePacketV47 (0x23), EntityLookPacketV47 (0x27), PlayerAbilitiesPacketV73 (0x2B), NettyPlayerListItemPacketV47 (0x2D), NettyDestroyEntitiesPacketV47 (0x30), SpawnPositionPacketV47 (0x43), NettyEntityPropertiesPacketV47 (0x4B)
- C2S: NettyChatC2SPacket (0x02), ClientCommandPacket (0x03), ConfirmTransactionPacket (0x05), EnchantItemPacket (0x06), NettyWindowClickPacketV47 (0x07), CloseWindowPacket (0x08), NettyPluginMessagePacketV47 (0x09), NettyUseEntityPacketV47 (0x0A), KeepAlivePacketV47 (0x0B), PlayerPositionPacketV47 (0x0C), PlayerPositionAndLookC2SPacketV47 (0x0D), PlayerLookPacket (0x0E), PlayerOnGroundPacket (0x0F), PlayerAbilitiesPacketV73 (0x12), PlayerDiggingPacketV47 (0x13), NettyEntityActionPacketV47 (0x14), NettySteerVehiclePacketV47 (0x15), HoldingChangePacketBeta (0x17), NettyCreativeSlotPacketV47 (0x18), NettyUpdateSignPacketV47 (0x19)

### Paletted Chunk Serialization
- `AlphaChunk.serializeForV109Protocol()` returns `V109ChunkData(byte[] rawData, int primaryBitMask)`.
- **Uses global palette mode (bitsPerBlock=13)**: Raw global block state IDs packed directly in data array. No section palette.
- Section palette mode (bitsPerBlock=4-8) has an unidentified bug causing blocks to be invisible on 1.9 clients (no rendering, no collision). Global palette is a bandwidth-heavy but working workaround.
- Format per section: byte bitsPerBlock(13) + VarInt(0) empty palette + VarInt dataArrayLength + long[] dataArray (entries span across long boundaries, 1.9-1.12 format) + 2048 blockLight + 2048 skyLight.
- After all sections: 256 biome bytes (all plains=1).
- Block state ID = `(blockId << 4) | metadata`.

### Handler Notes
- **S2C PlayerPosition Y semantics**: 1.7.x = eye-level (client subtracts yOffset=1.62f); 1.8+ = feet-level (client uses directly). Handler subtracts `PLAYER_EYE_HEIGHT` from internal eye-level Y for 1.8+ clients in both `handleJoinGame()` and `respawnToSafePosition()`.
- `NettyConnectionHandler.nextTeleportId` increments per V109 position packet. Client TeleportConfirm is silently accepted.
- `ClassicToNettyTranslator` converts fixed-point to double for V109 SpawnPlayer/EntityTeleport, scales deltas × 128 for relative moves.
- `ChunkManager` sends V109 chunks with `MapChunkPacketV109` and unloads with `UnloadChunkPacketV109`.

## Release 1.13 Protocol (v393) — "The Flattening"

### Overview
- Release 1.13-1.13.1 = v393 (July 2018). Biggest DATA format change in MC history.
- Wire protocol structure similar to 1.12.2 (same VarInt framing, same connection states).
- Block/item ID systems completely overhauled: `blockId << 4 | metadata` replaced by global block state IDs.
- Item damage field removed from slot wire format. Item IDs renumbered.
- ALL Play state packet IDs reshuffled again.
- New mandatory S2C packets: DeclareCommands (0x11), UpdateRecipes (0x54), UpdateTags (0x55).

### Key Changes from v340 (1.12.2)
- **Block state IDs**: `blockId << 4 | meta` → flat global IDs. `BlockStateMapper` maps legacy 0-255 to 1.13 state IDs.
- **Item slot format**: `short itemId, byte count, short damage, NBT` → `short itemId, byte count, NBT` (damage REMOVED). `NettySetSlotPacketV393`.
- **Item IDs renumbered**: cobblestone 4→12, grass_block 2→8, dirt 3→9, oak_planks 5→13.
- **Chunk palette**: 14-bit global mode (was 13-bit). Entries are `BlockStateMapper.toV393BlockState(blockId)`. Biomes changed from byte[256] to int[256].
- **Bit packing**: Same 1.9-1.12 style (entries span long boundaries). Changes to non-spanning in 1.16.
- **Position encoding**: UNCHANGED (x:26|y:12|z:26 packed long). Changes to x:26|z:26|y:12 in 1.14.
- **JoinGame, KeepAlive, entity packets**: Wire format UNCHANGED from v338/v340.
- **SpawnPlayer (0x05) and BlockChange (0x0B)**: IDs UNCHANGED from v338.

### New Packet Classes (5 files)
All in `rd-protocol/.../packet/netty/`:
- `NettySetSlotPacketV393`: slot without damage field (byte windowId, short slot, short itemId, byte count, byte TAG_End)
- `NettyBlockChangePacketV393`: Position packed long + VarInt globalBlockStateId (caller maps via BlockStateMapper)
- `DeclareCommandsPacketV393` (0x11): Minimal empty command tree (1 root node, no children)
- `UpdateRecipesPacketV393` (0x54): Empty recipe list (VarInt 0)
- `UpdateTagsPacketV393` (0x55): Empty tag lists (VarInt 0 for blocks, items, fluids)

### Registry (REGISTRY_V393 + REVERSE_V393)
- C2S: Complete 43-entry overlay (0x00-0x2A). Key: Chat→0x02, KeepAlive→0x0E, Position→0x10, Digging→0x18, BlockPlacement→0x29, UseItem→0x2A.
- S2C reverse: 18-entry delta from V338. Chat→0x0E, SetSlotV393→0x17, Disconnect→0x1B, KeepAlive→0x21, MapChunk→0x22, JoinGame→0x25, PlayerPosition→0x32, DestroyEntities→0x35, EntityTeleport→0x50, EntityProperties→0x52.
- V393-specific class entries: BlockChangeV393→0x0B, DeclareCommands→0x11, UpdateRecipes→0x54, UpdateTags→0x55.

### Handler Notes
- `NettyConnectionHandler`: Sends DeclareCommands, UpdateRecipes, UpdateTags after JoinGame for v393+. SetSlot uses `NettySetSlotPacketV393` with mapped item IDs.
- `ClassicToNettyTranslator`: SetBlockServerPacket → `NettyBlockChangePacketV393` with `BlockStateMapper.toV393BlockState()` for v393+.
- `ChunkManager`: v393 branch calls `chunk.serializeForV393Protocol()`, wraps in `MapChunkPacketV109`.

### Next Version Notes
- **1.13.2 (v404)**: Item slot format changes to `boolean present + VarInt itemId + byte count + NBT`. Separate follow-up.
- **1.14 (v477)**: Position encoding changes to x:26|z:26|y:12. Bit packing stays spanning until 1.16.
- **1.15 (v573)**: New AcknowledgePlayerDigging (0x08) shifts all S2C >= 0x08 by +1. SpawnPlayer removes entity metadata. JoinGame adds hashedSeed + enableRespawnScreen. Biomes int[256] -> int[1024] (3D). C2S unchanged from v477.

# Release 1.20.2 Protocol (v764)

## Overview
- Largest structural protocol change since 1.7.2. Introduces CONFIGURATION connection state between LOGIN and PLAY.
- `RELEASE_1_20_2(764, 71, Family.RELEASE, "Release 1.20.2 (v764)", 93)` in ProtocolVersion enum.

## Key Changes
- **New CONFIGURATION state**: Flow: LOGIN → LoginSuccess → LoginAcknowledged C2S (0x03) → CONFIGURATION (RegistryData, FeatureFlags, UpdateTags, FinishConfig S2C→C2S) → PLAY.
- **Dimension codec moved**: JoinGame no longer contains dimension codec NBT. Sent via SINGLE `RegistryDataPacketV764` S2C CONFIG 0x05 containing ALL registries.
- **RegistryData format (1.20.2-1.20.4)**: Single CompoundTag in network NBT (0x0A, NO root name). Contains all registries (dimension_type, biome, chat_type, damage_type) in JoinGame-style codec format: each registry = CompoundTag{type=String, value=ListTag[CompoundTag{id=Int, name=String, element=CompoundTag}]}.
- **CRITICAL: NOT per-entry format**: The per-entry format (String registryId + VarInt count + entries with Optional<CompoundTag>) was introduced in 1.20.5 (v766), NOT 1.20.2.
- **Network NBT (1.20.2+)**: ALL NBT over the network omits root compound name. Root = 0x0A + children + 0x00 (no writeShort(0) for name). Affects RegistryData, MapChunk heightmaps, etc.
- **SpawnPlayer removed**: Merged into generic SpawnEntity (0x01). Player entityType=122. Pitch comes BEFORE yaw (swapped vs SpawnPlayer).
- **Chunk batch system**: ChunkBatchStart (0x0D) → chunks → ChunkBatchFinished (0x0C, VarInt count). Client replies with ChunkBatchReceived C2S (0x07, Float).
- **JoinGame restructured (0x29)**: No codec. New field: `doLimitedCrafting` (Boolean). gameMode moved after hashedSeed.
- **UpdateTags (CONFIG 0x08, PLAY 0x70)**: Block +2 (camel_sand_step_sound_blocks, concrete_powder)=147. Entity type +1 (non_controlling_rider)=14. Game event: entity_roar/entity_shake→entity_action/unequip.
- **Extensive S2C/C2S packet ID reshuffling**: All IDs verified from ViaVersion enum ordinals.
- **C2S shift pattern**: 3 new C2S packets inserted (ChunkBatchReceived 0x07, ConfigurationAcknowledged 0x0B, PingRequest 0x1D). Shifts: 0x00-0x06 unchanged, 0x07-0x09 +1, 0x0A-0x19 +2, 0x1A-0x32 +3. MUST register FULL C2S forward map (54 entries) — sparse registration causes wrong packet handlers via cascade.
- **S2C shift**: SpawnPlayer removed at 0x03, 4 new packets added (ChunkBatch x2, PongResponse, StartConfiguration). BlockChangedAck shifted from 0x06 to 0x05. Disconnect shifted from 0x1A to 0x1B. ALL sent S2C packets need explicit v764 reverse entries — cascade to v762 gives wrong IDs.
- **Biome ID matching**: RegistryData biome entry must have `id=1` (plains) to match chunk serialization which writes biome palette value 1. id=0 causes invisible chunks.
- **Block state/item IDs unchanged** from V763.
- **LoginAcknowledged**: New C2S LOGIN 0x03 empty packet triggers LOGIN→CONFIGURATION transition.
- **ConfigFinish**: S2C 0x02 (server signals end) + C2S 0x02 (client acknowledges) triggers CONFIGURATION→PLAY transition.
- Pre-v764 clients: unchanged flow (LOGIN→PLAY directly).

# Release 1.20.5 Protocol (v766)

## Overview
- Release 1.20.5-1.20.6 = v766 (April 2024). Major registry and item format overhaul.
- `RELEASE_1_20_5(766, 73, Family.RELEASE, "Release 1.20.5 (v766)", 93)` in ProtocolVersion enum.

## Key Changes from v765 (1.20.3)

### CONFIG State Changes
- **SelectKnownPacks round-trip**: New handshake step. After LoginAcknowledged, server sends SelectKnownPacksS2CPacket (CONFIG 0x0E) with `minecraft:core v1.20.5`. Client replies with SelectKnownPacksC2SPacket (CONFIG 0x07). Server then sends registries the client doesn't have built-in.
- **RegistryData per-registry format**: Each registry sent as a SEPARATE packet (was single CompoundTag in v764). Format: `String registryId + VarInt entryCount + [String entryId + boolean hasData + Optional<CompoundTag> data]`. `RegistryDataPacketV766` (CONFIG 0x07).
- **Optional<Tag> encoding**: `boolean(true) + NBT tag` for present, `boolean(false)` for absent. Network NBT (no root name).
- **createBuiltIn()**: Setting hasData=false tells client to use built-in data. This is REQUIRED for dimension_type and biome — custom data causes CONFIG hang (client never sends ConfigFinishC2S).
- **UpdateEnabledFeatures**: CONFIG 0x0C (was 0x08 in v765).
- **UpdateTags**: CONFIG 0x0D (was 0x09 in v765). Play state 0x78.
- **ConfigFinish**: CONFIG S2C 0x03 (was 0x02 in v764). CONFIG C2S 0x03 (was 0x02).

### Registry Changes
- **8 registries sent**: minecraft:dimension_type (4 entries: overworld, overworld_caves, the_nether, the_end), minecraft:worldgen/biome (the_void + plains), minecraft:chat_type, minecraft:damage_type, minecraft:trim_pattern, minecraft:trim_material, minecraft:banner_pattern, minecraft:wolf_variant. Plus enchantment and painting_variant as built-in.
- **Biome IDs are position-based**: Entry position in the registry determines numeric ID. Unlike v764 where each entry had explicit `id` field in NBT. Chunks use biome palette value 1 for plains, so send the_void at index 0 and plains at index 1.

### LOGIN State Changes
- **EncryptionRequest**: boolean(false) cookie appended. `NettyEncryptionRequestPacketV766`.
- **LoginSuccess**: boolean strictErrorHandling appended. `LoginSuccessPacketV766`.

### PLAY State Changes
- **JoinGame**: dimensionType changed from String to VarInt(0) for overworld. `enforcesSecureChat` boolean added at end. `JoinGamePacketV766` (PLAY 0x2B).
- **Item slot format**: Count-first: `VarInt count + VarInt itemId` (if count>0). No boolean present flag. `NettySetSlotPacketV766` (PLAY 0x15).
- **Entity attributes**: Attribute keys changed from String to VarInt registry ID. `MOVEMENT_SPEED = 17`. `NettyEntityPropertiesPacketV766` (PLAY 0x75).
- **SpawnEntity player type**: Player entityType shifted from 122 to 128. 5 new entities inserted: bogged(11), breeze(12), breeze_wind_charge(13), ominous_item_spawner(61), wind_charge(117). `NettySpawnEntityPacketV766` (PLAY 0x01).

### Chunk Data
- **24 sections for built-in overworld**: Built-in overworld has minY=-64, height=384 (24 sections). Our world has 16 sections (Y 0-255). Pad with empty sections: 4 prepended (Y -64 to -1), 16 existing (Y 0-255), 4 appended (Y 256-319).
- **Empty section format**: 8 bytes: `short(0)` blockCount + `byte(0) VarInt(1) VarInt(0)` single-value air palette + `byte(0) VarInt(1) VarInt(1)` single-value plains biome palette.
- **Heightmap offset**: Add 64 to all heightmap values (minY shifted from 0 to -64).
- **Light mask shift**: Shift left by 4 bits (sections 0-7 move to positions 4-11 in 26-bit range).

### S2C Packet ID Shifts (PLAY)
- SpawnEntity: 0x01 (unchanged). SetSlot: 0x15 (was 0x15). JoinGame: 0x2B (was 0x2A). SystemChat: 0x6E (was 0x6D). PlayerPosition: 0x40 (was 0x3E). PlayerInfo: 0x3E (was 0x3C). DestroyEntities: 0x42 (was 0x40). EntityTeleport: 0x70 (was 0x6E). EntityRelativeMove: 0x2E (was 0x2D). EntityLookAndMove: 0x2F (was 0x2E). EntityLook: 0x30 (was 0x2F). EntityProperties: 0x75 (was 0x6F). KeepAlive: 0x26 (was 0x25). BlockChange: 0x09 (was 0x09). MapChunk: 0x27 (was 0x26). SpawnPosition: 0x54 (was 0x52). PlayerAbilities: 0x38 (was 0x36). PlayerListItem: 0x3E (was 0x3C). PlayerListRemove: 0x3D (was 0x3B). Disconnect: 0x1D (was 0x1B). DeclareCommands: 0x11 (was 0x11). UpdateRecipes: 0x74 (was 0x72). UpdateTags: 0x78 (was 0x74). UpdateEnabledFeatures: 0x76 (was 0x70). PluginMessage: 0x19 (was 0x19). ChangeGameState: 0x22 (was 0x22). ChunkBatchStart: 0x0D (was 0x0D). ChunkBatchFinished: 0x0C (was 0x0C). UnloadChunk: 0x21 (was 0x21).

### C2S Packet ID Shifts (PLAY)
- 3 new C2S packets: CHAT_COMMAND_SIGNED(0x05), COOKIE_RESPONSE(0x12), DEBUG_SAMPLE_SUBSCRIPTION(0x13).
- Shifts: 0x00-0x04 unchanged, 0x05 new, 0x05-0x10 +1, 0x12-0x13 new, 0x11-0x2F +3, 0x30+ +3.

### UpdateTags
- Block: 148 total (+badlands_terracotta vs v765).
- Item: 78 total (axolotl_tempt_items→axolotl_food, -tools, +enchantable/mace variants).
- Entity type: 20 total. Damage type: 25 total. Fluid: 2. Game event: 5.
- `UpdateTagsPacketV766`.

# Release 1.21.2 Protocol (v768)

## Overview
- Release 1.21.2-1.21.3 = v768. Massive S2C ID shifts (7 new packets, 1 removed), 2 new C2S packets, several rewritten wire formats.
- `RELEASE_1_21_2(768, 75, Family.RELEASE, "Release 1.21.2 (v768)", 93)` in ProtocolVersion enum.

## Key Changes from v767 (1.21)

### LOGIN State
- **LoginSuccess**: `strictErrorHandling` boolean REMOVED. `LoginSuccessPacketV768`. Wire: Long uuidMsb + Long uuidLsb + String username + VarInt propCount(0).

### CONFIG State
- **SelectKnownPacks**: 6 versions (1.20.5, 1.20.6, 1.21, 1.21.1, 1.21.2, 1.21.3).
- **New instrument registry**: 8 goat horn entries (ponder, sing, seek, feel, admire, call, yearn, dream). All built-in. `createInstrument()`.
- **Damage type registry**: 47 entries (v767's 45 + `ender_pearl` + `mace_smash`). `createDamageTypeV768()`.
- **UpdateTags**: `UpdateTagsPacketV768` adds 8th registry `minecraft:worldgen/biome` (36 biome tags). Item tags expanded from 78→96 (+18 `enchantable/*` sub-tags required for built-in enchantment parsing). Entity type tags updated from 24→35 (removed deflects_arrows/deflects_tridents, added 13 new including sensitive_to_bane_of_arthropods/impaling/smite). 1.21.2 client validates tag references EAGERLY during enchantment built-in data parsing (unlike 1.21 which validates lazily).

### PLAY State Wire Format Changes
- **PlayerPosition S2C rewritten**: `NettyPlayerPositionS2CPacketV768`. Wire: VarInt teleportId (FIRST), double x/y/z, double deltaMovX/Y/Z (0.0), float yaw/pitch, int flags (0=absolute). Packet ID 0x42.
- **EntityTeleport replaced by EntityPositionSync**: `EntityPositionSyncPacketV768`. Wire: VarInt entityId, double x/y/z, double deltaMovX/Y/Z (0.0), float yaw/pitch (degrees, NOT byte angles), boolean onGround. Packet ID 0x20. Constructor accepts byte angles and converts: `yaw * 360.0f / 256.0f`.
- **JoinGame gained seaLevel**: `JoinGamePacketV768`. VarInt seaLevel(64) appended between portalCooldown and enforcesSecureChat. Packet ID 0x2C.
- **TimeUpdate gained doDaylightCycle**: `NettyTimeUpdatePacketV768`. Wire: long worldAge, long dayTime (abs value), boolean doDaylightCycle. Packet ID 0x6B.
- **SpawnEntity player type shifted**: Player entityType 128→148 (20 new entities: boats split per wood type, creaking entities). `NettySpawnEntityPacketV768`. Packet ID 0x01.

### S2C Packet ID Shifts (PLAY)
- 7 new S2C packets inserted, 1 removed. Major shifts throughout.
- Key mappings: EntityPositionSync(0x20), UnloadChunk(0x22), ChangeGameState(0x23), KeepAlive(0x27), MapChunk(0x28), JoinGame(0x2C), EntityRelativeMove(0x2F), EntityLookAndMove(0x30), EntityLook(0x32), PlayerAbilities(0x3A), PlayerInfoRemove(0x3F), PlayerInfoUpdate(0x40), PlayerPosition(0x42), DestroyEntities(0x47), SetChunkCacheCenter(0x58), SetChunkCacheRadius(0x59), SpawnPosition(0x5B), TimeUpdate(0x6B), SystemChat(0x73), EntityProperties(0x7C), UpdateRecipes(0x7E), UpdateTags(0x7F).
- Unchanged from v766: BlockChangedAck(0x05), BlockChange(0x09), ChunkBatchFinished(0x0C), ChunkBatchStart(0x0D), DeclareCommands(0x11), SetSlot(0x15), PluginMessage(0x19), Disconnect(0x1D).

### C2S Packet ID Shifts (PLAY)
- 2 new C2S packets: BUNDLE_ITEM_SELECTED(0x02), CLIENT_TICK_END(0x0B).
- Shift pattern: v766 0x00-0x01 unchanged, v766 0x02-0x09 +1, v766 0x0A+ +2.
- Key C2S: TeleportConfirm(0x00), ChatCommand(0x05), Chat(0x07), ChunkBatchReceived(0x09), ClientCommand(0x0A), ClientSettings(0x0C), PluginMessage(0x14), UseEntity(0x18), KeepAlive(0x1A), Position(0x1C), PosLook(0x1D), Look(0x1E), OnGround(0x1F), Abilities(0x25), Digging(0x26), EntityAction(0x27), SteerVehicle(0x28), HoldingChange(0x31), UpdateSign(0x37), Animation(0x38), BlockPlacement(0x3A), UseItem(0x3B).

### C2S Wire Format Changes
- **PlayerInput (SteerVehicle) rewritten**: `PlayerInputPacketV768`. Old: float sideways + float forward + byte flags (9 bytes). New: byte flags (1 byte). Flags: forward(0x01), backward(0x02), left(0x04), right(0x08), jump(0x10), sneak(0x20), sprint(0x40).
- **BlockPlacement (USE_ITEM_ON) gained worldBorderHit**: `NettyBlockPlacementPacketV768`. Boolean `worldBorderHit` inserted between `isInsideBlock` and `sequence`. Without this fix, server reads worldBorderHit as sequence VarInt → wrong BlockChangedAck → client prediction blocks delayed block updates.
- **UpdateRecipes format changed**: `UpdateRecipesPacketV768`. Old: VarInt count + recipe definitions. New: VarInt propertySetCount + [String key + VarInt[] itemIds] + VarInt stonecutterRecipeCount + [...]. Empty = two VarInt(0).
- **onGround format**: v768 clients send unsigned byte instead of boolean for MOVE_PLAYER packets. Bit 0 = onGround, bit 1 = horizontalCollision. `readBoolean()` returns true for any non-zero byte. Our server doesn't use onGround for physics, so no fix needed currently.

# Release 1.21.4 Protocol (v769)

## Overview
- Release 1.21.4 = v769 (December 2024). Minimal version bump from v768.
- `RELEASE_1_21_4(769, 76, Family.RELEASE, "Release 1.21.4 (v769)", 93)` in ProtocolVersion enum.

## Key Changes from v768 (1.21.2)

### S2C Packet IDs
- **ALL S2C packet IDs identical to v768**. No shifts or new packets.
- **SpawnEntity player type shifted**: creaking_transient (ID 30) removed. Player 148→147. `NettySpawnEntityPacketV769`.

### C2S Packet ID Shifts
- PICK_ITEM (0x22) split into PICK_ITEM_FROM_BLOCK (0x22) + PICK_ITEM_FROM_ENTITY (0x23).
- PLAYER_LOADED (0x2A) added (empty packet, sent when client finishes loading terrain).
- Shift pattern: 0x00-0x21 unchanged, 0x22 split (+1), 0x23-0x28 +1, 0x2A new, 0x29-0x3B +2.
- Key C2S: TeleportConfirm(0x00), ChatCommand(0x05), Chat(0x07), ChunkBatchReceived(0x09), ClientCommand(0x0A), ClientSettings(0x0C), PluginMessage(0x14), UseEntity(0x18), KeepAlive(0x1A), Position(0x1C), PosLook(0x1D), Look(0x1E), OnGround(0x1F), Abilities(0x26), Digging(0x27), EntityAction(0x28), PlayerInput(0x29), HoldingChange(0x33), UpdateSign(0x39), Animation(0x3A), BlockPlacement(0x3C), UseItem(0x3D).

### CONFIG State
- **SelectKnownPacks**: 7 versions (1.20.5-1.21.4).
- **UpdateTags**: `UpdateTagsPacketV769`. Block: +bee_attractive, -tall_flowers (149 total). Item: +skeleton_preferred_weapons, +piglin_preferred_weapons, +pillager_preferred_weapons, +drowned_preferred_weapons, +wither_skeleton_disliked_weapons, -flowers, -tall_flowers, -trim_templates (98 total).

### LOGIN/JoinGame/Chunks
- Unchanged from v768. LoginSuccess, JoinGame, chunk format, item IDs all reuse v768 classes.

## Release 1.21.5 (v770)

### Key Changes
- **Heightmaps binary format**: Replaced NBT compound with `VarInt count + [VarInt typeId + VarInt longCount + long[]]`. Type ordinals: WORLD_SURFACE=1, MOTION_BLOCKING=4. `MapChunkPacketV770`.
- **Section palette data arrays**: VarInt length prefix REMOVED. `serializeForV770Protocol()` in AlphaChunk.
- **Entity type shift**: lingering_potion inserted at 100, player 147→148. `NettySpawnEntityPacketV770`.
- **S2C shift**: ADD_EXPERIENCE_ORB removed at 0x02, all 0x03-0x78 shift -1. TEST_INSTANCE_BLOCK_STATUS added at 0x78.
- **C2S shift**: SET_TEST_BLOCK at 0x39 (+1), TEST_INSTANCE_BLOCK_ACTION at 0x3D (+2). Sign(0x3A), Animation(0x3B), BlockPlacement(0x3E), UseItem(0x3F).
- **6 new synchronized registries** (all built-in): pig_variant(3: cold/temperate/warm), cow_variant(3), chicken_variant(3), frog_variant(3: cold/temperate/warm), cat_variant(11), wolf_sound_variant(7: classic/angry/big/cute/grumpy/puglin/sad).

### Tags
- `UpdateTagsPacketV770`. Block: +camels_spawnable_on, +edible_for_sheep, +plays_ambient_desert_block_sounds, +replaceable_by_mushrooms, +sword_instantly_mines (154 total). Item: +book_cloning_target, +eggs, +flowers (101 total). Entity type: +can_equip_saddle, +can_wear_horse_armor (37 total). Biome: +spawns_cold_variant_farm_animals, +spawns_warm_variant_farm_animals (38 total).

### CONFIG State
- **SelectKnownPacks**: 8 versions (1.20.5-1.21.5).
- **UpdateTags**: Config 0x0D, Play 0x7F (unchanged from v769).

### LOGIN/JoinGame/Chunks
- LoginSuccess, JoinGame reuse v768 classes. Chunk format changed (binary heightmaps + no VarInt data array length prefix).

## Release 1.21.6 (v771)

### Key Changes
- **S2C shift**: 1 new packet inserted before 0x1A -> +1 for 0x1A+. happy_ghast entity inserted (player: 148→149). `NettySpawnEntityPacketV771`.
- **SelectKnownPacks**: 9 versions (1.20.5-1.21.6).
- **UpdateTags**: `UpdateTagsPacketV771`. Added 9th registry `minecraft:dialog` (2 tags: pause_screen_additions, quick_actions). Block: +happy_ghast_avoids, +triggers_ambient_dried_ghast_block_sounds (157 total). Item: +happy_ghast_food, +happy_ghast_tempt_items, +harnesses, +gaze_disguise_equipment, +map_invisibility_equipment (104 total). Entity type: +happy_ghast_friends, +happy_ghast_food_provider (39 total).
- **JoinGame/chunk format**: Unchanged from v768/v770.

## Release 1.21.7-1.21.8 (v772)

### Key Changes
- **No S2C or C2S packet ID changes** from v771. All packet IDs identical.
- **SelectKnownPacks**: 11 versions (1.20.5-1.21.8).
- **No entity type changes**. Player stays at 149.
- **No tag changes**. Reuse UpdateTagsPacketV771.
- **JoinGame/chunk format**: Unchanged from v768/v770.

## Release 1.21.9-1.21.10 (v773, "The Copper Age")

### Key Changes
- **S2C shift**: 4 debug packets at 0x1A-0x1D (+4 for 0x1A-0x22), 1 test packet at 0x27 (+5 for 0x23+). 139 total S2C packets (was 134).
- **SpawnEntity wire format changed**: Velocity field changed from 3 shorts (6 bytes) to MOVEMENT_VECTOR encoding. Field reordered: velocity now between position (x,y,z) and rotation (pitch,yaw,headYaw), was previously at end after data. Zero velocity = single byte 0x00. `NettySpawnEntityPacketV773`.
- **Entity type shift**: copper_golem + mannequin inserted (player: 149→151).
- **SpawnPosition wire format changed**: GlobalBlockPosition format — `String dimension + Position (packed long) + float yaw + float pitch`. `SpawnPositionPacketV773`.
- **No C2S packet ID changes** from v771.
- **SelectKnownPacks**: 13 versions (1.20.5-1.21.10).

### Tags
- `UpdateTagsPacketV773`. Block: +bars, +chains, +copper, +copper_chests, +copper_golem_statues, +incorrect_for_copper_tool, +lanterns, +lightning_rods, +wooden_shelves (166 total). Item: +bars, +chains, +copper, +copper_chests, +copper_golem_statues, +copper_tool_materials, +lanterns, +lightning_rods, +repairs_copper_armor, +shearable_from_copper_golem, +wooden_shelves (115 total). Entity type: +accepts_iron_golem_gift, +candidate_for_iron_golem_gift, +cannot_be_pushed_onto_boats (42 total).
- channeling enchantment's built-in data references block tag `#minecraft:lightning_rods` — this tag MUST exist or client fails to parse enchantment registry.

### S2C Packet ID Shifts (PLAY)
- Key mappings: SpawnEntity(0x01), BlockChangedAck(0x04), BlockChange(0x08), ChunkBatchFinished(0x0B), ChunkBatchStart(0x0C), DeclareCommands(0x10), SetSlot(0x14), PluginMessage(0x18), Disconnect(0x20), EntityPositionSync(0x23), UnloadChunk(0x25), ChangeGameState(0x26), KeepAlive(0x2B), MapChunk(0x2C), JoinGame(0x30), EntityRelativeMove(0x33), EntityLookAndMove(0x34), EntityLook(0x36), PlayerAbilities(0x3E), PlayerInfoRemove(0x43), PlayerInfoUpdate(0x44), PlayerPosition(0x46), DestroyEntities(0x4B), SetChunkCacheCenter(0x5C), SetChunkCacheRadius(0x5D), SpawnPosition(0x5F), TimeUpdate(0x6F), SystemChat(0x77), EntityProperties(0x81), UpdateRecipes(0x83), UpdateTags(0x84).

### LOGIN/JoinGame/Chunks
- LoginSuccess, JoinGame reuse v768 classes. Chunk format unchanged from v770.
