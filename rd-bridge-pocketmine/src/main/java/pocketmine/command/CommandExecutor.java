package pocketmine.command;

/**
 * Callback invoked for every dispatched command. Plugins implement this
 * directly on their main class (which {@code PluginBase} already does) or
 * attach a separate executor at enable time. Return {@code false} to
 * indicate "usage should be shown"; {@code true} means "handled".
 */
public interface CommandExecutor {

    boolean onCommand(CommandSender sender, Command command, String label, String[] args);
}
