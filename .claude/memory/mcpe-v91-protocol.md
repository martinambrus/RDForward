---
name: MCPE v91 Protocol (0.16.0)
description: Protocol 91 differences from v81 for MCPE 0.16.0 - VarInt wire format, resource pack handshake, renumbered packet IDs, expanded StartGame
type: reference
---

## Protocol 91 (MCPE 0.16.0) — "The Boss Update"

Source: Genisys at `/tmp/genisys-full/` commit `7bddf814` ("Bump version to 0.16.0.5 and protocol to 91")

## Wire Format Revolution (from v81)

v91 changes the fundamental wire encoding:
- **Strings**: UnsignedVarInt length prefix (was Big-Endian short in v81)
- **Entity IDs**: Signed VarInt (was raw long in v81)
- **Block coords**: VarInt(x) + byte(y) + VarInt(z)
- **Many int fields**: Now VarInt-encoded (seed, dimension, generator, gamemode, difficulty, etc.)
- **Wrapper**: Still 0xFE (same as v81)
- **Batch packet**: 0x06 (same ID, but now at position 0x06 explicitly)

## Packet ID Remapping (v81 → v91)

3 resource pack packets inserted at 0x07-0x09. Additional new packets inserted throughout.
ALL game packet IDs from TEXT onward are shifted.

| Packet | v81 ID | v91 ID | Notes |
|--------|--------|--------|-------|
| LOGIN | 0x01 | 0x01 | Same |
| PLAY_STATUS | 0x02 | 0x02 | Same |
| S2C_HANDSHAKE | 0x03 | 0x03 | Same |
| C2S_HANDSHAKE | 0x04 | 0x04 | Same |
| DISCONNECT | 0x05 | 0x05 | Same |
| BATCH | 0x06 | 0x06 | Same |
| RESOURCE_PACKS_INFO | — | 0x07 | NEW |
| RESOURCE_PACK_STACK | — | 0x08 | NEW |
| RESOURCE_PACK_CLIENT_RESPONSE | — | 0x09 | NEW (C2S) |
| TEXT | 0x07 | 0x0a | +3 |
| SET_TIME | 0x08 | 0x0b | +3 |
| START_GAME | 0x09 | 0x0c | +3, format changed |
| ADD_PLAYER | 0x0a | 0x0d | +3 |
| ADD_ENTITY | 0x0b | 0x0e | +3 |
| REMOVE_ENTITY | 0x0c | 0x0f | +3 |
| ADD_ITEM_ENTITY | 0x0d | 0x10 | +3 |
| ADD_HANGING_ENTITY | — | 0x11 | NEW |
| TAKE_ITEM_ENTITY | 0x0e | 0x12 | +4 |
| MOVE_ENTITY | 0x0f | 0x13 | +4 |
| MOVE_PLAYER | 0x10 | 0x14 | +4, format changed |
| RIDER_JUMP | 0x11 | 0x15 | +4 |
| REMOVE_BLOCK | 0x12 | 0x16 | +4 |
| UPDATE_BLOCK | 0x13 | 0x17 | +4 |
| ADD_PAINTING | 0x14 | 0x18 | +4 |
| EXPLODE | 0x15 | 0x19 | +4 |
| LEVEL_SOUND_EVENT | — | 0x1a | NEW |
| LEVEL_EVENT | 0x16 | 0x1b | +5 |
| BLOCK_EVENT | 0x17 | 0x1c | +5 |
| ENTITY_EVENT | 0x18 | 0x1d | +5 |
| MOB_EFFECT | 0x19 | 0x1e | +5 |
| UPDATE_ATTRIBUTES | 0x1a | 0x1f | +5 |
| MOB_EQUIPMENT | 0x1b | 0x20 | +5 |
| MOB_ARMOR_EQUIPMENT | 0x1c | 0x21 | +5 |
| INTERACT | 0x1e | 0x22 | +4 |
| USE_ITEM | 0x1f | 0x23 | +4 |
| PLAYER_ACTION | 0x20 | 0x24 | +4 |
| HURT_ARMOR | 0x21 | 0x25 | +4 |
| SET_ENTITY_DATA | 0x22 | 0x26 | +4 |
| SET_ENTITY_MOTION | 0x23 | 0x27 | +4 |
| SET_ENTITY_LINK | 0x24 | 0x28 | +4 |
| SET_HEALTH | 0x25 | 0x29 | +4 |
| SET_SPAWN_POSITION | 0x26 | 0x2a | +4 |
| ANIMATE | 0x27 | 0x2b | +4 |
| RESPAWN | 0x28 | 0x2c | +4 |
| DROP_ITEM | 0x29 | 0x2d | +4 |
| INVENTORY_ACTION | — | 0x2e | NEW |
| CONTAINER_OPEN | 0x2a | 0x2f | +5 |
| CONTAINER_CLOSE | 0x2b | 0x30 | +5 |
| CONTAINER_SET_SLOT | 0x2c | 0x31 | +5 |
| CONTAINER_SET_DATA | 0x2d | 0x32 | +5 |
| CONTAINER_SET_CONTENT | 0x2e | 0x33 | +5 |
| CRAFTING_DATA | 0x2f | 0x34 | +5 |
| CRAFTING_EVENT | 0x30 | 0x35 | +5 |
| ADVENTURE_SETTINGS | 0x31 | 0x36 | +5, format changed |
| BLOCK_ENTITY_DATA | 0x32 | 0x37 | +5 |
| PLAYER_INPUT | 0x33 | 0x38 | +5 |
| FULL_CHUNK_DATA | 0x34 | 0x39 | +5, format changed |
| SET_COMMANDS_ENABLED | — | 0x3a | NEW |
| SET_DIFFICULTY | 0x35 | 0x3b | +6 |
| CHANGE_DIMENSION | 0x36 | 0x3c | +6 |
| SET_PLAYER_GAMETYPE | 0x37 | 0x3d | +6 |
| PLAYER_LIST | 0x38 | 0x3e | +6 |
| EVENT | 0x39 | 0x3f | +6 |
| SPAWN_EXPERIENCE_ORB | 0x3a | 0x40 | +6 |
| MAP_ITEM_DATA | 0x3b | 0x41 | +6 |
| MAP_INFO_REQUEST | 0x3c | 0x42 | +6 |
| REQUEST_CHUNK_RADIUS | 0x3d | 0x43 | +6 |
| CHUNK_RADIUS_UPDATED | 0x3e | 0x44 | +6 |
| ITEM_FRAME_DROP_ITEM | 0x3f | 0x45 | +6 |
| REPLACE_SELECTED_ITEM | 0x40 | 0x46 | +6 |
| GAME_RULES_CHANGED | — | 0x47 | NEW |
| CAMERA | — | 0x48 | NEW |
| ADD_ITEM | 0x41 | 0x49 | +8 |
| BOSS_EVENT | — | 0x4a | NEW |
| AVAILABLE_COMMANDS | — | 0x4b | NEW |
| COMMAND_STEP | — | 0x4c | NEW |
| RESOURCE_PACK_DATA_INFO | — | 0x4d | NEW |
| RESOURCE_PACK_CHUNK_DATA | — | 0x4e | NEW |
| RESOURCE_PACK_CHUNK_REQUEST | — | 0x4f | NEW |

