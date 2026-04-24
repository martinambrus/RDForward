// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import java.util.List;
import java.util.Map;

/**
 * Parsed view of a Bukkit {@code plugin.yml}. Only the fields the bridge
 * actually uses are modeled — full Bukkit supports many more keys
 * ({@code loadbefore}, {@code softdepend}, etc.) which can be added
 * incrementally.
 */
public record BukkitPluginDescriptor(
        String name,
        String version,
        String main,
        List<String> depend,
        Map<String, CommandSpec> commands
) {
    public BukkitPluginDescriptor(String name, String version, String main, List<String> depend) {
        this(name, version, main, depend, Map.of());
    }

    public String author() { return name; }

    /** One entry under {@code commands:} in {@code plugin.yml}. */
    public record CommandSpec(
            String name,
            String description,
            String usage,
            List<String> aliases,
            String permission
    ) {}
}
