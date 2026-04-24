package com.github.martinambrus.rdforward.api.command;

import java.util.List;

/**
 * Provides tab completion suggestions for a command.
 *
 * <p>Only supported on protocol versions that carry command suggestions
 * on-the-wire (1.13+ via Brigadier). For older clients, suggestions are
 * synthesized into chat when possible but cannot integrate with the client's
 * input UI.
 */
@FunctionalInterface
public interface TabCompleter {
    List<String> complete(CommandContext ctx);
}
