package com.github.martinambrus.rdforward.modloader;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.IOException;
import java.io.InputStream;
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
    private final LegacyGoogleRemapper legacyRemapper = new LegacyGoogleRemapper();

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

    /**
     * Override to intercept classes whose bytecode references Mojang's
     * transient 1.7.2-1.7.5 package shading
     * {@code net.minecraft.util.com.google.{common,gson}.*}. When the raw
     * class bytes contain that prefix anywhere in the constant pool, an
     * ASM {@link ClassRemapper} rewrites the references to the canonical
     * {@code com.google.*} packages bundled by the host server, so old
     * plugin jars resolve Guava and Gson without on-disk duplication.
     *
     * <p>Classes with no legacy reference fall through to
     * {@link URLClassLoader#findClass(String)} unchanged. The byte-scan in
     * {@link LegacyGoogleRemapper#classBytesContainLegacy(byte[])} keeps
     * the fast path allocation-free for typical modern plugins.
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String resource = name.replace('.', '/') + ".class";
        URL url = findResource(resource);
        if (url == null) throw new ClassNotFoundException(name);
        byte[] raw;
        try (InputStream in = url.openStream()) {
            raw = in.readAllBytes();
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
        if (!LegacyGoogleRemapper.classBytesContainLegacy(raw)) {
            return defineClass(name, raw, 0, raw.length);
        }
        ClassReader reader = new ClassReader(raw);
        ClassWriter writer = new ClassWriter(0);
        reader.accept(new ClassRemapper(writer, legacyRemapper), 0);
        byte[] remapped = writer.toByteArray();
        return defineClass(name, remapped, 0, remapped.length);
    }
}
