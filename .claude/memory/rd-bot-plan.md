# rd-bot Test Module — Implementation Status

## Overview

Gradle module `rd-bot` with headless bot clients reusing `rd-protocol` packet classes. Bots connect to a real RDServer instance, complete login, and execute test scenarios. JUnit 5 integration tests.

## Current Test Coverage (301 tests across 41 classes, all passing)

### AlphaLoginTest (2 tests)
- Parameterized login across ALL 8 Alpha versions: v13, v14, v1, v2, v3, v4, v5, v6
- Pre-1.2.0 versions (v1, v2, v13, v14) use TestServer warmup for TimSort kick
- Plus v6-specific cobblestone reception test

### BetaLoginTest (1 parameterized test)
- ALL 10 Beta versions: v7, v8, v9, v10, v11, v12, v13, v14, v17, v21
- Release pre-encryption: v22, v23, v28, v29

### ReleaseEncryptionLoginTest (1 parameterized test)
- ALL 9 encrypted Release versions: v39, v47, v49, v51, v60, v61, v73, v74, v78

### NettyLoginTest (1 parameterized test)
- 27 protocol versions from RELEASE_1_7_2 (v4) through RELEASE_1_21_11 (v774)

### ChatTest (8 tests)
- Bidirectional chat exchange across 8 protocol versions (Alpha, Beta, Release, Netty)

### CrossVersionChatTest (8 tests)
- Chat exchange between different protocol families (Alpha/Netty, Beta/Release, etc.)

### BlockPlaceTest (8 tests)
- Block placement confirmation and broadcast across multiple protocol versions

### CrossVersionBlockTest (9 tests)
- Block placement visibility across 9 cross-version pairs (Alpha, Release, Netty)

### MultiPlayerTest (6 tests)
- Player visibility when multiple bots connect across Alpha, Beta, Netty, v393, v735, v764, v774

### CrossVersionVisibilityTest (3 tests)
- SpawnPlayerPacket visibility across 6 protocol families and modern Netty versions

### JoinLeaveBroadcastTest (6 tests)
- Join/leave chat broadcasts for Alpha, Beta, Netty, v393, v764, and cross-version

### PlayerDespawnTest (6 tests)
- DestroyEntity packet broadcast when players disconnect across different versions

### BlockBreakTest (8 tests)
- Creative-mode instant block breaking and broadcast across cross-version pairs

### ColumnBuildTest (7 tests)
- 5 consecutive vertical block placements across Alpha, Beta, Netty versions

### WorldHeightLimitTest (14 tests)
- Block placement at max height (Y=63 valid, Y=64 rejected) across multiple versions

### BlockSyncTest (4 tests)
- Bidirectional block place/break sync between different protocol families

### VoidFallRespawnTest (1 test)
- Falling below Y=-10 teleports back to spawn

### VoidFallSpawnValidationTest (3 tests)
- Respawn lands on ground, Y matches spawn Y, block below is solid

### SpawnPositionTest (18 tests)
- Spawn position correctness for Alpha v6, Netty v47/v109/v477/v764/v774 clients

### CrossVersionSpawnTest (3 tests)
- 5 versions (Alpha, Beta, pre-Netty Release, Netty 1.7.x, Netty 1.8) all agree on ground, feet Y=43, grass at Y=42

### PorkchopInventoryTest (11 tests)
- v14/pre-1.0.17 porkchop distribution, v6 non-receipt, and modern protocol cobblestone

### CobblestoneReplenishmentTest (7 tests)
- Cobblestone replenishment after Q-drop and block placement across Alpha, Beta, Netty

### BedrockLoginTest (3 tests)
- Basic login, spawn position validity, chunk data reception

### BedrockChatTest (1 test)
- Bedrock send/receive chat echo

### BedrockBlockTest (2 tests)
- Bedrock block placement (UpdateBlockPacket with non-zero runtime ID)
- Bedrock block breaking (UpdateBlockPacket with runtime ID different from placed)

### BedrockCrossVersionTest (7 tests)
- Cross-version chat, block placement visibility, and despawn between Bedrock and TCP bots

### BedrockSpawnPositionTest (3 tests)
- Spawn eye-level Y correctness, feet Y above ground, chunk received and stays connected

### BedrockVoidFallTest (3 tests)
- Void fall teleports back to spawn, respawn Y matches spawn Y, bot stays connected

### BedrockBlockScenarioTest (6 tests)
- Grass conversion, 5-block column, max height success/failure, bidirectional sync (Netty + Alpha)

### BedrockAdminTimeWeatherTest (10 tests)
- Bedrock op kick/teleport TCP, TCP op kick/teleport Bedrock, time updates, time set, weather rain/clear, join/leave broadcasts (both directions)

### BedrockInventoryInteractionTest (8 tests)
- TCP inventory intact after Bedrock place/break, Alpha/Beta/Netty Q-drop with Bedrock present, concurrent building replenishment, rapid placement stability, rapid place+break stability

### BedrockMultiPlayerDespawnTest (5 tests)
- Two Bedrock bots see each other, Bedrock in multi-protocol visibility (Alpha+Netty), Bedrock→Bedrock despawn, TCP→Bedrock despawn, Bedrock→modern Netty despawn

### BedrockAdminExtendedTest (7 tests)
- Bedrock op bans/unbans TCP player, TCP op bans/unbans Bedrock player, Bedrock op tp-self-to-player, Bedrock op tp-player-to-player, kick reason broadcast to Bedrock observer

