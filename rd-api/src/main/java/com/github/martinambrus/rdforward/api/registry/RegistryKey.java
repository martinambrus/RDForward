package com.github.martinambrus.rdforward.api.registry;

import java.util.Objects;

/**
 * Namespaced identifier of the form {@code namespace:name}. The namespace
 * is conventionally the mod id, ensuring cross-mod uniqueness without a
 * central registration authority.
 *
 * <p>Both components must match {@code [a-z0-9_-]+}. Uppercase letters
 * and other characters are rejected to keep keys stable across config
 * serialization formats.
 *
 * @param namespace owning mod id (or {@code "minecraft"} for vanilla-derived entries)
 * @param name      identifier within the namespace
 */
public record RegistryKey(String namespace, String name) {

    public RegistryKey {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(name, "name");
        validate("namespace", namespace);
        validate("name", name);
    }

    /** Parse {@code "namespace:name"}. Throws {@link IllegalArgumentException} on malformed input. */
    public static RegistryKey parse(String key) {
        int colon = key.indexOf(':');
        if (colon <= 0 || colon >= key.length() - 1) {
            throw new IllegalArgumentException("expected 'namespace:name', got: " + key);
        }
        return new RegistryKey(key.substring(0, colon), key.substring(colon + 1));
    }

    @Override
    public String toString() {
        return namespace + ":" + name;
    }

    private static void validate(String field, String value) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be empty");
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-';
            if (!ok) {
                throw new IllegalArgumentException(field + " contains invalid character '" + c + "': " + value);
            }
        }
    }
}
