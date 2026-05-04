// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.vault;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Minimal {@link JavaPlugin} used as the registration owner for the
 * bridge's stub Vault Economy + Permission service providers. Carries
 * the synthetic plugin name {@code "Vault"} so
 * {@link org.bukkit.plugin.RegisteredServiceProvider#getPlugin()}
 * reports {@code "Vault"} (some consumer plugins log this on startup
 * for diagnostics) and
 * {@link org.bukkit.plugin.PluginManager#getPlugin(String)
 * Bukkit.getPluginManager().getPlugin("Vault")} returns a non-null
 * instance for null-checking probes.
 */
public final class VaultStubOwner extends JavaPlugin {

    public VaultStubOwner() {
        setDescription(new PluginDescriptionFile(
                "Vault",
                "1.7.3-rdforward",
                VaultStubOwner.class.getName(),
                "",
                List.of("RDForward"),
                List.of(),
                java.util.Map.of(),
                false));
        setEnabled(true);
    }
}
