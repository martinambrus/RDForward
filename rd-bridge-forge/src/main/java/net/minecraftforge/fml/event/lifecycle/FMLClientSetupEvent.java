// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.minecraftforge.fml.event.lifecycle;

import net.minecraftforge.eventbus.api.Event;

/**
 * Stub of Forge's client-only setup lifecycle event. Never fired from the
 * bridge (RDForward server is dedicated); present so mod code compiles.
 */
public class FMLClientSetupEvent extends Event {

    public void enqueueWork(Runnable work) { work.run(); }
}
