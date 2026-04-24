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

    /** List of command names visible to the given op level. */
    List<String> listForOpLevel(int opLevel);
}
