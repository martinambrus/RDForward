---
name: MCPE 0.7.x Protocol Details
description: MCPE Pocket Edition 0.7.x (protocol 11) implementation details, known client limitations, and packet behavior
type: project
---

## MCPE 0.7.0 (Protocol 11) - RakNet v6 UDP

### Architecture
- Custom RakNet v6 implementation in `rd-server/src/main/java/.../server/mcpe/`
- Key classes: LegacyRakNetServer, LegacyRakNetSession, MCPELoginHandler, MCPEGameplayHandler, MCPESessionWrapper, MCPEPacketBuffer, MCPEConstants
- Port: 19133 (separate from modern Bedrock on 19132)
- MCPESessionWrapper bridges Classic packet broadcasts to MCPE format

### Coordinate & Rotation Conventions
- MCPE C2S MovePlayer Y = feet-level; internal server Y = eye-level (feet + 1.62)
- MCPE yaw 0 = South; Classic yaw 0 = North; offset = +180 degrees (128 bytes)
- AddPlayer/StartGame/MovePlayer S2C Y = feet-level

### RakNet Split Packet Fixes (Critical)
- splitId uses separate counter (`session.nextSplitId()`), NOT sequence number counter
- Shared ordering index across all fragments of a split packet
- Sequence number allocated only when actually sending (not wasted on fragments)

### Chunk Format
- One section per packet (PocketMine Alpha_1.3 `getOrderedMiniChunk()` style)
- 8 packets per chunk (sections 0-7)
- Per-column per-section data: flag byte + 16 block IDs + 8 metadata nibble bytes
- Flag = `(1 << sectionIndex)` with exactly one bit set

### Block Placement
- Uses RubyDung palette: grass (ID 2) at surface Y, cobblestone (ID 4) everywhere else
- Matching all other handlers (Classic, Alpha, Netty, Bedrock)

### Known 0.7.0 Client Limitations
- **Block breaking does NOT send network packets**: Client sends only AnimatePacket (0xab) arm swings. Zero PlayerAction (0xa3) or RemoveBlock (0x96) packets. Creative mode destroys blocks client-side only. **Workaround**: Server-side raycast on arm swing — raycasts from player eye position along look direction, breaks first non-air block within 5-block reach. 300ms cooldown between breaks. Uses Classic yaw convention (0=North) from `floatYaw`.
- **Nametags do not render**: Tried AddPlayer metadata (flags, air, nametag string, show_nametag), SET_ENTITY_DATA follow-up packet, minimal metadata, full metadata. None work. The username field in AddPlayer also doesn't trigger nametag rendering. Likely a client or Ninecraft limitation.
- **UseItem face=255 spam**: Client sends UseItem with garbage coordinates and face=255 when tapping air. Handler correctly ignores these (face switch default case).

### What Works in 0.7.0
- Spawn position and terrain rendering (chunks load correctly)
- Cross-protocol movement (MCPE <-> Alpha see each other moving)
- Cross-protocol block placement (MCPE places blocks, Alpha sees them and vice versa)
- Chat (bidirectional between MCPE and Alpha)
- Player spawn/despawn (both directions)
- Disconnect detection (timeout-based)
- Block conversion (cobble/grass) for MCPE-placed blocks

### Ninecraft Client Source
- Located at ~/Ninecraft
- C++ wrapper/launcher in ninecraft/src/ - actual game logic is in compiled .so native libraries
- Test server at test_server.py (ping responder only)
- Available versions: 0.7.0, 0.7.2, 0.7.4, 0.8.0, 0.9.0, 0.9.5, 0.10.0, 0.11.0
