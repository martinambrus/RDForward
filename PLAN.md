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
- [x] Create `@Mixin(Level.class)` — implemented as `LevelAccessor` (accessor mixin for block array)
- [x] Create `@Mixin(Player.class)` — implemented as `PlayerAccessor` (accessor mixin for position fields)
- [x] Create `@Mixin(Timer.class)` — implemented as `TimerAccessor` (accessor mixin for tick count, partial tick); RubyDungMixin now uses Timer's actual tick count for frame-rate-independent position updates

**Key files:**
- `rd-client/src/main/resources/rdforward.mixins.json`
- `rd-client/src/main/java/.../mixin/RubyDungMixin.java`
- `rd-client/src/main/java/.../mixin/LevelAccessor.java`
- `rd-client/src/main/java/.../mixin/PlayerAccessor.java`
- `rd-client/src/main/java/.../mixin/TimerAccessor.java`

### Step 1.3: Define Fabric-Style Mod Entrypoints

- [x] Use Fabric's built-in `ModInitializer` / `ClientModInitializer` / `ServerModInitializer`
- [x] Define `fabric.mod.json` for RDForward (with mixin and entrypoint declarations)

### Step 1.4: Replace Old ModLoader

- [x] Remove the entire `rd-api` module (replaced by Fabric Loader)
- [ ] Create Fabric-compatible event system for keyboard/timer/game events (deferred to Phase 6.1)

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

Alpha protocol packets — 26 classes in `rd-protocol/src/main/java/.../packet/alpha/`:

- [x] Alpha login packets (0x01 LoginC2S/LoginS2C, 0x02 HandshakeC2S/HandshakeS2C, 0xFF Disconnect)
- [x] Alpha keep-alive (0x00 KeepAlive — empty packet, replaces Classic 0x01 Ping)
- [x] Alpha chat (0x03 Chat — string16, replaces Classic 0x0D Message)
- [x] Alpha player movement (0x0A OnGround, 0x0B Position, 0x0C Look, 0x0D PositionAndLook C2S/S2C — note y/stance swap in S2C)
- [x] Alpha player actions (0x0E Digging, 0x0F BlockPlacement — with conditional item data for protocol 14)
- [x] Alpha chunk packets (0x32 PreChunk, 0x33 MapChunk — chunk-based instead of full-world)
- [x] Alpha entity packets (0x14 SpawnPlayer, 0x1D DestroyEntity, 0x1F RelativeMove, 0x20 Look, 0x21 LookAndMove, 0x22 Teleport)
- [x] Alpha block packet (0x35 BlockChange — int coords + metadata, replaces Classic 0x06 SetBlock)
- [x] Alpha game state (0x04 TimeUpdate, 0x06 SpawnPosition, 0x08 UpdateHealth)
- [x] All registered in PacketRegistry for ALPHA_1_0_15 and ALPHA_1_2_6

### Step 2.2: Server World State Manager

The server needs to own the authoritative world state.

- [x] Create `ServerWorld` class — holds the full block array (256x64x256) for the world
- [x] Implement block get/set with bounds checking
- [x] Implement player tracking (connected players, positions, protocol versions) — via `PlayerManager`
- [x] Implement tick loop (20 TPS, matching Minecraft standard) — via `ServerTickLoop`
- [x] Implement block change validation (bounds checking in `ServerWorld.setBlock()`)

### Step 2.3: Server Tick Loop

- [x] Create `ServerTickLoop` — runs at 20 TPS (50ms per tick)
- [x] Each tick: process queued block changes, broadcast confirmed changes, pings, auto-save
- [x] Handle player position updates — currently broadcast per-packet in `ServerConnectionHandler`
- [x] Handle chunk loading/unloading based on player positions — `ChunkManager` tracks loaded chunks per player, generates/loads chunks on demand via `WorldGenerator.generateChunk()` + `AlphaLevelFormat`, sends `PreChunkPacket` + `MapChunkPacket` for newly visible chunks, unloads distant chunks when no player needs them; `ChunkCoord` provides immutable coordinate keys; `AlphaChunk.serializeForAlphaProtocol()` produces zlib-compressed data for the wire; tick loop updates every 5 ticks (250ms); dirty chunks saved to disk on auto-save and server shutdown

