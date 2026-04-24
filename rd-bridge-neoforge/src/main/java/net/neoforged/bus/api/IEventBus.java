package net.neoforged.bus.api;

/**
 * NeoForge's {@code IEventBus} — identical interface to Forge's. The bridge
 * exposes one concrete implementation ({@code ForgeEventBus}) under both
 * names by having the NeoForge type extend Forge's.
 */
public interface IEventBus extends net.minecraftforge.eventbus.api.IEventBus {}
