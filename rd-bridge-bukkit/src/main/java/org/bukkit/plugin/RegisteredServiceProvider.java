// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.plugin;

/**
 * Bukkit-shaped {@code RegisteredServiceProvider}. Carries the four
 * tuple values (service class, provider, priority, owning plugin) that
 * plugins (LuckPerms, WorldEdit's WEPIF) read after fetching a
 * registration via
 * {@link org.bukkit.plugin.ServicesManager#getRegistration}.
 *
 * <p>{@link Comparable} ordering matches paper-api: lower {@code
 * ServicePriority.ordinal()} sorts first so the highest-priority
 * provider is the LAST entry; consumers that want the best provider
 * use {@code list.get(list.size() - 1)} or simply call
 * {@code getRegistration(Class)} which returns the highest already.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class RegisteredServiceProvider implements java.lang.Comparable {

    private final java.lang.Class service;
    private final java.lang.Object provider;
    private final org.bukkit.plugin.ServicePriority priority;
    private final org.bukkit.plugin.Plugin plugin;

    public RegisteredServiceProvider(java.lang.Class service, java.lang.Object provider,
                                     org.bukkit.plugin.ServicePriority priority,
                                     org.bukkit.plugin.Plugin plugin) {
        this.service = service;
        this.provider = provider;
        this.priority = priority == null ? org.bukkit.plugin.ServicePriority.Normal : priority;
        this.plugin = plugin;
    }

    /** No-arg ctor for legacy plugin code that constructs an empty
     *  registration and then expects to populate it via reflection.
     *  RDForward never produces these — kept for API compatibility. */
    public RegisteredServiceProvider() {
        this.service = null;
        this.provider = null;
        this.priority = org.bukkit.plugin.ServicePriority.Normal;
        this.plugin = null;
    }

    public java.lang.Class getService() { return service; }
    public org.bukkit.plugin.Plugin getPlugin() { return plugin; }
    public java.lang.Object getProvider() { return provider; }
    public org.bukkit.plugin.ServicePriority getPriority() { return priority; }

    /** Typed convenience overload — not a {@link Comparable} override
     *  because the auto-gen surface implements raw {@link Comparable},
     *  but exists for plugins that bind to the typed signature. */
    public int compareTo(org.bukkit.plugin.RegisteredServiceProvider other) {
        if (other == null) return 1;
        return Integer.compare(this.priority.ordinal(), other.priority.ordinal());
    }

    @Override
    public int compareTo(java.lang.Object other) {
        if (other instanceof org.bukkit.plugin.RegisteredServiceProvider rsp) return compareTo(rsp);
        return 0;
    }
}
