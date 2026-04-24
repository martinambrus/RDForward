---
name: Server API architecture decisions
description: Architectural decisions from PLAN-SERVER-API.md kickoff — event model, client mod distribution, clean rewrite stance
type: project
---

# PLAN-SERVER-API.md Implementation Decisions (2026-04-23)

## Q1: Event class design (merged, not split)
- `PrioritizedEvent<T>` **extends** `Event<T>` and preserves `invoker()` pattern (plan's `fire(Function, BiConsumer)` is discarded)
- Stop-on-cancel happens inside the invoker loop: iterate priorities LOWEST→HIGHEST, break on FAIL, then always run MONITOR list (return values ignored)
- `ServerEvents` migrates fully to `PrioritizedEvent<T>` — existing `.register(T)` and `.invoker()` callers unchanged
- Fabric bridge upcasts `PrioritizedEvent<T>` to `Event<T>` — zero translation overhead preserved
- Mods wanting priority use new `register(EventPriority, T)` overload
- MONITOR listeners read cancelled-state via ThreadLocal set during dispatch

**Why:** keeps Fabric signature compatibility (plan's core Event<T> type-alias promise) AND delivers plan's Phase 4 priority/cancellation semantics in one class.

**How to apply:** all server-facing event definitions use `PrioritizedEvent<T>`; bridge layer never needs adapters.

## Q2: Client mod distribution via actual Fabric
- RDForward client runs under real Fabric Loader (`RubyDungMixin.java:633` references `FabricLoader.getInstance()`)
- Client mods = Fabric mods, loaded by actual Fabric Loader (NOT RDForward's ModLoader)
- **NO `rd-bridge-fabric` client-side stubs** — dropped from plan (plan lines 1509-1520 client/ subdirs)
- **NO separate `rd-mod-loader-client` module** — dropped
- `rd-api` client interfaces (DrawContext, ClientEvents, GameOverlay, KeyBinding, etc.) shipped with a Fabric companion mod in rd-client that wires dispatch from RubyDungMixin hooks
- Third-party Fabric client mods depend on `rdforward-api` jar + use our interfaces
- Cross-platform mods ship BOTH: rdmod.json (server) + fabric.mod.json (client) in same JAR or separate
- `rd-bridge-fabric` is **server-only** (Phase 8 server side only from plan)
- Hot-reload on client = Fabric's responsibility, not ours (Mixin bytecode reload is not supported anyway)

**Why:** avoid reinventing client-side modding that Fabric already provides. Mixin bytecode-reload is impossible on client, so Fabric's load-once model is appropriate.

**How to apply:** any task touching "client mod loader" or "client-side bridge" — skip. Client mods go through real Fabric.

## Q3: Clean rewrite of server API infra (no backward compat)
- Existing static singletons removed wholesale: `CommandRegistry` (rd-server/api), `Scheduler`, `ModConfig`, `PermissionManager`, `ServerEvents.clearAll()` pattern
- Replaced by rd-api interfaces + single implementation injected where needed
- All existing callers in `RDServer.java` (8 ServerEvents call sites, CommandRegistry.register, Scheduler.runLater, etc.) rewritten to new API in the same commits that remove the old
- Built-in server commands (help, list, stop, tp, kick, ban, etc.) registered with `__server__` owner through new CommandRegistry interface
- `Scheduler.init()/reset()` lifecycle dies — scheduler is ModManager-owned instance, lifecycled with server
- Existing feature classes (`BanManager`, `BlockOwnerRegistry`, `GriefProtection`, `WhitelistManager`, `TeamManager`, `ServerProperties`) stay put — they're features not API infrastructure

**Why:** project is early, single client + server version, no external API consumers yet. Clean break is cheaper than compat layer.

**How to apply:** when rewriting any of the five API utilities above, delete the old static class entirely, migrate all callers in the same PR. Do NOT leave static facade shims.

## Additional plan fixes applied before coding
- `VersionCapability`: registry class with static factory (not enum — enums can't extend)
- `KeyBinding` interface: add `onPressed()` callback method (plan's example uses constructor callback but interface had none)
- `Event<T>`: add `unregister(T)` method for per-mod listener cleanup (plan assumed existed)
- `ModDescriptorParser`: lives in `rd-mod-loader`, NOT rd-api (rd-api is interfaces-only, parser needs JSON lib)
- `rd-api` module: build dir has stale classes under wrong package `com.github.martinambrus.rdapi.*` (Timer, Game, ModLoader stubs) — clean before starting
- Thread cleanup: interrupt + 5s join + log warning on timeout. NO `Thread.stop()` (removed in Java 21)
- `rd-protocol` back-compat: old `Event`/`EventResult` classes extend new rd-api types for internal protocol callers, but rd-api is authoritative
- Core ModLoader scans `rdmod.{json,yml,toml}` only; `plugin.yml` and `fabric.mod.json` handled by their respective bridges
- Tab completion on Alpha/Beta/pre-1.13 clients: server-side pseudo-only (no protocol support for Brigadier-style completion)
