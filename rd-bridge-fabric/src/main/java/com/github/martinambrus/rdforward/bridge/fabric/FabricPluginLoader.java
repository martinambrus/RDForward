package com.github.martinambrus.rdforward.bridge.fabric;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import com.github.martinambrus.rdforward.api.mod.ServerMod;
import com.github.martinambrus.rdforward.bridge.fabric.client.FabricClientModWrapper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Loads a Fabric mod jar: opens the jar with a fresh {@link URLClassLoader},
 * reads {@code fabric.mod.json}, instantiates the entrypoint classes that
 * apply to the calling deployment ({@link EnvType}), and wraps the
 * collection as a {@link ServerMod}.
 *
 * <p>Two entrypoint paths:
 * <ul>
 *   <li>{@link #load(Path, ClassLoader)} — dedicated server. Instantiates
 *       {@code main} + {@code server} entrypoints. Rejects {@code "environment":
 *       "client"} mods outright.</li>
 *   <li>{@link #loadForClient(Path, ClassLoader)} — client host. Instantiates
 *       {@code main} + {@code client} entrypoints. Rejects {@code "environment":
 *       "server"} mods outright.</li>
 * </ul>
 *
 * <p>A mod with {@code "environment": "*"} (the default) is accepted by both
 * paths. Entrypoints for the wrong side are ignored — e.g. a server-path
 * load will not instantiate {@code client} entrypoints even if the jar
 * declares them.
 */
public final class FabricPluginLoader {

    private FabricPluginLoader() {}

    public enum EnvType { SERVER, CLIENT }

    /** Server-side load. See class Javadoc. */
    public static LoadedFabricMod load(Path jarPath, ClassLoader parent)
            throws IOException, ReflectiveOperationException {
        return loadInternal(jarPath, parent, EnvType.SERVER);
    }

    /** Client-side load. See class Javadoc. */
    public static LoadedFabricMod loadForClient(Path jarPath, ClassLoader parent)
            throws IOException, ReflectiveOperationException {
        return loadInternal(jarPath, parent, EnvType.CLIENT);
    }

    private static LoadedFabricMod loadInternal(Path jarPath, ClassLoader parent, EnvType env)
            throws IOException, ReflectiveOperationException {
        // Read fabric.mod.json directly from the target jar to avoid the
        // URLClassLoader's parent-first delegation handing us some other
        // fabric.mod.json that lives on the app classpath (e.g. rd-client's).
        FabricModDescriptor fabric;
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry entry = jarFile.getJarEntry("fabric.mod.json");
            if (entry == null) {
                throw new IOException("fabric.mod.json missing from " + jarPath);
            }
            try (InputStream in = jarFile.getInputStream(entry)) {
                fabric = FabricModJsonParser.parse(in);
            }
        }
        URL[] urls = { jarPath.toUri().toURL() };
        URLClassLoader classLoader = new URLClassLoader(urls, parent);

        String environment = fabric.environment();
        if (env == EnvType.SERVER && "client".equalsIgnoreCase(environment)) {
            classLoader.close();
            throw new IOException("Fabric mod " + fabric.id() + " is client-only; skip on dedicated server");
        }
        if (env == EnvType.CLIENT && "server".equalsIgnoreCase(environment)) {
            classLoader.close();
            throw new IOException("Fabric mod " + fabric.id() + " is server-only; skip on client");
        }

        List<ModInitializer> mainInits = new ArrayList<>();
        for (String fqcn : fabric.mainEntrypoints()) {
            Object inst = instantiate(classLoader, fqcn);
            if (!(inst instanceof ModInitializer mi)) {
                classLoader.close();
                throw new ReflectiveOperationException(
                        fabric.id() + ": main entrypoint " + fqcn + " does not implement ModInitializer");
            }
            mainInits.add(mi);
        }

        ServerMod wrapper;
        if (env == EnvType.SERVER) {
            List<DedicatedServerModInitializer> serverInits = new ArrayList<>();
            for (String fqcn : fabric.serverEntrypoints()) {
                Object inst = instantiate(classLoader, fqcn);
                if (!(inst instanceof DedicatedServerModInitializer si)) {
                    classLoader.close();
                    throw new ReflectiveOperationException(
                            fabric.id() + ": server entrypoint " + fqcn
                                    + " does not implement DedicatedServerModInitializer");
                }
                serverInits.add(si);
            }
            wrapper = new FabricModWrapper(mainInits, serverInits);
        } else {
            List<ClientModInitializer> clientInits = new ArrayList<>();
            for (String fqcn : fabric.clientEntrypoints()) {
                Object inst = instantiate(classLoader, fqcn);
                if (!(inst instanceof ClientModInitializer ci)) {
                    classLoader.close();
                    throw new ReflectiveOperationException(
                            fabric.id() + ": client entrypoint " + fqcn
                                    + " does not implement ClientModInitializer");
                }
                clientInits.add(ci);
            }
            wrapper = new FabricClientModWrapper(mainInits, clientInits);
        }

        ModDescriptor descriptor = toModDescriptor(fabric, env);
        return new LoadedFabricMod(descriptor, fabric, jarPath, classLoader, wrapper);
    }

    private static Object instantiate(URLClassLoader cl, String fqcn) throws ReflectiveOperationException {
        Class<?> cls = Class.forName(fqcn, true, cl);
        return cls.getDeclaredConstructor().newInstance();
    }

    private static ModDescriptor toModDescriptor(FabricModDescriptor fabric, EnvType env) {
        String entrypoint;
        if (env == EnvType.SERVER) {
            entrypoint = fabric.serverEntrypoints().isEmpty()
                    ? (fabric.mainEntrypoints().isEmpty() ? null : fabric.mainEntrypoints().get(0))
                    : fabric.serverEntrypoints().get(0);
        } else {
            entrypoint = fabric.clientEntrypoints().isEmpty()
                    ? (fabric.mainEntrypoints().isEmpty() ? null : fabric.mainEntrypoints().get(0))
                    : fabric.clientEntrypoints().get(0);
        }
        String epKey = env == EnvType.SERVER
                ? ModDescriptor.ENTRYPOINT_SERVER
                : ModDescriptor.ENTRYPOINT_CLIENT;
        Map<String, String> entrypoints = entrypoint == null
                ? Map.of()
                : Map.of(epKey, entrypoint);
        // fabric.mod.json declares deps that mostly describe the runtime
        // (fabricloader/minecraft/java) plus optional inter-mod deps. The
        // runtime triple is not an RDForward mod and would cause
        // DependencyResolver to abort; filter it out and surface the rest
        // as soft deps so missing peer mods only affect load order.
        java.util.Map<String, String> softDeps = new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<String, String> e : fabric.dependencies().entrySet()) {
            String key = e.getKey();
            if (key.equals("fabricloader") || key.equals("minecraft") || key.equals("java")) continue;
            softDeps.put(key, e.getValue());
        }
        return new ModDescriptor(
                fabric.id(),
                fabric.name(),
                fabric.version(),
                fabric.description(),
                fabric.authors(),
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
     * Result of a successful load. Caller owns {@code classLoader} and
     * must {@code close()} it when the mod is unloaded.
     *
     * <p>{@code serverMod} is typed as {@link ServerMod} rather than the
     * concrete wrapper class because the exact wrapper type depends on
     * the chosen {@link EnvType} — callers that need to branch can
     * {@code instanceof}-test it.
     */
    public record LoadedFabricMod(
            ModDescriptor descriptor,
            FabricModDescriptor fabricDescriptor,
            Path jarPath,
            URLClassLoader classLoader,
            ServerMod serverMod
    ) {}
}
