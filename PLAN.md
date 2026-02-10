# RDForward Implementation Plan

Detailed, step-by-step plan to build a multiplayer, mod-enabled RubyDung
that's forward-compatible with Minecraft Alpha (and beyond).

**Tech Stack**: Fabric Loader + Mixin, Netty, Alpha-format NBT, custom protocol with capability negotiation.

---

## Phase 1: Fabric Loader Integration (Replace rd-api)

The current `rd-api` module contains a custom mod loader (reflection-based `RDMod*` naming convention).
This will be replaced with Fabric Loader, which provides proper mod discovery, dependency resolution,
entrypoints, and Mixin-based code injection.

### Step 1.1: Create Fabric Game Provider

Fabric Loader is game-agnostic — it needs a "game provider" plugin to tell it how to launch our game.

- [ ] Create `RubyDungGameProvider` implementing `net.fabricmc.loader.impl.game.GameProvider`
- [ ] Implement `locateGame()` — find the RubyDung JAR (or classes directory)
- [ ] Implement `initialize()` — set up classpaths
- [ ] Implement `launch()` — call `RubyDung.main()` (the original game entry point)
- [ ] Register the provider in `META-INF/services/net.fabricmc.loader.impl.game.GameProvider`

**Key files:**
- `rd-client/src/main/java/.../fabric/RubyDungGameProvider.java`
- `rd-client/src/main/resources/META-INF/services/net.fabricmc.loader.impl.game.GameProvider`

### Step 1.2: Set Up Mixin for RubyDung Classes

Since RubyDung isn't obfuscated, we can target classes by their real names.

- [ ] Create `rubydung.mixins.json` mixin config
- [ ] Create `@Mixin(RubyDung.class)` to inject mod initialization into the game's `run()` method
- [ ] Create `@Mixin(Level.class)` to inject world save/load hooks
- [ ] Create `@Mixin(Player.class)` to inject movement hooks for multiplayer sync
- [ ] Create `@Mixin(Timer.class)` to inject tick event hooks

**Key files:**
- `rd-client/src/main/resources/rubydung.mixins.json`
- `rd-client/src/main/java/.../mixin/RubyDungMixin.java`
- `rd-client/src/main/java/.../mixin/LevelMixin.java`
- `rd-client/src/main/java/.../mixin/PlayerMixin.java`

### Step 1.3: Define Fabric-Style Mod Entrypoints

