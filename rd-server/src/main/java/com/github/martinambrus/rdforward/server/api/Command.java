package com.github.martinambrus.rdforward.server.api;

/**
 * A server command that can be executed by players (via chat) or the console.
 *
 * Commands are registered with {@link CommandRegistry} and invoked when a
 * player sends a message starting with "/" or the console types the command name.
 *
 * Example:
 * <pre>
 *   CommandRegistry.register("spawn", "Teleport to spawn", (ctx) -> {
 *       ctx.reply("Teleporting to spawn...");
 *   });
 * </pre>
 */
@FunctionalInterface
public interface Command {
    void execute(CommandContext context);
}
