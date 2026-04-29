// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.server;

/**
 * Fired by RDForward's BukkitPluginWrapper after a plugin's
 * {@code onEnable()} returns successfully. Essentials's
 * {@code EssentialsPluginListener.onPluginEnable} catches this and runs
 * {@code permissionsHandler.checkPermissions()} — without the event,
 * Essentials's handler stays on the default {@code NullPermissionsHandler}
 * (every {@code hasPermission} call returns false), so OP players are
 * denied access to every Essentials command. Plugin-fired events are
 * dispatched via {@code BukkitEventAdapter.dispatchPluginEvent}.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PluginEnableEvent extends org.bukkit.event.server.PluginEvent {

    private static final org.bukkit.event.HandlerList HANDLERS =
            new org.bukkit.event.HandlerList();

    public PluginEnableEvent(org.bukkit.plugin.Plugin plugin) {
        super(plugin);
    }

    public PluginEnableEvent() {
        super();
    }

    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