- [ ] Create `ModInitializer` entrypoint interface (or use Fabric's)
- [ ] Create `ClientModInitializer` for client-side mods
- [ ] Create `ServerModInitializer` for server-side mods
- [ ] Define `fabric.mod.json` schema for RDForward mods

### Step 1.4: Replace Old ModLoader

- [ ] Remove the old `rd-api/utils/ModLoader.java` reflection-based loader
- [ ] Replace `Game.java`, `GameInterface.java` with Fabric-compatible event system
- [ ] Keep keyboard/timer listeners as events within the new system
- [ ] Deprecate `RDMod*` naming convention in favor of `fabric.mod.json`

### Step 1.5: Build System Updates

- [ ] Add Fabric Loader as a dependency (via Fabric Maven)
- [ ] Add SpongePowered Mixin as a dependency
- [ ] Configure Mixin annotation processor in Gradle
- [ ] Create a Fabric Loom-compatible dev environment (or minimal equivalent)
- [ ] Ensure the build produces a JAR that Fabric Loader can launch

---

## Phase 2: Multiplayer Networking (rd-protocol + rd-server)

### Step 2.1: Complete the Packet Set

The skeleton already has Handshake, BlockChange, PlayerPosition, and ChatMessage.
Additional packets needed:

- [ ] `ChunkDataPacket` — compressed chunk data (blocks + metadata) for initial world sync
- [ ] `PlayerJoinPacket` — broadcast when a player joins (ID, name, initial position)
- [ ] `PlayerLeavePacket` — broadcast when a player disconnects
- [ ] `DisconnectPacket` — sent by either side to terminate connection (with reason string)
- [ ] `KeepAlivePacket` — periodic ping to detect dead connections

### Step 2.2: Server World State Manager

The server needs to own the authoritative world state.

- [ ] Create `ServerWorld` class — holds the full block array for the world
- [ ] Implement block get/set with bounds checking
- [ ] Implement player tracking (connected players, positions, protocol versions)
- [ ] Implement tick loop (20 TPS, matching Minecraft standard)
- [ ] Implement block change validation (is the position valid? is the block type valid for this version?)

### Step 2.3: Server Tick Loop

- [ ] Create `ServerTickLoop` — runs at 20 TPS (50ms per tick)
- [ ] Each tick: process queued player actions, update world state, broadcast changes
- [ ] Handle player position updates (aggregate and broadcast at tick rate, not per-packet)
- [ ] Handle chunk loading/unloading based on player positions

### Step 2.4: Client-Server World Sync

- [ ] On connect: server sends all chunks within render distance as `ChunkDataPacket`s
- [ ] Server sends delta updates via `BlockChangePacket` for subsequent changes
- [ ] Client maintains local world copy for rendering
- [ ] Client sends block place/break requests; server validates and responds
- [ ] Implement simple lag compensation: client predicts block changes, reverts if server rejects

### Step 2.5: Player Position Sync

- [ ] Client sends position updates at a fixed rate (e.g., every 50ms)
- [ ] Server validates movement (no teleporting through blocks)
- [ ] Server broadcasts other players' positions to each client
- [ ] Client interpolates remote player positions between updates

### Step 2.6: Connection Lifecycle

- [ ] Implement handshake timeout (disconnect if no handshake within 5 seconds)
- [ ] Implement KeepAlive (server sends every 15 seconds, client responds)
- [ ] Handle graceful disconnect (send DisconnectPacket before closing)
- [ ] Handle unexpected disconnect (remove player, broadcast leave)
- [ ] Implement max player count enforcement

---

## Phase 3: Cross-Version Protocol Translation

### Step 3.1: Complete Block Translation Tables

The skeleton has Alpha -> RubyDung. We need the reverse too.

- [ ] Complete Alpha -> RubyDung block translation (all 92 block IDs)
- [ ] Create RubyDung -> Alpha translation (Grass -> Grass, Cobble -> Cobble, Air -> Air)
- [ ] Make translation tables data-driven (load from JSON/NBT files, not hardcoded)
- [ ] Add unit tests for all block translations

### Step 3.2: Action Translation

Implement the "interrupt signal" concept for cross-version actions:

- [ ] Mining adapter: RubyDung has instant break, Alpha requires multiple hits
  - Server tracks mining progress per player per block
  - Older client's break request starts the timer instead of instant-breaking
  - Server sends break confirmation only after mining timer completes
  - Optional: send mining progress particles/animation to clients that support it

- [ ] Inventory adapter: RubyDung has no inventory, Alpha does
  - Server tracks inventory for all players
  - Inventory packets are only sent to clients that advertise the INVENTORY capability
  - Item drops from blocks are only visible to inventory-capable clients
  - Non-inventory clients get instant block placement (no item consumption)

### Step 3.3: Chunk Data Translation

When sending chunk data to an older client:

- [ ] Run every block ID through the BlockTranslator before sending
- [ ] Strip metadata nibble array for clients that don't support BLOCK_METADATA
- [ ] Adjust chunk height if the world exceeds the client's Y limit
- [ ] Compress chunks with GZip before sending (reduces bandwidth for all versions)

### Step 3.4: Outbound Packet Filtering

The VersionTranslator already drops packets the client can't understand.
Extend this with finer-grained filtering:

- [ ] Filter entity packets for pre-entity clients
- [ ] Filter time/day-night packets for pre-Alpha clients
- [ ] Filter health/damage packets for pre-survival clients
- [ ] Log filtered packets at debug level for development

---

## Phase 4: World Persistence

### Step 4.1: Complete Alpha-Format Save/Load

The skeleton has basic chunk and level.dat serialization. Complete it:

- [ ] Implement entity serialization in chunks (position, type, NBT data)
- [ ] Implement tile entity serialization (signs, chests, etc.)
- [ ] Implement player data in level.dat (position, rotation, health, inventory)
- [ ] Handle the session.lock ownership mechanism
- [ ] Implement auto-save (configurable interval, default every 5 minutes)

### Step 4.2: World Generation

RubyDung's original world is a simple flat terrain with random blocks:

- [ ] Port the original RubyDung terrain generator (from decompiled source)
- [ ] Wrap it in a `WorldGenerator` interface so mods can replace it
- [ ] Add a simple Alpha-style terrain generator option (hills, caves, ores)
- [ ] Make world size configurable (default 200x200x200 for RubyDung mode)

### Step 4.3: World Upgrade Path

Enable converting worlds between formats:

- [ ] RubyDung world -> Alpha format: add missing NBT fields with defaults
- [ ] Alpha format -> Region format (.mcr): implement McRegion writer for Beta+ compat
- [ ] Create a CLI tool: `java -jar rd-world.jar convert <input-dir> <output-dir> <target-format>`

---

## Phase 5: Client Modifications

### Step 5.1: Integrate Decompiled RubyDung

- [ ] Set up the build to download and decompile RubyDung JAR (from archive.org)
- [ ] Apply existing patches from RDModded
- [ ] Verify the game launches in single-player mode
- [ ] Create Fabric Loader launch wrapper

### Step 5.2: Add Multiplayer Client UI

The original RubyDung has no multiplayer UI. Add minimal functionality:

- [ ] Server connect screen (text input for host:port)
- [ ] Player name entry
- [ ] Server list (hardcoded or file-based initially)
- [ ] In-game player list (Tab key)
- [ ] Chat overlay (T key to open, Enter to send)

### Step 5.3: Render Remote Players

- [ ] Create a simple player model (colored cube or wireframe)
- [ ] Render remote players at their server-reported positions
- [ ] Interpolate movement between position updates
- [ ] Show player names above their heads (simple text rendering)

### Step 5.4: Client-Side Prediction

- [ ] Predict local player movement (render immediately, reconcile with server)
- [ ] Predict block placement (show immediately, revert if server rejects)
- [ ] Handle rubber-banding gracefully

---

## Phase 6: Mod Events and API

### Step 6.1: Event System

Using Fabric's event pattern, create game events that mods can listen to:

- [ ] `BlockBreakEvent` — fired before a block is broken, cancellable
- [ ] `BlockPlaceEvent` — fired before a block is placed, cancellable
- [ ] `PlayerJoinEvent` — fired when a player connects
- [ ] `PlayerLeaveEvent` — fired when a player disconnects
- [ ] `PlayerMoveEvent` — fired on position update
- [ ] `ChatEvent` — fired on chat message, cancellable/modifiable
- [ ] `ServerTickEvent` — fired every server tick
- [ ] `WorldSaveEvent` — fired before world save

### Step 6.2: Server-Side Mod API

- [ ] Command system: register custom commands (e.g., `/spawn`, `/tp`)
- [ ] Permission system: basic op/non-op distinction
- [ ] Configuration API: per-mod config files
- [ ] Scheduler API: run tasks on future ticks

### Step 6.3: Client-Side Mod API

- [ ] Keyboard binding API: register custom key handlers
- [ ] Render overlay API: draw 2D elements on screen
- [ ] Block texture replacement API (already proven with the chocolate blocks mod)

---

## Phase 7: Testing and Polish

### Step 7.1: Unit Tests

- [ ] BlockTranslator: verify all Alpha -> RubyDung mappings
- [ ] AlphaChunk: verify block get/set, nibble arrays, height map
- [ ] AlphaLevelFormat: verify round-trip save/load
- [ ] PacketEncoder/Decoder: verify round-trip for all packet types
- [ ] Capability negotiation: verify intersection logic

### Step 7.2: Integration Tests

- [ ] Start server, connect client, verify handshake completes
- [ ] Connect clients at different protocol versions, verify translation
- [ ] Place/break blocks, verify all connected clients see the change
- [ ] Save world, restart server, verify world loads correctly
- [ ] Load saved world in actual Minecraft Alpha, verify it works

### Step 7.3: Performance Testing

- [ ] Benchmark with 10, 50, 100 simultaneous clients
- [ ] Measure chunk send bandwidth per client
- [ ] Profile server tick time under load
- [ ] Optimize hot paths (block translation, chunk serialization)

---

## Implementation Order (Recommended)

The phases above are organized by domain, but the optimal implementation
order interleaves them to always have something testable:

1. **Phase 5.1** — Get RubyDung launching first (can't test anything without it)
2. **Phase 1.1-1.3** — Fabric Loader integration (mod loading works)
3. **Phase 2.1-2.2** — Basic Netty server + world state (server starts)
4. **Phase 2.4** — Client-server world sync (two players see same world)
5. **Phase 2.5** — Player position sync (players can see each other)
6. **Phase 4.1** — World save/load (worlds persist across restarts)
7. **Phase 3.1-3.3** — Version translation (RubyDung client on Alpha server)
8. **Phase 6.1** — Event system (mods can react to game events)
9. **Phase 5.2-5.3** — Multiplayer UI (chat, player list, server browser)
10. **Phase 4.2** — World generation (procedural worlds)
11. **Phase 3.2** — Action translation (mining, inventory cross-version)
12. **Phase 6.2-6.3** — Mod APIs (commands, permissions, rendering)
13. **Phase 4.3** — World format upgrader (export to Alpha/Beta)
14. **Phase 7** — Tests and performance optimization
