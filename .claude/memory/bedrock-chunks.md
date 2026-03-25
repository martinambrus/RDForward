# Bedrock Chunk Encoding Research

## Block Palette (v944 / MC Bedrock 1.26.10)

### File Source
- `rd-server/src/main/resources/bedrock/block_palette.nbt` is from CloudburstMC/Data for v944
- ~15,850 block state entries (golden dandelion + related states added in v944)
- File is sorted by FNV1-64 hash of block name (pre-computed in `name_hash` field)
- Index 0 = `minecraft:cyan_terracotta`, NOT `minecraft:air`
- Air is at index 12530, stone at 2532, cobblestone at 5088, grass_block at 11062

### Runtime ID Modes
- `blockNetworkIdsHashed = false` (default, what GeyserMC uses): runtime ID = sequential index in palette
- `blockNetworkIdsHashed = true` (vanilla BDS default): runtime ID = FNV1a-32 hash of serialized block state NBT (`network_id` field)
- We use false (sequential indices)

### Palette NOT sent in StartGamePacket (v419+)
- Starting from protocol v419, the Bedrock client has the block palette BUILT IN
- The StartGamePacket does NOT contain the block palette for v944
- The `blockProperties` field (in StartGamePacket) is for CUSTOM block overrides only
- The `SimpleDefinitionRegistry<BlockDefinition>` set on codec helper is only for server-side runtime ID lookups, NOT sent to client
- Client and server must agree on palette order — since client has it built-in, server must match

### Confirmed: Our palette file order matches the client's built-in palette
- Diagnostic test with runtime IDs 0-7 showed correct blocks (blue candle variants at indices 2-7 matched our file)
- This confirms the file order IS the canonical client palette order

## Sub-chunk Format

### Version 8 vs 9
- Version 8: `[8][numStorages][for each: paletteHeader + data + palette]`
- Version 9: `[9][numStorages][subChunkYIndex][for each: paletteHeader + data + palette]`
- Version 8 works (blocks render). Version 9 + zigzag caused client crash.

### Palette Header
- `(bitsPerBlock << 1) | 1` where low bit = 1 means runtime/network format
- Supported bpb values: 0, 1, 2, 3, 4, 5, 6, 8, 16

### bpb=0 (single block type)
- After header byte: single varint for the runtime ID, NO palette size prefix, NO bit array
- Confirmed working for single-byte varints (rids 0-127)

### Varint Format (SOLVED)
- **Must use `VarInts.writeInt` (zigzag encoding)** for palette entries and palette size
- `VarInts.writeInt` = zigzag: `(value << 1) ^ (value >> 31)` then write as unsigned varint
- `VarInts.writeUnsignedInt` = direct LEB128 — does NOT work for palette entries
- Unsigned varints appeared to work for small values (0-7) by coincidence (zigzag(n) for small n just doubles the value, mapping to similar block types)
- **Version 8 + zigzag = WORKS** (confirmed: stone, cobblestone, grass all render correctly)
- Version 9 + zigzag crashed — likely version 9 format issue, not zigzag
- Use `org.cloudburstmc.protocol.common.util.VarInts.writeInt()` directly from the library

## Biome Sections
- 24 sections for overworld (Y=-64 to Y=319)
- First section: `(0 << 1) | 1` header + varint biome ID (plains=1)
- Subsequent 23 sections: copy-previous marker `0xFF` (= `(127 << 1) | 1`)

## Block Mappings Issues
- Several entries in `block-mappings.properties` use OLD Bedrock names not in v924 palette
- Unmapped blocks fall back to air (airDefinition), not fence posts
- Known broken names: planks→oak_planks, sapling→oak_sapling, log→oak_log, leaves→oak_leaves, wool→white_wool, yellow_flower→dandelion, red_flower→poppy, fence→oak_fence, etc.
