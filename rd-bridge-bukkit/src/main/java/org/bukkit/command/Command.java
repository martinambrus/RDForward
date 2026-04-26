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
    private String label;
    private String description = "";
    private String usage = "";
    private List<String> aliases = Collections.emptyList();
    private String permission;

    public Command(String name) {
        this.name = name;
        this.label = name;
    }

    /** Upstream-shaped 4-arg constructor used by older plugin code
     *  (notably WorldEdit's {@code DynamicPluginCommand}) that builds
     *  ad-hoc Command instances directly. Keeps the same wiring as the
     *  fluent setters so the resulting command is fully populated by the
     *  time it leaves the constructor. */
    public Command(String name, String description, String usage, List<String> aliases) {
        this.name = name;
        this.label = name;
        this.description = description == null ? "" : description;
        this.usage = usage == null ? "" : usage;
        this.aliases = aliases == null ? Collections.emptyList() : List.copyOf(aliases);
    }

    public String getName() { return name; }

    /** @return the alias the command was last registered/dispatched under.
     *  Defaults to {@link #getName()} until {@link #setLabel(String)} swaps
     *  it. LuckPerms's command system reads this during {@code onEnable}
     *  to record its own active label, so a non-null return is required
     *  even before any real dispatch has happened. */
    public String getLabel() { return label; }

    /** @return {@code true} — this stub always accepts the new label. The
     *  return value mirrors paper-api, which also returns {@code true}
     *  unless the command has been registered under a previous label and
     *  cannot be relabelled. RDForward never tracks registration state, so
     *  the relabel always succeeds. */
    public boolean setLabel(String label) {
        this.label = label == null ? name : label;
        return true;
    }

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

    /** Real Bukkit's {@code setPermission} returns {@code void}; older
     *  plugin builds (notably WorldEdit 5.6.1) bind to that exact
     *  signature, so the void return type is required for runtime
     *  resolution. The setter is fluent in spirit only. */
    public void setPermission(String permission) { this.permission = permission; }

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
