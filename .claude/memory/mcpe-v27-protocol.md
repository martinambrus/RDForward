---
name: MCPE v27 Protocol (0.11.0)
description: Protocol 27 packet IDs, formats, and differences from v17-v20 for MCPE 0.11.0
type: reference
---

## Protocol 27 (MCPE 0.11.0)
Accepts ONLY protocol 27 (no range). Class renamed ServerSideNetworkHandler → ServerNetworkHandler.

## Packet IDs (completely renumbered from v11-v20)
- 0x01 LoginPacket
- 0x02 PlayStatusPacket (NEW, replaces LoginStatus)
- 0x03 DisconnectPacket (NEW)
- 0x04 TextPacket (replaces Message/Chat)
- 0x05 SetTimePacket
- 0x06 StartGamePacket
- 0x07 AddPlayerPacket
- 0x08 RemovePlayerPacket
- 0x09 AddEntityPacket
- 0x0A RemoveEntityPacket
- 0x0B AddItemEntityPacket
- 0x0C TakeItemEntityPacket
- 0x0D MoveEntityPacket
- 0x0E MovePlayerPacket
- 0x0F RemoveBlockPacket
- 0x10 UpdateBlockPacket
- 0x11 AddPaintingPacket
- 0x12 ExplodePacket
- 0x13 LevelEventPacket
- 0x14 TileEventPacket
- 0x15 EntityEventPacket
- 0x16 MobEffectPacket (NEW)
- 0x17 PlayerEquipmentPacket
- 0x18 PlayerArmorEquipmentPacket
- 0x19 InteractPacket
- 0x1A UseItemPacket
- 0x1B PlayerActionPacket
- 0x1C HurtArmorPacket
- 0x1D SetEntityDataPacket
- 0x1E SetEntityMotionPacket
- 0x1F SetEntityLinkPacket
- 0x20 SetHealthPacket
- 0x21 SetSpawnPositionPacket
- 0x22 AnimatePacket
- 0x23 RespawnPacket
- 0x24 DropItemPacket
- 0x25 ContainerOpenPacket
- 0x26 ContainerClosePacket
- 0x27 ContainerSetSlotPacket
- 0x28 ContainerSetDataPacket
- 0x29 ContainerSetContentPacket
- 0x2A ContainerAckPacket (NEW)
- 0x2B AdventureSettingsPacket
- 0x2C TileEntityDataPacket (NEW)
- 0x2D PlayerInputPacket
- 0x2E FullChunkDataPacket
- 0x2F SetDifficultyPacket (NEW)
- 0x30 BatchPacket (NEW — compressed multi-packet wrapper)

## Key Format Changes

### EntityUniqueID: int32 → int64 (long) EVERYWHERE
StartGamePacket, MovePlayerPacket, UseItemPacket all use 8-byte entity IDs.

### LoginPacket (0x01)
Wire: username(RakString), protocol1(int), protocol2(int), clientId(int), skinPresent?(byte), skinLength(short), skinData(bytes)
Skin data: 8192 bytes (64x32 RGBA) or 16384 bytes (64x64 RGBA).

### PlayStatusPacket (0x02) — NEW
Wire: status(int). Values: 0=success, 1=client outdated, 2=server outdated.

### StartGamePacket (0x06) — 10 fields
Wire: seed(int), dimension(int), generator(int), entityId(LONG), spawnX(int), spawnY(int), spawnZ(int), playerX(float), playerY(float), playerZ(float)
**Changed from v17-v20**: entityId 32→64 bit, spawnY ADDED back, gamemode REMOVED.

### MovePlayerPacket (0x0E) — 9 fields
Wire: entityId(LONG), x(float), y(float), z(float), yaw(float), pitch(float), headYaw(float), mode(byte), onGround(byte)
**Changed from v17-v20**: entityId 32→64 bit, headYaw/mode/onGround NEW.
Mode: 0=normal, 1=reset, 2=teleport.

### UseItemPacket (0x1A) — 13 fields
Wire: blockX(int), blockY(int), blockZ(int), face(byte), itemId(short), itemAux(short), entityId(LONG), faceX(float), faceY(float), faceZ(float), posX(float), posY(float), posZ(float)
Face remains byte. entityId now 64-bit.

### BatchPacket (0x30)
Wire: [byte 0x30] [int payloadLength] [zlib-compressed payload]
Decompressed: concatenated serialized packets (each starts with ID byte, no per-packet length prefix).

### AddPlayerPacket (0x07) — CONFIRMED format (Ninecraft client)
Wire: clientId(long), username(string), entityId(long), x(float), y(float), z(float), speedX(float), speedY(float), speedZ(float), yaw(float), headYaw(float), pitch(float), itemId(short), itemDamage(short), slim(byte), skin(string), metadata
- Speed fields ARE required (omitting them shifts all subsequent fields by 12 bytes)
- Skin: putString format (BE short length + raw RGBA bytes). 16384 bytes = 64x64, 8192 = 64x32. Length 0 = no skin.
- Y coordinate is feet-level (not eye-level)
- Without speed fields, client reads metadata as skin/item fields. Nametag still works via SET_ENTITY_DATA override, but skin is garbled.

### RemovePlayerPacket (0x08)
Wire: entityId(long), clientId(long)

### SetEntityDataPacket (0x1D)
Wire: entityId(long), metadata
- entityId upgraded from int32 to int64

### TextPacket (0x04)
Wire: type(byte), source(string), message(string)
- type 1 = CHAT

### UpdateBlockPacket (0x10) — SILENTLY IGNORED by Ninecraft client
Wire: x(int), z(int), y(byte), blockId(byte), flagsAndMeta(byte)
- Upper nibble of flagsAndMeta = flags (NEIGHBORS|NETWORK|PRIORITY = 0x0B)
- Workaround: resend entire chunk via FullChunkData after block changes

### Metadata encoding
- Header byte: (type << 5) | (index & 0x1F)
- String values: type=4, LE short length + UTF-8 bytes
- Short values: type=1, LE short
- Byte values: type=0, byte
- Terminator: 0x7F

### Skin data
- Login sends: slim(byte) + putString(skinData) where putString = BE short length + raw bytes
- Client skin is 16384 bytes (64x64 RGBA) for 0.11.0
- Skin stored on ConnectedPlayer for forwarding between MCPE clients
- Cross-protocol players (Alpha/Classic) use Steve skin from MC 1.8 JAR (resource: mcpe/steve_skin_64x64.raw)

### Creative block breaking
- START_BREAK (action=0) triggers instant destroy for v14+
- STOP_BREAK should be ignored (prevents double-break)
- 300ms cooldown between breaks
- Raycast (Animate handler) disabled for v14+ (they send PlayerAction)

## All multi-byte fields use big-endian wire encoding.
