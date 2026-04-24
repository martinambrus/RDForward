// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.command;

/**
 * Bukkit-shaped console sender. Upstream paper-api declares this as an
 * interface, so plugins (e.g. LuckPerms) compile classes that declare
 * {@code implements ConsoleCommandSender}. Keeping the same kind here
 * avoids {@link IncompatibleClassChangeError} when the plugin
 * class-loader validates the plugin's bytecode against our bridge.
 *
 * <p>RDForward's concrete console implementation lives in
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.DefaultConsoleCommandSender}
 * and is installed on {@link org.bukkit.Bukkit} via
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge}.
 */
public interface ConsoleCommandSender extends CommandSender {
}
