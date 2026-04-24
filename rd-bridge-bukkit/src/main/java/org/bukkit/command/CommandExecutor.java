// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.command;

/**
 * Bukkit-shaped command executor. Plugins attach implementations via
 * {@code JavaPlugin.getCommand(name).setExecutor(...)}.
 *
 * <p>RDForward does not currently plumb {@code setExecutor} through to the
 * rd-api command registry — plugins that want working commands should use
 * the rd-api registry directly. Documented in
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge}.
 */
@FunctionalInterface
public interface CommandExecutor {

    boolean onCommand(CommandSender sender, Command command, String label, String[] args);
}
