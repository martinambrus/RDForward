// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit;

import org.bukkit.block.Block;

/**
 * Bukkit-shaped {@code World} facade. Backed by an rd-api
 * {@link com.github.martinambrus.rdforward.api.world.World} via
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitWorldAdapter}.
 * Methods beyond what RDForward surfaces (weather, biomes, entity queries)
 * return sensible defaults or noop.
 */
public interface World {

    String getName();

    /** @return a Bukkit {@link Block} view of the world block at {@code (x,y,z)}, or null if out of bounds. */
    Block getBlockAt(int x, int y, int z);

    /** Minimum Y coordinate of the world. Bukkit 1.17+ added this so
     *  modern plugins (CoreProtect's {@code Bukkit_v1_17.getMinHeight}
     *  in rollback iteration) can iterate from world bottom upward.
     *  RDForward worlds are 0-based and Alpha-shaped, so 0 is the
     *  Y-floor. */
    default int getMinHeight() { return 0; }

    /** Convenience overload — CoreProtect's {@code BlockBreakListener}
     *  feeds a {@link org.bukkit.Location} pulled from the event. Forwards
     *  to {@link #getBlockAt(int,int,int)} using the location's int coords. */
    default Block getBlockAt(org.bukkit.Location loc) {
        if (loc == null) return null;
        return getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /** Set the block type at {@code (x,y,z)}. @return true if the placement succeeded. */
    boolean setBlockType(int x, int y, int z, Material type);

    /** Pre-Flattening numeric block id at {@code (x,y,z)}. WorldEdit
     *  5.6.1's {@code BukkitWorld.getBlockType} reads the source block
     *  through this signature on every {@code //set} pass — without it
     *  WE bails with {@link NoSuchMethodError} before any coercion runs. */
    default int getBlockTypeIdAt(int x, int y, int z) {
        Block b = getBlockAt(x, y, z);
        if (b == null) return 0;
        Material t = b.getType();
        return t == null ? 0 : t.getId();
    }

    /** Pre-Flattening data nibble at {@code (x,y,z)}. RDForward does
     *  not model block data — return 0. WE 5.6.1's {@code
     *  LocalWorld.getBlock} reads this when its NMS-direct path fails
     *  (which it always does here, since we have no NMS layer). */
    default byte getBlockData(int x, int y, int z) {
        return 0;
    }

    /** WorldEdit 5.6.1's {@code BukkitWorld.checkLoadedChunk} guards
     *  every {@code getBlock} call with this — RDForward holds the
     *  whole world in memory and treats every chunk as loaded. */
    default boolean isChunkLoaded(int chunkX, int chunkZ) {
        return true;
    }

    /** CoreProtect's {@code RollbackProcessor.processChunk} guards
     *  rollback work with {@code world.isChunkLoaded(block.getChunk())}.
     *  RDForward keeps the whole world in memory, so every chunk is
     *  loaded — accept any non-null chunk. */
    default boolean isChunkLoaded(org.bukkit.Chunk chunk) {
        return chunk != null;
    }

    /** CoreProtect's {@code RollbackProcessor.processChunk} requests
     *  the chunk at a {@link Location} immediately after the loaded
     *  check. Returns a snapshot {@link org.bukkit.Chunk} whose chunk
     *  coords are derived from the location's int block coords. */
    default org.bukkit.Chunk getChunkAt(Location loc) {
        if (loc == null) return null;
        return new com.github.martinambrus.rdforward.bridge.bukkit.BukkitChunk(
                this, loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    /** Chunk-coords overload mirroring real Bukkit. */
    default org.bukkit.Chunk getChunkAt(int chunkX, int chunkZ) {
        return new com.github.martinambrus.rdforward.bridge.bukkit.BukkitChunk(this, chunkX, chunkZ);
    }

    int getMaxHeight();

    /**
     * @return Y of the highest non-air block at column ({@code x},
     *         {@code z}), or 0 if the column is empty. Essentials's
     *         {@code Commandtop} reads this to find the safe-spawn Y
     *         above the player's column. Default scans from
     *         {@link #getMaxHeight()} down — concrete adapters may
     *         override with a heightmap lookup if available.
     */
    default int getHighestBlockYAt(int x, int z) {
        for (int y = getMaxHeight() - 1; y >= 0; y--) {
            org.bukkit.block.Block b = getBlockAt(x, y, z);
            if (b != null && b.getType() != Material.AIR) return y;
        }
        return 0;
    }

    long getTime();

    /** Noop for RDForward — surfaced so plugins that toggle time of day compile. */
    void setTime(long time);

    /**
     * Place a small tree (5-block trunk of {@link Material#OAK_LOG} +
     * 3x3x2 canopy of {@link Material#OAK_LEAVES}) at {@code loc}.
     * Real Bukkit grows species-specific trees per {@link TreeType};
     * RDForward only models OAK_LOG / OAK_LEAVES so every species
     * collapses to the same shape — sufficient for Essentials's
     * {@code /tree} / {@code /bigtree} commands. Returns whether any
     * blocks were actually placed.
     *
     * <p>Bukkit's {@code Commandbigtree} call site has descriptor
     * {@code (Lorg/bukkit/Location;Lorg/bukkit/TreeType;)Z} — this is
     * the pre-1.5 two-arg shape, distinct from the modern
     * {@code (Location, Random, TreeType)} variant on
     * {@link RegionAccessor}.
     */
    default boolean generateTree(Location loc, TreeType type) {
        if (loc == null) return false;
        int bx = loc.getBlockX();
        int by = loc.getBlockY();
        int bz = loc.getBlockZ();
        boolean placed = false;
        for (int i = 0; i < 5; i++) {
            placed |= setBlockType(bx, by + i, bz, Material.OAK_LOG);
        }
        int top = by + 4;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                    placed |= setBlockType(bx + dx, top + dy, bz + dz, Material.OAK_LEAVES);
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                placed |= setBlockType(bx + dx, top + 2, bz + dz, Material.OAK_LEAVES);
            }
        }
        return placed;
    }

    /* ---- Weather. RDForward has no weather model; getters report
     *  clear conditions, setters are no-ops, durations stay at zero.
     *  Essentials's {@code Commandweather} touches all six on every
     *  invocation. ---- */
    default boolean hasStorm() { return false; }
    default void setStorm(boolean storm) {}
    default int getWeatherDuration() { return 0; }
    default void setWeatherDuration(int duration) {}
    default boolean isThundering() { return false; }
    default void setThundering(boolean thundering) {}
    default int getThunderDuration() { return 0; }
    default void setThunderDuration(int duration) {}

    /**
     * @return the world's spawn point as a {@link Location}. Default is
     *         a fallback at (0, maxHeight, 0); concrete adapters
     *         (notably {@link
     *         com.github.martinambrus.rdforward.bridge.bukkit.BukkitWorldAdapter})
     *         override to surface the real chunk-aligned spawn from the
     *         backing rd-api World. Essentials's home-fallback path calls
     *         this when no per-user home is set.
     */
    default Location getSpawnLocation() {
        return new Location(this, 0d, getMaxHeight(), 0d, 0f, 0f);
    }

    /** @return cumulative world age in ticks. Bukkit defines
     *  {@link #getTime()} as the day-cycle time (modulo 24000) and
     *  {@code getFullTime()} as the monotonic world age. RDForward
     *  models a single static-time world, so the same backing tick
     *  counter satisfies both — Essentials's {@code KeywordReplacer}
     *  reads it for the {@code @TIME} keyword. */
    default long getFullTime() { return getTime(); }

    /** @return {@link World$Environment#NORMAL}. RDForward only models a
     *  single overworld dimension, but LuckPerms's
     *  {@code BukkitPlayerCalculator.calculate} reads
     *  {@code player.getWorld().getEnvironment()} on every permission
     *  check — and a {@link NoSuchMethodError} there cascades through
     *  {@code QueryOptionsCache.supply}, denying every {@code
     *  hasPermission} call and (because LoginSecurity routes its
     *  {@code onAuthChange} through {@code hasPermission}) kicking the
     *  player off the server. */
    default World$Environment getEnvironment() {
        return World$Environment.NORMAL;
    }

    /** VanishNoPacket plays a smoke particle effect at the toggling
     *  player's location on every {@code /vanish} (visual cue for the
     *  fake "poof"). RDForward has no particle/effect pipeline, so the
     *  call is a no-op — but it MUST exist or Vanish disconnects the
     *  client with NoSuchMethodError. The {@link
     *  com.github.martinambrus.rdforward.api.stub.StubCallLog} surface
     *  flags the gap once per plugin so operators see what's missing. */
    default void playEffect(org.bukkit.Location location, org.bukkit.Effect effect, int data) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.playEffect(Lorg/bukkit/Location;Lorg/bukkit/Effect;I)V");
    }

