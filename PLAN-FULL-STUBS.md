# RDForward Full-Stub Expansion - Hybrid No-Op Semantics

## Status

Not started. Tracks work layered on top of the shipped Phase 1-10 scope
described in `PLAN-SERVER-API.md`. Each phase in §Tasks below is updated
to `[shipped]` as it lands; upstream-version decisions accumulate in
§Decisions Log.

## Goal

Extend the bridge API stubs across all Java-target ecosystems (Bukkit,
Paper, Forge, NeoForge, Fabric) from the current integration-test
surface to a broad coverage surface that allows real third-party
plugins and mods to class-load and instantiate without
`NoClassDefFoundError` or `NoSuchMethodError`.

Runtime behavior of the expanded surface follows the Hybrid Semantics
Contract below: reads return sensible defaults, writes warn once and
no-op. Most runtime effects will not touch real RDForward state because
rd-server is protocol-forward and does not implement the full modern
Minecraft world / entity / item model. The expansion targets
load-linkability first; any plugin that genuinely relies on write-path
behavior will still malfunction, but predictably (warn log, not crash).

## Scope and Non-Goals

In scope:

- Java bridges: Bukkit, Paper (extends Bukkit), Forge, NeoForge
  (extends Forge), Fabric.
- Per bridge, one pinned upstream API version (currently latest stable).
- Signature coverage sufficient to class-load and instantiate typical
  mid-sized plugins.
- The Hybrid Semantics Contract (§2).
- A codegen module that drives stub emission from upstream JAR
  bytecode, plus CI drift detection.

Out of scope for the initial pass:

- PocketMine bridge. It is the Java-port target for a specific downstream
  project, not a general plugin loader; its current integration-test
  stub set is sufficient. Revisit after the five Java bridges ship.
- Stubbing `net.minecraft.*` internals. The vanilla Minecraft API is
  tens of thousands of version-specific classes with redistribution
  constraints. Tracked as the Known Constraint in §5; Forge, NeoForge,
  and Fabric ship with documented bounded support.
- Multi-version stub sets (e.g. Bukkit 1.8 signatures alongside Bukkit
  1.21 signatures). Pin one target version per bridge; multi-version
  support is future work.
- Plugins that use bytecode generation, Java agents, or OS-level native
  libraries. These are out of contract.
- Runtime correctness of write-path methods. Write-paths always no-op;
  plugins that depend on write-side effects will malfunction
  predictably, by design.

## 1. Bridge matrix

| Bridge | Upstream source | Pinned version | License | Stub approach |
|--------|-----------------|----------------|---------|---------------|
| Bukkit | `io.papermc.paper:paper-api` (proxy for spigot-api) | 1.21.4-R0.1-SNAPSHOT | MIT (paper-api) | ASM scan of JAR, emit `org.bukkit.*` stubs |
| Paper | `io.papermc.paper:paper-api` | 1.21.4 | MIT | Emit `io.papermc.*`, `net.kyori.adventure.*`, `com.mojang.brigadier.*`. Bukkit stubs shared from rd-bridge-bukkit. |
| Fabric | `net.fabricmc:fabric-loader` + `net.fabricmc.fabric-api:fabric-api` module jars | loader 0.16.x, api 0.100.x for MC 1.21.4 | Apache-2.0 / MIT | Emit `net.fabricmc.*` only. `net.minecraft.*` deliberately omitted (see §5). |
| Forge | `net.minecraftforge:forge` API jar (event bus + FML + event classes, `net.minecraft.*` stripped) | 1.21.4-55.x | LGPL-2.1 | Emit `net.minecraftforge.*`. |
| NeoForge | `net.neoforged:neoforge` API jar | 21.4.x | LGPL-2.1 | Emit `net.neoforged.*`; Forge types shared from rd-bridge-forge. |

## 2. Hybrid Semantics Contract

Every generated stub follows the table below. Adapter files under
`com.github.martinambrus.rdforward.bridge.*` keep their existing
hand-tuned behavior and are excluded from regeneration.

