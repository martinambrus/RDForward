package com.mojang.brigadier.exceptions;

/**
 * Stub for Brigadier's syntax exception. The bridge's command dispatcher
 * catches this and logs a warning; plugins porting real Brigadier code can
 * still {@code throw new CommandSyntaxException(...)} against our stub.
 */
public class CommandSyntaxException extends Exception {

    public CommandSyntaxException(String message) {
        super(message);
    }

    public CommandSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
