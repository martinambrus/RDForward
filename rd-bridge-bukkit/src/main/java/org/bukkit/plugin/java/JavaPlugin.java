// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.plugin.java;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.NoOpEbeanServer;

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
public abstract class JavaPlugin extends PluginBase implements CommandExecutor {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final List<Listener> registeredListeners = new ArrayList<>();
    private final Map<String, PluginCommand> commandMap = new LinkedHashMap<>();
    private PluginDescriptionFile description;
    private File dataFolder;
    private File file;
    private ClassLoader classLoader;
    private FileConfiguration config;
    private volatile boolean enabled = true;

    public void onLoad() {}
    public void onEnable() {}
    public void onDisable() {}

    /** Real paper-api {@code JavaPlugin.setEnabled} flips the live
     *  enabled flag and fires the Plugin{Enable,Disable}Event. RDForward
     *  has no plugin lifecycle event surface, so we just toggle the flag.
     *  VanishNoPacket 3.14+ calls {@code setEnabled(false)} on itself
     *  when its CraftBukkit-version detection fails — without this method
     *  the JVM throws {@link NoSuchMethodError} from {@code onEnable},
     *  aborting the whole boot. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Naggable flag — real paper-api gates {@link AuthorNagException}
     *  delivery so a misbehaving plugin only spams the console once.
     *  RDForward has no nag pipeline; stored verbatim so plugins that
     *  call {@code setNaggable(false)} during {@code onEnable} (BenCmd
     *  v1.3.5 silences its own nags this way) link cleanly and round-trip
     *  via {@link #isNaggable()}. */
    private volatile boolean naggable = true;

    public void setNaggable(boolean naggable) {
        this.naggable = naggable;
    }

    public boolean isNaggable() {
        return naggable;
    }

    /** Default {@link CommandExecutor#onCommand} — real Bukkit 1.x's
     *  {@code JavaPlugin} implements {@code CommandExecutor} so plugin
     *  bytecode that casts the plugin instance to {@code CommandExecutor}
     *  (notably WorldEdit 5.6.1's {@code CommandRegistration} ctor) verifies
     *  cleanly and the {@code invokeinterface} on {@code onCommand}
     *  dispatches to the plugin's override. Plugins that don't override
     *  inherit this no-op which signals "did not handle" so callers can
     *  fall through to usage strings. */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

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
     * @return the plugin's {@code config.yml}-backed configuration. Lazily
     *         calls {@link #reloadConfig()} on first access so the file
     *         (and its bundled defaults) are picked up before plugin code
     *         reads keys.
     */
    public FileConfiguration getConfig() {
        if (config == null) reloadConfig();
        return config;
    }

    /** Extract {@code config.yml} from the plugin jar to
     *  {@code <dataFolder>/config.yml} when the file is not yet present.
     *  Real paper-api copies bytes verbatim; RDForward does the same so
     *  plugins (Jail 3.x's {@code JailIO.loadConfig}) that read keys
     *  immediately after this call observe the bundled defaults. */
    public void saveDefaultConfig() {
        File target = new File(getDataFolder(), "config.yml");
        if (target.exists()) return;
        try (InputStream in = getResource("config.yml")) {
            if (in == null) return;
            target.getParentFile().mkdirs();
            java.nio.file.Files.copy(in, target.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (java.io.IOException ignored) {}
    }

    /** Re-load the {@code <dataFolder>/config.yml} file (if present) and
     *  set the bundled jar {@code config.yml} resource as the defaults
     *  source — matching real paper-api semantics. Plugins that ship a
     *  default config and read keys without first running
     *  {@link #saveDefaultConfig()} (Jail 3.x's
     *  {@code JailVoteManager.<init>} reads {@code jailvote.time}) still
     *  observe the bundled value because the missing on-disk key falls
     *  through to {@code defaults}. */
    public void reloadConfig() {
        YamlConfiguration loaded = new YamlConfiguration();
        File onDisk = new File(getDataFolder(), "config.yml");
        if (onDisk.isFile()) {
            try { loaded.load(onDisk); } catch (Exception ignored) {}
        }
        try (InputStream in = getResource("config.yml")) {
            if (in != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(in);
                loaded.setDefaults(defaults);
            }
        } catch (java.io.IOException ignored) {}
        config = loaded;
    }

    /** Persist the current in-memory configuration to
     *  {@code <dataFolder>/config.yml}. */
    public void saveConfig() {
        if (config == null) return;
        File target = new File(getDataFolder(), "config.yml");
        try {
            target.getParentFile().mkdirs();
            config.save(target);
        } catch (java.io.IOException ignored) {}
    }

    /** @return the plugin's live enabled flag. Defaults to {@code true}
     *  on construction; flipped by {@link #setEnabled(boolean)} when a
     *  plugin self-disables (e.g. Vanish on unsupported CraftBukkit
     *  version). */
    @Override
    public boolean isEnabled() { return enabled; }

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

    /** Bridge hook — populate the plugin's command map from {@code plugin.yml}.
     *
     *  <p>Renamed from {@code setCommandMap} to avoid clashing with plugin-internal
     *  same-named methods (notably Essentials's {@code Map<String,
     *  IEssentialsCommand> getCommandMap()} — same erased signature, so JVM
     *  dispatch picked the plugin's override and our 151-entry map was lost). */
    public void setRDPluginCommands(Map<String, PluginCommand> map) {
        commandMap.clear();
        commandMap.putAll(map);
    }

    /** Returns the EbeanServer for this plugin. Real Bukkit/CraftBukkit
     *  initialises an embedded Ebean ORM when {@code plugin.yml} declares
     *  {@code database: true}. RDForward has no embedded database, so this
     *  returns a no-op stub whose methods throw {@code UnsupportedOperationException}.
     *  Plugins that use Ebean (HomeSpawnPlus, dynmap) can catch those exceptions
     *  and fall back to YAML or file-based storage. */
    public EbeanServer getDatabase() {
        return new NoOpEbeanServer();
    }

    /** @return every command declared in {@code plugin.yml}. Read-only.
     *
     *  <p>Name disambiguated from {@code getCommandMap} for the same reason
     *  as {@link #setRDPluginCommands}. */
    public Map<String, PluginCommand> getRDPluginCommands() {
        return Collections.unmodifiableMap(commandMap);
    }
}
