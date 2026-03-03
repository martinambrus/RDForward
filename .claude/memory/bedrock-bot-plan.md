# Bedrock Bot Implementation Plan

## Problem

CloudburstMC Protocol library (used for Bedrock server support) is server-only. It provides codec/packet classes and a RakNet server transport, but no RakNet client. A headless Bedrock bot needs a client-side RakNet implementation.

## Options

### Option 1: GeyserMC's MCProtocolLib RakNet Client (Recommended)

- GeyserMC maintains a RakNet client in their `mcprotocollib` project
- MIT-licensed, actively maintained
- Handles RakNet session management, reliability layer, fragmentation
- Estimated effort: 2-3 weeks

### Option 2: From-Scratch RakNet Client

- Implement minimal RakNet (connection request, reliability, fragmentation)
- No external dependency beyond CloudburstMC codec
- High effort: 4-6 weeks, fragile

## Architecture

### BedrockBotClient (~280 LOC)

- Creates RakNet client session to server
- Pipeline: RakNet transport -> CloudburstMC codec -> BedrockBotPacketHandler
- Manages connection lifecycle and session state

### BedrockBotPacketHandler (~600 LOC)

- Login flow: RequestNetworkSettings -> NetworkSettings -> Login -> PlayStatus -> ResourcePacks -> StartGame
- Must handle encryption (if server enables it) and compression
- Routes S2C packets to BotSession

## Login Flow

1. Client sends `RequestNetworkSettingsPacket` (protocol version)
2. Server responds with `NetworkSettingsPacket` (compression threshold)
3. Client sends `LoginPacket` (chain data, skin data)
4. Server sends `PlayStatusPacket` (LOGIN_SUCCESS)
5. Server sends `ResourcePacksInfoPacket`
6. Client responds `ResourcePackClientResponsePacket` (COMPLETED)
7. Server sends `StartGamePacket` (entityId, spawn position, world settings)
8. Client sends `SetLocalPlayerAsInitializedPacket`

## Key S2C Packets

| Packet | Data Extracted |
|--------|---------------|
| StartGamePacket | entityId, spawn position, runtime block palette |
| MovePlayerPacket | position (eye-level), rotation |
| LevelChunkPacket | chunk data (subchunks with block palette) |
| TextPacket | chat messages |
| AddPlayerPacket | spawned players (entityId, name, position) |
| RemoveEntityPacket | despawned entities |
| UpdateBlockPacket | block changes |
| InventorySlotPacket | inventory slot updates |

## Key C2S Packets

| Packet | Purpose |
|--------|---------|
| PlayerAuthInputPacket | position + rotation (sent every tick) |
| TextPacket | chat messages |
| InventoryTransactionPacket | block placement (UseItemOnEntity type) |
| PlayerActionPacket | block breaking (DIMENSION_CHANGE_REQUEST_OR_CREATIVE_DESTROY_BLOCK for creative) |

## Position and Rotation Notes

- MovePlayerPacket position = eye/head level
- AddPlayerPacket/StartGamePacket/PlayerAuthInputPacket = feet level
- MovePlayerPacket/AddPlayerPacket rotation = (yaw, pitch, headYaw)
- PlayerAuthInputPacket rotation = (pitch, yaw, headYaw)
- Pitch: signed degrees -90 to 90

## Test Scenarios

Parallel to existing TCP bot tests:

- BedrockLoginTest: connect, verify StartGamePacket received
- BedrockSpawnPositionTest: verify spawn at expected Y
- BedrockChunkLoadingTest: verify LevelChunkPacket received for spawn chunks
- BedrockCrossVersionVisibility: Bedrock + Alpha/Netty bots see each other
- BedrockCrossVersionChat: Bedrock <-> Alpha/Netty bidirectional chat
- BedrockBlockPlacementTest: Bedrock places block, TCP bot sees it
- BedrockBlockBreakTest: creative block breaking cross-version

## Dependencies

- GeyserMC RakNet client (or equivalent)
- CloudburstMC Protocol library (already in project)
- Possibly `jose4j` or similar for JWT in LoginPacket chain data

## Estimated Effort

- With GeyserMC client: 2-3 weeks
- From scratch: 4-6 weeks
