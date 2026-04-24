package com.github.martinambrus.rdforward.bridge.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Hooks that don't fit on {@link ForgeEventBus} directly: the
 * {@link Mod.EventBusSubscriber} class-path scan. At enable time the bridge
 * asks the adapter to walk every class in the mod jar annotated with
 * {@code @Mod.EventBusSubscriber}, and registers each class's static
 * {@code @SubscribeEvent} methods on the target bus — either
 * {@link MinecraftForge#EVENT_BUS} (FORGE) or the mod's modBus (MOD).
 *
 * <p>Per-instance {@code @SubscribeEvent} registration is handled by
 * {@code IEventBus.register(Object)} directly (the wrapped
 * {@link ForgeEventBus} walks the instance's methods when the mod
 * constructor calls {@code MinecraftForge.EVENT_BUS.register(this)} or
 * similar).
 */
public final class ForgeEventAdapter {

    private static final Logger LOG = Logger.getLogger("RDForward/ForgeBridge");

    private ForgeEventAdapter() {}

    /**
     * Scan the jar for classes annotated with {@code @Mod.EventBusSubscriber},
     * and for each such class register it as a static subscriber on the
     * configured bus. {@code modBuses} maps mod-id -&gt; modBus; classes
     * with {@code bus=MOD} target that mod's bus, {@code bus=FORGE} targets
     * the global {@link MinecraftForge#EVENT_BUS}.
     */
    public static void registerEventBusSubscribers(Path jarPath,
                                                   URLClassLoader classLoader,
                                                   Map<String, IEventBus> modBuses,
                                                   Dist currentDist) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                String name = e.getName();
                if (!name.endsWith(".class") || name.startsWith("META-INF/")) continue;
                String fqcn = name.substring(0, name.length() - ".class".length()).replace('/', '.');
                Class<?> cls;
                try {
                    cls = Class.forName(fqcn, false, classLoader);
                } catch (Throwable t) {
                    continue;
                }
                Mod.EventBusSubscriber ann = cls.getAnnotation(Mod.EventBusSubscriber.class);
                if (ann == null) continue;
                if (!distAllows(ann.value(), currentDist)) continue;
                IEventBus target = resolveBus(ann, modBuses);
                if (target == null) {
                    LOG.warning("[ForgeBridge] @EventBusSubscriber on " + fqcn
                            + " references unknown modid='" + ann.modid() + "' — skipped");
                    continue;
                }
                target.register(cls);
            }
        }
    }

    private static boolean distAllows(Dist[] values, Dist current) {
        if (values == null || values.length == 0) return true;
        for (Dist d : values) if (d == current) return true;
        return false;
    }

    private static IEventBus resolveBus(Mod.EventBusSubscriber ann,
                                        Map<String, IEventBus> modBuses) {
        return switch (ann.bus()) {
            case FORGE -> MinecraftForge.EVENT_BUS;
            case MOD -> modBuses.get(ann.modid());
        };
    }
}
