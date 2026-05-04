// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.block;

import org.bukkit.event.Listener;

/**
 * Pre-Bukkit-1.x {@code BlockListener} concrete base class. Modern
 * Bukkit listeners are plain {@link Listener} implementors with
 * {@code @EventHandler}-annotated methods, but legacy plugins
 * (Jail 2.1) extend this class and override the specific
 * {@code onBlockXxx} method they care about.
 *
 * <p>Methods are concrete no-ops so subclasses can override only what
 * they need (the historical pattern). RDForward's bridge does not
 * route the legacy {@code registerEvent(Event.Type, Listener, ...)}
 * form into actual event dispatch, so overrides here will not fire
 * — plugins extending {@code BlockListener} load cleanly but their
 * callbacks remain dormant. Modernized plugins using {@code
 * @EventHandler} continue to work via {@link
 * com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter}.
 */
@SuppressWarnings("unused")
public class BlockListener implements Listener {

    public void onBlockBreak(BlockBreakEvent event) {}

    public void onBlockPlace(BlockPlaceEvent event) {}

    public void onBlockDamage(BlockDamageEvent event) {}

    public void onBlockCanBuild(BlockCanBuildEvent event) {}

    public void onBlockFromTo(BlockFromToEvent event) {}

    public void onBlockIgnite(BlockIgniteEvent event) {}

    public void onBlockPhysics(BlockPhysicsEvent event) {}

    public void onBlockRedstoneChange(BlockRedstoneEvent event) {}

    public void onBlockBurn(BlockBurnEvent event) {}

    public void onBlockFade(BlockFadeEvent event) {}

    public void onBlockForm(BlockFormEvent event) {}

    public void onBlockSpread(BlockSpreadEvent event) {}

    public void onLeavesDecay(LeavesDecayEvent event) {}

    public void onSignChange(SignChangeEvent event) {}

    public void onBlockDispense(BlockDispenseEvent event) {}

    public void onBlockPistonExtend(BlockPistonExtendEvent event) {}

    public void onBlockPistonRetract(BlockPistonRetractEvent event) {}

    public void onBlockExp(BlockExpEvent event) {}
}
