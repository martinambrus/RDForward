package pocketmine.plugin;

import java.util.regex.Pattern;

/**
 * Stub mirroring PocketMine-MP's {@code PluginLoader} interface. The
 * bridge itself drives jar loading through its own
 * {@code PocketMinePluginLoader} — this type exists so plugin code that
 * calls {@code Plugin.getPluginLoader()} has something to receive.
 */
public interface PluginLoader {

    Plugin loadPlugin(String path);

    default Pattern[] getPluginFilters() {
        return new Pattern[] { Pattern.compile("^.+\\.jar$") };
    }
}
