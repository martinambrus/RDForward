// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.minecraftforge.common;

import com.github.martinambrus.rdforward.bridge.forge.ForgeEventBus;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Stub of Forge's {@code MinecraftForge} constants. Exposes the global
 * {@code EVENT_BUS} — in real Forge this is the dispatch target for gameplay
 * events ({@code @SubscribeEvent} handlers posted via {@link IEventBus#post}).
 * The bridge installs forwarders from rd-api {@code ServerEvents} into this
 * bus so Forge listeners see them.
 */
public final class MinecraftForge {

    private MinecraftForge() {}

    public static final IEventBus EVENT_BUS = new ForgeEventBus("forge");
}
