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
 */
public final class DefaultConsoleCommandSender implements ConsoleCommandSender {

    private static final Logger LOG =
            Logger.getLogger("RDForward/ConsoleCommandSender");

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public void sendMessage(String message) {
        LOG.info(message);
    }

    @Override
    public boolean isOp() {
        return true;
    }
}
