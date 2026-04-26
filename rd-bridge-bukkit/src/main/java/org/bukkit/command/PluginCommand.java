// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.command;

import org.bukkit.plugin.Plugin;

/**
 * Concrete command a plugin exposes via {@code plugin.yml}. Carries the
 * same metadata as {@link Command} plus {@link #setExecutor} /
 * {@link #setTabCompleter} hooks the plugin uses at {@code onEnable()} time.
 *
 * <p>The Bukkit bridge scans {@code JavaPlugin.getCommand(name)} for plugins
 * that assign an executor, then registers a forwarder with the rd-api
 * {@code CommandRegistry} when the plugin is enabled.
 *
 * <p>Implements {@link PluginIdentifiableCommand} so plugin code (notably
 * LuckPerms) that does {@code ((PluginIdentifiableCommand) cmd).getPlugin()}
 * — or that calls {@code PluginCommand#getPlugin} directly — gets a real
 * answer. The owner reference is wired by
 * {@code BukkitPluginLoader} immediately after the plugin instance and
 * its command map are wired together.
 */
public class PluginCommand extends Command implements PluginIdentifiableCommand {

    private CommandExecutor executor;
    private TabCompleter tabCompleter;
    private Plugin owner;

    public PluginCommand(String name) {
        super(name);
    }

    /** Upstream-shaped constructor for code that instantiates commands
     *  outside the loader (rare; LuckPerms's command registry takes this
     *  path when synthesising commands at runtime). */
    public PluginCommand(String name, Plugin owner) {
        super(name);
        this.owner = owner;
    }

    public CommandExecutor getExecutor() { return executor; }
    public void setExecutor(CommandExecutor executor) { this.executor = executor; }

    public TabCompleter getTabCompleter() { return tabCompleter; }
    public void setTabCompleter(TabCompleter tabCompleter) { this.tabCompleter = tabCompleter; }

    @Override
    public Plugin getPlugin() { return owner; }

    /** Bridge hook — called after plugin instantiation so
     *  {@link #getPlugin()} returns the loaded {@link Plugin} rather than
     *  {@code null}. */
    public void setPlugin(Plugin plugin) { this.owner = plugin; }
}
