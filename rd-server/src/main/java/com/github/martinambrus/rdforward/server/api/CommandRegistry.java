package com.github.martinambrus.rdforward.server.api;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for server commands. Mods register commands here; the server
 * dispatches chat messages starting with "/" and console input through this.
 *
 * Thread-safe: commands can be registered from any thread (e.g., mod init).
 */
public final class CommandRegistry {

    private CommandRegistry() {}

    private static final Map<String, RegisteredCommand> commands = new ConcurrentHashMap<>();

    /**
     * Register a command.
     *
     * @param name        command name (without "/"), case-insensitive
     * @param description short help text
     * @param command     the command handler
     */
    public static void register(String name, String description, Command command) {
        commands.put(name.toLowerCase(), new RegisteredCommand(name.toLowerCase(), description, command));
    }

    /**
     * Register a command that requires operator permissions.
     */
    public static void registerOp(String name, String description, Command command) {
        commands.put(name.toLowerCase(), new RegisteredCommand(name.toLowerCase(), description, command, true));
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
        String[] parts = input.split("\\s+", 2);
        String cmdName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];

        RegisteredCommand registered = commands.get(cmdName);
        if (registered == null) {
            return false;
        }

        // Permission check
        if (registered.requiresOp && !isConsole && !PermissionManager.isOp(sender)) {
            replySender.sendMessage("You don't have permission to use this command.");
            return true;
        }

        CommandContext ctx = new CommandContext(sender, args, isConsole, replySender);
        try {
            registered.command.execute(ctx);
        } catch (Exception e) {
            replySender.sendMessage("Command error: " + e.getMessage());
            System.err.println("Error executing command /" + cmdName + ": " + e.getMessage());
        }
        return true;
    }

    /**
     * Get all registered commands (unmodifiable).
     */
    public static Map<String, RegisteredCommand> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

    public static class RegisteredCommand {
        public final String name;
        public final String description;
        public final Command command;
        public final boolean requiresOp;

        RegisteredCommand(String name, String description, Command command) {
            this(name, description, command, false);
        }

        RegisteredCommand(String name, String description, Command command, boolean requiresOp) {
            this.name = name;
            this.description = description;
            this.command = command;
            this.requiresOp = requiresOp;
        }
    }
}
