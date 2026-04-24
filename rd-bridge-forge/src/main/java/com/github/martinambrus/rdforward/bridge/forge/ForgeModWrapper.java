package com.github.martinambrus.rdforward.bridge.forge;

import com.github.martinambrus.rdforward.api.mod.ServerMod;
import com.github.martinambrus.rdforward.api.server.Server;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Adapts Forge's lifecycle to rd-api {@link ServerMod}. On enable:
 * <ol>
 *   <li>Scan the mod jar for {@link net.minecraftforge.fml.common.Mod.EventBusSubscriber}
 *       classes and wire each to the right bus (global or per-mod).</li>
 *   <li>For every declared {@code [[mods]]} entry, fire
 *       {@link FMLCommonSetupEvent} then {@link FMLDedicatedServerSetupEvent}
 *       on that mod's {@code modEventBus}.</li>
 * </ol>
 * <p>Disable is a no-op — {@code ForgeBridge.uninstall()} tears down the
 * global-bus forwarders, and rd-mod-loader's {@code ResourceSweeper}
 * handles any rd-api listeners that mods registered directly.
 */
public final class ForgeModWrapper implements ServerMod {

    private static final Logger LOG = Logger.getLogger("RDForward/ForgeBridge");

    private final ForgeModDescriptor descriptor;
    private final Map<String, ForgeModLoader.ModHandle> mods;
    private final URLClassLoader classLoader;
    private final Path jarPath;

    public ForgeModWrapper(ForgeModDescriptor descriptor,
                           Map<String, ForgeModLoader.ModHandle> mods,
                           URLClassLoader classLoader,
                           Path jarPath) {
        this.descriptor = descriptor;
        this.mods = mods;
        this.classLoader = classLoader;
        this.jarPath = jarPath;
    }

    @Override
    public void onEnable(Server server) {
        Map<String, IEventBus> modBuses = new LinkedHashMap<>();
        for (Map.Entry<String, ForgeModLoader.ModHandle> e : mods.entrySet()) {
            modBuses.put(e.getKey(), e.getValue().modBus());
        }
        try {
            ForgeEventAdapter.registerEventBusSubscribers(
                    jarPath, classLoader, modBuses, Dist.DEDICATED_SERVER);
        } catch (IOException ioe) {
            LOG.warning("[ForgeBridge] @EventBusSubscriber scan failed: " + ioe);
        }

        for (ForgeModLoader.ModHandle h : mods.values()) {
            h.modBus().post(new FMLCommonSetupEvent());
            h.modBus().post(new FMLDedicatedServerSetupEvent());
        }
    }

    @Override
    public void onDisable() {
        for (ForgeModLoader.ModHandle h : mods.values()) h.modBus().clear();
    }

    public ForgeModDescriptor forgeDescriptor() { return descriptor; }
    public Map<String, ForgeModLoader.ModHandle> mods() { return mods; }
}
