package org.bukkit.command;

/**
 * Bukkit-shaped sender of a command. Concrete implementations are
 * {@link org.bukkit.entity.Player} (in-game player) and
 * {@link ConsoleCommandSender} (server console).
 */
public interface CommandSender {

    String getName();

    void sendMessage(String message);

    /** @return true if this sender has operator privileges. */
    boolean isOp();
}
