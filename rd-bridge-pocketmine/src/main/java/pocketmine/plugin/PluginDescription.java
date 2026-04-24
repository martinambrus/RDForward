package pocketmine.plugin;

import java.util.List;
import java.util.Map;

/**
 * Parsed view of a PocketMine {@code plugin.yml}. Shape matches PocketMine-MP:
 * {@code name}, {@code version}, {@code main} (entrypoint FQN),
 * {@code api} (minimum PocketMine API version; advisory in the bridge),
 * dependency lists, and the optional {@code commands} block whose values
 * become {@link pocketmine.command.Command} registrations.
 */
public record PluginDescription(
        String name,
        String version,
        String main,
        String api,
        List<String> depend,
        List<String> softDepend,
        List<String> loadBefore,
        List<String> authors,
        String description,
        Map<String, Map<String, Object>> commands
) {
    public PluginDescription {
        depend = depend == null ? List.of() : List.copyOf(depend);
        softDepend = softDepend == null ? List.of() : List.copyOf(softDepend);
        loadBefore = loadBefore == null ? List.of() : List.copyOf(loadBefore);
        authors = authors == null ? List.of() : List.copyOf(authors);
        commands = commands == null ? Map.of() : Map.copyOf(commands);
    }
}
