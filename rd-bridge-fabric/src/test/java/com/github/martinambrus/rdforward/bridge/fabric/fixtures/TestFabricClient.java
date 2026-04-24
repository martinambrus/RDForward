package com.github.martinambrus.rdforward.bridge.fabric.fixtures;

import com.github.martinambrus.rdforward.api.registry.RegistryKey;
import com.github.martinambrus.rdforward.client.api.KeyBinding;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.tooltip.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

/**
 * Fixture Fabric client mod used by
 * {@code FabricClientBridgeIntegrationTest}. Registers one listener per
 * shim so the test can assert that each dispatch path actually fires
 * the registered callback.
 *
 * <p>State leaks back to tests via system properties because the fixture
 * is loaded inside an isolated URLClassLoader — static fields on the
 * fixture class would be invisible from the test classloader.
 */
public class TestFabricClient implements ClientModInitializer {

    public static final String PROP_CLIENT = "rdforward.test.fabric.client";
    public static final String PROP_HUD = "rdforward.test.fabric.hud";
    public static final String PROP_TICK_END = "rdforward.test.fabric.tick.end";
    public static final String PROP_CLIENT_STARTED = "rdforward.test.fabric.client.started";
    public static final String PROP_KEYBINDING = "rdforward.test.fabric.keybinding";
    public static final String PROP_NETWORKING = "rdforward.test.fabric.networking";
    public static final String PROP_WORLD_LAST = "rdforward.test.fabric.world.last";
    public static final String PROP_WORLD_END = "rdforward.test.fabric.world.end";
    public static final String PROP_SCREEN_OPEN = "rdforward.test.fabric.screen.open";
    public static final String PROP_SCREEN_CLOSE = "rdforward.test.fabric.screen.close";
    public static final String PROP_NOOP_STUBS_OK = "rdforward.test.fabric.noop.stubs.ok";

    public static final String KEY_BINDING_NAME = "Fly Toggle";
    public static final int KEY_BINDING_CODE = 71;
    public static final String CHANNEL_NAMESPACE = "rdforward";
    public static final String CHANNEL_NAME = "test-echo";

    @Override
    public void onInitializeClient() {
        System.setProperty(PROP_CLIENT, "true");

        HudRenderCallback.EVENT.register((ctx, tickDelta) ->
                System.setProperty(PROP_HUD, ctx.getScreenWidth() + "x" + ctx.getScreenHeight()));

        ClientTickEvents.END_CLIENT_TICK.register(() ->
                System.setProperty(PROP_TICK_END, "true"));

        ClientLifecycleEvents.CLIENT_STARTED.register(() ->
                System.setProperty(PROP_CLIENT_STARTED, "true"));

        KeyBinding flyKey = new KeyBinding(KEY_BINDING_NAME, KEY_BINDING_CODE, () -> {});
        KeyBindingHelper.registerKeyBinding(flyKey);
        System.setProperty(PROP_KEYBINDING, flyKey.getName() + ":" + flyKey.getKeyCode());

        ClientPlayNetworking.registerGlobalReceiver(
                new RegistryKey(CHANNEL_NAMESPACE, CHANNEL_NAME),
                payload -> System.setProperty(PROP_NETWORKING, new String(payload)));

        WorldRenderEvents.LAST.register(ctx ->
                System.setProperty(PROP_WORLD_LAST, String.valueOf(ctx.tickDelta())));
        WorldRenderEvents.END.register(ctx ->
                System.setProperty(PROP_WORLD_END, String.valueOf(ctx.tickDelta())));

        ScreenEvents.OPEN.register(screen ->
                System.setProperty(PROP_SCREEN_OPEN, screen == null ? "null" : "open"));
        ScreenEvents.CLOSE.register(screen ->
                System.setProperty(PROP_SCREEN_CLOSE, screen == null ? "null" : "close"));

        try {
            ParticleFactoryRegistry.getInstance().register(new Object(), new Object());
            ParticleFactoryRegistry.getInstance().register(new Object(), new Object(), new Object());
            EntityRendererRegistry.register(new Object(), new Object());
            ArmorRenderer renderer = (matrices, vertexConsumers, stack, entity, slot, light, model) -> {};
            ArmorRenderer.register(renderer, new Object(), new Object());
            ModelLoadingPlugin.REGISTER.register(plugin -> {});
            TooltipComponentCallback.EVENT.register(data -> null);
            WorldRenderEvents.START.register(ctx -> {});
            WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {});
            ScreenEvents.BEFORE_INIT.register(screen -> {});
            ScreenEvents.AFTER_INIT.register(screen -> {});
            ScreenEvents.REMOVE.register(screen -> {});
            System.setProperty(PROP_NOOP_STUBS_OK, "true");
        } catch (RuntimeException e) {
            System.setProperty(PROP_NOOP_STUBS_OK, "fail:" + e);
        }
    }
}
