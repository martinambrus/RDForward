package io.papermc.paper.plugin.configuration;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PluginMeta extends net.kyori.adventure.key.Namespaced {
    java.lang.String getName();
    default java.lang.String getDisplayName() {
        return null;
    }
    java.lang.String getMainClass();
    org.bukkit.plugin.PluginLoadOrder getLoadOrder();
    java.lang.String getVersion();
    java.lang.String getLoggerPrefix();
    java.util.List getPluginDependencies();
    java.util.List getPluginSoftDependencies();
    java.util.List getLoadBeforePlugins();
    java.util.List getProvidedPlugins();
    java.util.List getAuthors();
    java.util.List getContributors();
    java.lang.String getDescription();
    java.lang.String getWebsite();
    java.util.List getPermissions();
    org.bukkit.permissions.PermissionDefault getPermissionDefault();
    java.lang.String getAPIVersion();
    default java.lang.String namespace() {
        return null;
    }
}
