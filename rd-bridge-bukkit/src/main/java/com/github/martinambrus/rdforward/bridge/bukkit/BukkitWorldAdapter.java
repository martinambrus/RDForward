// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.RDServer;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.server.ServerWorld.WeatherState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
public final class BukkitWorldAdapter implements World {

    private final com.github.martinambrus.rdforward.api.world.World backing;

    public BukkitWorldAdapter(com.github.martinambrus.rdforward.api.world.World backing) {
        this.backing = backing;
    }

    @Override public String getName() { return backing.getName(); }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        com.github.martinambrus.rdforward.api.world.Block b = backing.getBlockAt(x, y, z);
        // Out-of-bounds coords (Y below floor / above ceiling, unloaded
        // chunk) return null from rd-api but Bukkit plugins (Essentials's
        // teleport-safety check) call .getType() unconditionally — return
        // an AIR-typed Block stub at the requested coords instead.
        if (b == null) return new BukkitBlock(this, x, y, z, Material.AIR);
        return new BukkitBlock(this, b.getX(), b.getY(), b.getZ(), MaterialMapper.fromApi(b.getType()));
    }

    @Override
    public boolean setBlockType(int x, int y, int z, Material type) {
        return backing.setBlock(x, y, z, MaterialMapper.toApi(type));
    }

    @Override public int getMaxHeight() { return backing.getHeight(); }
    @Override public long getTime() { return backing.getTime(); }
    @Override public void setTime(long time) { backing.setTime(time); }

    /** Chunk-aligned world center, mirroring {@code ServerWorld.getSpawnX/Z}.
     *  Y is the max height so plugin-side safe-teleport scans start above
     *  ground. Bukkit feet-level convention; the +0.5 centers the player
     *  on the spawn block. */
    @Override
    public Location getSpawnLocation() {
        int spawnX = ((backing.getWidth() / 2) >> 4) * 16 + 8;
        int spawnZ = ((backing.getDepth() / 2) >> 4) * 16 + 8;
        return new Location(this, spawnX + 0.5, backing.getHeight(), spawnZ + 0.5, 0f, 0f);
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
}
