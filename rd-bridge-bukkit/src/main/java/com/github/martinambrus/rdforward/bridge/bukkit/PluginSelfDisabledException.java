// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

/**
 * Marker thrown by {@link BukkitPluginWrapper#onEnable} when the
 * underlying Bukkit plugin self-disables during its own {@code
 * onEnable} -- typically by calling {@code setEnabled(false)} or
 * {@code Bukkit.getPluginManager().disablePlugin(this)} after a
 * pre-flight check fails (VanishNoPacket on CraftBukkit version
 * mismatch, mcbans on online-mode mismatch). The mod loader catches
 * Throwable on enable, so this routes the container into the same
 * ERROR terminal state as a thrown {@code RuntimeException} would --
 * and crucially, prevents the plugin's listeners and commands from
 * being wired into the live server registries.
 *
 * <p>Subclassing {@link RuntimeException} keeps the existing catch
 * blocks in {@code ModManager.enable} unchanged.
 */
public final class PluginSelfDisabledException extends RuntimeException {

    public PluginSelfDisabledException(String pluginName) {
        super(pluginName + " disabled itself during onEnable; treating as load failure");
    }
}
