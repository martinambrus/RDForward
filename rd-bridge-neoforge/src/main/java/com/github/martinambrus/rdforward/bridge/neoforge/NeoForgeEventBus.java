package com.github.martinambrus.rdforward.bridge.neoforge;

import com.github.martinambrus.rdforward.bridge.forge.ForgeEventBus;

/**
 * NeoForge-package {@link net.neoforged.bus.api.IEventBus} impl. Extends
 * {@link ForgeEventBus} to reuse dispatch logic and also declares the
 * NeoForge subinterface so reflective constructor injection satisfies
 * mods whose entrypoint parameter is typed as
 * {@code net.neoforged.bus.api.IEventBus}.
 */
public final class NeoForgeEventBus extends ForgeEventBus
        implements net.neoforged.bus.api.IEventBus {

    public NeoForgeEventBus(String name) {
        super(name);
    }
}
