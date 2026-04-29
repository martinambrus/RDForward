// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.server;

/**
 * Fired by RDForward's BukkitPluginWrapper after a plugin's
 * {@code onDisable()} returns. Mirrors {@link PluginEnableEvent} — used
 * by Essentials's {@code EssentialsPluginListener} to clean up payment
 * method bindings when a dependency unloads.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PluginDisableEvent extends org.bukkit.event.server.PluginEvent {

    private static final org.bukkit.event.HandlerList HANDLERS =
            new org.bukkit.event.HandlerList();

    public PluginDisableEvent(org.bukkit.plugin.Plugin plugin) {
        super(plugin);
    }

    public PluginDisableEvent() {
        super();
    }

    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
