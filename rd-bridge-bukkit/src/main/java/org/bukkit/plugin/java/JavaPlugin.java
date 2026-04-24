package org.bukkit.plugin.java;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Stub of Bukkit's {@code JavaPlugin} base class. RDForward's Bukkit
 * bridge instantiates subclasses reflectively and calls
 * {@link #onEnable()} / {@link #onDisable()} in response to the
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
public abstract class JavaPlugin {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final List<Listener> registeredListeners = new ArrayList<>();
    private final Map<String, PluginCommand> commandMap = new LinkedHashMap<>();

    public void onLoad() {}
    public void onEnable() {}
    public void onDisable() {}

    public Logger getLogger() { return logger; }

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
