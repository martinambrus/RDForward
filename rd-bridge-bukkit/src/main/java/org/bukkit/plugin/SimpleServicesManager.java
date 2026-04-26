// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bukkit-shaped {@code SimpleServicesManager} with a real
 * {@code Map<Class, List<RegisteredServiceProvider>>} backing store.
 *
 * <p>LuckPerms publishes its API via {@code register(Class, instance,
 * plugin, ServicePriority.Normal)} during {@code onEnable}; WorldEdit's
 * WEPIF resolver looks it up via {@code getRegistration(Class)}.
 * Without a real registry the lookup returned {@code null} and WEPIF
 * fell back to the Bukkit Permissions API path — the resolver still
 * worked but we logged a noisy {@code [StubCall]} warning. With the
 * registry wired, plugin-to-plugin service handoff works and the
 * warning disappears.
 *
 * <p>Registrations within a service are kept sorted by priority
 * (highest last) per paper-api semantics; {@link #getRegistration}
 * returns the highest. Reads are unsynchronised — Bukkit plugins do
 * registration only on the main thread during {@code onEnable}, and
 * looks-ups happen later from the same thread or are tolerant of
 * approximate ordering.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class SimpleServicesManager implements org.bukkit.plugin.ServicesManager {

    private final Map<Class<?>, List<RegisteredServiceProvider>> services = new LinkedHashMap<>();

    public SimpleServicesManager() {}

    @Override
    public void register(java.lang.Class service, java.lang.Object provider,
                         org.bukkit.plugin.Plugin plugin,
                         org.bukkit.plugin.ServicePriority priority) {
        if (service == null || provider == null) return;
        RegisteredServiceProvider entry = new RegisteredServiceProvider(service, provider, priority, plugin);
        synchronized (services) {
            List<RegisteredServiceProvider> list = services.computeIfAbsent(service, k -> new ArrayList<>());
            list.add(entry);
            Collections.sort(list);
        }
    }

    @Override
    public void unregisterAll(org.bukkit.plugin.Plugin plugin) {
        if (plugin == null) return;
        synchronized (services) {
            for (List<RegisteredServiceProvider> list : services.values()) {
                list.removeIf(rsp -> rsp.getPlugin() == plugin);
            }
            services.values().removeIf(List::isEmpty);
        }
    }

    @Override
    public void unregister(java.lang.Class service, java.lang.Object provider) {
        if (service == null) return;
        synchronized (services) {
            List<RegisteredServiceProvider> list = services.get(service);
            if (list == null) return;
            list.removeIf(rsp -> provider == null || rsp.getProvider() == provider);
            if (list.isEmpty()) services.remove(service);
        }
    }

    @Override
    public void unregister(java.lang.Object provider) {
        if (provider == null) return;
        synchronized (services) {
            for (List<RegisteredServiceProvider> list : services.values()) {
                list.removeIf(rsp -> rsp.getProvider() == provider);
            }
            services.values().removeIf(List::isEmpty);
        }
    }

    @Override
    public java.lang.Object load(java.lang.Class service) {
        RegisteredServiceProvider rsp = getRegistration(service);
        return rsp == null ? null : rsp.getProvider();
    }

    @Override
    public org.bukkit.plugin.RegisteredServiceProvider getRegistration(java.lang.Class service) {
        if (service == null) return null;
        synchronized (services) {
            List<RegisteredServiceProvider> list = services.get(service);
            return (list == null || list.isEmpty()) ? null : list.get(list.size() - 1);
        }
    }

    @Override
    public java.util.List getRegistrations(org.bukkit.plugin.Plugin plugin) {
        List<RegisteredServiceProvider> out = new ArrayList<>();
        if (plugin == null) return out;
        synchronized (services) {
            for (List<RegisteredServiceProvider> list : services.values()) {
                for (RegisteredServiceProvider rsp : list) {
                    if (rsp.getPlugin() == plugin) out.add(rsp);
                }
            }
        }
        return out;
    }

    @Override
    public java.util.List getRegistrations(java.lang.Class service) {
        if (service == null) return Collections.emptyList();
        synchronized (services) {
            List<RegisteredServiceProvider> list = services.get(service);
            return list == null ? Collections.emptyList() : new ArrayList<>(list);
        }
    }

    @Override
    public java.util.Set getKnownServices() {
        synchronized (services) {
            return new java.util.LinkedHashSet<>(services.keySet());
        }
    }

    @Override
    public boolean isProvidedFor(java.lang.Class service) {
        if (service == null) return false;
        synchronized (services) {
            List<RegisteredServiceProvider> list = services.get(service);
            return list != null && !list.isEmpty();
        }
    }
}
