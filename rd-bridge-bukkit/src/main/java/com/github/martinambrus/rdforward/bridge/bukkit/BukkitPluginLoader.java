// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import com.github.martinambrus.rdforward.bridge.bukkit.compat.PluginLibraryResolver;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads a Bukkit-style plugin jar into the running server: opens the jar
 * with a fresh {@link URLClassLoader}, reads {@code plugin.yml} from the
 * classpath, instantiates the plugin's main class (which must extend
 * {@link JavaPlugin}), populates the plugin's command map from the
 * descriptor, and returns a {@link LoadedPlugin} holding the rd-api
 * descriptor, classloader, plugin instance and a ready-to-enable
 * {@link BukkitPluginWrapper}.
 *
 * <p>The caller is responsible for driving the mod lifecycle via
 * {@link BukkitPluginWrapper} (typically by handing it to
 * {@code ModManager}). The {@link URLClassLoader} is the caller's to close
 * when the plugin is unloaded.
 */
public final class BukkitPluginLoader {

    private BukkitPluginLoader() {}

    /**
     * Read the jar's {@code plugin.yml}, build an rd-api {@link ModDescriptor}
     * from it, instantiate the plugin, and wrap it as a ServerMod.
     *
     * @param jarPath path to the plugin jar
     * @param parent  parent classloader; should expose rd-api + Bukkit stubs
     */
    public static LoadedPlugin load(Path jarPath, ClassLoader parent) throws IOException, ReflectiveOperationException {
        // Parse plugin.yml directly from the jar without creating a classloader,
        // so we can resolve libraries and build a single URL array up front.
        // This avoids the close-and-recreate dance that breaks on Windows
        // (jar file handles not released before the new classloader opens them).
        BukkitPluginDescriptor bukkit;
        try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(jarPath.toFile())) {
            java.util.zip.ZipEntry entry = zf.getEntry("plugin.yml");
            if (entry == null) throw new IOException("plugin.yml missing from " + jarPath);
            try (InputStream in = zf.getInputStream(entry)) {
                bukkit = BukkitPluginParser.parse(in);
            }
        }
        // Resolve Paper-style libraries declared in plugin.yml.
        URL[] libUrls = PluginLibraryResolver.resolve(bukkit.libraries());
        URL[] urls = new URL[1 + libUrls.length];
        urls[0] = jarPath.toUri().toURL();
        System.arraycopy(libUrls, 0, urls, 1, libUrls.length);
        URLClassLoader classLoader = new com.github.martinambrus.rdforward.bridge.bukkit.compat.LegacyPluginClassLoader(urls, parent);
        com.github.martinambrus.rdforward.api.stub.StubCallLog
                .registerPluginLoader(classLoader, bukkit.name());
        // Set data dir BEFORE loading any classes so NullFileParentTransformer
        // can bake the path into File(File,String) / File(String,String) calls.
        ((com.github.martinambrus.rdforward.bridge.bukkit.compat.LegacyPluginClassLoader) classLoader)
                .setPluginDataDir("plugins/" + bukkit.name());
        Class<?> mainCls = Class.forName(bukkit.main(), true, classLoader);
        if (!JavaPlugin.class.isAssignableFrom(mainCls)) {
            com.github.martinambrus.rdforward.api.stub.StubCallLog.unregisterPluginLoader(classLoader);
            classLoader.close();
            throw new ReflectiveOperationException(
                    bukkit.main() + " does not extend org.bukkit.plugin.java.JavaPlugin");
        }
        JavaPlugin plugin = (JavaPlugin) mainCls.getDeclaredConstructor().newInstance();
        plugin.setDescription(toDescriptionFile(bukkit));
        Map<String, PluginCommand> commands = buildCommandMap(bukkit);
        for (PluginCommand pc : commands.values()) pc.setPlugin(plugin);
        plugin.setRDPluginCommands(commands);
        plugin.setFile(jarPath.toFile());
        plugin.setClassLoader(classLoader);
        plugin.setDataFolder(new java.io.File("plugins/" + bukkit.name()));
        ModDescriptor descriptor = toModDescriptor(bukkit);
        BukkitPluginWrapper wrapper = new BukkitPluginWrapper(plugin, bukkit.name());
        // Track the plugin by its declared name so getPluginManager()
        // .getPlugin(name) lookups (mcbans BukkitInterface looks itself
        // up to pass to disablePlugin) resolve to the live instance.
        // BukkitPluginWrapper unregisters on every disable / failure
        // exit so the registry mirrors the live plugin set.
        BukkitBridge.registerPlugin(bukkit.name(), plugin);
        return new LoadedPlugin(descriptor, bukkit, jarPath, classLoader, plugin, wrapper);
    }

    /**
     * Descriptor-only extraction: opens the jar, parses {@code plugin.yml},
     * and returns the corresponding rd-api {@link ModDescriptor} without
     * creating a classloader or loading any plugin classes.
     *
     * <p>Used by {@code BridgeRegistry} during the scan phase so that
     * dependency resolution can proceed before any bridge plugin is
     * instantiated.
     */
    public static ModDescriptor readDescriptor(Path jarPath) throws IOException {
        try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(jarPath.toFile())) {
            java.util.zip.ZipEntry entry = zf.getEntry("plugin.yml");
            if (entry == null) {
                throw new IOException("plugin.yml missing from " + jarPath);
            }
            try (InputStream in = zf.getInputStream(entry)) {
                BukkitPluginDescriptor bukkit = BukkitPluginParser.parse(in);
                return toModDescriptor(bukkit);
            }
        }
    }

    /** Turn each {@code commands:} entry into a ready-to-configure {@link PluginCommand}. */
    private static Map<String, PluginCommand> buildCommandMap(BukkitPluginDescriptor bukkit) {
        Map<String, PluginCommand> out = new LinkedHashMap<>();
        for (Map.Entry<String, BukkitPluginDescriptor.CommandSpec> e : bukkit.commands().entrySet()) {
            BukkitPluginDescriptor.CommandSpec spec = e.getValue();
            PluginCommand cmd = new PluginCommand(spec.name());
            cmd.setDescription(spec.description() == null ? "" : spec.description());
            cmd.setUsage(spec.usage() == null ? "" : spec.usage());
            cmd.setAliases(spec.aliases());
            cmd.setPermission(spec.permission());
            out.put(e.getKey(), cmd);
        }
        return out;
    }

    /** Build the paper-api {@link PluginDescriptionFile} the plugin observes via {@code getDescription()}. */
    private static PluginDescriptionFile toDescriptionFile(BukkitPluginDescriptor bukkit) {
        return new PluginDescriptionFile(
                bukkit.name(),
                bukkit.version(),
                bukkit.main(),
                "",
                List.of(bukkit.author()),
                bukkit.depend(),
                toCommandsMap(bukkit.commands()),
                bukkit.database());
    }

    /** Convert parsed {@code plugin.yml} commands into the map shape Bukkit
     *  exposes via {@link PluginDescriptionFile#getCommands()}: outer key is
     *  the command name, inner map mirrors the original yaml keys
     *  ({@code description}, {@code usage}, {@code aliases}, {@code permission}).
     *  EssentialsX iterates this in its {@code reload} path to register
     *  fallback aliases. */
    private static Map<String, Map<String, Object>> toCommandsMap(
            Map<String, BukkitPluginDescriptor.CommandSpec> specs) {
        if (specs == null || specs.isEmpty()) return java.util.Collections.emptyMap();
        Map<String, Map<String, Object>> out = new LinkedHashMap<>();
        for (Map.Entry<String, BukkitPluginDescriptor.CommandSpec> e : specs.entrySet()) {
            BukkitPluginDescriptor.CommandSpec spec = e.getValue();
            Map<String, Object> inner = new LinkedHashMap<>();
            if (spec.description() != null) inner.put("description", spec.description());
            if (spec.usage() != null)       inner.put("usage", spec.usage());
            if (spec.aliases() != null && !spec.aliases().isEmpty()) inner.put("aliases", spec.aliases());
            if (spec.permission() != null)  inner.put("permission", spec.permission());
            out.put(e.getKey(), inner);
        }
        return out;
    }

    /** Synthesise an rd-api {@link ModDescriptor} from a {@code plugin.yml}.
     *
     * <p>Maps Bukkit's load-order semantics 1-1: {@code depend:} becomes a
     * <em>hard</em> dependency (resolution refuses to load the plugin if
     * any listed plugin is missing), {@code softdepend:} becomes a
     * <em>soft</em> dependency (load-order hint, missing entries are
     * tolerated). Treating {@code depend:} as soft (the previous shape)
     * let plugins like EssentialsDiscordLink boot without their required
     * partner plugin and NPE on every event — the operator now sees a
     * clean "X requires Y but it is not installed" error from
     * {@code DependencyResolver} instead. */
    private static ModDescriptor toModDescriptor(BukkitPluginDescriptor bukkit) {
        Map<String, String> hardDeps = new HashMap<>();
        for (String d : bukkit.depend()) hardDeps.put(d, "*");
        Map<String, String> softDeps = new HashMap<>();
        for (String d : bukkit.softdepend()) softDeps.put(d, "*");
        Map<String, String> entrypoints = Map.of(ModDescriptor.ENTRYPOINT_SERVER, bukkit.main());
        return new ModDescriptor(
                bukkit.name(),
                bukkit.name(),
                bukkit.version(),
                "",
                List.of(bukkit.author()),
                "*",
                entrypoints,
                hardDeps,
                softDeps,
                List.of(),
                false,
                null,
                null);
    }

    /**
     * Result of a successful load. The caller owns {@code classLoader} and
     * must {@code close()} it when the plugin is unloaded.
     */
    public record LoadedPlugin(
            ModDescriptor descriptor,
            BukkitPluginDescriptor bukkitDescriptor,
            Path jarPath,
            URLClassLoader classLoader,
            JavaPlugin plugin,
            BukkitPluginWrapper serverMod
    ) {}
}
