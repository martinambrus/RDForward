package com.github.martinambrus.rdforward.bridge.bukkit.fixtures;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event$Priority;
import org.bukkit.event.Event$Type;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Fixture plugin for {@code LegacyApiLinkageTest}. Touches the
 * pre-Bukkit-1.x API surfaces RDForward stubbed out for LogBlock 1.41
 * and LogBlockQuestioner 0.02 — extending {@link PlayerListener},
 * dispatching through the legacy
 * {@code registerEvent(Event.Type, Listener, Event.Priority, Plugin)}
 * overload, resolving block ids via {@link Material#matchMaterial} (both
 * numeric-string and namespaced forms), and walking nested
 * {@link ConfigurationSection}s. A class-load failure on any of those
 * surfaces an {@code onEnable} exception that the test catches.
 */
public class LegacyApiProbePlugin extends JavaPlugin {

    public static final String PROP_OK = "rdforward.legacy.ok";
    public static final String PROP_FAILURE = "rdforward.legacy.failure";

    @Override
    public void onEnable() {
        try {
            // Material.matchMaterial — numeric and namespaced lookups
            // (both shapes appear in LogBlock's bundled materials.yml).
            if (Material.matchMaterial("0") != Material.AIR) {
                throw new IllegalStateException("matchMaterial('0') must round-trip to AIR");
            }
            if (Material.matchMaterial("minecraft:stone") != Material.STONE) {
                throw new IllegalStateException("matchMaterial('minecraft:stone') must round-trip to STONE");
            }

            // PlayerListener subclass: must load + instantiate cleanly.
            // The override stays dormant under the legacy registerEvent
            // path (no dispatch) but the linkage must resolve.
            ProbeListener listener = new ProbeListener();

            // Legacy registerEvent overload: must be callable as a no-op.
            PluginManager pm = Bukkit.getServer().getPluginManager();
            pm.registerEvent(
                    Event$Type.PLAYER_COMMAND_PREPROCESS,
                    listener,
                    Event$Priority.Normal,
                    null);

            // Hierarchical configuration sections — LogBlock walks
            // tools.* entries via getConfigurationSection + getKeys(false).
            FileConfiguration cfg = getConfig();
            cfg.set("tools.alpha.aliases", java.util.Arrays.asList("a"));
            cfg.set("tools.alpha.item", 270);
            cfg.set("tools.beta.aliases", java.util.Arrays.asList("b"));
            cfg.set("tools.beta.item", 271);

            ConfigurationSection tools = cfg.getConfigurationSection("tools");
            if (tools == null) {
                throw new IllegalStateException("tools subsection must be non-null");
            }
            java.util.Set<?> keys = tools.getKeys(false);
            if (!(keys.contains("alpha") && keys.contains("beta") && keys.size() == 2)) {
                throw new IllegalStateException("tools.getKeys(false) must yield {alpha, beta}, got " + keys);
            }
            ConfigurationSection alpha = tools.getConfigurationSection("alpha");
            if (alpha == null || alpha.getInt("item") != 270) {
                throw new IllegalStateException("nested subsection read mismatch");
            }

            System.setProperty(PROP_OK, "true");
        } catch (Throwable t) {
            System.setProperty(PROP_FAILURE,
                    t.getClass().getName() + ":" + String.valueOf(t.getMessage()));
            throw t;
        }
    }

    public static final class ProbeListener extends PlayerListener {
        @Override
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            // never invoked — bridge does not route the legacy enum
            // form into dispatch. Method exists only to force
            // PlayerListener resolution at class-load time.
        }
    }
}
