package io.papermc.paper.plugin.provider.classloader;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ConfiguredPluginClassLoader extends java.io.Closeable {
    io.papermc.paper.plugin.configuration.PluginMeta getConfiguration();
    java.lang.Class loadClass(java.lang.String arg0, boolean arg1, boolean arg2, boolean arg3) throws java.lang.ClassNotFoundException;
    void init(org.bukkit.plugin.java.JavaPlugin arg0);
    org.bukkit.plugin.java.JavaPlugin getPlugin();
    io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup getGroup();
}
