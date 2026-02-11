# RDForward Implementation Plan

Detailed, step-by-step plan to build a multiplayer, mod-enabled RubyDung
that's forward-compatible with Minecraft Alpha (and beyond).

**Tech Stack**: Fabric Loader 0.18.4 + Mixin 0.8.7, Netty 4.1.131, Alpha-format NBT, MC-compatible protocol.
**Build**: Gradle 8.14.4, Vineflower 1.11.2, JUnit 5.14.2, Java 21+.

---

## Protocol Alignment with Minecraft (Nati Protocol)

The RDForward protocol ("Nati protocol") is designed to be as close to the real
Minecraft server protocol as possible, so that when we progress to MC versions
with existing Fabric adapters, we can reuse their work directly.

### Protocol Foundation: MC Classic (wiki.vg protocol version 7)

The earliest fully documented MC protocol is **Classic** (c0.0.20a - c0.30),
with 16 packet types, all fixed-size. Since RubyDung is a pre-Classic prototype,
Classic is the natural starting point.

**Wire format (Nati framing):**
- `[4 bytes]` length prefix (our Netty extension — real MC Classic has none)
- `[1 byte]` packet ID (matches real MC Classic IDs from wiki.vg)
- `[N bytes]` payload (MC-compatible field structure)

**Classic packet IDs implemented (all 16):**

| ID | C→S | S→C | Name |
|----|-----|-----|------|
| 0x00 | PlayerIdentification | ServerIdentification | Login |
| 0x01 | — | Ping | Keep-alive |
| 0x02 | — | LevelInitialize | World transfer start |
| 0x03 | — | LevelDataChunk | World data (1KB chunks) |
| 0x04 | — | LevelFinalize | World transfer end |
| 0x05 | SetBlock | — | Block place/break |
| 0x06 | — | SetBlock | Block change broadcast |
| 0x07 | — | SpawnPlayer | Player join |
| 0x08 | PlayerTeleport | PlayerTeleport | Absolute position |
| 0x09 | — | PosOrientUpdate | Relative pos+rot |
| 0x0A | — | PositionUpdate | Relative pos only |
| 0x0B | — | OrientationUpdate | Rotation only |
| 0x0C | — | DespawnPlayer | Player leave |
| 0x0D | Message | Message | Chat |
| 0x0E | — | Disconnect | Kick with reason |
| 0x0F | — | UpdateUserType | Op status |

### Version Progression Path

1. **RubyDung (version 0)** — uses Classic packet format (our custom version number)
2. **Classic (version 7)** — real MC Classic protocol, identical packets
3. **Alpha 1.0.15 (version 10)** — first Alpha SMP, different packet ID space (0x00-0xFF)
4. **Alpha 1.2.6 (version 14)** — final Alpha, adds health/time/mobs/explosions

The `PacketRegistry` resolves (version, direction, packetId) → Packet class,
handling the ID overlap between Classic and Alpha. The `VersionTranslator` chain
converts between protocol versions, matching ViaLegacy's `c0_28_30toa1_0_15`
translation path.

### Data Type Compatibility

| Era | Strings | Coordinates | Angles | Framing |
|-----|---------|-------------|--------|---------|
| Classic | 64-byte fixed, ASCII | Fixed-point shorts (/32) | Byte (0-255) | None (fixed-size) |
| Alpha/Beta | string16 (short + UTF-16BE) | int/double | float (degrees) | None (known layout) |
| 1.7+ (future) | VarInt + UTF-8 | double + VarInt | float | VarInt length |

Our `McDataTypes` class supports all three formats for forward compatibility.

---

## Phase 1: Fabric Loader Integration (Replace rd-api)

The current `rd-api` module contains a custom mod loader (reflection-based `RDMod*` naming convention).
This will be replaced with Fabric Loader, which provides proper mod discovery, dependency resolution,
entrypoints, and Mixin-based code injection.

### Step 1.1: Create Fabric Game Provider

Fabric Loader is game-agnostic — it needs a "game provider" plugin to tell it how to launch our game.

- [x] Create `RubyDungGameProvider` implementing `net.fabricmc.loader.impl.game.GameProvider`
- [x] Implement `locateGame()` — find the RubyDung JAR (or classes directory)
- [x] Implement `initialize()` — set up classpaths
- [x] Implement `launch()` — call `RubyDung.main()` (the original game entry point)
- [x] Register the provider in `META-INF/services/net.fabricmc.loader.impl.game.GameProvider`

**Key files:**
- `rd-client/src/main/java/.../fabric/RubyDungGameProvider.java`
- `rd-client/src/main/resources/META-INF/services/net.fabricmc.loader.impl.game.GameProvider`

### Step 1.2: Set Up Mixin for RubyDung Classes

Since RubyDung isn't obfuscated, we can target classes by their real names.

