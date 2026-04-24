package com.github.martinambrus.rdforward.bridge.neoforge.fixtures;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Fixture NeoForge mod. Mirrors the real NeoForge entrypoint shape: a
 * class annotated with {@code @Mod("testmod")} whose constructor accepts
 * {@link IEventBus} + {@link ModContainer} and is invoked by the loader
 * with both supplied reflectively. The mod registers itself on both the
 * injected modBus (for lifecycle events) and the global
 * {@link MinecraftForge#EVENT_BUS} (for gameplay events).
 */
@Mod("testmod")
public class TestNeoForgeMod {

    public static final String PROP_CTOR = "rdforward.test.neoforge.ctor";
    public static final String PROP_BUS_CLASS = "rdforward.test.neoforge.bus_class";
    public static final String PROP_MOD_ID = "rdforward.test.neoforge.mod_id";
    public static final String PROP_COMMON_SETUP = "rdforward.test.neoforge.common_setup";
    public static final String PROP_BREAK = "rdforward.test.neoforge.break";

    public TestNeoForgeMod(IEventBus modBus, ModContainer container) {
        System.setProperty(PROP_CTOR, "true");
        if (modBus != null) System.setProperty(PROP_BUS_CLASS, modBus.getClass().getName());
        if (container != null) System.setProperty(PROP_MOD_ID, container.getModId());
        modBus.register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent e) {
        System.setProperty(PROP_COMMON_SETUP, "true");
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent e) {
        System.setProperty(PROP_BREAK, e.getPlayerName() + "@" + e.getX() + "," + e.getY() + "," + e.getZ());
    }
}
