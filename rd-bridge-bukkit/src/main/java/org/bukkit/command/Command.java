// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.command;

import java.util.Collections;
import java.util.List;

/**
 * Bukkit-shaped command descriptor. RDForward's real command objects live
 * in {@code com.github.martinambrus.rdforward.api.command.Command}; this
 * stub exists so plugins that override {@code onCommand(CommandSender,
 * Command, String, String[])} compile. Only {@code name}, {@code
 * description}, {@code usage}, {@code aliases} and {@code permission}
 * round-trip — execution flow runs through the rd-api command registry.
 */
public class Command {

    private final String name;
    private String description = "";
    private String usage = "";
    private List<String> aliases = Collections.emptyList();
    private String permission;

    public Command(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public String getDescription() { return description; }
    public Command setDescription(String description) { this.description = description; return this; }

    public String getUsage() { return usage; }
    public Command setUsage(String usage) { this.usage = usage; return this; }

    public List<String> getAliases() { return aliases; }
    public Command setAliases(List<String> aliases) {
        this.aliases = aliases == null ? Collections.emptyList() : List.copyOf(aliases);
        return this;
    }

    public String getPermission() { return permission; }
    public Command setPermission(String permission) { this.permission = permission; return this; }

    /**
     * Noop stub — real command dispatch runs through rd-api
     * {@code CommandRegistry}. Subclasses can override for custom behavior;
     * the bridge only invokes this form when a plugin explicitly constructs
     * {@code Command} subclasses (rare).
     */
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return true;
    }
}
