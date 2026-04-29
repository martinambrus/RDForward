// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Plugin classloader that runs every plugin-owned class through
 * {@link LegacyGuavaTransformer} before defining it. Classes outside the
 * plugin jar (parent-loaded server classes) are not touched.
 *
 * <p>The transformer is a no-op for any class whose constant pool does
 * not reference Guava's cache types, so the cost on regular classes is a
 * single {@link ClassReader} scan.
 */
public final class LegacyPluginClassLoader extends URLClassLoader {

    public LegacyPluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String resourceName = name.replace('.', '/') + ".class";
        try (InputStream in = findResourceAsStream(resourceName)) {
            if (in == null) {
                return super.findClass(name);
            }
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
            return defineClass(name, transformed, 0, transformed.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
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
