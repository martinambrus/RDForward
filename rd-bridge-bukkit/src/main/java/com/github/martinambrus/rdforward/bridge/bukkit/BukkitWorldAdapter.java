// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.RDServer;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.server.ServerWorld.WeatherState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.RegionAccessor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 * Wraps an rd-api {@link com.github.martinambrus.rdforward.api.world.World}
 * as a Bukkit-shaped {@link World}. Block reads/writes translate through
 * {@link MaterialMapper}.
 *
 * <p>Weather methods cast the rd-api backing to {@link ServerWorld} (the
 * only real implementation in production); test fixtures that pass a
 * non-ServerWorld backing observe no-op weather behaviour. Cancellable
 * {@link WeatherChangeEvent} / {@link ThunderChangeEvent} are fired
 * before any state change so plugin listeners can veto.
 */
public final class BukkitWorldAdapter implements World, RegionAccessor {

    private final com.github.martinambrus.rdforward.api.world.World backing;

    public BukkitWorldAdapter(com.github.martinambrus.rdforward.api.world.World backing) {
        this.backing = backing;
    }

    @Override public String getName() { return backing.getName(); }

    /** Override getBlockAt(Location) to log when called from HSP's findSafeLocation2. */
    @Override
    public Block getBlockAt(org.bukkit.Location loc) {
        if (loc == null) return null;
        return getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        com.github.martinambrus.rdforward.api.world.Block b = backing.getBlockAt(x, y, z);
        Material mat = b == null ? Material.AIR : MaterialMapper.fromApi(b.getType());
        if (b == null) return new BukkitBlock(this, x, y, z, Material.AIR);
        return new BukkitBlock(this, b.getX(), b.getY(), b.getZ(), mat);
    }

    @Override
    public boolean setBlockType(int x, int y, int z, Material type) {
        return backing.setBlock(x, y, z, MaterialMapper.toApi(type));
    }

    @Override public int getMaxHeight() { return backing.getHeight(); }

    /** Override the World default to size the border to the actual rd-server
     *  world bounds. The default {@link RDStubWorldBorder} reports
     *  {@code Integer.MAX_VALUE}; EssentialsX's {@code RandomTeleport.getMaxRange}
     *  reads {@code border.getSize() / 2.0} as the default max-range, which
     *  with MAX_VALUE picks teleport targets ~1 billion blocks away — far
     *  outside our 256-block world, so the player ends up in unloaded chunks
     *  and the position packet effectively no-ops on the client. */
    @Override
    public WorldBorder getWorldBorder() {
        if (cachedBorder == null) {
            RDStubWorldBorder b = new RDStubWorldBorder(this);
            b.setSize(Math.min(backing.getWidth(), backing.getDepth()));
            cachedBorder = b;
        }
        return cachedBorder;
    }
    private WorldBorder cachedBorder;
    @Override public long getTime() { return backing.getTime(); }
    @Override public void setTime(long time) { backing.setTime(time); }

