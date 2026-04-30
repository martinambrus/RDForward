---
name: Biome must be a class, not interface
description: EssentialsX 2.21.2 emits invokevirtual on Biome.name() — Biome must be an abstract class in our paper-api stubs or /tpr (and any other biome-named lookup) silently dies inside CompletableFuture chains
type: feedback
originSessionId: f9022a27-11d1-4641-9c36-f295e1baba09
---
`org.bukkit.block.Biome` in our paper-api stubs (rd-bridge-bukkit) MUST be declared as `public abstract class Biome implements OldEnum, Keyed, Translatable`, NOT as an interface.

**Why:** EssentialsX 2.21.2's `LegacyBiomeNameProvider.getBiomeName(Block)` was compiled when Bukkit's `Biome` was still an enum. Its bytecode emits `invokevirtual Method org/bukkit/block/Biome.name:()Ljava/lang/String;`. JVM rejects `invokevirtual` on an interface with `IncompatibleClassChangeError`. The exception is raised inside `CompletableFuture.thenAccept` lambda (Essentials's `RandomTeleport.attemptRandomLocation`/cache-fill) — `thenAccept` swallows lambda exceptions into the returned future, so the chain just stops with no log line. `/tpr` ends up at "Teleporting to a random location..." with zero further activity — no recursion to next attempt, no `TeleportWarmupEvent`, no error.

Symptom signature when this regresses (or a similar invokevirtual-on-interface mismatch appears for another type):
- A plugin command prints its first user-facing message then nothing happens.
- The CompletableFuture chain visibly executes the FIRST iteration (chunk lookup, biome lookup, etc.) but never recurses for additional attempts.
- Cache-fill loops run all 10 iterations cleanly (because each iteration's exception is independently swallowed) — but no location ever gets cached.
- No exception in any log; the failure is invisible without bytecode-level diagnostics.

**How to apply:** When auto-generating paper-api stubs, leave `Biome` as a class. If Paper upstream changes it, double-check via `javap -c` on a representative plugin (e.g. `com/earth2me/essentials/RandomTeleport.class`, `com/earth2me/essentials/User.class`) whether they emit `invokevirtual` or `invokeinterface` on `Biome.name`. The bytecode the plugin was compiled against is what counts, not the latest Paper source. Apply the same check to other Bukkit types that flipped between enum/class and interface over the years (e.g. `EntityType`, `Material`, `Particle`, `Statistic`).

For the same root cause, also keep `BukkitWorldAdapter implements RegionAccessor` — EssentialsX's `PaperBiomeKeyProvider.getBiomeKey` casts `(RegionAccessor) block.getWorld()` and otherwise CCE'd silently in the same chain. The fix landed alongside the Biome class conversion.
