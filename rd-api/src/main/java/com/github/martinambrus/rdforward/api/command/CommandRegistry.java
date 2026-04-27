package com.github.martinambrus.rdforward.api.command;

import java.util.List;

/**
 * Mod-facing command registry.
 *
 * <p>Mods register commands scoped to their mod id. Every command is exposed
 * under a namespaced form {@code <modId>:<name>} and, unless shadowed by a
 * conflicting registration, also under its bare name.
 */
public interface CommandRegistry {

    /** Register a public command (no op level required). */
    void register(String modId, String name, String description, Command handler);

    /** Register a command that requires the given op level. */
    void registerOp(String modId, String name, String description, int opLevel, Command handler);

    /** Optionally attach a tab completer for a registered command. */
    void setTabCompleter(String modId, String name, TabCompleter completer);

    /** Remove every command registered by the given mod. */
    int unregisterByOwner(String modId);

    /** True if a bare command name resolves to a handler. */
    boolean exists(String name);

    /** @return the mod id currently owning the bare-alias handler for
     *  {@code name}, or {@code null} if no command is registered under
     *  that name. Built-in server commands appear under the
     *  {@code "__server__"} pseudo mod id. Foreign bridges (e.g. the
     *  Bukkit bridge) use this to surface useful conflict warnings
     *  ("/kick already claimed by 'Essentials'") instead of generic
     *  "claimed by another plugin" text. Default returns {@code null}
     *  for back-compat with mock registries that don't track ownership. */
    default String ownerOf(String name) { return null; }

    /** List of command names visible to the given op level. */
    List<String> listForOpLevel(int opLevel);
}
