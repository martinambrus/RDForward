package io.papermc.paper.command.brigadier;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

/**
 * Brigadier "source" passed into every command invocation. The bridge
 * always returns a real {@link CommandSender}; {@link #getExecutor()} is
 * stubbed to {@code null} in v1 (the bridge has no entity context).
 */
public interface CommandSourceStack {

    CommandSender getSender();

    Entity getExecutor();
}
