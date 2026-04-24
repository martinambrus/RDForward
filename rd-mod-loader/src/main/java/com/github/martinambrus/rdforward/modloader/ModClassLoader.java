package com.github.martinambrus.rdforward.modloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Objects;

/**
 * Per-mod classloader. Parented to the API classloader so every mod sees
 * the {@code rd-api} interfaces, but not the server implementation.
 * Additional peer {@link ModClassLoader}s representing declared hard
 * dependencies are consulted after the parent, letting mod B resolve
 * classes exported by mod A.
 *
 * <p>Classes exported by {@code rd-api} always win over classes bundled
 * inside a mod jar (parent-first for API types), while everything else
 * defaults to child-first so mod-private code and shaded libraries do
 * not collide across mods.
 */
public final class ModClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private static final String API_PACKAGE_PREFIX = "com.github.martinambrus.rdforward.api.";

    private final String modId;
    private final List<ModClassLoader> dependencies;

    public ModClassLoader(String modId, URL[] urls, ClassLoader apiParent, List<ModClassLoader> dependencies) {
        super("mod:" + modId, urls, apiParent);
        this.modId = Objects.requireNonNull(modId, "modId");
        this.dependencies = List.copyOf(dependencies);
    }

    public String getModId() {
        return modId;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);

            if (c == null && name.startsWith(API_PACKAGE_PREFIX)) {
                return super.loadClass(name, resolve);
            }

            if (c == null) {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException ignored) {
                    // fall through to dep search, then parent
                }
            }

            if (c == null) {
                for (ModClassLoader dep : dependencies) {
                    try {
                        c = dep.loadClass(name, false);
                        break;
                    } catch (ClassNotFoundException ignored) {
                        // try next dep
                    }
                }
            }

            if (c == null) {
                c = super.loadClass(name, false);
            }

            if (resolve) resolveClass(c);
            return c;
        }
    }
}
