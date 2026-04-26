package com.github.martinambrus.rdforward.bridge.pocketmine;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import pocketmine.plugin.PluginBase;
import pocketmine.plugin.PluginDescription;
import pocketmine.plugin.PluginLoader;
import pocketmine.scheduler.TaskScheduler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Loads a PocketMine-shaped Java plugin jar. Same shape as
 * {@code BukkitPluginLoader}: open with a fresh {@link URLClassLoader},
 * parse {@code plugin.yml}, instantiate the {@code main} class (which must
 * extend {@link PluginBase}), initialise it with a scheduler + description,
 * and wrap as a {@code ServerMod}.
 *
 * <p>{@code main} is validated at load time — a class that does not extend
 * {@code PluginBase} raises {@link ReflectiveOperationException} before
 * any {@code onLoad}/{@code onEnable} runs, mirroring PocketMine-MP's
 * rejection of non-PluginBase mains.
 */
public final class PocketMinePluginLoader {

    private static final Logger LOG = Logger.getLogger("RDForward/PocketMineBridge");

    private PocketMinePluginLoader() {}

    public static LoadedPocketMinePlugin load(Path jarPath, ClassLoader parent)
            throws IOException, ReflectiveOperationException {
        URL[] urls = { jarPath.toUri().toURL() };
        URLClassLoader classLoader = new URLClassLoader(urls, parent);
        PluginDescription description;
        try (InputStream in = classLoader.getResourceAsStream("plugin.yml")) {
            if (in == null) {
                classLoader.close();
                throw new IOException("plugin.yml missing from " + jarPath);
            }
            description = PocketMinePluginParser.parse(in);
        }
        Class<?> mainCls = Class.forName(description.main(), true, classLoader);
        if (!PluginBase.class.isAssignableFrom(mainCls)) {
            classLoader.close();
            throw new ReflectiveOperationException(
                    description.main() + " does not extend pocketmine.plugin.PluginBase");
        }
        PluginBase plugin = (PluginBase) mainCls.getDeclaredConstructor().newInstance();
        File dataFolder = jarPath.toAbsolutePath().getParent() == null
                ? new File(description.name())
                : new File(jarPath.toAbsolutePath().getParent().toFile(), description.name());
        Logger pluginLogger = Logger.getLogger("RDForward/PocketMineBridge/" + description.name());
        TaskScheduler scheduler = new RDPocketMineScheduler(description.name());
        PluginLoader pluginLoader = new StaticPluginLoader();
        plugin.init(description, dataFolder, pluginLogger, scheduler, pluginLoader);

        ModDescriptor descriptor = toModDescriptor(description);
        PocketMinePluginWrapper wrapper = new PocketMinePluginWrapper(plugin, description);
        return new LoadedPocketMinePlugin(descriptor, description, jarPath, classLoader, plugin, wrapper);
    }

    private static ModDescriptor toModDescriptor(PluginDescription nf) {
        // PocketMine plugin.yml depend: entries reference other PocketMine
        // plugins, not RDForward mods. Surface as soft deps so missing peers
        // do not abort load.
        Map<String, String> softDeps = new HashMap<>();
        for (String d : nf.depend()) softDeps.put(d, "*");
        Map<String, String> entrypoints = Map.of(ModDescriptor.ENTRYPOINT_SERVER, nf.main());
        List<String> authors = nf.authors().isEmpty() ? List.of() : nf.authors();
        return new ModDescriptor(
                nf.name(),
                nf.name(),
                nf.version(),
                nf.description() == null ? "" : nf.description(),
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
     * Placeholder {@link PluginLoader} handed to every plugin. Real
     * PocketMine exposes jar-loader metadata here; the bridge loads jars
     * through {@link PocketMinePluginLoader#load} so this is only a
     * pass-through that satisfies {@code Plugin.getPluginLoader()}.
     */
    private static final class StaticPluginLoader implements PluginLoader {
        @Override
        public pocketmine.plugin.Plugin loadPlugin(String path) { return null; }

        @Override
        public Pattern[] getPluginFilters() {
            return new Pattern[] { Pattern.compile("^.+\\.jar$") };
        }
    }

    /**
     * Placeholder {@link TaskScheduler}. Real rd-api {@code Scheduler}
     * wiring is plugged in when the wrapper's {@link PocketMinePluginWrapper#onEnable}
     * runs and has access to the live {@code Server}. Until then the
     * scheduler is a no-op — plugins that schedule tasks during {@code
     * onLoad} will see them silently discarded.
     */
    static final class RDPocketMineScheduler implements TaskScheduler {

        private final String pluginName;
        private boolean cancelled;

        RDPocketMineScheduler(String pluginName) {
            this.pluginName = pluginName;
        }

        public String pluginName() { return pluginName; }

        @Override
        public TaskHandle scheduleDelayedTask(pocketmine.scheduler.Task task, int delayTicks) {
            LOG.fine("[PocketMineBridge] scheduleDelayedTask pending (no rd-api server yet) — plugin='"
                    + pluginName + "', delayTicks=" + delayTicks);
            return Cancelled.INSTANCE;
        }

        @Override
        public TaskHandle scheduleRepeatingTask(pocketmine.scheduler.Task task, int periodTicks) {
            LOG.fine("[PocketMineBridge] scheduleRepeatingTask pending — plugin='" + pluginName + "'");
            return Cancelled.INSTANCE;
        }

        @Override
        public TaskHandle scheduleDelayedRepeatingTask(pocketmine.scheduler.Task task,
                                                       int delayTicks, int periodTicks) {
            LOG.fine("[PocketMineBridge] scheduleDelayedRepeatingTask pending — plugin='"
                    + pluginName + "'");
            return Cancelled.INSTANCE;
        }

        @Override
        public void cancelAllTasks() { cancelled = true; }

        public boolean isCancelled() { return cancelled; }

        private static final class Cancelled implements TaskHandle {
            static final Cancelled INSTANCE = new Cancelled();
            @Override public void cancel() {}
            @Override public boolean isCancelled() { return true; }
        }
    }

    /**
     * Result of a successful load. Owner of {@link #classLoader} is the
     * caller; close it on unload.
     */
    public record LoadedPocketMinePlugin(
            ModDescriptor descriptor,
            PluginDescription pocketMineDescription,
            Path jarPath,
            URLClassLoader classLoader,
            PluginBase plugin,
            PocketMinePluginWrapper serverMod
    ) {
        public Map<String, Map<String, Object>> commands() {
            return new LinkedHashMap<>(pocketMineDescription.commands());
        }
    }
}
