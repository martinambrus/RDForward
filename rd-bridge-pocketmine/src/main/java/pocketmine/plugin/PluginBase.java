package pocketmine.plugin;

import pocketmine.command.Command;
import pocketmine.command.CommandSender;
import pocketmine.scheduler.TaskScheduler;

import java.io.File;
import java.util.logging.Logger;

/**
 * Default {@link Plugin} implementation. A PocketMine plugin's main class
 * extends this to receive lifecycle hooks ({@link #onLoad}, {@link #onEnable},
 * {@link #onDisable}) and gain a ready-made data folder, logger, and
 * task scheduler. {@link #onCommand} returns {@code true} by default;
 * subclasses override when they declare commands in {@code plugin.yml}.
 *
 * <p>Mirrors PocketMine-MP's abstract {@code PluginBase} PHP class.
 * Constructed by {@code PocketMinePluginLoader} after it wires in the
 * parsed {@link PluginDescription}, plugin-local logger, and scheduler.
 */
public abstract class PluginBase implements Plugin {

    private boolean enabled;
    private PluginDescription description;
    private File dataFolder;
    private Logger logger;
    private TaskScheduler scheduler;
    private PluginLoader pluginLoader;

    public void init(PluginDescription description,
                     File dataFolder,
                     Logger logger,
                     TaskScheduler scheduler,
                     PluginLoader pluginLoader) {
        this.description = description;
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.scheduler = scheduler;
        this.pluginLoader = pluginLoader;
    }

    public void onLoad() {}

    public void onEnable() {}

    public void onDisable() {}

    public final void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override public boolean isEnabled() { return enabled; }

    @Override public File getDataFolder() { return dataFolder; }

    @Override public PluginDescription getDescription() { return description; }

    @Override public Logger getLogger() { return logger; }

    @Override public String getName() { return description == null ? "unknown" : description.name(); }

    @Override public TaskScheduler getScheduler() { return scheduler; }

    @Override public PluginLoader getPluginLoader() { return pluginLoader; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return true;
    }
}
