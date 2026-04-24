package com.github.martinambrus.rdforward.bridge.paper;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginDescriptor.CommandSpec;

import java.util.List;
import java.util.Map;

/**
 * Parsed view of a {@code paper-plugin.yml}. Only the fields the bridge
 * uses are modeled: identification, the Paper-specific {@code bootstrapper}
 * and {@code loader} entries, bootstrap/server dependency lists, and the
 * optional {@code commands:} block (identical shape to {@code plugin.yml}).
 */
public record PaperPluginDescriptor(
        String name,
        String version,
        String main,
        String bootstrapper,
        String loader,
        String description,
        List<String> authors,
        String apiVersion,
        List<String> bootstrapDeps,
        List<String> serverDeps,
        Map<String, CommandSpec> commands
) {
}