| Method kind | Return value | Runtime effect |
|-------------|--------------|----------------|
| Primitive getter (`getX`, `isX`, `hasX`) | Typed zero (0, false, 0.0, `'\0'`) | no-op |
| Reference getter returning `@Nullable` per upstream docs or unannotated scalar | `null` | no-op |
| Reference getter returning a collection or optional | `Collections.emptyList()` / `emptySet()` / `emptyMap()` / `Optional.empty()` | no-op |
| Reference getter returning a type with a known empty sentinel (registered in `empty-sentinels.properties`) | That sentinel (`ItemStack.EMPTY`, `Component.empty()`, `Location.ZERO`) | no-op |
| Setter (`setX`, `addX`, `removeX`, `clearX`, `broadcastX`, registry `register`, any `void` non-lifecycle) | N/A | `StubCallLog.logOnce(pluginId, signature)` then no-op |
| Non-void setter (builder-pattern mutation) | `this` (fluent) | `StubCallLog.logOnce(...)` then no-op |
| Method declared `throws` | Typed default | `StubCallLog.logOnce(...)`; never throws |
| Methods already forwarded to rd-api by an adapter class | Existing behavior | Existing forwarding |
| Constructor | Fields set from args if trivial; otherwise args stored; instance usable as identity token | no-op |
| Static factory (`of`, `builder`, `create`) | New stub instance | no-op |

`StubCallLog` dedupes warnings per `(pluginId, methodSignature)` pair
via a `ConcurrentHashMap<String, Set<String>>`. Plugin identity is
provided by the bridge's plugin wrapper via a `ThreadLocal<String>`
scoped around the plugin entry point; if absent, the plugin's class
loader name is used as a best-effort identifier.

Warning format:

```
[<bridge>Bridge] Plugin '<name>' called <fqcn>.<method><descriptor>
— unsupported in RDForward, returning <default>. Further calls from
this plugin to this method will be silent.
```

## 3. Source Strategy

Stubs are generated from upstream API JAR bytecode via an ASM 9.x
scanner in a new `codegen/` Gradle subproject. Source parsing is
rejected because:

- ASM runs uniformly against all five upstream APIs without per-repo
  parsers.
- Bytecode carries exactly the signatures the JVM uses for linking,
  including generic erasure and synthetic members.
- The generator produces output that is deterministic per (JAR,
  generator version) pair, enabling reliable CI drift detection.

Source trees remain useful as a reference for method contracts (to
pick sensible getter defaults) but are not consumed by the tool.

License position: generated stubs are re-implementations of public API
shape. Only signatures and public `static final` constant values are
carried over; no upstream source or bytecode is redistributed. Each
emitted file carries a header noting the upstream artifact coordinates
and version so downstream auditors can trace the source.

Generator output rules:

- Preserve the upstream package declaration.
- Preserve class modifiers, generics, `extends`, and `implements`.
- Preserve fields only when they are `public static final` with a
  trivial literal initializer (primitives or string constants).
  Reference-typed constants are emitted as `null` unless listed in
  `empty-sentinels.properties`.
- Regenerate every public and protected method and constructor per the
  Hybrid Semantics Contract.
- Emit a single-line Javadoc: `Auto-generated stub from <artifact>:<version>.
  Behavior: <getter-default|noop-setter|preserved>.`
- Skip classes under `net.minecraft.*`.
- Skip private, package-private, and synthetic members.
- Skip methods annotated `@Deprecated(forRemoval = true)`.

Hand-maintained files are marked with `// @rdforward:preserve` at the
top of the file and are never overwritten by the generator. All files
under `com.github.martinambrus.rdforward.bridge.*` carry this marker;
stubs under foreign namespaces are added to the preserve set on a
case-by-case basis when their current hand-tuned behavior is load-bearing.

Check-in policy: generated files are committed to the repo. The
generator runs on demand via `./gradlew :codegen:generateStubs
-Pbridge=<name>`. A CI job runs the generator against the pinned
upstream JAR and fails if `git diff --exit-code` is non-empty.

## 4. Hand-maintained vs Generated

Hand-maintained, never regenerated:

- All files under `com.github.martinambrus.rdforward.bridge.<name>.*`
  (adapter glue).
- Foreign-namespace stubs currently referenced by adapter code with
  non-trivial behavior (e.g. `org.bukkit.entity.Player.teleport`,
  `net.minecraftforge.eventbus.api.IEventBus.post`).

Generated, overwritten by the generator:

- All other foreign-namespace files under each bridge's `src/main/java`.

The generator enforces the split: a file with `@rdforward:preserve` at
its head is read but not written. New additions to the preserve list
require a separate PR that explains the reason for the carve-out.

## 5. Known Constraint: Forge, NeoForge, Fabric and `net.minecraft.*`

Forge, NeoForge, and Fabric mods typically import `net.minecraft.*`
classes (e.g. `ServerPlayer`, `Block`, `Item`, `Level`,
`ResourceLocation`). Stubbing that surface is out of scope for the
initial pass.

Behavior for mods that reference `net.minecraft.*`:

- The mod loader performs a bytecode pre-scan of the mod jar's class
  constant pools (ASM `ClassReader`) before loading. Any reference to
  a class whose internal name starts with `net/minecraft/` causes the
  loader to refuse the mod and throw
  `UnsupportedOperationException` with a message listing the
  referenced classes and noting that RDForward does not stub vanilla
  Minecraft internals.
- The refusal happens before class-loading so the JVM never emits
  `NoClassDefFoundError`; the error surface is a single predictable
  exception type plugin authors and downstream tooling can catch.
- Implemented in `ForgeModLoader`, `NeoForgeModLoader`, and
  `FabricPluginLoader`. Each bridge's README documents the limit and
  lists the small subset of mod categories that stay clear of
  `net.minecraft.*` (event-bus-only utility mods, config wrappers,
  pure scheduler mods).

Future scope (not in this plan): targeted `net.minecraft.*` stub sets
for specific mod categories where the referenced surface is narrow
and well-known (e.g. server-side utility mods that only touch
`CommandSourceStack` and `MinecraftServer`). Each such category would
be opt-in via a bridge-level feature flag.

Bukkit, Paper, and PocketMine mods stay inside their ecosystem API and
are not affected by this constraint.

## 6. Tasks

### Phase 0. Codegen bootstrap [shipped]

| # | Task | Signal |
|---|------|--------|
| 0.1 | Create `codegen/` Gradle subproject with ASM 9.x dep and a `StubGenerator` main class driven by `-Pbridge=<name>`. | `./gradlew :codegen:help --task generateStubs` lists the task; `./gradlew :codegen:generateStubs -Pbridge=bukkit` runs the placeholder; missing/unknown bridge args fail cleanly. |
| 0.2 | Implement `StubSemantics` classifier (getter/setter/builder/preserve) over `MethodNode` input. Unit-test over hand-crafted bytecode fixtures. | `MethodClassifierTest` (13 tests) passes. |
| 0.3 | Implement default-value resolver: primitive zero; collection empty; `empty-sentinels.properties` lookup; else `null`. | `DefaultValueResolverTest` (15 tests) passes; bundled properties file ships empty, populated per phase. |
| 0.4 | Implement `@rdforward:preserve` skip logic. | `PreserveMarkerTest` (12 tests) passes; header-only scan of first 20 lines. |
| 0.5 | Implement `StubCallLog` dedup infra in `rd-api` (shared across bridges). | `StubCallLogTest` (9 tests) passes; dedup by (pluginId, signature) with null/blank fallback to `<unknown>`. |

### Phase A. Bukkit expansion