### BedrockCrossVersionExtendedTest (6 tests)
- Bedrock↔Beta chat, Bedrock↔1.13 chat, Bedrock↔1.20.2 chat, Bedrock↔1.21.11 chat, Bedrock observer sees modern Netty join/leave, modern Netty observer sees Bedrock join/leave

### GrassConversionTest (9 tests)
- Creative block conversion to grass at surface or cobblestone below for v1.8+/v477/v764/v774

### CreativeBlockPlacementTest (12 tests)
- Creative block conversion sweep and non-block item rejection across multiple versions

### InventoryClickTest (8 tests)
- Beta 1.7.3 window click handling (swap, split, drop, empty slot, close)

### InventoryNettyTest (3 tests)
- Netty v47 (creative, 1 cobblestone): left-click swap, right-click on single item, close window

### QDropTest (11 tests)
- Survival Q-drop replenishment: Alpha v6, v14, v1 (PickupSpawnPacket), Beta v14 (PlayerDiggingPacket status 4)
- Creative Q-drop no-change: Beta v17, Release v39, Netty v47, v340, v477, v764, v774

### AdminCommandTest (8 tests)
- kick, teleport, ban, and unban admin commands across Beta bots

### TimeWeatherTest (5 tests)
- Time advancement, /time set/freeze, /weather rain/clear commands

## Architecture

- `BotClient`: Netty Bootstrap, selects pipeline by protocol family (raw TCP vs VarInt-framed)
- `BotBedrockClient`: CloudburstMC RakNet client for Bedrock protocol (UDP), connects to TestServer's Bedrock port
- `BotBedrockPacketHandler`: Handles Bedrock login flow (ResourcePacksInfo->ResourcePackClientResponse->StartGame->RequestChunkRadius->PlayStatus) and S2C gameplay packets (UpdateBlock, TextPacket, AddPlayer, RemoveEntity, MovePlayer)
- `BotSession`: Thread-safe state tracker, version-aware send methods, wait-for-packet listeners, chunk block storage, `isOnGround()`/`getBlockAt()` methods. Bedrock C2S uses `sendPacketImmediately()` (not batched `sendPacket()`).
- `BotPacketHandler`: Pre-Netty handler (Alpha through v78), handles encryption for v39+, chunk parsing for Alpha + V28/V39
- `BotNettyPacketHandler`: Netty-specific handler with HANDSHAKING->LOGIN->CONFIG->PLAY state machine, chunk parsing for V39 + V47 + V107-V774
- `TestServer`: Starts RDServer on random port with Bedrock on random UDP port, handles pre-1.2.0 warmup automatically

## Key Bugs Found & Fixed

- **LoginS2CPacketV2 missing for v13/v14**: PacketRegistry registered LoginS2CPacket (with mapSeed) for ALPHA_1_0_15/16, but server sends LoginS2CPacketV2 (no mapSeed). Fixed by adding explicit registrations.
- **Netty cipher placement**: Bot placed cipher before "decoder" (=NettyPacketDecoder) instead of before "frameDecoder" (=VarIntFrameDecoder). Fixed to use correct pipeline handler names.
- **V47 registry gaps**: NettyPacketRegistry lacked V47 S2C forward entries and C2S reverse entries for bot-side decode/encode.
- **Netty block placement empty slot**: NettyBlockPlacementPacket sent empty slot (short -1), server rejected. Fixed with item-aware constructor and conditional slot writing.
- **Flaky cobblestone test**: Thread.sleep(500) replaced with polling loop (50ms intervals, 5s timeout).
- **Flaky despawn/leave tests**: Static ServerEvents listeners accumulated across test classes (no clearListeners). Fixed by adding Event.clearListeners(), ServerEvents.clearAll(), Scheduler.reset() called from RDServer.stop().
- **Bedrock TextPacket silent failure**: TextPacket requires `xuid`, `platformChatId`, `filteredMessage` set to empty string `""` (not null) or serialization silently drops the packet.
- **Bedrock sendPacketImmediately required**: `bedrockSession.sendPacket()` batches packets; bots must use `sendPacketImmediately()` for reliable delivery in tests.
- **Bedrock InventoryTransactionPacket required fields**: v924 codec requires `headPosition`, `triggerType` (PLAYER_INPUT), `clientInteractPrediction` (SUCCESS) in addition to standard fields.
- **Bedrock block breaking via InventoryTransactionPacket**: PlayerActionPacket (START_BREAK, CREATIVE_DESTROY) did not work in bot tests. InventoryTransactionPacket with actionType=2 works reliably.
- **Bedrock air runtime ID != 0**: Bedrock palette places minecraft:air at runtime ID ~12530 (not 0). Break tests compare "differs from placed" rather than "equals 0".
- **Bedrock MovePlayerPacket TELEPORT NPE**: Server set `mode=TELEPORT` without `setTeleportationCause()` in 4 places across 3 files. CloudburstMC serializer writes `teleportationCause.ordinal()` which NPEs if null. Fixed by adding `setTeleportationCause(MovePlayerPacket.TeleportationCause.UNKNOWN)` in all 4 locations.
- **Bedrock server-initiated disconnect detection**: `BedrockServerSession.disconnect()` sends a `DisconnectPacket` but RakNet-level `onDisconnect` callback fires too late for tests. Fixed by handling `DisconnectPacket` directly in `BotBedrockPacketHandler` to set `BotSession.disconnected` flag immediately.

## Not Covered

- Classic protocol bots (would need Nati-framed pipeline)
- Online mode (requires real Mojang authentication servers)
