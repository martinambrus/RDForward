package com.github.martinambrus.rdforward.bridge.paper;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Commands} implementation that buffers registrations made from a
 * COMMANDS lifecycle handler. The bridge later walks the entries and
 * registers each with the rd-api {@code CommandRegistry} via
 * {@link BrigadierCommandBridge}.
 */
final class CollectingCommandsRegistrar implements Commands {

    record Entry(LiteralCommandNode<CommandSourceStack> node, String description, List<String> aliases) {}

    private final List<Entry> entries = new ArrayList<>();

    List<Entry> entries() { return entries; }

    @Override public void register(LiteralCommandNode<CommandSourceStack> node) {
        entries.add(new Entry(node, "", List.of()));
    }

    @Override public void register(LiteralCommandNode<CommandSourceStack> node, String description) {
        entries.add(new Entry(node, description == null ? "" : description, List.of()));
    }

    @Override public void register(LiteralCommandNode<CommandSourceStack> node, String description, List<String> aliases) {
        entries.add(new Entry(node, description == null ? "" : description, aliases == null ? List.of() : List.copyOf(aliases)));
    }
}
