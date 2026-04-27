// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.entity;

import org.bukkit.event.Listener;

/**
 * Pre-Bukkit-1.x {@code EntityListener} concrete base class. Modern
 * Bukkit listeners are plain {@link Listener} implementors with
 * {@code @EventHandler}-annotated methods, but legacy plugins
 * (OpenWarp v1.1) extend this class and override the specific
 * {@code onEntityXxx} method they care about.
 *
 * <p>Methods are concrete no-ops so subclasses can override only what
 * they need (the historical pattern). RDForward's bridge does not
 * route the legacy {@code registerEvent(Event.Type, Listener, ...)}
 * form into actual event dispatch, so overrides here will not fire
 * — plugins extending {@code EntityListener} load cleanly but their
 * callbacks remain dormant. Modernised plugins using {@code
 * @EventHandler} continue to work via {@link
 * com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter}.
 */
@SuppressWarnings("unused")
public class EntityListener implements Listener {

    public void onEntityDeath(EntityDeathEvent event) {}

    public void onEntityDamage(EntityDamageEvent event) {}

    public void onEntityCombust(EntityCombustEvent event) {}

    public void onEntityExplode(EntityExplodeEvent event) {}

    public void onEntityTarget(EntityTargetEvent event) {}

    public void onEntityInteract(EntityInteractEvent event) {}

    public void onEntityRegainHealth(EntityRegainHealthEvent event) {}

    public void onEntityShootBow(EntityShootBowEvent event) {}

    public void onEntityTeleport(EntityTeleportEvent event) {}

    public void onEntityTame(EntityTameEvent event) {}

    public void onCreatureSpawn(CreatureSpawnEvent event) {}

    public void onExplosionPrime(ExplosionPrimeEvent event) {}

    public void onItemDespawn(ItemDespawnEvent event) {}

    public void onItemSpawn(ItemSpawnEvent event) {}

    public void onProjectileHit(ProjectileHitEvent event) {}

    public void onFoodLevelChange(FoodLevelChangeEvent event) {}
}
