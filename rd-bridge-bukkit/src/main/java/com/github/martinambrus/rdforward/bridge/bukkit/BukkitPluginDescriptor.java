// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import java.util.List;
import java.util.Map;

/**
 * Parsed view of a Bukkit {@code plugin.yml}. Only the fields the bridge
 * actually uses are modeled — full Bukkit supports many more keys
 * ({@code loadbefore}, etc.) which can be added incrementally.
 *
 * <p>Bukkit semantics: {@code depend} is a hard requirement (the plugin
 * fails to load if any listed plugin is missing); {@code softdepend} is
 * an optional load-order hint. The bridge surfaces the two lists
 * separately so {@code DependencyResolver} can refuse to load a plugin
 * whose hard deps are absent (clean error message) rather than letting
 * it boot half-initialised and NPE later — the EssentialsDiscordLink
 * case where a missing EssentialsDiscord left {@code this.api} null and
 * the chat pipeline crashed on first message.
 */
public record BukkitPluginDescriptor(
        String name,
        String version,
        String main,
        List<String> depend,
        List<String> softdepend,
        Map<String, CommandSpec> commands
) {
    public BukkitPluginDescriptor(String name, String version, String main, List<String> depend) {
        this(name, version, main, depend, List.of(), Map.of());
    }

    public BukkitPluginDescriptor(String name, String version, String main,
                                  List<String> depend, List<String> softdepend) {
        this(name, version, main, depend, softdepend, Map.of());
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
