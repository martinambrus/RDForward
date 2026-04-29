// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.server;

/**
 * Carries the {@link org.bukkit.plugin.Plugin} reference for
 * {@link PluginEnableEvent} / {@link PluginDisableEvent} so listeners that
 * call {@code event.getPlugin()} (Essentials's
 * {@code EssentialsPluginListener.onPluginEnable} dispatches its
 * permissions handler reload off this) get a real plugin instead of the
 * auto-stub's {@code null}.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class PluginEvent extends org.bukkit.event.server.ServerEvent {

    private final org.bukkit.plugin.Plugin plugin;

    protected PluginEvent(org.bukkit.plugin.Plugin plugin) {
        this.plugin = plugin;
    }

    protected PluginEvent() {
        this.plugin = null;
    }

    public org.bukkit.plugin.Plugin getPlugin() {
        return plugin;
    }
}
