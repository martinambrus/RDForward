package com.github.martinambrus.rdforward.server.api;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Registry for server commands. Mods register commands here; the server
 * dispatches chat messages starting with "/" and console input through this.
 *
 * Thread-safe: commands can be registered from any thread (e.g., mod init).
 */
public final class CommandRegistry {

    private CommandRegistry() {}

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Map<String, RegisteredCommand> commands = new ConcurrentHashMap<>();

    /**
     * Register a command available to all players (no op required).
     * The config file can override this to require an op level.
     *
     * @param name        command name (without "/"), case-insensitive
     * @param description short help text
     * @param command     the command handler
     */
    public static void register(String name, String description, Command command) {
        String key = name.toLowerCase();
        int configLevel = PermissionManager.getCommandOpLevel(key, 0);
        commands.put(key, new RegisteredCommand(key, description, command, configLevel));
    }

    /**
     * Register a command that requires a specific op level.
     * The actual level used is the config file override if present,
     * otherwise the provided default.
     *
     * @param name        command name (without "/"), case-insensitive
     * @param description short help text
     * @param opLevel     default minimum op level (1-{@link PermissionManager#MAX_OP_LEVEL})
     * @param command     the command handler
     * @throws IllegalArgumentException if the code-provided opLevel is out of range
     */
    public static void registerOp(String name, String description, int opLevel, Command command) {
        if (opLevel < 1 || opLevel > PermissionManager.MAX_OP_LEVEL) {
            throw new IllegalArgumentException("opLevel must be between 1 and "
                    + PermissionManager.MAX_OP_LEVEL + ", got " + opLevel);
        }
        String key = name.toLowerCase();
        int resolvedLevel = PermissionManager.getCommandOpLevel(key, opLevel);
        commands.put(key, new RegisteredCommand(key, description, command, resolvedLevel));
    }

    /**
     * Register a command that requires operator permissions (defaults to OP_MANAGE).
     * Kept for backwards compatibility.
     */
    public static void registerOp(String name, String description, Command command) {
        registerOp(name, description, PermissionManager.OP_MANAGE, command);
    }

    /**
     * Try to dispatch a command string. Returns true if a command was found and executed.
     *
     * @param input  the full command string (e.g., "tp Player1 0 64 0")
     * @param sender name of the sender
     * @param isConsole true if from server console
     * @param replySender callback for sending response messages
     * @return true if a command was matched, false if no command found
     */
    public static boolean dispatch(String input, String sender, boolean isConsole,
                                   CommandContext.CommandSender replySender) {
        String[] parts = WHITESPACE.split(input, 2);
        String cmdName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? WHITESPACE.split(parts[1]) : new String[0];

        RegisteredCommand registered = commands.get(cmdName);
        if (registered == null) {
            return false;
        }

        // Permission check: console always passes; players need sufficient op level
        if (registered.requiredOpLevel > 0 && !isConsole) {
            int senderLevel = PermissionManager.getOpLevel(sender);
            if (senderLevel < registered.requiredOpLevel) {
                System.out.println("[WARN] " + sender + " denied access to /"
                        + cmdName + " (has level " + senderLevel
                        + ", needs " + registered.requiredOpLevel + ")");
                replySender.sendMessage("You don't have permission to use this command.");
                return true;
            }
        }

        CommandContext ctx = new CommandContext(sender, args, isConsole, replySender);
        try {
            registered.command.execute(ctx);
        } catch (Exception e) {
            replySender.sendMessage("An internal error occurred.");
            System.err.println("Error executing command /" + cmdName
                    + " (sender: " + sender + "): " + e.getMessage());
            e.printStackTrace(System.err);
        }
        return true;
    }

    /**
     * Get all registered commands (unmodifiable).
     */
    public static Map<String, RegisteredCommand> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

    /** Clear all registered commands. Package-private, for testing only. */
    static void clearForTesting() {
        commands.clear();
    }

    public static class RegisteredCommand {
        public final String name;
        public final String description;
        public final Command command;
        /** Minimum op level required (0 = no op needed, 1-4 = op level). */
        public final int requiredOpLevel;

        RegisteredCommand(String name, String description, Command command, int requiredOpLevel) {
            this.name = name;
            this.description = description;
            this.command = command;
            this.requiredOpLevel = requiredOpLevel;
        }
    }
}