| # | Task | Signal |
|---|------|--------|
| A.1 | Resolve `io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT` JAR via Gradle into a generator-input cache directory. | `./gradlew :codegen:resolveUpstream -Pbridge=bukkit` emits the JAR path. |
| A.2 | Run generator against `org.bukkit.*` packages from the JAR. Preserve existing hand-tuned stubs. | `./gradlew :codegen:generateStubs -Pbridge=bukkit` completes; `git diff` shows new files only under `rd-bridge-bukkit/src/main/java/org/bukkit/...`. |
| A.3 | Add a `BukkitLinkageTest` that loads a synthetic plugin touching a wide spread of generated classes (`ItemStack`, `World.spawnEntity`, `PluginManager.registerEvents`, `Bukkit.broadcastMessage`). Assert `onEnable()` completes without throwing. | Test passes. |
| A.4 | Add a smoke test that loads one real third-party plugin jar (license-permitting candidate: `LuckPerms-Bukkit`) and asserts its `onEnable()` completes. Fixture jar cached under `rd-bridge-bukkit/src/test/resources/third-party-fixtures/`. | Test passes; test is tagged `@Tag("thirdparty")` and excluded from default CI. |
| A.5 | Document `BukkitStubReport` Gradle task that diffs pinned upstream signatures vs emitted stubs and fails on drift. Wire into CI. | CI job fails on intentional breakage; passes on clean state. |

### Phase B. Paper expansion

| # | Task | Signal |
|---|------|--------|
| B.1 | Generator runs for `io.papermc.paper.*`, `net.kyori.adventure.*`, `com.mojang.brigadier.*`. Preserve hand-maintained Paper adapter files. | Generated stub count logged; no preserve-marker file touched. |
| B.2 | `PaperLinkageTest` parallel to `BukkitLinkageTest` using Paper-specific types (lifecycle events, Brigadier commands). | Test passes. |
| B.3 | Paper bootstrap smoke test: a fixture Paper plugin with a `PluginBootstrap` that registers a Brigadier command. Assert bootstrap + enable fire. | Test passes. |

### Phase C. Fabric expansion

| # | Task | Signal |
|---|------|--------|
| C.1 | Resolve `net.fabricmc:fabric-loader` + fabric-api module jars for MC 1.21.4. | Resolve task lists the 40+ module jars. |
| C.2 | Generator runs across all resolved module jars. `net.minecraft.*` explicitly filtered. | Generation completes; no `net/minecraft/` file emitted. |
| C.3 | `FabricLinkageTest` using `ModInitializer`, `ServerLifecycleEvents`, `CommandRegistrationCallback`, `ClientTickEvents`. | Test passes. |
| C.4 | Bounded-support doc: append §5 content to `rd-bridge-fabric/README.md`. Implement `net.minecraft.*` pre-scan in `FabricPluginLoader`: refuse the mod and throw `UnsupportedOperationException` listing referenced classes. | README exists; a synthetic mod importing `net.minecraft.world.level.Level` is rejected with the documented exception; unit test asserts the exception message contains the offending class list. |

### Phase D. Forge expansion

| # | Task | Signal |
|---|------|--------|
| D.1 | Resolve Forge API JAR. Strip `net.minecraft.*` classes before feeding to generator. | Input class list to generator contains zero `net/minecraft/` entries. |
| D.2 | Generator emits `net.minecraftforge.*` stubs. Preserve existing event bus and `@Mod` hand-tuned code. | Generation completes; hand-tuned files untouched. |
| D.3 | `ForgeLinkageTest` with a synthetic `@Mod` that uses `MinecraftForge.EVENT_BUS.addListener` for `BlockEvent.BreakEvent` (already wired) plus calls several noop stub methods. | Test passes; warning logs observed exactly once per method. |
| D.4 | Bounded-support doc in `rd-bridge-forge/README.md`. `net.minecraft.*` pre-scan in `ForgeModLoader` — refuse mod and throw `UnsupportedOperationException`. | Doc lands; unit test asserts a synthetic `@Mod` importing `net.minecraft.world.level.Level` is rejected with the expected exception. |

### Phase E. NeoForge expansion

