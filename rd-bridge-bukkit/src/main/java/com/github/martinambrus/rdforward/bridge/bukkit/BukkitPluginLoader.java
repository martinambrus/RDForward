package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import org.bukkit.command.PluginCommand;
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
        URL[] urls = { jarPath.toUri().toURL() };
        URLClassLoader classLoader = new URLClassLoader(urls, parent);
        BukkitPluginDescriptor bukkit;
        try (InputStream in = classLoader.getResourceAsStream("plugin.yml")) {
            if (in == null) {
                classLoader.close();
                throw new IOException("plugin.yml missing from " + jarPath);
            }
            bukkit = BukkitPluginParser.parse(in);
        }
        Class<?> mainCls = Class.forName(bukkit.main(), true, classLoader);
        if (!JavaPlugin.class.isAssignableFrom(mainCls)) {
            classLoader.close();
            throw new ReflectiveOperationException(
                    bukkit.main() + " does not extend org.bukkit.plugin.java.JavaPlugin");
        }
        JavaPlugin plugin = (JavaPlugin) mainCls.getDeclaredConstructor().newInstance();
        plugin.setCommandMap(buildCommandMap(bukkit));
        ModDescriptor descriptor = toModDescriptor(bukkit);
        BukkitPluginWrapper wrapper = new BukkitPluginWrapper(plugin, bukkit.name());
        return new LoadedPlugin(descriptor, bukkit, jarPath, classLoader, plugin, wrapper);
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

    /** Synthesise an rd-api {@link ModDescriptor} from a {@code plugin.yml}. */
    private static ModDescriptor toModDescriptor(BukkitPluginDescriptor bukkit) {
        Map<String, String> deps = new HashMap<>();
        for (String d : bukkit.depend()) deps.put(d, "*");
        Map<String, String> entrypoints = Map.of(ModDescriptor.ENTRYPOINT_SERVER, bukkit.main());
        return new ModDescriptor(
                bukkit.name(),
                bukkit.name(),
                bukkit.version(),
                "",
                List.of(bukkit.author()),
                "*",
                entrypoints,
                deps,
                Map.of(),
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
