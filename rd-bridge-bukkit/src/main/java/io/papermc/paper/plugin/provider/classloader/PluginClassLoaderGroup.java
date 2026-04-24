package io.papermc.paper.plugin.provider.classloader;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PluginClassLoaderGroup {
    java.lang.Class getClassByName(java.lang.String arg0, boolean arg1, io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader arg2);
    void remove(io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader arg0);
    void add(io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader arg0);
    io.papermc.paper.plugin.provider.classloader.ClassLoaderAccess getAccess();
}
