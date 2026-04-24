package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.api.command.Command;
import com.github.martinambrus.rdforward.api.command.CommandContext;
import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.command.TabCompleter;
import com.github.martinambrus.rdforward.modloader.admin.CommandConflictResolver;
import com.github.martinambrus.rdforward.server.api.CommandRegistry.RegisteredCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter from {@link com.github.martinambrus.rdforward.server.api.CommandRegistry}
 * (static, server-internal) to the mod-facing {@link CommandRegistry}. Adds
 * per-mod ownership tracking so {@link #unregisterByOwner(String)} removes
 * every command a mod installed.
 *
 * <p>Namespaced aliases ({@code <modId>:<name>}) are registered alongside
 * the bare name; the conflict-resolution layer introduced in Phase 5 will
 * decide which mod owns the bare alias when two mods collide.
 */
public final class RDCommandRegistry implements CommandRegistry {

    private final Map<String, List<String>> namesByOwner = new ConcurrentHashMap<>();

    @Override
    public void register(String modId, String name, String description, Command handler) {
        registerInternal(modId, name, description, 0, handler);
    }

    @Override
    public void registerOp(String modId, String name, String description, int opLevel, Command handler) {
        registerInternal(modId, name, description, opLevel, handler);
    }

    private void registerInternal(String modId, String name, String description, int opLevel, Command handler) {
        com.github.martinambrus.rdforward.server.api.Command adapter =
                serverCtx -> handler.execute(new CtxAdapter(serverCtx));

        // Bare alias goes through the conflict resolver so the server picks a winner
        // when multiple mods claim the same name. The namespaced alias is direct —
        // there's no way for it to collide.
        CommandConflictResolver.claim(modId, name, description, opLevel, adapter);
        String namespaced = modId + ":" + name;
        if (opLevel > 0) {
            com.github.martinambrus.rdforward.server.api.CommandRegistry.registerOp(
                    namespaced, description, opLevel, adapter);
        } else {
            com.github.martinambrus.rdforward.server.api.CommandRegistry.register(
                    namespaced, description, adapter);
        }

        namesByOwner.computeIfAbsent(modId, k -> new ArrayList<>()).add(name);
        namesByOwner.get(modId).add(namespaced);
    }

    @Override
    public void setTabCompleter(String modId, String name, TabCompleter completer) {
        // rd-server's static CommandRegistry does not expose tab completion yet —
        // the conflict-resolution phase will wire this alongside the bare/namespaced alias handling.
    }

    @Override
    public int unregisterByOwner(String modId) {
        List<String> names = namesByOwner.remove(modId);
        if (names == null) return 0;
        int removed = 0;
        // Release bare-alias claims so the resolver can pick a new winner (or clear
        // the alias). Namespaced aliases remain in the static registry only for this
        // mod — delete those directly.
        removed += CommandConflictResolver.unclaimAll(modId);
        for (String n : names) {
            if (n.startsWith(modId + ":")
                    && com.github.martinambrus.rdforward.server.api.CommandRegistry.unregister(n)) {
                removed++;
            }
        }
        return removed;
    }

    @Override
    public boolean exists(String name) {
        return com.github.martinambrus.rdforward.server.api.CommandRegistry.getCommands().containsKey(name);
    }

    @Override
    public List<String> listForOpLevel(int opLevel) {
        return com.github.martinambrus.rdforward.server.api.CommandRegistry.getCommandNamesForOpLevel(opLevel);
    }

    /** @return mod ids currently owning at least one command (stable copy). */
    public Collection<String> owners() {
        return new LinkedHashMap<>(namesByOwner).keySet();
    }

    private record CtxAdapter(com.github.martinambrus.rdforward.server.api.CommandContext delegate)
            implements CommandContext {
        @Override public String getSenderName() { return delegate.getSenderName(); }
        @Override public String[] getArgs() { return delegate.getArgs(); }
        @Override public boolean isConsole() { return delegate.isConsole(); }
        @Override public void reply(String message) { delegate.reply(message); }
    }
}