### Step 2.4: Client-Server World Sync

- [x] On connect: server sends world via Classic protocol (LevelInitialize + LevelDataChunks + LevelFinalize)
- [x] Server sends delta updates via `SetBlockServerPacket` (Classic 0x06) for subsequent changes
- [x] Client maintains local world copy for rendering (world replacement via LevelAccessor)
- [x] Client sends block place/break requests; server validates and broadcasts
- [x] Implement simple lag compensation: client predicts block changes, reverts after 2s timeout if server doesn't confirm

### Step 2.5: Player Position Sync

- [x] Client sends position updates at a fixed rate (every 3 frames via RubyDungMixin tick hook)
- [x] Server validates movement (max distance check + solid block collision, teleports player back on violation)
- [x] Server broadcasts other players' positions to each client
- [x] Client interpolates remote player positions between updates (via `RemotePlayer` prev/current tracking)

### Step 2.6: Connection Lifecycle

- [x] Implement login timeout (Netty `ReadTimeoutHandler`, 5 seconds, removed after login)
- [x] Implement Ping (server sends Classic 0x01 PingPacket every 2 seconds via tick loop)
- [x] Handle graceful disconnect (send Classic 0x0E DisconnectPacket before closing)
- [x] Handle unexpected disconnect (remove player, broadcast DespawnPlayerPacket)
- [x] Implement max player count enforcement (127-player limit from Classic protocol ID space)

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

Basic chunk and level.dat serialization is complete. Server world persistence via `server-world.dat` (GZip) is also working.

- [x] Implement entity serialization in chunks — `AlphaEntity` wrapper stores raw NBT CompoundTag for round-trip fidelity; common field helpers (id, Pos, Motion, Rotation, OnGround); entities stored in `AlphaChunk.entities` list; serialized/deserialized in `AlphaLevelFormat`
- [x] Implement tile entity serialization — `AlphaTileEntity` wrapper stores raw NBT CompoundTag; common field helpers (id, x, y, z); `AlphaChunk` provides `getTileEntityAt()`/`removeTileEntityAt()` for block-level lookup; serialized/deserialized in `AlphaLevelFormat`
- [x] Implement player data save/restore (`server-players.dat`, GZip — position + rotation per username, restored on reconnect)
- [x] Handle the session.lock ownership mechanism (8-byte timestamp in `AlphaLevelFormat`)
- [x] Implement auto-save (every 5 minutes / 6000 ticks in `ServerTickLoop`)
- [x] Server world save/load (`ServerWorld.save()` / `ServerWorld.load()` via GZip compressed `server-world.dat`)
- [x] Alpha-format chunk save/load (`AlphaChunk` + `AlphaLevelFormat` with NBT)
- [x] `level.dat` serialization (seed, spawn position, time, lastPlayed)

### Step 4.2: World Generation

RubyDung's original world is a simple flat terrain with random blocks:

- [x] Create `WorldGenerator` interface — supports both finite-world generation (`generate(blocks, w, h, d, seed)`) and chunk-based generation (`generateChunk(chunkX, chunkZ, seed)`) via `supportsChunkGeneration()` flag; includes static `blockIndex()` helper
- [x] Create `FlatWorldGenerator` implementing the RubyDung flat terrain (cobblestone + grass surface at height*2/3)
- [x] Wire into `ServerWorld.generate(WorldGenerator, long seed)` and `RDServer` — generator and seed are passed through constructor, logged at startup
- [x] Port the original RubyDung terrain generator — `RubyDungWorldGenerator` faithfully replicates the Level constructor (grass surface at height*2/3, cobblestone subsurface, air above); selectable via `-Drdforward.generator=rubydung`
- [x] Add Alpha-style terrain generator shell — `AlphaWorldGenerator` with 7-phase pipeline (terrain/caves/surface/ores/fluids/trees/lighting); `PerlinNoise` utility for heightmaps; `supportsChunkGeneration()` = true; phases 2/4/6 (caves, ores, trees) are documented stubs ready for Alpha-accurate logic; selectable via `-Drdforward.generator=alpha`
- [x] Make world size configurable via server properties — `-Drdforward.world.width`, `.height`, `.depth`, `-Drdforward.generator=flat|rubydung|alpha`, `-Drdforward.seed`; `RDServer` constructor accepts custom dimensions

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

