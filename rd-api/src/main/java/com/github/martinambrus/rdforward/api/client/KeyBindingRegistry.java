package com.github.martinambrus.rdforward.api.client;

/**
 * Registers {@link KeyBinding}s with the client input loop. Registrations
 * are tagged with the owning mod id and auto-removed when the mod unloads.
 */
public interface KeyBindingRegistry {

    void register(String modId, KeyBinding binding);
}
