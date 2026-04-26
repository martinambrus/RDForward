package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.command.ConsoleCommandSender;

import java.util.logging.Logger;

/**
 * RDForward's concrete {@link ConsoleCommandSender} implementation.
 * Lives here rather than in {@code org.bukkit.command} so the preserved
 * {@link ConsoleCommandSender} facade can stay an interface (matching
 * upstream paper-api) while plugin code that reflects on implementing
 * classes still sees a real type behind the {@code Server.getConsoleSender()}
 * return.
 *
 * <p>Messages are routed through a named {@link Logger} at INFO level so
 * console output shares the host server's standard logging pipeline.
 * Bukkit's {@code §<char>} colour codes are stripped before logging so
 * Windows consoles (cp1252) don't render the section sign as {@code ?}
 * — real Paper either ANSI-converts or strips depending on terminal,
 * but a stripped fallback is always readable and matches the behaviour
 * of paper-api's {@code ChatColor.stripColor}.
 */
public final class DefaultConsoleCommandSender implements ConsoleCommandSender {

    private static final Logger LOG =
            Logger.getLogger("RDForward/ConsoleCommandSender");

    /** Bukkit's colour-code prefix character. Public for tests. */
    public static final char COLOR_CHAR = '§';

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public void sendMessage(String message) {
        LOG.info(stripColor(message));
    }

    @Override
    public boolean isOp() {
        return true;
    }

    /**
     * Strip Bukkit-style colour codes ({@code §<any-char>}) from
     * {@code input}. Each {@code §} byte and the immediately following
     * character are removed; the Bedrock RGB form
     * ({@code §x§r§r§g§g§b§b}) is therefore stripped too because every
     * paired pattern matches the rule. {@code null} round-trips so
     * callers don't need a separate guard.
     */
    public static String stripColor(String input) {
        if (input == null || input.isEmpty()) return input;
        int len = input.length();
        StringBuilder out = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (c == COLOR_CHAR && i + 1 < len) {
                i++;
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }
}
