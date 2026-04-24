package com.github.martinambrus.rdforward.api.command;

/**
 * Handler invoked when a registered command is dispatched.
 */
@FunctionalInterface
public interface Command {
    void execute(CommandContext ctx);
}