    /** Chunk-aligned world center, mirroring {@code ServerWorld.getSpawnX/Z}.
     *  Y rides the highest non-air block at the spawn column so plugins
     *  that run a "safe spawn" probe (HomeSpawnPlus's Teleport.findSafeLocation2,
     *  EssentialsX's spawn warmup) see solid ground directly below feet
     *  instead of an unbounded air column. Earlier versions returned
     *  {@code backing.getHeight()} (the world ceiling); HSP then scanned
     *  ±maxRange levels and never reached real ground, returned null,
     *  and a downstream re-entry crashed with a null-baseLocation NPE.
     *  Bukkit feet-level convention; +0.5 centers the player on the
     *  spawn block. */
    @Override
    public Location getSpawnLocation() {
        int spawnX = ((backing.getWidth() / 2) >> 4) * 16 + 8;
        int spawnZ = ((backing.getDepth() / 2) >> 4) * 16 + 8;
        int height = backing.getHeight();
        // Walk down from the world ceiling looking for the first
        // air-air-solid stack: feet (Y) air, head (Y+1) air, ground
        // (Y-1) solid. {@code getHighestBlockYAt} alone reports the
        // topmost non-air block, but if the spawn column has player-
        // placed blocks near the ceiling it returns a Y where there's
        // no air gap above. HomeSpawnPlus's {@code Teleport.findSafeLocation2}
        // needs the air-gap pattern to mark the spawn safe; without it
        // the strategy result wraps null, gets re-entered downstream,
        // and crashes with a null-baseLocation NPE.
        int groundY = -1;
        for (int y = height - 1; y >= 1; y--) {
            org.bukkit.block.Block feet = getBlockAt(spawnX, y, spawnZ);
            org.bukkit.block.Block head = getBlockAt(spawnX, y + 1, spawnZ);
            org.bukkit.block.Block floor = getBlockAt(spawnX, y - 1, spawnZ);
            if (feet != null && feet.getType() == Material.AIR
                    && (head == null || head.getType() == Material.AIR)
                    && floor != null && floor.getType() != Material.AIR) {
                groundY = y;
                break;
            }
        }
        if (groundY < 0) {
            // No air-air-solid stack — column is either fully air (rare;
            // unwritten overlay) or fully solid (player filled the whole
            // shaft). Pick the highest non-air Y +1 instead and trust the
            // plugin-side safety scan to handle the head/feet check.
            int top = -1;
            for (int y = height - 1; y >= 0; y--) {
                org.bukkit.block.Block b = getBlockAt(spawnX, y, spawnZ);
                if (b != null && b.getType() != Material.AIR) { top = y; break; }
            }
            groundY = top >= 0 ? Math.min(top + 1, height - 1) : height / 3;
        }
        return new Location(this, spawnX + 0.5, groundY, spawnZ + 0.5, 0f, 0f);
    }

    /* ---- Weather. Backed by {@link ServerWorld#getWeather} /
     *  {@code setWeather} + {@link PlayerManager#broadcastWeatherChange}.
     *  Bukkit splits "is storming" and "is thundering" into two booleans;
     *  rd-server collapses to one tristate {@link WeatherState}, mapped:
     *  <ul>
     *    <li>!storm           -> CLEAR</li>
     *    <li>storm  + !thunder -> RAIN</li>
     *    <li>storm  + thunder  -> THUNDER (Minecraft requires storm to thunder)</li>
     *  </ul>
     *  ---- */

    private ServerWorld serverWorld() {
        // The api-side world the bridge wraps is
        // {@code modloader.impl.RDWorld}, NOT a raw {@link ServerWorld};
        // unwrap via its {@code delegate()} accessor. Test fixtures that
        // pass a non-RDWorld backing observe no-op weather behaviour.
        if (backing instanceof com.github.martinambrus.rdforward.modloader.impl.RDWorld rdw) {
            return rdw.delegate();
        }
        return backing instanceof ServerWorld sw ? sw : null;
    }

    private PlayerManager playerManager() {
        // Same wrapper layering: {@code BukkitBridge.currentRdServer()}
        // returns {@code modloader.impl.RDServer} (api-side adapter),
        // which exposes the live {@link PlayerManager} via
        // {@code playerManager()}.
        com.github.martinambrus.rdforward.api.server.Server rd = BukkitBridge.currentRdServer();
        if (rd instanceof com.github.martinambrus.rdforward.modloader.impl.RDServer api) {
            return api.playerManager();
        }
        return rd instanceof RDServer rds ? rds.getPlayerManager() : null;
    }

    @Override
    public boolean hasStorm() {
        ServerWorld sw = serverWorld();
        return sw != null && sw.getWeather() != WeatherState.CLEAR;
    }

    @Override
    public boolean isThundering() {
        ServerWorld sw = serverWorld();
        return sw != null && sw.getWeather() == WeatherState.THUNDER;
    }

    @Override
    public int getWeatherDuration() {
        ServerWorld sw = serverWorld();
        return sw == null ? 0 : sw.getWeatherDuration();
    }

