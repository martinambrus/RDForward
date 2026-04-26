package com.github.martinambrus.rdforward.bridge.forge;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Loads a Forge-style mod jar: opens the jar with a fresh
 * {@link URLClassLoader}, reads {@code META-INF/mods.toml}, scans the jar's
 * class entries for classes annotated with {@link Mod}, and instantiates
 * each one while threading an {@link FMLJavaModLoadingContext} through a
 * {@code ThreadLocal} so the mod's no-arg constructor can call
 * {@code FMLJavaModLoadingContext.get().getModEventBus()}.
 *
 * <p>Every mod class gets its own {@link ForgeEventBus} as its modBus.
 * Handlers registered there receive lifecycle events; the bridge's gameplay
 * forwarders target {@code MinecraftForge.EVENT_BUS}.
 */
public final class ForgeModLoader {

    private ForgeModLoader() {}

    public static LoadedForgeMod load(Path jarPath, ClassLoader parent)
            throws IOException, ReflectiveOperationException {
        URL[] urls = { jarPath.toUri().toURL() };
        URLClassLoader classLoader = new URLClassLoader(urls, parent);
        ForgeModDescriptor forge;
        try (InputStream in = classLoader.getResourceAsStream("META-INF/mods.toml")) {
            if (in == null) {
                classLoader.close();
                throw new IOException("META-INF/mods.toml missing from " + jarPath);
            }
            forge = ForgeModsTomlParser.parse(in);
        }
        if (forge.primary() == null) {
            classLoader.close();
            throw new IOException("mods.toml declared no [[mods]] entry: " + jarPath);
        }

        Map<String, Class<?>> byId = scanModClasses(jarPath, classLoader);

        Map<String, ModHandle> handles = new LinkedHashMap<>();
        for (ForgeModDescriptor.Entry entry : forge.mods()) {
            Class<?> cls = byId.get(entry.modId());
            if (cls == null) continue;
            ForgeEventBus modBus = new ForgeEventBus(entry.modId());
            FMLJavaModLoadingContext ctx = new FMLJavaModLoadingContext(modBus);
            Object instance;
            try {
                FMLJavaModLoadingContext.setCurrent(ctx);
                instance = cls.getDeclaredConstructor().newInstance();
            } finally {
                FMLJavaModLoadingContext.setCurrent(null);
            }
            modBus.register(instance);
            handles.put(entry.modId(), new ModHandle(entry, cls, instance, modBus));
        }

        ModDescriptor descriptor = toModDescriptor(forge);
        ForgeModWrapper wrapper = new ForgeModWrapper(forge, handles, classLoader, jarPath);
        return new LoadedForgeMod(descriptor, forge, jarPath, classLoader, handles, wrapper);
    }

    private static Map<String, Class<?>> scanModClasses(Path jarPath, URLClassLoader classLoader) throws IOException {
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
                Mod ann = cls.getAnnotation(Mod.class);
                if (ann == null) continue;
                out.put(ann.value(), cls);
            }
        }
        return out;
    }

    private static ModDescriptor toModDescriptor(ForgeModDescriptor forge) {
        ForgeModDescriptor.Entry primary = forge.primary();
        Map<String, String> entrypoints = Map.of(ModDescriptor.ENTRYPOINT_SERVER,
                forge.mods().get(0).modId());
        List<String> authors = primary.authors() == null || primary.authors().isBlank()
                ? List.of()
                : List.of(primary.authors());
        // Filter Forge runtime triple (forge/minecraft) out of deps; the
        // remainder describes peer mods that may or may not be RDForward
        // mods, so surface them as soft so a missing peer is non-fatal.
        Map<String, String> softDeps = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : forge.dependencies().entrySet()) {
            String key = e.getKey();
            if (key.equalsIgnoreCase("forge") || key.equalsIgnoreCase("minecraft")) continue;
            softDeps.put(key, e.getValue());
        }
        return new ModDescriptor(
                primary.modId(),
                primary.displayName(),
                primary.version(),
                primary.description() == null ? "" : primary.description(),
                authors,
                "*",
                entrypoints,
                Map.of(),
                softDeps,
                List.of(),
                false,
                null,
                null);
    }

    /**
     * Per-mod state held by the loader: the parsed entry, the mod class, a
     * singleton instance registered on its own {@code modEventBus}, and that
     * bus. Wrappers use the bus to fire lifecycle events at enable time.
     */
    public record ModHandle(
            ForgeModDescriptor.Entry entry,
            Class<?> modClass,
            Object instance,
            ForgeEventBus modBus
    ) {}

    /**
     * Result of a successful load. The caller owns {@code classLoader} and
     * must {@code close()} it when the mod is unloaded.
     */
    public record LoadedForgeMod(
            ModDescriptor descriptor,
            ForgeModDescriptor forgeDescriptor,
            Path jarPath,
            URLClassLoader classLoader,
            Map<String, ModHandle> mods,
            ForgeModWrapper serverMod
    ) {
        public ModHandle primary() {
            return mods.isEmpty() ? null : mods.values().iterator().next();
        }
    }
}
