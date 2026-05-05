package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.bridge.bukkit.compat.LegacyPluginClassLoader;
import com.github.martinambrus.rdforward.bridge.bukkit.compat.Log4JFilterTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link Log4JFilterTransformer} adds missing LifeCycle methods
 * to classes that implement Log4j2's Filter interface directly.
 */
class Log4JFilterTransformerTest {

    @Test
    void addsMissingLifeCycleMethodsToFilterImpl() {
        byte[] before = emitFilterImpl(false);
        byte[] after = Log4JFilterTransformer.transform(before);

        MethodScan scan = scanMethods(after);
        assertTrue(scan.hasStart, "start() must be added");
        assertTrue(scan.hasStop, "stop() must be added");
        assertTrue(scan.hasIsStarted, "isStarted() must be added");
        assertTrue(scan.hasIsStopped, "isStopped() must be added");
    }

    @Test
    void leavesCompleteFilterImplUntouched() {
        byte[] before = emitFilterImpl(true);
        byte[] after = Log4JFilterTransformer.transform(before);

        MethodScan scan = scanMethods(after);
        assertTrue(scan.hasStart, "pre-existing start() must remain");
        assertTrue(scan.hasStop, "pre-existing stop() must remain");
        assertTrue(scan.hasIsStarted, "pre-existing isStarted() must remain");
        assertTrue(scan.hasIsStopped, "pre-existing isStopped() must remain");
        // Verify no duplicates — count methods
        assertEquals(1, scan.startCount, "should not duplicate start()");
        assertEquals(1, scan.stopCount, "should not duplicate stop()");
    }

    @Test
    void skipsNonFilterClasses() {
        byte[] before = emitNonFilter();
        byte[] after = Log4JFilterTransformer.transform(before);
        // Same bytes — transformer is a no-op
        assertEquals(before.length, after.length,
                "non-Filter class must pass through unchanged");
    }

    @Test
    void patchedClassLinksAndExecutesViaClassLoader(@TempDir Path dir) throws Exception {
        byte[] raw = emitFilterImpl(false);
        Path jar = writeProbe(dir, "filter/ProbeFilter", raw);
        try (LegacyPluginClassLoader loader = new LegacyPluginClassLoader(
                new URL[] { jar.toUri().toURL() }, getClass().getClassLoader())) {
            Class<?> cls = loader.loadClass("filter.ProbeFilter");
            Object instance = cls.getDeclaredConstructor().newInstance();

            Method start = cls.getMethod("start");
            Method stop = cls.getMethod("stop");
            Method isStarted = cls.getMethod("isStarted");
            Method isStopped = cls.getMethod("isStopped");

            start.invoke(instance);
            stop.invoke(instance);
            assertTrue((boolean) isStarted.invoke(instance), "isStarted() should return true");
            assertFalse((boolean) isStopped.invoke(instance), "isStopped() should return false");
        }
    }

    /* ---------- emit / scan helpers ---------- */

    /** Emit a class that implements Log4j2's Filter. If {@code complete} is
     *  true, include all four LifeCycle methods (simulating a well-behaved
     *  impl). Otherwise omit them all (simulating AuthMe's Log4JFilter). */
    private static byte[] emitFilterImpl(boolean complete) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "filter/ProbeFilter", null,
                "java/lang/Object",
                new String[] { "org/apache/logging/log4j/core/Filter" });

        // default ctor
        MethodVisitor c = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        c.visitCode();
        c.visitVarInsn(Opcodes.ALOAD, 0);
        c.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        c.visitInsn(Opcodes.RETURN);
        c.visitMaxs(0, 0);
        c.visitEnd();

        if (complete) {
            addVoidMethod(cw, "start");
            addVoidMethod(cw, "stop");
            addReturnBooleanMethod(cw, "isStarted", true);
            addReturnBooleanMethod(cw, "isStopped", false);
        }

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static byte[] emitNonFilter() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "filter/NotAFilter", null,
                "java/lang/Object", null);
        MethodVisitor c = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        c.visitCode();
        c.visitVarInsn(Opcodes.ALOAD, 0);
        c.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        c.visitInsn(Opcodes.RETURN);
        c.visitMaxs(0, 0);
        c.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void addVoidMethod(ClassWriter cw, String name) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, name, "()V", null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 1);
        mv.visitEnd();
    }

    private static void addReturnBooleanMethod(ClassWriter cw, String name, boolean value) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, name, "()Z", null, null);
        mv.visitCode();
        mv.visitInsn(value ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private static MethodScan scanMethods(byte[] classBytes) {
        MethodScan scan = new MethodScan();
        new ClassReader(classBytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc,
                                             String sig, String[] exceptions) {
                if ("start".equals(name) && "()V".equals(desc)) { scan.hasStart = true; scan.startCount++; }
                if ("stop".equals(name) && "()V".equals(desc)) { scan.hasStop = true; scan.stopCount++; }
                if ("isStarted".equals(name) && "()Z".equals(desc)) { scan.hasIsStarted = true; }
                if ("isStopped".equals(name) && "()Z".equals(desc)) { scan.hasIsStopped = true; }
                return null;
            }
        }, 0);
        return scan;
    }

    private static Path writeProbe(Path dir, String resourceBase, byte[] classBytes) throws IOException {
        Path jar = dir.resolve(resourceBase.replace('/', '_') + ".jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry(resourceBase + ".class"));
            out.write(classBytes);
            out.closeEntry();
        }
        return jar;
    }

    private static final class MethodScan {
        boolean hasStart;
        boolean hasStop;
        boolean hasIsStarted;
        boolean hasIsStopped;
        int startCount;
        int stopCount;
    }
}