    @Override
    public int getThunderDuration() {
        // ServerWorld uses a single duration counter for both states;
        // expose it on the thunder accessor too so plugins reading
        // either Bukkit field see a non-zero value while a thunder
        // state is active.
        ServerWorld sw = serverWorld();
        return sw == null || sw.getWeather() != WeatherState.THUNDER ? 0 : sw.getWeatherDuration();
    }

    @Override
    public void setStorm(boolean storming) {
        ServerWorld sw = serverWorld();
        if (sw == null) return;
        boolean wasStorming = sw.getWeather() != WeatherState.CLEAR;
        if (wasStorming == storming) return;
        WeatherChangeEvent ev = new WeatherChangeEvent(this, storming, null);
        BukkitEventAdapter.dispatchPluginEvent(ev);
        if (ev.isCancelled()) return;
        WeatherState target = storming
                // Preserve thunder if already in THUNDER; turning storm on
                // from CLEAR drops to RAIN per Bukkit semantics.
                ? (sw.getWeather() == WeatherState.THUNDER ? WeatherState.THUNDER : WeatherState.RAIN)
                : WeatherState.CLEAR;
        sw.setWeather(target, sw.getWeatherDuration());
        PlayerManager pm = playerManager();
        if (pm != null) pm.broadcastWeatherChange(target);
    }

    @Override
    public void setThundering(boolean thundering) {
        ServerWorld sw = serverWorld();
        if (sw == null) return;
        boolean wasThundering = sw.getWeather() == WeatherState.THUNDER;
        if (wasThundering == thundering) return;
        ThunderChangeEvent ev = new ThunderChangeEvent(this, thundering, null);
        BukkitEventAdapter.dispatchPluginEvent(ev);
        if (ev.isCancelled()) return;
        // Thunder requires storm to be on (Minecraft semantic). Turning
        // thunder on from CLEAR escalates straight to THUNDER; turning
        // thunder off keeps storm if it was on.
        WeatherState target = thundering
                ? WeatherState.THUNDER
                : (sw.getWeather() == WeatherState.CLEAR ? WeatherState.CLEAR : WeatherState.RAIN);
        sw.setWeather(target, sw.getWeatherDuration());
        PlayerManager pm = playerManager();
        if (pm != null) pm.broadcastWeatherChange(target);
    }

    @Override
    public void setWeatherDuration(int duration) {
        ServerWorld sw = serverWorld();
        if (sw == null) return;
        // Re-apply current state with the new duration; broadcast not
        // needed since the visible weather state isn't changing.
        sw.setWeather(sw.getWeather(), duration);
    }

    @Override
    public void setThunderDuration(int duration) {
        ServerWorld sw = serverWorld();
        if (sw == null) return;
        sw.setWeather(sw.getWeather(), duration);
    }

    /* ---- {@link RegionAccessor} surface. EssentialsX's
     *  {@code PaperBiomeKeyProvider.getBiomeKey} casts
     *  {@code (RegionAccessor) block.getWorld()} during /tpr's biome
     *  exclusion check; without this the cast throws CCE silently inside
     *  the CompletableFuture chain and the teleport never fires. Most
     *  methods are stubs — RDForward has no entity / per-block-data /
     *  fluid model — but the type assignability is what matters at the
     *  bytecode CHECKCAST instruction. ---- */

