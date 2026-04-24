// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.plugin;

/**
 * Stub of Bukkit's {@code PluginBase}. Sits between {@link Plugin} and
 * {@link org.bukkit.plugin.java.JavaPlugin} so that plugin instances load
 * as {@code org.bukkit.plugin.Plugin} (adventure-platform-bukkit and many
 * other libraries cast to {@code Plugin}).
 *
 * <p>The generated stub had {@code public final} on {@link #getName()}
 * returning {@code null}, which made subclasses unable to supply a real
 * name. The preserved version delegates to {@link #getDescription()} so
 * the name is sourced from {@code plugin.yml}.
 */
public abstract class PluginBase implements Plugin {

    public PluginBase() {}

    @Override
    public String getName() {
        PluginDescriptionFile d = getDescription();
        return d == null ? getClass().getSimpleName() : d.getName();
    }

    /**
     * Upstream paper-api introduced {@code namespace()} as a lowercase,
     * whitespace-stripped form of the name. Ours returns {@code getName()}
     * lowercased — enough for plugins that use it as an identifier.
     */
    public String namespace() {
        String n = getName();
        return n == null ? "" : n.toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public int hashCode() {
        String n = getName();
        return n == null ? 0 : n.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PluginBase pb)) return false;
        String a = getName();
        String b = pb.getName();
        return a != null && a.equals(b);
    }
}
