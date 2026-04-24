// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.command;

/**
 * Concrete command a plugin exposes via {@code plugin.yml}. Carries the
 * same metadata as {@link Command} plus {@link #setExecutor} /
 * {@link #setTabCompleter} hooks the plugin uses at {@code onEnable()} time.
 *
 * <p>The Bukkit bridge scans {@code JavaPlugin.getCommand(name)} for plugins
 * that assign an executor, then registers a forwarder with the rd-api
 * {@code CommandRegistry} when the plugin is enabled.
 */
public class PluginCommand extends Command {

    private CommandExecutor executor;
    private TabCompleter tabCompleter;

    public PluginCommand(String name) {
        super(name);
    }

    public CommandExecutor getExecutor() { return executor; }
    public void setExecutor(CommandExecutor executor) { this.executor = executor; }

    public TabCompleter getTabCompleter() { return tabCompleter; }
    public void setTabCompleter(TabCompleter tabCompleter) { this.tabCompleter = tabCompleter; }
}
