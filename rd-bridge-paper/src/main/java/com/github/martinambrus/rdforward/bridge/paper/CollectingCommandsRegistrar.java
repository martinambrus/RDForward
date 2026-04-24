package com.github.martinambrus.rdforward.bridge.paper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.configuration.PluginMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * {@link Commands} implementation that buffers registrations made from a
 * COMMANDS lifecycle handler. The bridge later walks the entries and
 * registers each with the rd-api {@code CommandRegistry} via
 * {@link BrigadierCommandBridge}.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class CollectingCommandsRegistrar implements Commands {

    record Entry(LiteralCommandNode node, String description, List<String> aliases) {}

    private final List<Entry> entries = new ArrayList<>();

    List<Entry> entries() { return entries; }

    @Override public CommandDispatcher getDispatcher() { return null; }

    @Override public Set register(LiteralCommandNode node, String description, Collection aliases) {
        entries.add(new Entry(node, description == null ? "" : description,
                aliases == null ? List.of() : List.copyOf(aliases)));
        return Collections.emptySet();
    }

    @Override public Set register(PluginMeta meta, LiteralCommandNode node, String description, Collection aliases) {
        return register(node, description, aliases);
    }

    @Override public Set registerWithFlags(PluginMeta meta, LiteralCommandNode node, String description, Collection aliases, Set flags) {
        return register(node, description, aliases);
    }

    @Override public Set register(String label, String description, Collection aliases, io.papermc.paper.command.brigadier.BasicCommand command) {
        return Collections.emptySet();
    }

    @Override public Set register(PluginMeta meta, String label, String description, Collection aliases, io.papermc.paper.command.brigadier.BasicCommand command) {
        return Collections.emptySet();
    }
}
