// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.command;

import java.util.List;

/**
 * Bukkit-shaped tab completer. Plugins attach via
 * {@code JavaPlugin.getCommand(name).setTabCompleter(...)}. The bridge
 * does not forward tab completion through to the rd-api command registry
 * — this stub exists only for compile compatibility.
 */
@FunctionalInterface
public interface TabCompleter {

    List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args);
}