- [x] Create `rubydung.mixins.json` mixin config
- [x] Create `@Mixin(RubyDung.class)` to inject mod initialization into the game's `run()` method
- [ ] Create `@Mixin(Level.class)` to inject world save/load hooks
- [ ] Create `@Mixin(Player.class)` to inject movement hooks for multiplayer sync
- [ ] Create `@Mixin(Timer.class)` to inject tick event hooks

**Key files:**
- `rd-client/src/main/resources/rubydung.mixins.json`
- `rd-client/src/main/java/.../mixin/RubyDungMixin.java`
- `rd-client/src/main/java/.../mixin/LevelMixin.java`
- `rd-client/src/main/java/.../mixin/PlayerMixin.java`

### Step 1.3: Define Fabric-Style Mod Entrypoints

- [x] Use Fabric's built-in `ModInitializer` / `ClientModInitializer` / `ServerModInitializer`
- [x] Define `fabric.mod.json` for RDForward (with mixin and entrypoint declarations)

### Step 1.4: Replace Old ModLoader

- [x] Remove the entire `rd-api` module (replaced by Fabric Loader)
- [ ] Create Fabric-compatible event system for keyboard/timer/game events

### Step 1.5: Build System Updates

- [x] Add Fabric Loader as a dependency (via Fabric Maven)
- [x] Add SpongePowered Mixin as a compile dependency
- [x] Create `runModdedClient` task (launches via Fabric Knot)
- [x] Ensure the build produces a JAR that Fabric Loader can launch

---

## Phase 2: Multiplayer Networking (rd-protocol + rd-server)

### Step 2.1: Complete the Packet Set

**DONE** — All 17 MC Classic protocol packet classes are implemented in
`rd-protocol/src/main/java/.../packet/classic/`. The packet IDs match the
real MC Classic protocol (wiki.vg protocol version 7).

Remaining for Alpha protocol (when we reach Alpha versions):

- [ ] Alpha login packets (0x01 Login, 0x02 Handshake — different from Classic)
- [ ] Alpha chunk packets (0x32 PreChunk, 0x33 MapChunk — chunk-based instead of full-world)
- [ ] Alpha entity packets (0x14 AddPlayer, 0x1D RemoveEntities, etc.)
- [ ] Alpha block packets (0x35 BlockUpdate — uses int coords instead of short)
- [ ] Alpha keep-alive (0x00 — different from Classic 0x01 Ping)

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

- [ ] On connect: server sends world via Classic protocol (LevelInitialize + LevelDataChunks + LevelFinalize)
- [ ] Server sends delta updates via `SetBlockServerPacket` (Classic 0x06) for subsequent changes
- [ ] Client maintains local world copy for rendering
- [ ] Client sends block place/break requests; server validates and responds
- [ ] Implement simple lag compensation: client predicts block changes, reverts if server rejects

### Step 2.5: Player Position Sync

- [ ] Client sends position updates at a fixed rate (e.g., every 50ms)
- [ ] Server validates movement (no teleporting through blocks)
- [ ] Server broadcasts other players' positions to each client
- [ ] Client interpolates remote player positions between updates

### Step 2.6: Connection Lifecycle

- [ ] Implement login timeout (disconnect if no PlayerIdentification within 5 seconds)
- [ ] Implement Ping (server sends Classic 0x01 PingPacket periodically)
- [ ] Handle graceful disconnect (send Classic 0x0E DisconnectPacket before closing)
- [ ] Handle unexpected disconnect (remove player, broadcast leave)
- [ ] Implement max player count enforcement

---

## Phase 3: Cross-Version Protocol Translation

### Step 3.1: Complete Block Translation Tables

**DONE** — Three translation tables implemented in `BlockTranslator`:
- Classic (50 blocks) -> RubyDung (3 blocks)
- Alpha (82 blocks) -> RubyDung (3 blocks)
- Alpha (82 blocks) -> Classic (50 blocks)

Remaining:

- [ ] Make translation tables data-driven (load from JSON/NBT files, not hardcoded)
- [ ] Add unit tests for all block translations
- [ ] Add RubyDung -> Classic (upward translation, mostly pass-through)

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

**BuildTools approach** (Spigot-style, avoids redistributing Mojang code):

