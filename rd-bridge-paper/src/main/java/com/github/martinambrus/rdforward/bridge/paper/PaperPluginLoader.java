package com.github.martinambrus.rdforward.bridge.paper;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginDescriptor;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginLoader;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
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
 * Loads a Paper-style plugin jar. If the jar ships {@code paper-plugin.yml}
 * the loader reads it, instantiates the declared bootstrapper (if any),
 * runs {@code bootstrap(BootstrapContext)}, then either uses the
 * bootstrapper's {@code createPlugin} return value or instantiates the
 * {@code main:} class directly. When the jar only ships {@code plugin.yml}
 * the loader transparently delegates to {@link BukkitPluginLoader}.
 *
 * <p>The caller owns the returned {@link URLClassLoader} and must
 * {@code close()} it on unload.
 */
public final class PaperPluginLoader {

    public static final String PAPER_MANIFEST = "paper-plugin.yml";

    private PaperPluginLoader() {}

    /** Union return type — either a Paper-style load or a Bukkit fallback. */
    public sealed interface Result permits LoadedPaperPlugin, BukkitFallback {}

    public record LoadedPaperPlugin(
            ModDescriptor descriptor,
            PaperPluginDescriptor paperDescriptor,
            Path jarPath,
            URLClassLoader classLoader,
            JavaPlugin plugin,
            PaperPluginWrapper serverMod
    ) implements Result {}

    public record BukkitFallback(BukkitPluginLoader.LoadedPlugin inner) implements Result {}

    public static Result load(Path jarPath, ClassLoader parent) throws IOException, ReflectiveOperationException {
        URL[] urls = { jarPath.toUri().toURL() };
        URLClassLoader classLoader = new URLClassLoader(urls, parent);

        PaperPluginDescriptor paper;
        try (InputStream in = classLoader.getResourceAsStream(PAPER_MANIFEST)) {
            if (in == null) {
                classLoader.close();
                return new BukkitFallback(BukkitPluginLoader.load(jarPath, parent));
            }
            paper = PaperPluginParser.parse(in);
        }

        PluginBootstrap bootstrapper = null;
        BootstrapLifecycleManager lifecycleManager = new BootstrapLifecycleManager();
        if (paper.bootstrapper() != null && !paper.bootstrapper().isBlank()) {
            Class<?> bsCls = Class.forName(paper.bootstrapper(), true, classLoader);
            if (!PluginBootstrap.class.isAssignableFrom(bsCls)) {
                classLoader.close();
                throw new ReflectiveOperationException(
                        paper.bootstrapper() + " does not implement io.papermc.paper.plugin.bootstrap.PluginBootstrap");
            }
            bootstrapper = (PluginBootstrap) bsCls.getDeclaredConstructor().newInstance();
            bootstrapper.bootstrap(new BootstrapContextImpl(lifecycleManager, paper.name()));
        }

        JavaPlugin plugin = null;
        if (bootstrapper != null) {
            plugin = bootstrapper.createPlugin(new PluginProviderContextImpl(paper.name()));
        }
        if (plugin == null) {
            Class<?> mainCls = Class.forName(paper.main(), true, classLoader);
            if (!JavaPlugin.class.isAssignableFrom(mainCls)) {
                classLoader.close();
                throw new ReflectiveOperationException(
                        paper.main() + " does not extend org.bukkit.plugin.java.JavaPlugin");
            }
            plugin = (JavaPlugin) mainCls.getDeclaredConstructor().newInstance();
        }
        plugin.setCommandMap(buildCommandMap(paper));

        ModDescriptor descriptor = toModDescriptor(paper);
        PaperPluginWrapper wrapper = new PaperPluginWrapper(plugin, paper.name(), bootstrapper, lifecycleManager, paper);
        return new LoadedPaperPlugin(descriptor, paper, jarPath, classLoader, plugin, wrapper);
    }

    private static Map<String, PluginCommand> buildCommandMap(PaperPluginDescriptor paper) {
        Map<String, PluginCommand> out = new LinkedHashMap<>();
        if (paper.commands() == null) return out;
        for (Map.Entry<String, BukkitPluginDescriptor.CommandSpec> e : paper.commands().entrySet()) {
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

    private static ModDescriptor toModDescriptor(PaperPluginDescriptor paper) {
        Map<String, String> deps = new HashMap<>();
        for (String d : paper.serverDeps()) deps.put(d, "*");
        for (String d : paper.bootstrapDeps()) deps.putIfAbsent(d, "*");
        Map<String, String> entrypoints = Map.of(ModDescriptor.ENTRYPOINT_SERVER, paper.main());
        List<String> authors = paper.authors().isEmpty() ? List.of(paper.name()) : paper.authors();
        return new ModDescriptor(
                paper.name(),
                paper.name(),
                paper.version(),
                paper.description() == null ? "" : paper.description(),
                authors,
                paper.apiVersion() == null || paper.apiVersion().isBlank() ? "*" : paper.apiVersion(),
                entrypoints,
                deps,
                Map.of(),
                List.of(),
                false,
                null,
                null);
    }
}
