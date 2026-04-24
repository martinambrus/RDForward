package io.papermc.paper.plugin.provider.classloader;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PaperClassLoaderStorage {
    static io.papermc.paper.plugin.provider.classloader.PaperClassLoaderStorage instance() {
        return null;
    }
    io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup registerSpigotGroup(org.bukkit.plugin.java.PluginClassLoader arg0);
    io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup registerOpenGroup(io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader arg0);
    io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup registerAccessBackedGroup(io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader arg0, io.papermc.paper.plugin.provider.classloader.ClassLoaderAccess arg1);
    void unregisterClassloader(io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader arg0);
    boolean registerUnsafePlugin(io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader arg0);
}
