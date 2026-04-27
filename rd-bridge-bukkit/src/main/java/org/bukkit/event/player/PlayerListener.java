// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

import org.bukkit.event.Listener;

/**
 * Pre-Bukkit-1.x {@code PlayerListener} concrete base class. Modern
 * Bukkit listeners are plain {@link Listener} implementors with
 * {@code @EventHandler}-annotated methods, but legacy plugins
 * (LogBlockQuestioner 0.02) extend this class and override the
 * specific {@code onPlayerXxx} method they care about.
 *
 * <p>Methods are concrete no-ops so subclasses can override only what
 * they need (the historical pattern). RDForward's bridge does not
 * route the legacy {@code registerEvent(Event.Type, Listener, ...)}
 * form into actual event dispatch, so overrides here will not fire
 * — plugins extending {@code PlayerListener} load cleanly but their
 * callbacks remain dormant. Modernized plugins using {@code
 * @EventHandler} continue to work via {@link
 * com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter}.
 */
@SuppressWarnings("unused")
public class PlayerListener implements Listener {

    public void onPlayerJoin(PlayerJoinEvent event) {}

    public void onPlayerQuit(PlayerQuitEvent event) {}

    public void onPlayerKick(PlayerKickEvent event) {}

    public void onPlayerChat(PlayerChatEvent event) {}

    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {}

    public void onPlayerMove(PlayerMoveEvent event) {}

    public void onPlayerTeleport(PlayerTeleportEvent event) {}

    public void onPlayerInteract(PlayerInteractEvent event) {}

    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {}

    public void onPlayerLogin(PlayerLoginEvent event) {}

    public void onPlayerPreLogin(PlayerPreLoginEvent event) {}

    public void onPlayerEggThrow(PlayerEggThrowEvent event) {}

    public void onPlayerAnimation(PlayerAnimationEvent event) {}

    public void onItemHeldChange(PlayerItemHeldEvent event) {}

    public void onPlayerItemHeld(PlayerItemHeldEvent event) {}

    public void onPlayerDropItem(PlayerDropItemEvent event) {}

    public void onPlayerPickupItem(PlayerPickupItemEvent event) {}

    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {}

    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {}

    public void onPlayerBedEnter(PlayerBedEnterEvent event) {}

    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {}

    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {}

    public void onPlayerBucketFill(PlayerBucketFillEvent event) {}

    public void onPlayerRespawn(PlayerRespawnEvent event) {}

    public void onPlayerVelocity(PlayerVelocityEvent event) {}
}
