// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.command;

/**
 * Bukkit-shaped sender of a command. Concrete implementations are
 * {@link org.bukkit.entity.Player} (in-game player) and
 * {@link ConsoleCommandSender} (server console). Extends
 * {@link org.bukkit.permissions.Permissible} to match paper-api so
 * plugin bytecode that calls {@code sender.hasPermission(String)} via
 * {@code invokeinterface} on a {@link CommandSender}-typed reference
 * resolves to a real interface method (avoiding
 * {@link NoSuchMethodError} at link time).
 */
public interface CommandSender extends org.bukkit.permissions.Permissible {

    String getName();

    void sendMessage(String message);

    /** @return true if this sender has operator privileges. */
    boolean isOp();
}
