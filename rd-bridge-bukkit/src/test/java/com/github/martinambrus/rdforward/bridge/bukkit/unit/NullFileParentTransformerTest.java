package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.compat.BridgeFiles;
import com.github.martinambrus.rdforward.bridge.bukkit.compat.NullFileParentTransformer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that {@link NullFileParentTransformer} rewrites
 * {@code new File(File, String)} and {@code new File(String, String)}
 * constructor results through {@link BridgeFiles#resolve}.
 */
class NullFileParentTransformerTest {

    @Test
    void transformedClassRedirectsNullParentFile() throws Exception {
        String pluginDir = "plugins/TestPlugin";
        byte[] original = compileTestClass();
        byte[] transformed = NullFileParentTransformer.transform(original, pluginDir);

        ClassLoader loader = new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) {
                if ("test.Target".equals(name)) {
                    return defineClass(name, transformed, 0, transformed.length);
                }
                return null;
            }
        };

        Class<?> cls = loader.loadClass("test.Target");
        Method m = cls.getMethod("makeFile", String.class);
        File result = (File) m.invoke(null, "spawns.yml");

        assertNotNull(result);
        // After transformation, new File((File)null, "spawns.yml")
        // should redirect to plugins/TestPlugin/spawns.yml
        assertEquals(new File(new File(pluginDir), "spawns.yml"), result);
    }

    @Test
    void returnsOriginalBytesWhenPluginDirIsNull() {
        byte[] original = new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};
        byte[] result = NullFileParentTransformer.transform(original, null);
        assertSame(original, result, "null pluginDir should return original bytes");
    }

    /** Emit a minimal class that calls {@code new File((File)null, child)}. */
    private static byte[] compileTestClass() {
        // Use ASM to generate a class equivalent to:
        // package test;
        // public class Target {
        //     public static File makeFile(String child) {
        //         return new File((File) null, child);
        //     }
        // }
        org.objectweb.asm.ClassWriter cw = new org.objectweb.asm.ClassWriter(
                org.objectweb.asm.ClassWriter.COMPUTE_FRAMES | org.objectweb.asm.ClassWriter.COMPUTE_MAXS);
        cw.visit(org.objectweb.asm.Opcodes.V17, org.objectweb.asm.Opcodes.ACC_PUBLIC,
                "test/Target", null, "java/lang/Object", null);

        // Default constructor
        org.objectweb.asm.MethodVisitor ctor = cw.visitMethod(
                org.objectweb.asm.Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(org.objectweb.asm.Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(org.objectweb.asm.Opcodes.INVOKESPECIAL,
                "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(org.objectweb.asm.Opcodes.RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();

        // makeFile method
        org.objectweb.asm.MethodVisitor mv = cw.visitMethod(
                org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_STATIC,
                "makeFile", "(Ljava/lang/String;)Ljava/io/File;", null, null);
        mv.visitCode();
        mv.visitTypeInsn(org.objectweb.asm.Opcodes.NEW, "java/io/File");
        mv.visitInsn(org.objectweb.asm.Opcodes.DUP);
        mv.visitInsn(org.objectweb.asm.Opcodes.ACONST_NULL);  // null File parent
        mv.visitVarInsn(org.objectweb.asm.Opcodes.ALOAD, 0);   // child String
        mv.visitMethodInsn(org.objectweb.asm.Opcodes.INVOKESPECIAL,
                "java/io/File", "<init>", "(Ljava/io/File;Ljava/lang/String;)V", false);
        mv.visitInsn(org.objectweb.asm.Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}
