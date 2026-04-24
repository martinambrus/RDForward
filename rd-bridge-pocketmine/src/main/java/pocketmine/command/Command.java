package pocketmine.command;

import java.util.List;

/**
 * Declarative command metadata. Populated from a plugin.yml
 * {@code commands:} entry and handed to the {@link CommandExecutor}
 * (by default, the plugin itself) on dispatch.
 */
public class Command {

    private final String name;
    private String description;
    private String usage;
    private List<String> aliases;
    private String permission;

    public Command(String name) { this.name = name; }

    public String getName() { return name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUsage() { return usage; }
    public void setUsage(String usage) { this.usage = usage; }

    public List<String> getAliases() { return aliases == null ? List.of() : aliases; }
    public void setAliases(List<String> aliases) { this.aliases = aliases == null ? null : List.copyOf(aliases); }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
}
