---
name: BlockPolicy chokepoint architecture
description: Universal block coercion at ServerWorld.setBlock(byte) with lazy YAML deltas, per-world policies, dedup logging
type: project
---

ServerWorld.setBlock(byte) runs a per-world BlockPolicy before storage so non-Bukkit mods, MCPE, Bedrock and any future protocol cannot bypass coercion. This is the single chokepoint — putting the policy in rd-bridge-bukkit only would leave the rest of the API surface unprotected.

## Components

Location: `rd-server/src/main/java/com/github/martinambrus/rdforward/server/world/` unless noted.

- `BlockPolicy` (rd-api, package `com.github.martinambrus.rdforward.api.world`): functional interface `coerce(int x, int y, int z, BlockType requested) -> BlockType`. `IDENTITY` constant returns the request verbatim.
- `ServerWorld`: constructor takes `String name`; legacy constructors delegate with `"overworld"`. `volatile BlockPolicy policy = IDENTITY`. `setPolicy(BlockPolicy)`. Coercion runs OUTSIDE the chunk write lock to avoid lock-ordering issues.
- `BlockCoercionLog`: `ConcurrentHashMap<String, Set<String>>` (newKeySet) keyed by `(worldName, requested.getName())`. First hit emits a WARNING JUL line; subsequent hits are silent. `resetForTests()` for unit tests. Null/blank world names fold into `<unknown>`. Mirrors the StubCallLog dedup pattern.
- `BlockReplacementRegistry`: lazy YAML loader. Resource path `/replacements/<protocolKey>.yml` where `protocolKey = ProtocolVersion.name().toLowerCase()`. NO_FILE sentinel caches missing resources so disk lookup never repeats. `getReplacement(version, blockName)` returns the mapped name or null. `introducedAfter(blockName, target)` walks every loaded file with `sortOrder > target.sortOrder` and returns true if any of them defines that block. SnakeYAML pinned at compile scope (`rd-server/build.gradle`).
- `BlockTypesLookup`: reflection-built name -> BlockType map seeded from `BlockTypes` constants. Fallback when nothing matches: `BlockTypes.COBBLE` (RubyDung lacks STONE in early versions, so STONE is not a safe default).
- `RubyDungBlockPolicy(int grassLayerY)`: position-aware. Air -> air; null -> air; `y == grassLayerY` -> grass regardless of input; else -> cobble. Only three blocks exist in RubyDung: air, cobble, grass.
- `VersionedBlockPolicy(ProtocolVersion target)`: per-instance `ConcurrentHashMap<String, BlockType>` memo. For a requested block, walks the chain `getReplacement()` across files newer than target by sortOrder; cycle guard via `Set<String> visited`; chain exhaustion -> `BlockTypes.COBBLE`. Memo returns the same instance for the same source, so `assertSame` works in tests.

## Wiring

`RDServer.choosePolicyFor(ProtocolVersion v)` selects by `v.getFamily()`:
- `PRE_CLASSIC` / `CLASSIC` -> `RubyDungBlockPolicy` with the world's grass layer Y
- `ALPHA` / `BETA` / `RELEASE` -> `VersionedBlockPolicy(v)`
- otherwise -> `BlockPolicy.IDENTITY`

World construction passes the level name through so `BlockCoercionLog` can dedup per-world.

## Resource files

`rd-server/src/main/resources/replacements/<protocolKey>.yml` — flat YAML map of `minecraft:<from>: minecraft:<to>`. Bootstrap file `b1.7.3.yml` maps `piston`, `sticky_piston`, `piston_head` -> `minecraft:cobblestone`. New protocol version that drops blocks: add a new YAML, do NOT touch ServerWorld coercion path.

## Tests

`rd-server/src/test/java/com/github/martinambrus/rdforward/server/world/`:
- `RubyDungBlockPolicyTest` — 7 tests
- `VersionedBlockPolicyTest` — 6 tests (uses `NamedBlock` test record so the chain triggers on the namespaced name path)
- `BlockReplacementRegistryTest` — 6 tests
- `BlockCoercionLogTest` — 5 tests
- `ServerWorldPolicyIntegrationTest` — 6 end-to-end tests through real ServerWorld

All 30 pass with `BlockReplacementRegistry.resetForTests()` and `BlockCoercionLog.resetForTests()` in `@BeforeEach` / `@AfterEach`.

## Why

Backwards compat (highest priority): every API surface that places a block — Bukkit Material.setType, mod loader direct setBlock, MCPE wire packets, Bedrock SubChunk updates — funnels through `ServerWorld.setBlock(byte)`. Putting the policy here is the only way to guarantee no path bypasses coercion.

Lazy YAML deltas: an Alpha-only server never loads Beta or Release replacement files. Same shape as `BedrockProtocolConstants` and `ProtocolDetectionHandler` — load on first need, cache forever, NO_FILE sentinel for misses.

## How to apply

- New protocol version that drops blocks: drop a YAML at `/replacements/<key>.yml`, no code changes.
- New chokepoint policy (e.g., creative-restricted, claim-protected): write a `BlockPolicy` impl, set via `world.setPolicy(...)`. Compose with existing policies by chaining inside `coerce`.
- Do NOT add coercion logic above `ServerWorld.setBlock(byte)` — duplicating it in the bridge would let non-Bukkit code paths skip it.
