package org.bukkit.command;

import java.util.logging.Logger;

/**
 * Bukkit-shaped console sender. Writes messages to
 * {@link java.util.logging.Logger} at INFO level so console output is
 * routed through RDForward's normal logging pipeline.
 */
public class ConsoleCommandSender implements CommandSender {

    private static final Logger LOG = Logger.getLogger("RDForward/ConsoleCommandSender");

    @Override public String getName() { return "CONSOLE"; }
    @Override public void sendMessage(String message) { LOG.info(message); }
    @Override public boolean isOp() { return true; }
}
