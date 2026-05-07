// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.mojang.authlib;

import java.util.UUID;

/**
 * Mojang authlib stub. Real authlib's {@code GameProfile} carries
 * the player's UUID, name, and signed property bag (skin textures
 * etc.). RDForward has no authlib runtime, so this stub stores the
 * triple and exposes the same accessor surface plugins expect —
 * skin signature roundtrips and Yggdrasil session lookups are
 * out of scope and silently no-op via {@link com.github.martinambrus.rdforward.api.stub.StubCallLog}.
 */
public class GameProfile {
    private final UUID id;
    private final String name;
    private final com.mojang.authlib.properties.PropertyMap properties =
            new com.mojang.authlib.properties.PropertyMap();

    public GameProfile(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public com.mojang.authlib.properties.PropertyMap getProperties() { return properties; }
    public boolean isComplete() { return id != null && name != null && !name.isEmpty(); }
    public boolean isLegacy() { return false; }
}
