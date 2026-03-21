---
name: MCPE v81 Protocol (0.15.0)
description: Protocol 81 differences from v45 for MCPE 0.15.0 - wrapper 0xFE, renumbered IDs 0x01+, JWT login, AdventureSettings permissions
type: reference
---

## Protocol 81 (MCPE 0.15.0 "Friendly Update")
Protocol range 46-80 were dev-only.

## Wrapper Byte: 0xFE (changed from 0x8E)
All game packets prefixed with 0xFE. Same standalone/batch strategy as v45.
Batch ID changed from 0x92 to 0x06.

## Packet ID Renumbering
ALL game packet IDs renumbered from 0x8F+ range to 0x01+ range.
Key IDs: LOGIN=0x01, PLAY_STATUS=0x02, DISCONNECT=0x05, BATCH=0x06,
TEXT=0x07, START_GAME=0x09, ADD_PLAYER=0x0A, REMOVE_ENTITY=0x0C,
MOVE_PLAYER=0x10, FULL_CHUNK_DATA=0x34, PLAYER_LIST=0x38,
ADVENTURE_SETTINGS=0x31, SET_DIFFICULTY=0x35.

## REMOVE_PLAYER Dropped
Use REMOVE_ENTITY (0x0C) instead for player despawn.

## JWT Login Format
Wire: [int protocol][int compressedLen][zlib payload]
Decompressed: [LInt chainLen][chain JSON][LInt skinTokenLen][skin JWT]
Chain JSON: {"chain":["jwt1",...]} — extract displayName from extraData.
Skin JWT payload: SkinId, SkinData (base64 RGBA), ClientRandomId.

## AdventureSettings (PocketMine-MP v81 reference)
Format: [int flags][int userPermission][int globalPermission]
Flags shifted from v45: AUTO_JUMP=0x40 (was 0x20), ALLOW_FLIGHT=0x80 (was 0x40).
Permission levels: 0=normal, 1=operator, 2=host, 3=automation, 4=admin.
PocketMine sends userPermission=2, globalPermission=2 for op players.

## PlayerList (TYPE_ADD)
Format: uuid(16) + entityId(long) + username(string) + skinId(string) + skinData(short+rawbytes)
skinData is raw bytes with short length prefix (NOT base64 encoded).

## StartGame Trailing Fields
After player position: byte(1) + byte(1) + byte(0) + string(levelId).

## Slot/Item NBT Endianness
Same as v45: NBT length is little-endian short.

## Chunk Format
Unchanged from v34/v38/v45.
