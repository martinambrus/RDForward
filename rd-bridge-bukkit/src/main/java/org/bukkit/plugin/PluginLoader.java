package org.bukkit.plugin;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PluginLoader {
    org.bukkit.plugin.Plugin loadPlugin(java.io.File arg0) throws org.bukkit.plugin.InvalidPluginException, org.bukkit.plugin.UnknownDependencyException;
    org.bukkit.plugin.PluginDescriptionFile getPluginDescription(java.io.File arg0) throws org.bukkit.plugin.InvalidDescriptionException;
    java.util.regex.Pattern[] getPluginFileFilters();
    java.util.Map createRegisteredListeners(org.bukkit.event.Listener arg0, org.bukkit.plugin.Plugin arg1);
    void enablePlugin(org.bukkit.plugin.Plugin arg0);
    void disablePlugin(org.bukkit.plugin.Plugin arg0);
}
