// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin classloader that runs every plugin-owned class through
 * {@link LegacyGuavaTransformer} before defining it. Classes outside the
 * plugin jar (parent-loaded server classes) are not touched.
 *
 * <p>The transformer is a no-op for any class whose constant pool does
 * not reference Guava's cache types, so the cost on regular classes is a
 * single {@link ClassReader} scan.
 *
 * <p>Loaders register themselves in a static set so that a plugin which
 * declares another plugin in its {@code depend:} list (e.g. EssentialsAntiBuild
 * referencing {@code com.earth2me.essentials.IConf}) can resolve the
 * dependency's classes through its sibling classloader. The sibling lookup
 * is one-hop: when sibling's class is returned, its defining classloader
 * is the sibling, so any further class resolutions JVM performs on that
 * class go through the sibling directly, without re-entering this loader.
 */
public final class LegacyPluginClassLoader extends URLClassLoader {

    private static final Set<LegacyPluginClassLoader> REGISTRY = ConcurrentHashMap.newKeySet();

    /** Plugin data directory path (e.g. "plugins/HomeSpawnPlus"), set by BukkitPluginLoader
     *  before any classes are loaded. Used by {@link NullFileParentTransformer} to redirect
     *  null-parent {@code new File(null, child)} calls to the plugin's data directory. */
    private volatile String pluginDataDir;

    public LegacyPluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        REGISTRY.add(this);
    }

    /** Set the plugin data directory path. Must be called before any plugin classes
     *  are loaded so {@link NullFileParentTransformer} can bake the path into the
     *  rewritten bytecode. */
    public void setPluginDataDir(String dir) {
        this.pluginDataDir = dir;
    }

    /** Snapshot of all live plugin classloaders. Used by
     *  {@code YamlConfiguration.loadFromString} to resolve SnakeYAML
     *  {@code !!} class tags from plugin data files. */
    public static List<URLClassLoader> allLoaders() {
        return List.copyOf(REGISTRY);
    }

    @Override
    public void close() throws IOException {
        REGISTRY.remove(this);
        super.close();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> own = findClassFromOwn(name);
        if (own != null) return own;
        for (LegacyPluginClassLoader other : REGISTRY) {
            if (other == this) continue;
            Class<?> c = other.findClassFromOwn(name);
            if (c != null) return c;
        }
        throw new ClassNotFoundException(name);
    }

    /**
     * Try to load {@code name} from this classloader's own URLs only.
     * Does NOT recurse into the sibling registry, so cross-plugin class
     * resolution never produces cycles even when many plugins are loaded.
     *
     * @return the loaded class, or {@code null} if this loader's URLs do
     *         not contain a definition for {@code name}
     */
    public Class<?> findClassFromOwn(String name) {
        Class<?> already = findLoadedClass(name);
        if (already != null) return already;
        String resourceName = name.replace('.', '/') + ".class";
        try (InputStream in = findResourceAsStream(resourceName)) {
            if (in == null) return null;
            byte[] raw = readAll(in);
            byte[] transformed;
            try {
                transformed = LegacyGuavaTransformer.transform(raw);
            } catch (Throwable t) {
                transformed = raw;
            }
            try {
                transformed = LegacyHealthTransformer.transform(transformed);
            } catch (Throwable t) {
                // leave whatever we had after the previous pass
            }
            try {
                transformed = LegacySnakeYamlTransformer.transform(transformed);
            } catch (Throwable t) {
                // leave whatever we had after the previous pass
            }
            try {
                transformed = LegacyCraftServerTransformer.transform(transformed);
            } catch (Throwable t) {
                // leave whatever we had after the previous pass
            }
            if (pluginDataDir != null) {
                try {
                    transformed = NullFileParentTransformer.transform(transformed, pluginDataDir);
                } catch (Throwable t) {
                    // leave whatever we had after the previous pass
                }
            }
            if (name.equals("com.andune.minecraft.hsp.shade.commonlib.Teleport")) {
                try {
                    transformed = HSPSafeLocationPatch.transform(transformed);
                } catch (Throwable t) {
                    // leave unpatched on failure
                }
            }
            return defineClass(name, transformed, 0, transformed.length);
        } catch (IOException e) {
            return null;
        } catch (LinkageError e) {
            return findLoadedClass(name);
        }
    }

    private InputStream findResourceAsStream(String resourceName) throws IOException {
        URL u = findResource(resourceName);
        return u == null ? null : u.openStream();
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) baos.write(buf, 0, n);
        return baos.toByteArray();
    }
}
