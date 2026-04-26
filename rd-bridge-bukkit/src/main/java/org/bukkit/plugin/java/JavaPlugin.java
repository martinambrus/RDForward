// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.plugin.java;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Stub of Bukkit's {@code JavaPlugin} base class. Extends {@link PluginBase}
 * so every plugin instance satisfies the {@link org.bukkit.plugin.Plugin}
 * interface — libraries such as adventure-platform-bukkit cast plugin
 * references to {@code Plugin} and fail if the class hierarchy doesn't
 * include it.
 *
 * <p>RDForward's Bukkit bridge instantiates subclasses reflectively and
 * calls {@link #onEnable()} / {@link #onDisable()} in response to the
 * RDForward mod lifecycle.
 *
 * <p>Plugin authors call {@link #registerListener(Listener)} to hook up
 * their event handlers; the bridge collects those during
 * {@code onEnable()} and wires them to the real RDForward events via
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter}.
 *
 * <p>Commands declared under {@code commands:} in {@code plugin.yml} are
 * preloaded into the plugin's command map by the bridge's loader. Plugins
 * attach behaviour via {@code getCommand(name).setExecutor(...)} in their
 * {@code onEnable()}, and the bridge then registers those executors with
 * rd-api's {@code CommandRegistry} under the plugin's mod id.
 */
public abstract class JavaPlugin extends PluginBase {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final List<Listener> registeredListeners = new ArrayList<>();
    private final Map<String, PluginCommand> commandMap = new LinkedHashMap<>();
    private PluginDescriptionFile description;
    private File dataFolder;
    private File file;
    private ClassLoader classLoader;
    private FileConfiguration config;

    public void onLoad() {}
    public void onEnable() {}
    public void onDisable() {}

    @Override
    public Logger getLogger() { return logger; }

    /**
     * @return the global {@link Server} facade that
     *         {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge#install}
     *         installed on {@link Bukkit}. Real paper-api's
     *         {@code JavaPlugin.getServer()} is a plugin-local accessor,
     *         but RDForward runs a single server per JVM so delegating
     *         to {@link Bukkit#getServer()} is equivalent and keeps
     *         plugin bootstraps (e.g. LuckPerms) that call it
     *         inside their constructor working.
     */
    @Override
    public Server getServer() { return Bukkit.getServer(); }

    /**
     * @return the {@link PluginDescriptionFile} wired in by the bridge
     *         loader from {@code plugin.yml}. May be a synthetic empty
     *         descriptor in test paths that didn't call
     *         {@link #setDescription(PluginDescriptionFile)}.
     */
    @Override
    public PluginDescriptionFile getDescription() {
        if (description == null) {
            description = new PluginDescriptionFile(getClass().getSimpleName(), "0.0.0", getClass().getName());
        }
        return description;
    }

    /** Bridge hook — called after reflective instantiation. */
    public void setDescription(PluginDescriptionFile description) {
        this.description = description;
    }

    /**
     * @return a plugin-scoped data directory. RDForward gives every
     *         plugin a unique subdirectory under {@code plugins/} named
     *         after the plugin's declared id (or its class name when no
     *         descriptor is present). The directory is created lazily on
     *         first access.
     */
    public File getDataFolder() {
        if (dataFolder == null) {
            String id = getDescription() == null ? getClass().getSimpleName() : getDescription().getName();
            dataFolder = new File("plugins/" + id);
            if (!dataFolder.exists()) dataFolder.mkdirs();
        }
        return dataFolder;
    }

    /** Bridge hook — overrides the default {@code plugins/<id>} data folder. */
    public void setDataFolder(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    /** @return the jar file this plugin was loaded from. Real paper-api
     *  exposes this via a protected {@code getFile()} backed by a field set
     *  inside {@code JavaPlugin#init}; subclasses such as
     *  {@code WorldEditPlugin} call it to locate their own jar for resource
     *  extraction. {@code null} until {@link #setFile(File)} is called by the
     *  bridge loader. */
    public File getFile() {
        return file;
    }

    /** Bridge hook — populated by {@code BukkitPluginLoader.load()} /
     *  {@code PaperPluginLoader.load()} immediately after reflective
     *  instantiation, so {@link #getFile()} returns the source jar by the
     *  time {@code onEnable} runs. */
    public void setFile(File file) {
        this.file = file;
    }

    /** @return the {@link ClassLoader} that loaded this plugin's jar. Real
     *  paper-api uses {@code PluginClassLoader}; under RDForward it is the
     *  {@link java.net.URLClassLoader} created by the bridge loader. */
    public ClassLoader getClassLoader() {
        return classLoader == null ? getClass().getClassLoader() : classLoader;
    }

    /** Bridge hook — invoked by the bridge loader after instantiation so
     *  {@link #getClassLoader()} returns the per-plugin loader rather than
     *  falling back to {@link Class#getClassLoader()}. */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * @return an input stream reading {@code filename} from the plugin
     *         JAR, or {@code null} if no such resource is present. Real
     *         paper-api resolves this through the plugin's class loader;
     *         RDForward defers to {@code getClass().getClassLoader()}.
     */
    public InputStream getResource(String filename) {
        return getClassLoader().getResourceAsStream(filename);
    }

    /**
     * @return the plugin's {@code config.yml}-backed configuration. Real
     *         paper-api lazily loads from {@code <dataFolder>/config.yml}
     *         and falls back to the bundled {@code config.yml} resource.
     *         RDForward returns an empty {@link YamlConfiguration} that is
     *         safe to read/write but never persisted — every accessor on
     *         {@link org.bukkit.configuration.MemorySection} returns the
     *         caller-supplied default. The instance is non-null and stable
     *         across calls so plugins (e.g. SimpleLogin) that store the
     *         result in a field continue to function.
     */
    public FileConfiguration getConfig() {
        if (config == null) {
            config = new YamlConfiguration();
        }
        return config;
    }

    /** Stub — matches upstream signature. Real save is a no-op under RDForward. */
    public void saveDefaultConfig() {}

    /** Re-creates the in-memory {@link YamlConfiguration} so subsequent
     *  {@link #getConfig()} calls observe a fresh instance. RDForward never
     *  parses the underlying file, so the resulting configuration is empty. */
    public void reloadConfig() {
        config = new YamlConfiguration();
    }

    /** Stub — matches upstream signature. Real save is a no-op under RDForward. */
    public void saveConfig() {}

    /** @return {@code true} — RDForward plugins are considered enabled once loaded. */
    @Override
    public boolean isEnabled() { return true; }

    /** Record a listener so the bridge can wire it up after {@code onEnable()}. */
    public void registerListener(Listener listener) {
        registeredListeners.add(listener);
    }

    public List<Listener> getRegisteredListeners() {
        return Collections.unmodifiableList(registeredListeners);
    }

    /**
     * @return the {@link PluginCommand} with the given name, or {@code null}
     *         if the plugin's {@code plugin.yml} does not declare it.
     */
    public PluginCommand getCommand(String name) {
        return commandMap.get(name);
    }

    /** Bridge hook — populate the plugin's command map from {@code plugin.yml}. */
    public void setCommandMap(Map<String, PluginCommand> map) {
        commandMap.clear();
        commandMap.putAll(map);
    }

    /** @return every command declared in {@code plugin.yml}. Read-only. */
    public Map<String, PluginCommand> getCommandMap() {
        return Collections.unmodifiableMap(commandMap);
    }
}
