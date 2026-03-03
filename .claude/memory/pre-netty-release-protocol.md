# Pre-Netty Release Protocols (v21-v78)

## Beta 1.9 Pre5 Protocol (v21)
- Beta 1.9 Prerelease 5-6 = v21. Hybrid wire format between v17 and v22.
- **S2C packets same as v17**: Login, Respawn, KeepAlive (int ID), MapChunk (old sub-chunk), PreChunk, BlockChange (byte block ID), SpawnPlayer (no metadata), DestroyEntity (single int). No PlayerAbilities (0xCA), no encryption.
- **C2S item slots same as v22**: Conditional NBT. BlockPlacement, WindowClick, CreativeSlot use v22 variants. S2C SetSlot/WindowItems also use v22 variants.
- ViaLegacy does NOT support v21 (jumps from v17 to v22).
- `giveItem()` routes to `SetSlotPacketV22` for `isAtLeast(BETA_1_9_PRE5)`.
- PacketRegistry item slot format threshold is `>= 21` (not `>= 22`).

## Release 1.0 Protocol (v22)
- Release 1.0.0 = v22. Wire protocol nearly identical to v17 — only **item slots gained NBT tag data**.
- Item slot format: `[short itemId, if >= 0: byte count, short damage, short nbtLength, if nbtLength > 0: nbtLength bytes gzipped NBT]`
- `McDataTypes.skipNbtItemTag()` / `writeEmptyNbtItemTag()` helpers.
- Affected C2S: `PlayerBlockPlacementPacketV22`, `WindowClickPacketV22`, `CreativeSlotPacketV22`.
- Affected S2C: `SetSlotPacketV22`, `WindowItemsPacketV22`.
- New C2S: `EnchantItemPacket`, `PlayerAbilitiesPacket` (4-boolean format).
- **CRITICAL: item NBT is CONDITIONAL on item type**: Only for damageable items (`Item.isDamageable()` = maxDamage > 0 && !hasSubtypes). `McDataTypes.isNbtDamageableItem(int)`.

## Release 1.1 Protocol (v23)
- Release 1.1 = v23. Wire protocol nearly identical to v22.
- **Login S2C/C2S**: Added `String16 levelType`. `LoginS2CPacketV23`.
- **Respawn (0x09)**: Added `String16 levelType` at end. `RespawnPacketV23`.
- **Custom Payload (0xFA)**: New bidirectional packet. Silently consumed.

## Release 1.2.1 Protocol (v28)
- Release 1.2.1 = v28. Significant wire format changes from v23.
- **Login S2C**: Seed removed, dimension `byte->int`. `LoginS2CPacketV28`.
- **Respawn**: Seed removed, dimension `byte->int`. `RespawnPacketV28`.
- **MapChunk (0x33)**: New section-based format. `MapChunkPacketV28`. Data uses 16x16x16 sections with YZX ordering.
- **PreChunk (0x32) still required for v28/v29**.
- `AlphaChunk.serializeForV28Protocol()` converts flat arrays to section-based format.

## Release 1.2.4 Protocol (v29)
- Release 1.2.4-1.2.5 = v29. Wire protocol nearly identical to v28.
- Only change: **C2S PlayerAbilities (0xCA) now actually sent by client**. Already handled.

## Release 1.3.1 Protocol (v39)
- Release 1.3.1-1.3.2 = v39. Mandatory encryption added.
- **Encryption**: RSA 1024-bit + AES/CFB8. `CipherDecoder`/`CipherEncoder` pipeline handlers.
- **Offline mode serverId MUST be `"-"`** (not `""`).
- **Handshake format changed**: `byte protocolVersion + String16 username + String16 hostname + int port`.
- **Login flow**: Handshake -> EncryptionKeyRequest -> EncryptionKeyResponse -> enable AES -> ClientStatuses(0xCD) -> Login.
- **Login S2C**: Removed empty username String16, gameMode/dimension int->byte. `LoginS2CPacketV39`.
- **PreChunk removed**: v39 uses MapChunk with primaryBitMask=0 for unloads.
- **MapChunk**: Removed unused int(0). `MapChunkPacketV39`.
- **BlockChange**: Block ID byte->short. `BlockChangePacketV39`.
- **PlayerAbilities**: `byte flags + byte flySpeed + byte walkSpeed`. `PlayerAbilitiesPacketV39`.
- **Item slot NBT unconditional**: All items always have `short nbtLength`. V39 packet variants.
- **DestroyEntity**: `byte count + int[]`. `DestroyEntityPacketV39`.
- **SpawnPlayer**: Gained entity metadata (0x7F terminator). `SpawnPlayerPacketV39`.

## Release 1.4.2 Protocol (v47)
- Release 1.4.2 = v47. Wire format nearly identical to v39.
- **S2C TimeUpdate (0x04) changed**: 2 longs (worldAge + timeOfDay) instead of 1 long (time). `TimeUpdatePacketV47`.
- **C2S change**: ClientSettings gained `boolean showCape`. `ClientSettingsPacketV47`.
- **Server list ping (1.4.2+)**: `0xFE 0x01` -> `0xFF` + String16(`"§1\0protocol\0version\0motd\0players\0max"`).

## Release 1.4.4 Protocol (v49)
- Release 1.4.4-1.4.5 = v49. No C2S format changes. All v47 packets remain correct.

## Release 1.4.6 Protocol (v51)
- Release 1.4.6-1.4.7 = v51. No new packet classes needed. All v47 packets remain correct.

## Release 1.5 Protocol (v60/v61)
- Release 1.5 = v60, 1.5.2 = v61. Details in PacketRegistry.

## Release 1.6.1 Protocol (v73)
- Release 1.6.1 = v73. First version since v39 needing new packet classes.
- **PlayerAbilities**: speeds byte->float. `PlayerAbilitiesPacketV73(flags, 0.05f, 0.1f)`.
- **EntityAction C2S**: gained `int jumpBoost`. `EntityActionPacketV73`.
- **Entity Properties S2C REQUIRED**: `generic.movementSpeed = 0.10000000149011612`. v73 format has NO modifier count/list.
- **Chat S2C**: JSON text component `{"text":"..."}`.
- **Server list ping**: 1.6+ clients append MC|PingHost with protocol version.
- **E2E agent**: runMethodName=`d` (not `run`), serverHostFieldName=null (CLI args handle connection via `--server`/`--port`).

## Release 1.6.2 Protocol (v74)
- Release 1.6.2-1.6.4 = v74. Only change: Entity Properties gained `short modifierCount` + modifier list. `EntityPropertiesPacketV74`.
- v74 and v78 (1.6.4) have identical wire format.