    default void playEffect(org.bukkit.Location location, org.bukkit.Effect effect, int data, int radius) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.playEffect(Lorg/bukkit/Location;Lorg/bukkit/Effect;II)V");
    }

    /** Generic-typed effect data variant — Effect.RECORD_PLAY etc.
     *  use {@code Material} for {@code data}. Same no-op shape. */
    default <T> void playEffect(org.bukkit.Location location, org.bukkit.Effect effect, T data) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.playEffect(Lorg/bukkit/Location;Lorg/bukkit/Effect;Ljava/lang/Object;)V");
    }

    default <T> void playEffect(org.bukkit.Location location, org.bukkit.Effect effect, T data, int radius) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.playEffect(Lorg/bukkit/Location;Lorg/bukkit/Effect;Ljava/lang/Object;I)V");
    }

    /** VanishNoPacket 3.15's "fake explosion" /vanish effect calls
     *  {@code world.playSound(loc, Sound.EXPLODE, 4.0f, 0.7f)} for the
     *  audio cue. RDForward has no audio pipeline — no-op + log once.
     *  Without this overload Vanish disconnects the client with
     *  NoSuchMethodError. */
    default void playSound(org.bukkit.Location location, org.bukkit.Sound sound,
                           float volume, float pitch) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.playSound(Lorg/bukkit/Location;Lorg/bukkit/Sound;FF)V");
    }

    default void playSound(org.bukkit.Location location, String sound,
                           float volume, float pitch) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.playSound(Lorg/bukkit/Location;Ljava/lang/String;FF)V");
    }

    /** VanishNoPacket fires a fake (no-block-damage, no-fire) explosion
     *  at the toggling player's location for the audio/particle cue.
     *  RDForward has no explosion pipeline — return {@code false}
     *  (not handled) and log once so operators see the gap. Without
     *  this overload Vanish disconnects the client with NoSuchMethodError
     *  on the (double, double, double, float, boolean, boolean) form. */
    default boolean createExplosion(double x, double y, double z, float power,
                                    boolean setFire, boolean breakBlocks) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.createExplosion(DDDFZZ)Z");
        return false;
    }

    default boolean createExplosion(double x, double y, double z, float power,
                                    boolean setFire) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.createExplosion(DDDFZ)Z");
        return false;
    }

    default boolean createExplosion(double x, double y, double z, float power) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.createExplosion(DDDF)Z");
        return false;
    }

    default boolean createExplosion(org.bukkit.Location location, float power) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.createExplosion(Lorg/bukkit/Location;F)Z");
        return false;
    }

    default boolean createExplosion(org.bukkit.Location location, float power, boolean setFire) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.createExplosion(Lorg/bukkit/Location;FZ)Z");
        return false;
    }

    /** VanishNoPacket strikes a fake (no-damage, visual-only) lightning
     *  bolt at the toggling player's location for the audio/visual cue.
     *  RDForward has no lightning entity — return null + log once.
     *  Plugins that null-check the result (most do) handle the absent
     *  strike gracefully; without this overload the legacy single-arg
     *  form crashes the client with NoSuchMethodError. */
    default org.bukkit.entity.LightningStrike strikeLightningEffect(org.bukkit.Location loc) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.strikeLightningEffect(Lorg/bukkit/Location;)Lorg/bukkit/entity/LightningStrike;");
        return null;
    }

    default org.bukkit.entity.LightningStrike strikeLightning(org.bukkit.Location loc) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.strikeLightning(Lorg/bukkit/Location;)Lorg/bukkit/entity/LightningStrike;");
        return null;
    }

    /** VanishNoPacket spawns 10 fake bats per /vanish toggle and chains
     *  {@code .getUniqueId()} on each return value to stash the UUIDs
     *  for later visual cleanup. RDForward has no entity system, but the
     *  caller still needs a non-null {@link org.bukkit.entity.Entity}
     *  with a valid UUID — return a {@link
     *  com.github.martinambrus.rdforward.bridge.bukkit.StubEntity} proxy.
     *  The Vanish cleanup task iterates {@link #getEntities()} 60 ticks
     *  later and only matches proxies by UUID; with our empty entity
     *  list it silently no-ops, so the proxy never needs to back a real
     *  entity. */
    default <T extends org.bukkit.entity.Entity> T spawn(org.bukkit.Location loc, java.lang.Class<T> clazz) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.spawn(Lorg/bukkit/Location;Ljava/lang/Class;)Lorg/bukkit/entity/Entity;");
        if (clazz == null) {
            // Plugins (e.g. Essentials's /spawnmob) may pass a null class
            // when EntityType.getEntityClass() doesn't resolve to a known
            // interface. Returning a stub typed only as Entity would fail
            // the bytecode-level checkcast that follows ((LivingEntity)
            // world.spawn(...)). Returning null is safe — Java's
            // checkcast accepts null for any reference type.
            return null;
        }
        return com.github.martinambrus.rdforward.bridge.bukkit.StubEntity.create(clazz, null, loc);
    }

    default org.bukkit.entity.Entity spawnEntity(org.bukkit.Location loc, org.bukkit.entity.EntityType type) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.spawnEntity(Lorg/bukkit/Location;Lorg/bukkit/entity/EntityType;)Lorg/bukkit/entity/Entity;");
        return com.github.martinambrus.rdforward.bridge.bukkit.StubEntity.create(type, loc);
    }

    default org.bukkit.entity.Entity spawnEntity(org.bukkit.Location loc, org.bukkit.entity.EntityType type, boolean randomizeData) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.World.spawnEntity(Lorg/bukkit/Location;Lorg/bukkit/entity/EntityType;Z)Lorg/bukkit/entity/Entity;");
        return com.github.martinambrus.rdforward.bridge.bukkit.StubEntity.create(type, loc);
    }

    /** RDForward has no entity system; return an empty list. Vanish's
     *  delayed bat-cleanup iterates this; an empty list lets the
     *  cleanup loop run as a no-op instead of NPE-ing on null. */
    default java.util.List<org.bukkit.entity.Entity> getEntities() {
        return java.util.Collections.emptyList();
    }
}