| # | Task | Signal |
|---|------|--------|
| E.1 | Resolve NeoForge API JAR. Strip `net.minecraft.*`. | Empty `net/minecraft/` list. |
| E.2 | Generator emits `net.neoforged.*` stubs. Respect the fact that `net.minecraftforge.*` stubs are shared from `rd-bridge-forge` and must not be duplicated. | Generation completes; no duplicate FQCN across bridge classpaths. |
| E.3 | `NeoForgeLinkageTest` parallel to `ForgeLinkageTest` plus a test covering the `NeoForge.EVENT_BUS` alias to `MinecraftForge.EVENT_BUS`. | Test passes. |
| E.4 | Bounded-support doc in `rd-bridge-neoforge/README.md`. `net.minecraft.*` pre-scan in `NeoForgeModLoader` — refuse mod and throw `UnsupportedOperationException`. | Doc lands; unit test asserts a synthetic NeoForge mod importing `net.minecraft.world.level.Level` is rejected with the expected exception. |

### Phase F. Cross-cutting verification

| # | Task | Signal |
|---|------|--------|
| F.1 | For each bridge, add an integration test that loads a fixture plugin calling several unsupported write-path methods. Assert: `onEnable()` completes, each distinct method produces exactly one warning log, rd-api state unchanged. | Tests pass across all five bridges. |
| F.2 | Weekly drift-detector CI workflow: resolves pinned upstream JARs, runs generator, fails if `git diff` is non-empty. | Workflow passes on clean state; fails on intentional breakage. |
| F.3 | Update `PLAN-SERVER-API.md` bridge dependency overview to cite this plan. Add pointers from each bridge's README. | Docs cross-reference this file. |

## 7. Verification Matrix

| Layer | What is verified | Where |
|-------|------------------|-------|
| Unit | Generator emits expected stub for a hand-written bytecode fixture. | `codegen/src/test/java/.../StubGeneratorTest.java` |
| Unit | Hybrid Semantics Contract classification per method kind. | `codegen/src/test/java/.../StubSemanticsTest.java` |
| Unit | `StubCallLog` dedup per (plugin, method). | `rd-api/src/test/java/.../StubCallLogTest.java` |
| Linkage | Each bridge jar classloads a synthetic plugin that imports a wide spread of stubbed classes. | `rd-bridge-<x>/src/test/java/.../<X>LinkageTest.java` |
| Smoke | One real third-party plugin jar loads and its lifecycle callbacks complete. | `rd-bridge-<x>/src/test/resources/third-party-fixtures/`, tagged `@Tag("thirdparty")`. |
| Drift | Upstream JAR signature set matches emitted stub signature set. | CI workflow runs `./gradlew :codegen:generateStubs --dry-run`. |

## 8. Plan File Discipline

This document is the single source of truth for progress on this
initiative. Rules:

- When a phase completes, move its rows into a `### Completed` table
  at the end of that phase's section, mirroring the `PLAN-SERVER-API.md`
  style.
- Record upstream-version pins, generator-behavior decisions, and any
  scope adjustments in the Decisions Log below.
- Keep the active Tasks tables reflective of remaining work only.

## 9. Decisions Log

- 2026-04-24 — Phase 0.2: `StubSemantics` enum extended beyond the four
  categories named in §2 (GETTER_DEFAULT, SETTER_WARN_NOOP, BUILDER_CHAIN,
  PRESERVE_EXISTING) with `CONSTRUCTOR`, `STATIC_INITIALIZER`,
  `LIFECYCLE`, and `STATIC_FACTORY`. Rationale: constructors and
  `<clinit>` need distinct generator branches; lifecycle methods
  (`onEnable`, `onLoad`, `onDisable`, `onInitialize*`) are plugin
  override targets that must stay warn-free; static non-void methods
  have no plugin-identity ThreadLocal to key the dedup map on, so they
  return defaults without logging. `PRESERVE_EXISTING` is file-level
  rather than method-level so it lives in `PreserveMarker`, not the
  method-classifier enum.
- 2026-04-24 — Phase 0.3: `empty-sentinels.properties` ships empty;
  phase A onward populates it with ecosystem-specific types
  (`ItemStack.EMPTY`, `Component.empty()`, etc.) as stubs land.
- 2026-04-24 — Phase 0.5: `StubCallLog` lives under
  `com.github.martinambrus.rdforward.api.stub` in `rd-api` so every
  bridge can call it without additional dependencies. Warning prefix
  is the uniform `[StubCall]` tag rather than a per-bridge prefix;
  the signature itself already names the originating FQCN.
