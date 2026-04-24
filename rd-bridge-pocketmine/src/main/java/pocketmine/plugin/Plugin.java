package pocketmine.plugin;

import pocketmine.command.CommandExecutor;
import pocketmine.scheduler.TaskScheduler;

import java.io.File;
import java.util.logging.Logger;

/**
 * Base contract shared by every PocketMine plugin. Narrows the
 * PocketMine-MP {@code pocketmine\Plugin} interface to the pieces rd-api
 * can actually service — data folder, logger, description, and the
 * scheduler that replaces PHP's {@code TaskScheduler}. All other methods
 * PocketMine exposes (event manager, plugin loader discovery, config)
 * are out of scope and not declared here.
 */
public interface Plugin extends CommandExecutor {

    boolean isEnabled();

    File getDataFolder();

    PluginDescription getDescription();

    Logger getLogger();

    String getName();

    TaskScheduler getScheduler();

    PluginLoader getPluginLoader();
}
