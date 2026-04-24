package com.github.martinambrus.rdforward.api.network;

import com.github.martinambrus.rdforward.api.registry.RegistryKey;

/**
 * Immutable envelope for a custom plugin-channel payload. Opaque at this
 * layer — the server and client bridges dispatch on {@link #channel()}
 * and hand the raw bytes to the registered handler.
 *
 * @param channel channel identifier this message arrived on
 * @param data    raw payload bytes (never {@code null}; may be empty)
 */
public record PluginMessage(RegistryKey channel, byte[] data) {

    public PluginMessage {
        if (channel == null) throw new IllegalArgumentException("channel is required");
        if (data == null) throw new IllegalArgumentException("data is required (use new byte[0] for empty)");
    }
}
