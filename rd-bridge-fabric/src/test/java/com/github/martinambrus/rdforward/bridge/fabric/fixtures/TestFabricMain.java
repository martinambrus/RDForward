package com.github.martinambrus.rdforward.bridge.fabric.fixtures;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.api.registry.RegistryKey;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

public class TestFabricMain implements ModInitializer {

    public static final String PROP_MAIN = "rdforward.test.fabric.main";
    public static final String PROP_JOIN = "rdforward.test.fabric.join";
    public static final String PROP_STARTED = "rdforward.test.fabric.started";
    public static final String PROP_STOPPING = "rdforward.test.fabric.stopping";
    public static final String PROP_TICK_END = "rdforward.test.fabric.server.tick.end";
    public static final String PROP_COMMANDS = "rdforward.test.fabric.commands";
    public static final String PROP_NET_IN = "rdforward.test.fabric.server.net.in";
    public static final String PROP_LOADER_OK = "rdforward.test.fabric.loader.ok";
    public static final String PROP_PAYLOAD_OK = "rdforward.test.fabric.payload.ok";

    public static final String CHANNEL_NAMESPACE = "rdforward";
    public static final String CHANNEL_NAME = "server-echo";

    @Override
    public void onInitialize() {
        System.setProperty(PROP_MAIN, "true");
        ServerEvents.PLAYER_JOIN.register((name, version) ->
                System.setProperty(PROP_JOIN, name + ":" + version));

        ServerLifecycleEvents.SERVER_STARTED.register(server ->
                System.setProperty(PROP_STARTED, "true"));
        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
                System.setProperty(PROP_STOPPING, "true"));
        ServerTickEvents.END_SERVER_TICK.register(server ->
                System.setProperty(PROP_TICK_END, "true"));

        CommandRegistrationCallback.EVENT.register(registry ->
                System.setProperty(PROP_COMMANDS, registry == null ? "null" : "ready"));

        ServerPlayNetworking.registerGlobalReceiver(
                new RegistryKey(CHANNEL_NAMESPACE, CHANNEL_NAME),
                (player, payload) -> System.setProperty(PROP_NET_IN, new String(payload)));

        FabricLoader loader = FabricLoader.getInstance();
        System.setProperty(PROP_LOADER_OK, loader == FabricLoader.getInstance() ? "true" : "false");

        try {
            PayloadTypeRegistry.<byte[]>playS2C().register(
                    new RegistryKey(CHANNEL_NAMESPACE, CHANNEL_NAME), byte[].class);
            PayloadTypeRegistry.<byte[]>playC2S().register(
                    new RegistryKey(CHANNEL_NAMESPACE, CHANNEL_NAME), byte[].class, null);
            System.setProperty(PROP_PAYLOAD_OK, "true");
        } catch (RuntimeException e) {
            System.setProperty(PROP_PAYLOAD_OK, "fail:" + e);
        }
    }
}
