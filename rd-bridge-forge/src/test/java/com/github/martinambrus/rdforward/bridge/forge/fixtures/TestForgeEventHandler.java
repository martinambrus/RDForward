package com.github.martinambrus.rdforward.bridge.forge.fixtures;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Fixture class using Forge's class-level {@link Mod.EventBusSubscriber}
 * auto-registration path. Loader picks it up during the jar scan and
 * wires its static {@code @SubscribeEvent} methods to
 * {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 */
@Mod.EventBusSubscriber(modid = "testmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TestForgeEventHandler {

    public static final String PROP_STATIC_CHAT = "rdforward.test.forge.static_chat";

    @SubscribeEvent
    public static void onChat(ServerChatEvent e) {
        System.setProperty(PROP_STATIC_CHAT, e.getUsername() + ":" + e.getMessage());
    }
}
