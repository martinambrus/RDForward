// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.minecraftforge.fml.event.lifecycle;

import net.minecraftforge.eventbus.api.Event;

/**
 * Stub of Forge's dedicated-server setup lifecycle event. Fired on the
 * mod's {@code modEventBus} on enable when running under RDForward's
 * dedicated server.
 */
public class FMLDedicatedServerSetupEvent extends Event {

    public void enqueueWork(Runnable work) { work.run(); }
}