The original RubyDung has no multiplayer UI. Minimal functionality added:

- [x] CLI-based server connect (`--server` flag, `-PmpServer` Gradle property, or F6 toggle for localhost)
- [x] CLI-based player name entry (`--username` flag, `-PmpUsername` Gradle property, or server auto-assign)
- [x] HUD text overlay showing connection status, server address, player count
- [ ] Server connect screen (graphical text input for host:port) — `GameScreen` interface ready, `ServerListScreen` skeleton defined
- [ ] Server list (hardcoded or file-based initially) — `ServerListScreen.ServerEntry` model ready
- [ ] In-game player list (Tab key) — `PlayerListOverlay` abstract class ready, data available via `MultiplayerState.getRemotePlayers()`
- [x] Chat overlay (T key to open, Enter to send) — `ChatRenderer` displays messages at bottom-left with 10s auto-fade; `ChatInput` captures text via GLFW char callback, T opens (releases cursor), Enter sends + recaptures, Escape cancels + recaptures; `MultiplayerState.pollChatMessage()` now wired to `ChatRenderer.addMessage()` in the mixin render loop; UI groundwork interfaces: `GameOverlay`, `GameScreen`, `PlayerListOverlay`, `ServerListScreen`

### Step 5.3: Render Remote Players

- [x] Create a simple player model (colored cube 0.6x1.8x0.6 with wireframe outline, 8-color rotation)
- [x] Render remote players at their server-reported positions (correct eye height 1.62)
- [x] Interpolate movement between position updates (prev/current position tracking in `RemotePlayer`)
- [x] Show player names above their heads (billboarded Java2D→texture name tags via `NameTagRenderer`)

### Step 5.4: Client-Side Prediction

- [x] Predict local player movement (local player moves instantly, position synced to server)
- [x] Predict block placement (applied immediately, reverted after 2s timeout if server doesn't confirm)
- [x] Handle rubber-banding gracefully (server teleports player back on invalid movement)

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

## Additional Features Implemented (Beyond Original Plan)

These features were built during development but weren't in the original plan:

- [x] **LWJGL 2 → LWJGL 3 migration** — all 7 game source files ported (Display→GLFW, Keyboard→glfwGetKey, GLU→manual matrix math, etc.)
- [x] **Fat JAR tasks** — `fatJar`, `fatModdedJar`, `fatJars`, `buildAll` Gradle tasks for self-contained distribution
- [x] **CLI flags** — `--server` and `--username` for fat JAR clients, `-PmpServer` and `-PmpUsername` for Gradle
- [x] **F6 multiplayer toggle** — in-game key to connect/disconnect from localhost server
- [x] **Server auto-naming** — unique player names with duplicate prevention (suffix numbering)
- [x] **Server console commands** — `list`, `save`, `stop` interactive commands
- [x] **Single-player fallback** — auto-reverts to single-player when server is unavailable
- [x] **World backup/restore** — client backs up local world before multiplayer, restores on disconnect
- [x] **Escape key mouse release** — press Escape to release mouse grab, click to re-grab
- [x] **Server unavailable HUD** — detects server down, shows message, graceful fallback

---

## Implementation Order (Recommended)

The phases above are organized by domain, but the optimal implementation
order interleaves them to always have something testable:

1. ~~**Phase 5.1** — Get RubyDung launching first~~ ✅ **DONE**
2. ~~**Phase 1.1-1.3** — Fabric Loader integration (mod loading works)~~ ✅ **DONE**
3. ~~**Phase 2.1-2.2** — Basic Netty server + world state (server starts)~~ ✅ **DONE**
4. ~~**Phase 2.4** — Client-server world sync (two players see same world)~~ ✅ **DONE**
5. ~~**Phase 2.5** — Player position sync (players can see each other)~~ ✅ **DONE**
6. ~~**Phase 4.1** — World save/load (worlds persist across restarts)~~ ✅ **DONE** (basic — entity/player data TBD)
7. **Phase 3.1-3.3** — Version translation (RubyDung client on Alpha server) ⬅️ **NEXT**
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