## New Login Sequence

v81: LOGIN → PlayStatus(success) → StartGame → SetTime → chunks → doFirstSpawn
v91: LOGIN → PlayStatus(success) → ResourcePacksInfo → [C2S: ResourcePackClientResponse] → StartGame → SetTime → chunks → doFirstSpawn

ResourcePacksInfo (0x07): bool mustAccept + short behaviourCount + entries + short resourceCount + entries
(Send with zero entries for no packs.)

ResourcePackClientResponse (0x09 C2S): byte status + short count
(Status values: likely 1=refused, 2=downloading, 3=completed, 4=accepted)

## StartGame (0x0c) — Expanded Format

```
entityUniqueId: VarInt (signed zigzag)
entityRuntimeId: VarInt (signed zigzag)
x, y, z: 3x LFloat (Little-Endian float)
yaw: LFloat
pitch: LFloat
seed: VarInt
dimension: VarInt
generator: VarInt (0=old, 1=infinite, 2=flat)
gamemode: VarInt
difficulty: VarInt
spawnX: VarInt, spawnY: byte, spawnZ: VarInt (blockCoords)
hasAchievementsDisabled: bool (byte)
dayCycleStopTime: VarInt (-1 = not stopped)
eduMode: bool
rainLevel: LFloat
lightningLevel: LFloat
commandsEnabled: bool
isTexturePacksRequired: bool
unknown: string (VarInt-prefixed)
worldName: string (VarInt-prefixed)
```

## MovePlayer (0x14) — Changed Format

```
entityRuntimeId: VarInt (signed zigzag)
x, y, z: 3x LFloat
pitch: LFloat
yaw: LFloat
bodyYaw: LFloat
mode: byte (0=normal, 1=reset, 2=rotation)
onGround: bool
```

## AddPlayer (0x0d) — Changed Format

```
uuid: 16 bytes (LE longs)
username: string (VarInt-prefixed)
entityUniqueId: VarInt
entityRuntimeId: VarInt
x, y, z: 3x LFloat (position)
speedX, speedY, speedZ: 3x LFloat
yaw: LFloat
headYaw: LFloat
pitch: LFloat
item: Slot (VarInt itemId, VarInt count, VarInt damage, VarInt nbtLen + nbt)
metadata: binary metadata blob
```

## AdventureSettings (0x36) — Changed Format

```
flags: UnsignedVarInt (was raw int in v81)
userPermission: UnsignedVarInt (was raw int in v81)
```
Flag bits same as v81: 0x01=worldImmutable, 0x20=autoJump, 0x40=allowFlight, 0x80=noClip, 0x200=isFlying

## FullChunkData (0x39) — Changed Format

```
chunkX: VarInt (was int in v81)
chunkZ: VarInt (was int in v81)
order: byte (0=ORDER_COLUMNS)
data: string (VarInt-length-prefixed terrain data)
```

## SetTime (0x0b)

Uses putVarInt for time + putBool for started flag (different from v81's raw int time).
