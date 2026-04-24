package org.bukkit.plugin.java;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class JavaPluginLoader implements org.bukkit.plugin.PluginLoader {
    public JavaPluginLoader(org.bukkit.Server arg0) {}
    public JavaPluginLoader() {}
    public org.bukkit.plugin.Plugin loadPlugin(java.io.File arg0) throws org.bukkit.plugin.InvalidPluginException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.JavaPluginLoader.loadPlugin(Ljava/io/File;)Lorg/bukkit/plugin/Plugin;");
        return null;
    }
    public org.bukkit.plugin.PluginDescriptionFile getPluginDescription(java.io.File arg0) throws org.bukkit.plugin.InvalidDescriptionException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.JavaPluginLoader.getPluginDescription(Ljava/io/File;)Lorg/bukkit/plugin/PluginDescriptionFile;");
        return null;
    }
    public java.util.regex.Pattern[] getPluginFileFilters() {
        return new java.util.regex.Pattern[0];
    }
    public java.util.Map createRegisteredListeners(org.bukkit.event.Listener arg0, org.bukkit.plugin.Plugin arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.JavaPluginLoader.createRegisteredListeners(Lorg/bukkit/event/Listener;Lorg/bukkit/plugin/Plugin;)Ljava/util/Map;");
        return java.util.Collections.emptyMap();
    }
    public void enablePlugin(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.JavaPluginLoader.enablePlugin(Lorg/bukkit/plugin/Plugin;)V");
    }
    public void disablePlugin(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.JavaPluginLoader.disablePlugin(Lorg/bukkit/plugin/Plugin;)V");
    }
}
