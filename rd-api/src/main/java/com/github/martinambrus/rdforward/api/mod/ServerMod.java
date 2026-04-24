package com.github.martinambrus.rdforward.api.mod;

import com.github.martinambrus.rdforward.api.server.Server;

/**
 * Server-side entrypoint. The mod loader calls {@link #onEnable(Server)}
 * after the mod's classloader, config and permission registrations have
 * been set up, and {@link #onDisable()} on shutdown or hot-reload.
 *
 * <p>Event and command registrations made during {@code onEnable} are
 * tagged with the owning mod id and automatically removed on
 * {@code onDisable} — mods do not need to unregister manually.
 */
public interface ServerMod extends Mod {

    void onEnable(Server server);

    void onDisable();
}
