---
name: MCPE v9 Protocol (0.6.1)
description: Protocol 9 differences from v11 for MCPE 0.6.1 - no SetEntityLink (IDs 0xA8+ shifted -1), simpler login/AddPlayer, PlaceBlock 0x95, ClientMessage 0xB4, RakNet v5
type: reference
---

## Protocol 9 (MCPE 0.6.1)

Source: PocketMine-MP Alpha_1.1 at `/tmp/pocketmine-alpha11/`

## RakNet Version: 5 (v11+ uses 6)
Server already accepts any RakNet protocol version (reads but doesn't validate).

## No SetEntityLink Packet
All packet IDs >= 0xA8 shift -1 from v11/v12 canonical.
toWireId: canonical >= 0xA9 → wire = canonical - 1.
toCanonicalId: wire >= 0xA8 → canonical = wire + 1.

## Login Format
`string username, int protocol1, int protocol2` — NO clientId, NO skin data.
v11+ adds `int clientId` and optional skin fields.

## AddPlayer (0x89)
`long clientID, string username, int eid, float x, float y, float z, metadata`
No yaw, pitch, or held item fields (added in v11).

## PlaceBlock (0x95) — v9-Only C2S
`int eid, int x, int z, byte y, byte block, byte meta, byte face`
PocketMine doesn't actually handle this — v9 client uses UseItem (0xA2) for placement.
Handler added as safety net.

## ClientMessage (0xB4) — v9-Only C2S Chat
`string message` — separate from S2C MESSAGE (0x85).
In v11+, chat became bidirectional at 0x85 and ClientMessage was dropped.

## AdventureSettings (0xB6 S2C)
Encode writes single byte 0xFF. No flags int.

## SetTime (0x86)
`int time` only — no trailing flag byte (v11+ adds 0x80/0x00 flag).

## StartGame (0x87)
`int seed, int generator, int gamemode, int eid, float x, float y, float z`
Same as v11 (no dimension field, which was added in v17).

## MovePlayer (0x94)
`int eid, float x, float y, float z, float yaw, float pitch`
Same as v11-v13. Y is feet-level.

## Chunk Format
Section-based (same as v11-v14). 8 sections per chunk, 256 columns each.
