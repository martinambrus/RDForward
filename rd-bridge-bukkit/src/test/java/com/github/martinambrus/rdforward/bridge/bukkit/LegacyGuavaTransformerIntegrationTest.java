package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.bridge.bukkit.compat.LegacyPluginClassLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * End-to-end integration for the legacy Guava rewriter. The unit test
 * ({@code LegacyGuavaTransformerMaximumSizeTest}) verifies the bytecode
 * shape after rewrite. This test wires a synthetic plugin class through
 * {@link LegacyPluginClassLoader} (the production load path) and
 * invokes a method that calls {@code CacheBuilder.maximumSize(int)} —
 * the call Essentials Pre-2.14 makes from {@code UserMap.<init>}. If
 * the transformer were absent or broken, JVM linkage against the
 * server's bundled Guava 33.5.0 (which dropped the int overload at
 * Guava 11) would throw {@link NoSuchMethodError} on first invocation.
 */
class LegacyGuavaTransformerIntegrationTest {

    @Test
    void maximumSizeIntCallSiteExecutesAfterRewrite(@TempDir Path dir) throws Exception {
        Path jar = writeProbeJar(dir.resolve("guava-probe.jar"));

        try (LegacyPluginClassLoader loader = new LegacyPluginClassLoader(
                new URL[] { jar.toUri().toURL() },
                getClass().getClassLoader())) {
            Class<?> probe = loader.loadClass("GuavaMaximumSizeProbe");
            Method m = probe.getDeclaredMethod("buildLimitedCache");
            // If the transformer didn't rewrite, this throws
            // NoSuchMethodError on the maximumSize(I) call site. With
            // the rewrite, the static GuavaCompat.maximumSize shim
            // forwards to CacheBuilder.maximumSize(long) and returns
            // a non-null builder.
            Object result = m.invoke(null);
            assertNotNull(result, "rewritten call site must return a CacheBuilder");
        }
    }

    private Path writeProbeJar(Path target) throws Exception {
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("GuavaMaximumSizeProbe.class"));
            jar.write(emitProbeClass());
            jar.closeEntry();
        }
        return target;
    }

    /** {@code public class GuavaMaximumSizeProbe {
     *      public static CacheBuilder buildLimitedCache() {
     *          return CacheBuilder.newBuilder().maximumSize(100);
     *      }
     *  }} — the {@code maximumSize(100)} call site uses the legacy
     *  {@code (I)} descriptor that Essentials's UserMap was compiled
     *  against. */
    private static byte[] emitProbeClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC,
                "GuavaMaximumSizeProbe", null, "java/lang/Object", null);

        // default ctor
        MethodVisitor ctor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "buildLimitedCache",
                "()Lcom/google/common/cache/CacheBuilder;",
                null, null);
        mv.visitCode();
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "com/google/common/cache/CacheBuilder",
                "newBuilder",
                "()Lcom/google/common/cache/CacheBuilder;",
                false);
        mv.visitIntInsn(Opcodes.BIPUSH, 100);
        // The legacy (I) descriptor Essentials UserMap calls — the
        // transformer must rewrite this to GuavaCompat.maximumSize(...).
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "com/google/common/cache/CacheBuilder",
                "maximumSize",
                "(I)Lcom/google/common/cache/CacheBuilder;",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}
