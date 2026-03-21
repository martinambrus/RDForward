---
name: MCPE v45 Protocol (0.14.0)
description: Protocol 45 differences from v38 for MCPE 0.14.0 - wrapper byte, standalone vs batch sending, slot NBT endianness, skin format
type: reference
---

## Protocol 45 (MCPE 0.14.0 "Overworld Update")
Released February 18, 2016. Protocol range 41-70 across 0.14.0.x versions.

## Critical Change: 0x8E Wrapper Byte
All game packets (both C2S and S2C) are prefixed with 0x8E on the wire.
- Standalone: `[0x8E][PacketID][PayloadData]`
- Batch outer: `[0x8E][0x92][int compLen][compressed...]`
- Batch inner (C2S): `[int len][0x8E][PacketID][data...]` (client adds 0x8E per sub-packet)
- Batch inner (S2C): `[int len][PacketID][data...]` (server does NOT add 0x8E per sub-packet)
- RakNet connected packets (0x00, 0x09, 0x13) are NOT wrapped.
- 0x8E is safe to detect unconditionally: it is never sent C2S in pre-v45 versions (ADD_ITEM_ENTITY is S2C only).

## S2C Sending Strategy (ImagicalMine reference)
Small packets (<256 bytes) are sent as standalone `[0x8E][PacketID][data]`.
Large packets (>=256 bytes, e.g. chunks) are batch-wrapped as `[0x8E][0x92][compressed...]`.
The v45 client does NOT process batched packets during early login — PlayStatus(LOGIN_SUCCESS) MUST be standalone.

## Packet IDs
Same as v34/v38 (0x8F-0xC3 range). Confirmed via ImagicalMine ProtocolInfo (CURRENT_PROTOCOL=45).
Two new reserved IDs:
- ITEM_FRAME_DROP_ITEM_PACKET = 0xCA
- REPLACE_SELECTED_ITEM_PACKET = 0xCB
- REQUEST_CHUNK_RADIUS_PACKET = 0xC8
- CHUNK_RADIUS_UPDATE_PACKET = 0xC9

## Login Packet
Same format as v38: username, protocol1, protocol2, clientId(long), UUID(16 bytes), serverAddress, clientSecret, skinName(string), skin(short+bytes).

## PlayerList Packet
Same as v38: uuid(16), entityId(long), username(string), skinName(string), skinData(short+bytes).

## Slot/Item NBT Endianness
v38: NBT length is big-endian short.
v45: NBT length is little-endian short.
Only affects NBT length field, not item ID or damage. For items with nbtLen=0, no difference.

## Chunk Format
Unchanged from v34/v38. FullChunkData with order byte, terrain data.

## StartGame, MovePlayer, AddPlayer, TextPacket
All unchanged from v38.
