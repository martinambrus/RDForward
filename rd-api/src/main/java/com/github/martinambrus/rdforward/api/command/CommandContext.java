package com.github.martinambrus.rdforward.api.command;

/**
 * Context passed to a {@link Command} during dispatch.
 */
public interface CommandContext {

    /** Name of the sender (player name or "console"). */
    String getSenderName();

    String[] getArgs();

    boolean isConsole();

    /** Send a reply to the sender. */
    void reply(String message);
}
