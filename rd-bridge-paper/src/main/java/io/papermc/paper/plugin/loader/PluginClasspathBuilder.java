package io.papermc.paper.plugin.loader;

/**
 * Paper's classpath builder passed to {@link PluginLoader#classloader}. Stub:
 * the bridge ignores classpath augmentation requests.
 */
public interface PluginClasspathBuilder {
    default PluginClasspathBuilder addLibrary(Object library) { return this; }
}
