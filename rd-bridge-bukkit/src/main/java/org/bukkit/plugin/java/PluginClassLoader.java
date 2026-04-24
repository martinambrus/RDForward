package org.bukkit.plugin.java;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PluginClassLoader extends java.net.URLClassLoader implements io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader {
    public io.papermc.paper.plugin.provider.entrypoint.DependencyContext dependencyContext = null;
    public PluginClassLoader(java.lang.ClassLoader arg0, org.bukkit.plugin.PluginDescriptionFile arg1, java.io.File arg2, java.io.File arg3, java.lang.ClassLoader arg4, java.util.jar.JarFile arg5, io.papermc.paper.plugin.provider.entrypoint.DependencyContext arg6) throws java.io.IOException, org.bukkit.plugin.InvalidPluginException, java.net.MalformedURLException { super(new java.net.URL[0]); }
    public PluginClassLoader() { super(new java.net.URL[0]); }
    public java.net.URL getResource(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.PluginClassLoader.getResource(Ljava/lang/String;)Ljava/net/URL;");
        return null;
    }
    public java.util.Enumeration getResources(java.lang.String arg0) throws java.io.IOException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.PluginClassLoader.getResources(Ljava/lang/String;)Ljava/util/Enumeration;");
        return null;
    }
    public java.lang.Class loadClass(java.lang.String arg0, boolean arg1, boolean arg2, boolean arg3) throws java.lang.ClassNotFoundException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.PluginClassLoader.loadClass(Ljava/lang/String;ZZZ)Ljava/lang/Class;");
        return null;
    }
    public io.papermc.paper.plugin.configuration.PluginMeta getConfiguration() {
        return null;
    }
    public void init(org.bukkit.plugin.java.JavaPlugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.PluginClassLoader.init(Lorg/bukkit/plugin/java/JavaPlugin;)V");
    }
    public org.bukkit.plugin.java.JavaPlugin getPlugin() {
        return null;
    }
    protected java.lang.Class loadClass(java.lang.String arg0, boolean arg1) throws java.lang.ClassNotFoundException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.PluginClassLoader.loadClass(Ljava/lang/String;Z)Ljava/lang/Class;");
        return null;
    }
    protected java.lang.Class findClass(java.lang.String arg0) throws java.lang.ClassNotFoundException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.PluginClassLoader.findClass(Ljava/lang/String;)Ljava/lang/Class;");
        return null;
    }
    public void close() throws java.io.IOException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.PluginClassLoader.close()V");
    }
    public void initialize(org.bukkit.plugin.java.JavaPlugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.java.PluginClassLoader.initialize(Lorg/bukkit/plugin/java/JavaPlugin;)V");
    }
    public java.lang.String toString() {
        return null;
    }
    public io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup getGroup() {
        return null;
    }
}
