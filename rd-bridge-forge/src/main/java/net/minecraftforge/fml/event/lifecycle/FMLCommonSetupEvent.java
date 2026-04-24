package net.minecraftforge.fml.event.lifecycle;

import net.minecraftforge.eventbus.api.Event;

/**
 * Stub of Forge's common (client+server shared) setup lifecycle event.
 * Fired once on the mod's {@code modEventBus} during enable.
 */
public class FMLCommonSetupEvent extends Event {

    public void enqueueWork(Runnable work) { work.run(); }
}
