package io.papermc.paper.plugin.loader;

/**
 * Paper's runtime classpath loader hook. The bridge does not resolve extra
 * libraries or jar-in-jar entries, so this stub is a no-op — plugins that
 * declare {@code loader:} still load, but {@code classloader()} is never
 * actually called.
 */
public interface PluginLoader {
    void classloader(PluginClasspathBuilder classpathBuilder);
}
