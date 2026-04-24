package com.github.martinambrus.rdforward.bridge.neoforge;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import com.github.martinambrus.rdforward.bridge.forge.ForgeEventBus;
import net.minecraftforge.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Loads a NeoForge mod jar. Deltas vs Forge:
 * <ul>
 *   <li>Parses {@code META-INF/neoforge.mods.toml} instead of {@code mods.toml}.</li>
 *   <li>Accepts either {@link net.minecraftforge.fml.common.Mod @Mod}
 *       (Forge) or {@link net.neoforged.fml.common.Mod @Mod} (NeoForge)
 *       on the entrypoint class.</li>
 *   <li>Falls back to the {@code mainClass} field in a {@code [[mods]]}
 *       entry when no annotation is found — NeoForge allows this.</li>
 *   <li>Constructor-injects the mod's {@link ForgeEventBus modBus},
 *       {@link ModContainer} holder, and {@link Dist} in declared order
 *       rather than using Forge's {@code FMLJavaModLoadingContext}
 *       ThreadLocal.</li>
 * </ul>
 */
public final class NeoForgeModLoader {

    private static final Logger LOG = Logger.getLogger("RDForward/NeoForgeBridge");

    private NeoForgeModLoader() {}

    public static LoadedNeoForgeMod load(Path jarPath, ClassLoader parent)
            throws IOException, ReflectiveOperationException {
        URL[] urls = { jarPath.toUri().toURL() };
        URLClassLoader classLoader = new URLClassLoader(urls, parent);
        NeoForgeModDescriptor nf;
        try (InputStream in = classLoader.getResourceAsStream("META-INF/neoforge.mods.toml")) {
            if (in == null) {
                classLoader.close();
                throw new IOException("META-INF/neoforge.mods.toml missing from " + jarPath);
            }
            nf = NeoForgeModsTomlParser.parse(in);
        }
        if (nf.primary() == null) {
            classLoader.close();
            throw new IOException("neoforge.mods.toml declared no [[mods]] entry: " + jarPath);
        }

        Map<String, Class<?>> byAnnotation = scanAnnotatedClasses(jarPath, classLoader);

        Map<String, ModHandle> handles = new LinkedHashMap<>();
        for (NeoForgeModDescriptor.Entry entry : nf.mods()) {
            Class<?> cls = byAnnotation.get(entry.modId());
            if (cls == null && entry.mainClass() != null && !entry.mainClass().isBlank()) {
                cls = Class.forName(entry.mainClass(), true, classLoader);
            }
            if (cls == null) {
                LOG.warning("[NeoForgeBridge] no @Mod class + no mainClass for modId='"
                        + entry.modId() + "' — skipped");
                continue;
            }
            ForgeEventBus modBus = new NeoForgeEventBus(entry.modId());
            ModContainer container = new ModContainer(entry.modId(), entry.version());
            Object instance = instantiate(cls, modBus, container);
            modBus.register(instance);
            handles.put(entry.modId(), new ModHandle(entry, cls, instance, modBus));
        }

        ModDescriptor descriptor = toModDescriptor(nf);
        NeoForgeModWrapper wrapper = new NeoForgeModWrapper(nf, handles, classLoader, jarPath);
        return new LoadedNeoForgeMod(descriptor, nf, jarPath, classLoader, handles, wrapper);
    }

    private static Map<String, Class<?>> scanAnnotatedClasses(Path jarPath, URLClassLoader classLoader) throws IOException {
        Map<String, Class<?>> out = new LinkedHashMap<>();
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
                Mod neoAnn = cls.getAnnotation(Mod.class);
                if (neoAnn != null) { out.put(neoAnn.value(), cls); continue; }
                net.minecraftforge.fml.common.Mod forgeAnn =
                        cls.getAnnotation(net.minecraftforge.fml.common.Mod.class);
                if (forgeAnn != null) out.put(forgeAnn.value(), cls);
            }
        }
        return out;
    }

    private static Object instantiate(Class<?> cls, ForgeEventBus modBus, ModContainer container)
            throws ReflectiveOperationException {
        Constructor<?>[] ctors = cls.getDeclaredConstructors();
        if (ctors.length == 0) {
            throw new ReflectiveOperationException(cls.getName() + " has no constructors");
        }
        Constructor<?> ctor = ctors[0];
        ctor.setAccessible(true);
        Class<?>[] paramTypes = ctor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = resolveArg(paramTypes[i], modBus, container);
        }
        return ctor.newInstance(args);
    }

    private static Object resolveArg(Class<?> type, ForgeEventBus modBus, ModContainer container) {
        if (net.minecraftforge.eventbus.api.IEventBus.class.isAssignableFrom(type)) return modBus;
        if (ModContainer.class.isAssignableFrom(type)) return container;
        if (Dist.class.isAssignableFrom(type)) return Dist.DEDICATED_SERVER;
        if (net.neoforged.api.distmarker.Dist.class.isAssignableFrom(type)) {
            return net.neoforged.api.distmarker.Dist.DEDICATED_SERVER;
        }
        LOG.warning("[NeoForgeBridge] unsupported constructor parameter type: " + type.getName()
                + " — passing null");
        return null;
    }

    private static ModDescriptor toModDescriptor(NeoForgeModDescriptor nf) {
        NeoForgeModDescriptor.Entry primary = nf.primary();
        Map<String, String> entrypoints = Map.of(ModDescriptor.ENTRYPOINT_SERVER, primary.modId());
        List<String> authors = primary.authors() == null || primary.authors().isBlank()
                ? List.of()
                : List.of(primary.authors());
        return new ModDescriptor(
                primary.modId(),
                primary.displayName(),
                primary.version(),
                primary.description() == null ? "" : primary.description(),
                authors,
                "*",
                entrypoints,
                new LinkedHashMap<>(nf.dependencies()),
                Map.of(),
                List.of(),
                false,
                null,
                null);
    }

    public record ModHandle(
            NeoForgeModDescriptor.Entry entry,
            Class<?> modClass,
            Object instance,
            ForgeEventBus modBus
    ) {}

    public record LoadedNeoForgeMod(
            ModDescriptor descriptor,
            NeoForgeModDescriptor neoForgeDescriptor,
            Path jarPath,
            URLClassLoader classLoader,
            Map<String, ModHandle> mods,
            NeoForgeModWrapper serverMod
    ) {
        public ModHandle primary() {
            return mods.isEmpty() ? null : mods.values().iterator().next();
        }
    }
}
