package org.bukkit.plugin;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ServicesManager {
    void register(java.lang.Class arg0, java.lang.Object arg1, org.bukkit.plugin.Plugin arg2, org.bukkit.plugin.ServicePriority arg3);
    void unregisterAll(org.bukkit.plugin.Plugin arg0);
    void unregister(java.lang.Class arg0, java.lang.Object arg1);
    void unregister(java.lang.Object arg0);
    java.lang.Object load(java.lang.Class arg0);
    org.bukkit.plugin.RegisteredServiceProvider getRegistration(java.lang.Class arg0);
    java.util.List getRegistrations(org.bukkit.plugin.Plugin arg0);
    java.util.Collection getRegistrations(java.lang.Class arg0);
    java.util.Collection getKnownServices();
    boolean isProvidedFor(java.lang.Class arg0);
}