- [x] Create `rd-game` module with BuildTools Gradle tasks
- [x] `downloadRubyDung` — downloads rd-132211.jar from Mojang CDN, verifies SHA1
- [x] `decompileRubyDung` — decompiles with Vineflower (Fabric ecosystem standard)
- [x] `extractSources` / `extractResources` — populates `rd-game/src/`
- [x] `applyPatches` — applies patches from `rd-game/patches/`
- [x] `setupWorkspace` — orchestrates full pipeline (one command)
- [x] `rebuildPatches` — generates patches from modified source (dev workflow)
- [x] `runClient` — launches the game with LWJGL natives
- [x] `extractNatives` — extracts platform-specific LWJGL native libraries
- [x] GitHub Actions CI (build verification on push/PR)
- [x] GitHub Actions Release (creates GitHub Release on version tags, no JAR artifacts)
- [x] Versioning via `gradle.properties` + git tags (`vX.Y.Z`)
- [x] Verify the game launches in single-player mode
- [x] Create Fabric Loader launch wrapper

**Build commands:**
```bash
./gradlew :rd-game:setupWorkspace   # First-time: download + decompile + patch
./gradlew :rd-game:runClient        # Launch the game
./gradlew :rd-game:rebuildPatches   # Save source changes as patches
./gradlew build                     # Build all modules
```

**rd-132211 details:**
- Source: `https://launcher.mojang.com/v1/objects/<sha1>/client.jar`
- SHA1: `393e8d4b4d708587e2accd7c5221db65365e1075`
- Size: 26,704 bytes (~6 classes)
- Package: `com.mojang.rubydung`
- Main class: `com.mojang.rubydung.RubyDung`
- Dependencies: LWJGL 2.9.3 (from Maven Central)

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

## Phase 8: Android Port (via libGDX)

Android requires replacing LWJGL (desktop-only) with a cross-platform rendering
backend. **libGDX** is the best fit: it targets Desktop (LWJGL), Android (OpenGL ES),
and iOS, with a single shared codebase. The multiplayer networking layer (Netty,
rd-protocol, rd-world) is pure Java and works on Android unchanged.

### Step 8.1: Introduce libGDX Abstraction Layer

- [ ] Add `rd-render` module — rendering interface that abstracts OpenGL calls
- [ ] Define `RDRenderer` interface: `init()`, `render()`, `resize()`, `dispose()`
- [ ] Define `RDInput` interface: keyboard polling, mouse/touch input
- [ ] Extract RubyDung's rendering logic into `RDRenderer` implementations
- [ ] Create `DesktopRenderer` (LWJGL 3 backend, current code) implementing `RDRenderer`

### Step 8.2: libGDX Desktop Backend

- [ ] Add libGDX core + desktop dependencies to a new `rd-desktop` module
- [ ] Create `LibGDXRenderer` implementing `RDRenderer` using libGDX's OpenGL wrapper
- [ ] Port fixed-function GL calls to libGDX equivalents (or raw GLES20 calls)
- [ ] Verify desktop parity: same rendering output as LWJGL 3 backend

### Step 8.3: Android Module

- [ ] Create `rd-android` module (Android Gradle plugin)
- [ ] Create `AndroidLauncher` extending `AndroidApplication`
- [ ] Replace `GL11.glBegin/glEnd` (fixed-function) with vertex buffer objects (VBOs)
  - OpenGL ES 2.0+ has no fixed-function pipeline — all rendering must use shaders
  - This is the biggest porting effort: RubyDung uses `glBegin/glVertex3f/glEnd` everywhere
- [ ] Write minimal vertex + fragment shaders for block rendering
- [ ] Port `Tesselator` to use VBOs instead of immediate mode
- [ ] Handle touch input mapping (tap = click, drag = mouse look, on-screen buttons = WASD)
- [ ] Test on Android emulator and physical device

### Step 8.4: Shared Multiplayer on Android

- [ ] Verify Netty works on Android (it does — used by many Android apps)
- [ ] Create Android-specific UI for server connect (native Android dialog or libGDX scene2d)
- [ ] Test full multiplayer flow: Android client connecting to desktop server

### Key Considerations

| Concern | Desktop (current) | Android |
|---------|-------------------|---------|
| Rendering API | OpenGL 1.1 (fixed-function) | OpenGL ES 2.0+ (shader-based) |
| Input | Keyboard + Mouse (GLFW) | Touch screen (libGDX Input) |
| Networking | Netty (unchanged) | Netty (unchanged) |
| Window management | GLFW | Android Activity lifecycle |
| Texture loading | BufferedImage + GL11 | libGDX Texture / Pixmap |
| Build system | Gradle (Java) | Gradle (Android plugin) |

The fixed-function → shader pipeline conversion is the critical path. All other
changes (input, lifecycle, UI) are straightforward libGDX patterns.

---

## Implementation Order (Recommended)

The phases above are organized by domain, but the optimal implementation
order interleaves them to always have something testable:

1. **Phase 5.1** — Get RubyDung launching first (**BuildTools done**, verify launch pending)
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
15. **Phase 8.1-8.2** — libGDX abstraction + desktop backend (rendering refactor)
16. **Phase 8.3** — Android module (shader pipeline, touch input)
17. **Phase 8.4** — Android multiplayer testing