    @Override public org.bukkit.block.Biome getBiome(int x, int y, int z) { return Block.RD_STUB_BIOME; }
    @Override public org.bukkit.block.Biome getComputedBiome(int x, int y, int z) { return Block.RD_STUB_BIOME; }
    @Override public void setBiome(int x, int y, int z, org.bukkit.block.Biome biome) { /* no-op */ }
    @Override public org.bukkit.block.BlockState getBlockState(int x, int y, int z) {
        return new BukkitBlockState(this, x, y, z, getType(x, y, z));
    }
    @Override public io.papermc.paper.block.fluid.FluidData getFluidData(int x, int y, int z) { return null; }
    @Override public org.bukkit.block.data.BlockData getBlockData(int x, int y, int z) {
        return new BukkitBlockData(getType(x, y, z));
    }
    @Override public Material getType(int x, int y, int z) {
        com.github.martinambrus.rdforward.api.world.Block b = backing.getBlockAt(x, y, z);
        return b == null ? Material.AIR : MaterialMapper.fromApi(b.getType());
    }
    @Override public void setBlockData(int x, int y, int z, org.bukkit.block.data.BlockData data) {
        if (data == null) { backing.setBlock(x, y, z, MaterialMapper.toApi(Material.AIR)); return; }
        backing.setBlock(x, y, z, MaterialMapper.toApi(data.getMaterial()));
    }
    @Override public boolean generateTree(Location loc, java.util.Random rand, org.bukkit.TreeType type) {
        return generateTree(loc, type);
    }
    @Override public boolean generateTree(Location loc, java.util.Random rand, org.bukkit.TreeType type, java.util.function.Consumer consumer) {
        return generateTree(loc, type);
    }
    @Override public boolean generateTree(Location loc, java.util.Random rand, org.bukkit.TreeType type, java.util.function.Predicate predicate) {
        return generateTree(loc, type);
    }
    @Override public java.util.List getLivingEntities() { return java.util.Collections.emptyList(); }
    @Override public java.util.Collection getEntitiesByClass(Class clazz) { return java.util.Collections.emptyList(); }
    @Override public java.util.Collection getEntitiesByClasses(Class[] classes) { return java.util.Collections.emptyList(); }
    @Override public org.bukkit.entity.Entity createEntity(Location loc, Class clazz) { return null; }
    @Override public org.bukkit.entity.Entity spawn(Location loc, Class clazz, java.util.function.Consumer consumer, org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason reason) { return null; }
    @Override public org.bukkit.entity.Entity spawn(Location loc, Class clazz, boolean random, java.util.function.Consumer consumer) { return null; }
    @Override public int getHighestBlockYAt(int x, int z, org.bukkit.HeightMap hm) { return getHighestBlockYAt(x, z); }
    @Override public int getHighestBlockYAt(Location loc, org.bukkit.HeightMap hm) { return getHighestBlockYAt(loc); }
    @Override public org.bukkit.entity.Entity addEntity(org.bukkit.entity.Entity entity) { return entity; }
    @Override public io.papermc.paper.world.MoonPhase getMoonPhase() { return null; }
    @Override public NamespacedKey getKey() { return NamespacedKey.minecraft(getName() == null ? "world" : getName().toLowerCase(java.util.Locale.ENGLISH)); }
    @Override public boolean lineOfSightExists(Location from, Location to) { return true; }
    @Override public boolean hasCollisionsIn(org.bukkit.util.BoundingBox box) { return false; }
    @Override public java.util.Set getFeatureFlags() { return java.util.Collections.emptySet(); }

    /* Default-default conflicts between {@link World} and
     *  {@link RegionAccessor} — Java forces a class override when the
     *  same signature has visible defaults on two unrelated interfaces.
     *  RegionAccessor's {@code spawn} is raw {@code Entity spawn(Location, Class)};
     *  World's is generic {@code <T extends Entity> T spawn(Location, Class<T>)}.
     *  Same erasure → name clash without a single bridge override using raw types. */
    @Override public org.bukkit.entity.Entity spawnEntity(Location loc, org.bukkit.entity.EntityType type) {
        return World.super.spawnEntity(loc, type);
    }
    @Override public org.bukkit.entity.Entity spawn(Location loc, Class clazz) {
        return World.super.spawn(loc, clazz);
    }

    /* getHighestBlockYAt(Location) — World provides a default but Java
     *  still requires explicit resolution because RegionAccessor declares
     *  it abstract on the same interface set. Bridge to World.super. */
    @Override public int getHighestBlockYAt(Location loc) {
        return World.super.getHighestBlockYAt(loc);
    }
    @Override public int getHighestBlockYAt(int x, int z) {
        return World.super.getHighestBlockYAt(x, z);
    }
    @Override public java.util.List getEntities() { return World.super.getEntities(); }
    @Override public org.bukkit.entity.Entity spawnEntity(Location loc, org.bukkit.entity.EntityType type, boolean random) {
        return World.super.spawnEntity(loc, type, random);
    }
}
