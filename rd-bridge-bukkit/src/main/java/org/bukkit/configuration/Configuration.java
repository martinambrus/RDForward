// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.configuration;

/**
 * Bukkit-shaped read/write configuration section. Extends
 * {@link ConfigurationSection} so plugins that cast {@code getConfig()}
 * to {@code Configuration} (e.g. ClearLag) can call list getters like
 * {@link #getIntegerList} and {@link #getStringList} inherited from the
 * section interface.
 */
public interface Configuration extends ConfigurationSection {
}
