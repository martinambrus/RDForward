package com.github.martinambrus.rdforward.bridge.forge.fixtures;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Fixture Forge mod. Mirrors what a real mod ships: a class annotated
 * {@code @Mod("testmod")} with a no-arg constructor that grabs its
 * {@code modEventBus} from {@link FMLJavaModLoadingContext} and registers
 * itself on both the mod bus (for FML lifecycle events) and the global
 * {@link MinecraftForge#EVENT_BUS} (for gameplay events).
 */
@Mod("testmod")
public class TestForgeMod {

    public static final String PROP_CTOR = "rdforward.test.forge.ctor";
    public static final String PROP_COMMON_SETUP = "rdforward.test.forge.common_setup";
    public static final String PROP_BREAK = "rdforward.test.forge.break";
    public static final String PROP_TICK = "rdforward.test.forge.tick";

    public TestForgeMod() {
        System.setProperty(PROP_CTOR, "true");
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
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
        if ("veto".equals(e.getPlayerName())) e.setCanceled(true);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent e) {
        String prior = System.getProperty(PROP_TICK, "0");
        int count = Integer.parseInt(prior) + 1;
        System.setProperty(PROP_TICK, Integer.toString(count));
    }
}
