package net.neoforged.neoforge.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * NeoForge's global constants. {@link #EVENT_BUS} is the same instance as
 * {@link MinecraftForge#EVENT_BUS} — listeners registered on either end
 * see each other. Installing {@code NeoForgeBridge} on top of
 * {@code ForgeBridge} therefore needs no extra plumbing for the shared bus.
 */
public final class NeoForge {

    private NeoForge() {}

    public static final IEventBus EVENT_BUS = MinecraftForge.EVENT_BUS;
}
