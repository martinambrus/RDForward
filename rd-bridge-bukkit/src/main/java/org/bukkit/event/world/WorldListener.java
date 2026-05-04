// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.world;

import org.bukkit.event.Listener;

/**
 * Pre-Bukkit-1.x {@code WorldListener} concrete base class. Modern
 * Bukkit listeners use plain {@link Listener} + {@code @EventHandler},
 * but legacy plugins (SpawnControl 1.x's {@code SCWorldListener})
 * extend this class and override the specific {@code onWorldXxx} method
 * they care about.
 *
 * <p>Methods are concrete no-ops so subclasses can override only what
 * they need (the historical pattern). Mirrors the
 * {@link org.bukkit.event.block.BlockListener} /
 * {@link org.bukkit.event.entity.EntityListener} /
 * {@link org.bukkit.event.player.PlayerListener} stubs.
 */
@SuppressWarnings("unused")
public class WorldListener implements Listener {

    public void onWorldInit(WorldInitEvent event) {}

    public void onWorldLoad(WorldLoadEvent event) {}

    public void onWorldUnload(WorldUnloadEvent event) {}

    public void onWorldSave(WorldSaveEvent event) {}

    public void onChunkLoad(ChunkLoadEvent event) {}

    public void onChunkUnload(ChunkUnloadEvent event) {}

    public void onChunkPopulated(ChunkPopulateEvent event) {}

    public void onSpawnChange(SpawnChangeEvent event) {}

    public void onPortalCreate(PortalCreateEvent event) {}
}
