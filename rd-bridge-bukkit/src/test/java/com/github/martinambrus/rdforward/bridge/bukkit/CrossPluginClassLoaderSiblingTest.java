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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Mirrors the EssentialsAntiBuild scenario: a plugin classloader needs
 * to resolve a class declared inside another plugin's classloader (in
 * production EssentialsAntiBuild references {@code com.earth2me.essentials
 * .IConf} from the Essentials jar). Without sibling lookup the depending
 * plugin crashes with {@code NoClassDefFoundError} during {@code onEnable}.
 *
 * <p>Each test stages two synthetic jars in separate
 * {@link LegacyPluginClassLoader}s so the loaders share nothing through
 * their parent — verification that resolution actually traverses the
 * sibling registry rather than the JVM application classloader.
 */
class CrossPluginClassLoaderSiblingTest {

    @Test
    void siblingLoaderResolvesDependencyClass(@TempDir Path dir) throws Exception {
        Path libJar = writeJar(dir.resolve("lib.jar"), "lib/LibProvider", emitLibProvider());
        Path appJar = writeJar(dir.resolve("app.jar"), "app/Caller", emitCaller());

        // Parent classloader is the system one; importantly it does NOT
        // contain lib/LibProvider, so resolution must reach across to the
        // sibling loader for the dependency to link.
        try (LegacyPluginClassLoader libLoader = new LegacyPluginClassLoader(
                new URL[] { libJar.toUri().toURL() }, getClass().getClassLoader());
             LegacyPluginClassLoader appLoader = new LegacyPluginClassLoader(
                     new URL[] { appJar.toUri().toURL() }, getClass().getClassLoader())) {

            Class<?> caller = appLoader.loadClass("app.Caller");
            Method m = caller.getDeclaredMethod("callLib");
            Object result = m.invoke(null);
            assertEquals("shared-value", result,
                    "app loader must resolve lib/LibProvider via sibling registry");

            // The sibling-resolved class is defined by the LIB loader,
            // not the app loader — so further class lookups JVM performs
            // on the resolved class flow through the sibling, never
            // re-enter app's findClass and risk recursion.
            Class<?> resolvedLib = caller.getClassLoader().loadClass("lib.LibProvider");
            assertSame(libLoader, resolvedLib.getClassLoader(),
                    "sibling-resolved class must keep its origin loader");
        }
    }

    @Test
    void siblingLookupSkipsAfterClose(@TempDir Path dir) throws Exception {
        Path libJar = writeJar(dir.resolve("lib.jar"), "lib/LibProvider", emitLibProvider());
        Path appJar = writeJar(dir.resolve("app.jar"), "app/Caller", emitCaller());

        LegacyPluginClassLoader libLoader = new LegacyPluginClassLoader(
                new URL[] { libJar.toUri().toURL() }, getClass().getClassLoader());
        LegacyPluginClassLoader appLoader = new LegacyPluginClassLoader(
                new URL[] { appJar.toUri().toURL() }, getClass().getClassLoader());
        try {
            // Close the lib loader BEFORE the app loader looks for the
            // dependency: the registry deregisters in close(), so the
            // sibling lookup must throw ClassNotFoundException rather
            // than continuing to serve classes from a dead loader.
            libLoader.close();
            assertThrows(ClassNotFoundException.class,
                    () -> appLoader.loadClass("lib.LibProvider"),
                    "closed loader must drop out of the sibling registry");
        } finally {
            appLoader.close();
        }
    }

    @Test
    void independentLoadersStayIsolatedForOwnClasses(@TempDir Path dir) throws Exception {
        // Two loaders loading the SAME class name independently must each
        // define their own copy. This guards against the registry
        // accidentally short-circuiting own-URL lookups via siblings.
        Path jarA = writeJar(dir.resolve("a.jar"), "lib/LibProvider", emitLibProvider());
        Path jarB = writeJar(dir.resolve("b.jar"), "lib/LibProvider", emitLibProvider());
        try (LegacyPluginClassLoader a = new LegacyPluginClassLoader(
                new URL[] { jarA.toUri().toURL() }, getClass().getClassLoader());
             LegacyPluginClassLoader b = new LegacyPluginClassLoader(
                     new URL[] { jarB.toUri().toURL() }, getClass().getClassLoader())) {
            Class<?> ca = a.loadClass("lib.LibProvider");
            Class<?> cb = b.loadClass("lib.LibProvider");
            assertNotNull(ca);
            assertNotNull(cb);
            // Each loader's findClass returns its OWN definition for a
            // local hit, never delegating to the sibling that happens
            // to also have the same name.
            assertSame(a, ca.getClassLoader());
            assertSame(b, cb.getClassLoader());
            assertNotSame(ca, cb, "each loader must produce its own Class instance");
        }
    }

    private static Path writeJar(Path target, String resourceBase, byte[] classBytes) throws Exception {
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry(resourceBase + ".class"));
            jar.write(classBytes);
            jar.closeEntry();
        }
        return target;
    }

    /** {@code public class LibProvider {
     *      public static String getValue() { return "shared-value"; }
     *  }} */
    private static byte[] emitLibProvider() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "lib/LibProvider", null, "java/lang/Object", null);
        defaultCtor(cw);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "getValue", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn("shared-value");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    /** {@code public class Caller {
     *      public static String callLib() { return LibProvider.getValue(); }
     *  }} — the {@code lib/LibProvider} reference forces the JVM to ask
     *  Caller's defining classloader for the dependency, which is the
     *  exact path that fails for EssentialsAntiBuild when sibling
     *  resolution is missing. */
    private static byte[] emitCaller() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "app/Caller", null, "java/lang/Object", null);
        defaultCtor(cw);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "callLib", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "lib/LibProvider", "getValue",
                "()Ljava/lang/String;", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void defaultCtor(ClassWriter cw) {
        MethodVisitor c = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        c.visitCode();
        c.visitVarInsn(Opcodes.ALOAD, 0);
        c.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        c.visitInsn(Opcodes.RETURN);
        c.visitMaxs(0, 0);
        c.visitEnd();
    }
}
