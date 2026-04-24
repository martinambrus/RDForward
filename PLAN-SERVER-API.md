# RDForward Unified Modding API - Remaining Work

## Status

Phases 1-8 implemented and shipped. All corresponding Verification Plan items
green under `./gradlew test`. The remainder of this document tracks only work
that has not landed yet.

### Completed (removed from this plan)

| Phase | Scope | Signal |
|-------|-------|--------|
| 1. Core `rd-api` | Interfaces for mod, event, server, world, player, command, scheduler, config, permission, network, version, client | `rd-api/src/main/java/.../api/` populated; tests: `VersionSupportTest`, `EventOwnershipTest`, `ResourceSweeperTest`, `PrioritizedEventTest` |
| 2. `rd-mod-loader` | Discovery, classloader hierarchy, descriptor parsing, dependency resolution, lifecycle state machine, API bindings (`RD*` impls) | Module exists; tests: `DescriptorParserTest`, `DependencyResolverTest`, `VersionRangeTest`, `ModLoaderIntegrationTest` |
| 3. Hot-reload | Ownership tracking (events, commands, scheduler, threads), forced cleanup, classloader close, orphan detection, `Reloadable` state, ClassLoader WeakRef GC probe | `ModLoaderIntegrationTest.repeatedReloadDoesNotLeakClassLoaders` (50 iterations + GC probe + orphan check) |
| 4. Event bubbling, cancellation, admin | `PrioritizedEvent` stop-on-cancel + MONITOR pass-through, intentional Spigot incompat, admin `/events` commands, `event-overrides.json` persistence + startup reconciliation | `PrioritizedEventTest`, `EventManagerTest` |
| 5. Command conflict resolution | Namespaced commands (`<modId>:<cmd>`), conflict detection, admin `/commands` commands, `command-overrides.json` persistence + startup reconciliation, `__server__` pseudo-mod | `CommandConflictResolverTest` |
| 6. Version abstraction | `VersionCapability` registry, `VersionSupport.isSupported()`, mod-side capability queries, API-impl version branching | `VersionSupportTest` |
| 7. Bukkit bridge | `org.bukkit.*` stubs, `plugin.yml` loader, `@EventHandler` reflection + priority mapping, `ignoreCancelled=false` one-time warning, `PlayerMoveEvent` wiring (fixed-point /32, byte-angle /256*360), `commands:` block -> `PluginCommand` -> rd-api `CommandRegistry` forwarder | `BukkitBridgeIntegrationTest` (12 tests) |
| 8. Fabric bridge | `net.fabricmc.*` stubs, `fabric.mod.json` loader, server events + client events + `HudRenderCallback` + `WorldRenderEvents` + `KeyBindingHelper` + `ScreenEvents` + `ClientPlayNetworking`, `Event<T>` as type alias | `FabricBridgeIntegrationTest`, `FabricClientBridgeIntegrationTest` |
| 9.3 Paper bridge | `io.papermc.paper.*` + `net.kyori.adventure.*` + `com.mojang.brigadier.*` stubs, `paper-plugin.yml` loader with Bukkit-yml fallback, `PluginBootstrap` + `createPlugin` lifecycle, `AsyncChatEvent` via `BukkitEventAdapter` delegation + `AdventureTranslator`, bootstrap-phase Brigadier `Commands` registrar flushed to rd-api `CommandRegistry` on enable | `PaperBridgeIntegrationTest` (6 tests) |
| 9.1 Forge bridge | `net.minecraftforge.*` stubs (Dist, MinecraftForge.EVENT_BUS, IEventBus, @SubscribeEvent, @Mod + @Mod.EventBusSubscriber, FML lifecycle + server events, TickEvent, PlayerEvent, BlockEvent, ServerChatEvent), `mods.toml` parser (toml4j), `ForgeModLoader` with jar-entry `@Mod` scan + per-mod `FMLJavaModLoadingContext` ThreadLocal for ctor injection, `ForgeEventBus` dispatch + priority sort, `ForgeEventAdapter` for `@EventBusSubscriber` classpath auto-register, `ForgeBridge.install` wires rd-api `ServerEvents` forwarders into global bus with cancellation bubble-back | `ForgeBridgeIntegrationTest` (5 tests) |
| 9.2 NeoForge bridge | `net.neoforged.*` stubs (bus IEventBus subinterface, NeoForge.EVENT_BUS aliasing `MinecraftForge.EVENT_BUS`, @Mod + lifecycle + gameplay events as subclasses of their Forge counterparts), `neoforge.mods.toml` parser + optional per-mod `mainClass` field, `NeoForgeModLoader` with reflective constructor-injection of `IEventBus` / `ModContainer` / `Dist` (both Forge and NeoForge `Dist` packages), `NeoForgeEventBus` extending `ForgeEventBus` to satisfy NeoForge-typed bus parameters, `NeoForgeBridge` delegating install/uninstall to `ForgeBridge` (single shared bus handles both ecosystems) | `NeoForgeBridgeIntegrationTest` (4 tests) |
| 9.4 PocketMine bridge | `pocketmine.*` stubs (Server singleton + PluginManager, PluginBase lifecycle, Listener + Cancellable + bridge-local `@HandleEvent(priority, ignoreCancelled)` replacing PocketMine's PHPDoc directives, player + block cancellable events, Command + CommandSender + CommandExecutor, TaskScheduler contract), snakeyaml `plugin.yml` parser producing `PluginDescription`, `PocketMinePluginLoader` with URLClassLoader isolation + PluginBase instantiation + per-plugin logger + scheduler wiring, `PocketMinePluginWrapper` driving `onLoad`/`onEnable`/`onDisable` and forwarding `commands:` entries into rd-api `CommandRegistry`, `PocketMineEventAdapter` walking `@HandleEvent` methods and binding each to the matching rd-api `ServerEvents` entry with cancellation bubble-back, `PocketMineBridge.install` exposes `pocketmine.Server.getInstance()` whose `PluginManager.registerEvents` dispatches to the adapter | `PocketMineBridgeIntegrationTest` (4 tests) |
| 10. API-only distribution | `distApi` Gradle task in `rd-api`, `rd-bridge-bukkit`, `rd-bridge-fabric`, `rd-bridge-paper`, `rd-bridge-forge`, `rd-bridge-neoforge`, `rd-bridge-pocketmine` — produces classes + sources + javadoc JARs for mod authors | `./gradlew :rd-api:distApi`, etc. |

### Known deferred

- **Verification §9 — GL-dependent client API tests** (`DrawContext.drawText`
  rasterization, `DrawContext.fillRect` pixel placement, `RENDER_HUD` /
  `RENDER_WORLD` fire-point assertions, `KEY_PRESS` integration).
  These require a live OpenGL context. They were delegated to the e2e
  suite, which the user dropped from scope (formerly §10/§11 of this
  plan). The pure-Java portions of `DrawContext` (text metrics, screen
  dims) are covered by `RDDrawContextTextMetricsTest`. Re-open only if
  the e2e suite is reinstated.

---

## Bridge dependency overview

```
rd-api
  +-- rd-bridge-bukkit               (depends on rd-api)               [shipped]
  +-- rd-bridge-fabric               (depends on rd-api)               [shipped]
  +-- rd-bridge-paper                (depends on rd-bridge-bukkit)     [shipped]
  +-- rd-bridge-forge                (depends on rd-api)               [shipped]
  +-- rd-bridge-neoforge             (depends on rd-bridge-forge)      [shipped]
  +-- rd-bridge-pocketmine           (depends on rd-api)               [shipped]
```

All Phase 9 bridges landed. Future ecosystems (Spigot plugin extensions,
Velocity, modded clients outside the shipped set) would follow the same
template described below.

---

## Verification for new bridges

Each new bridge should ship alongside an integration test mirroring the
Bukkit/Fabric pattern:

- Load an in-repo fixture plugin/mod via the bridge's loader.
- Assert lifecycle callbacks (`onEnable` / `onInitialize` / `@Mod` ctor) fire.
- Assert a cancellable event adapter round-trips (rd-api event -> bridge event
  -> listener -> cancellation bubbled back to rd-api).
- Assert one version-specific capability no-ops cleanly on an older client.
- For client-side bridges, assert HUD render + key binding + screen open/close
  hooks dispatch without touching GL.
