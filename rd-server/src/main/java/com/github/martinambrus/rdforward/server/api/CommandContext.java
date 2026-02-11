package com.github.martinambrus.rdforward.server.api;

/**
 * Context for a command execution, providing access to the sender,
 * arguments, and server utilities.
 */
public class CommandContext {

    private final String senderName;
    private final String[] args;
    private final boolean isConsole;
    private final CommandSender sender;

    public CommandContext(String senderName, String[] args, boolean isConsole, CommandSender sender) {
        this.senderName = senderName;
        this.args = args;
        this.isConsole = isConsole;
        this.sender = sender;
    }

    /** The name of the player or "CONSOLE" for server console. */
    public String getSenderName() {
        return senderName;
    }

    /** Arguments passed after the command name. */
    public String[] getArgs() {
        return args;
    }

    /** True if the command was run from the server console. */
    public boolean isConsole() {
        return isConsole;
    }

    /** Send a response message to the command sender. */
    public void reply(String message) {
        sender.sendMessage(message);
    }

    /**
     * Interface for sending messages back to the command sender.
     * Implemented differently for player senders (chat packet) vs console (stdout).
     */
    @FunctionalInterface
    public interface CommandSender {
        void sendMessage(String message);
    }
}
