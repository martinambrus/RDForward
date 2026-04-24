package com.github.martinambrus.rdforward.bridge.neoforge;

import com.github.martinambrus.rdforward.api.mod.ServerMod;
import com.github.martinambrus.rdforward.api.server.Server;
import com.github.martinambrus.rdforward.bridge.forge.ForgeEventAdapter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ServerMod adapter for NeoForge mods. Mirrors
 * {@code com.github.martinambrus.rdforward.bridge.forge.ForgeModWrapper}
 * but fires NeoForge-package lifecycle events. NeoForge listeners on the
 * Forge parent type also match because the NeoForge events extend them.
 */
public final class NeoForgeModWrapper implements ServerMod {

    private static final Logger LOG = Logger.getLogger("RDForward/NeoForgeBridge");

    private final NeoForgeModDescriptor descriptor;
    private final Map<String, NeoForgeModLoader.ModHandle> mods;
    private final URLClassLoader classLoader;
    private final Path jarPath;

    public NeoForgeModWrapper(NeoForgeModDescriptor descriptor,
                              Map<String, NeoForgeModLoader.ModHandle> mods,
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
        for (Map.Entry<String, NeoForgeModLoader.ModHandle> e : mods.entrySet()) {
            modBuses.put(e.getKey(), e.getValue().modBus());
        }
        try {
            ForgeEventAdapter.registerEventBusSubscribers(
                    jarPath, classLoader, modBuses, Dist.DEDICATED_SERVER);
        } catch (IOException ioe) {
            LOG.warning("[NeoForgeBridge] @EventBusSubscriber scan failed: " + ioe);
        }

        for (NeoForgeModLoader.ModHandle h : mods.values()) {
            h.modBus().post(new net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent());
            h.modBus().post(new net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent());
        }
    }

    @Override
    public void onDisable() {
        for (NeoForgeModLoader.ModHandle h : mods.values()) h.modBus().clear();
    }

    public NeoForgeModDescriptor neoForgeDescriptor() { return descriptor; }
    public Map<String, NeoForgeModLoader.ModHandle> mods() { return mods; }
}
