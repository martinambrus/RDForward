package com.github.martinambrus.rdforward.modloader;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link ModClassLoader} rewrites legacy
 * {@code net.minecraft.util.com.google.*} references in plugin bytecode
 * to canonical {@code com.google.*}, so a class compiled against the
 * Mojang 1.7.2-1.7.5 transient package shading still resolves Guava /
 * Gson when those libraries live at their normal FQCN on the runtime
 * classpath.
 */
class ModClassLoaderRemapTest {

    @Test
    void legacyImmutableListReferenceRemapsToCanonicalGuava(@TempDir Path tempDir) throws Exception {
        Path jar = tempDir.resolve("legacy-mod.jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry("legacy/LegacyConsumer.class"));
            out.write(generateLegacyConsumerClass());
            out.closeEntry();
        }

        URL[] urls = { jar.toUri().toURL() };
        try (ModClassLoader loader = new ModClassLoader(
                "legacy-test", urls, ModClassLoaderRemapTest.class.getClassLoader(), List.of())) {

            Class<?> consumer = Class.forName("legacy.LegacyConsumer", true, loader);
            Method mk = consumer.getMethod("mk");
            Object result = mk.invoke(null);

            assertNotNull(result, "remapped invocation should return a non-null list");
            assertTrue(
                    result instanceof ImmutableList,
                    "remapped call must produce a real com.google.common.collect.ImmutableList; got " + result.getClass());
            assertEquals(List.of("a", "b"), result);
        }
    }

    @Test
    void cleanClassWithoutLegacyRefIsNotRewritten(@TempDir Path tempDir) throws Exception {
        Path jar = tempDir.resolve("clean-mod.jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry("clean/CleanConsumer.class"));
            out.write(generateCleanConsumerClass());
            out.closeEntry();
        }

        URL[] urls = { jar.toUri().toURL() };
        try (ModClassLoader loader = new ModClassLoader(
                "clean-test", urls, ModClassLoaderRemapTest.class.getClassLoader(), List.of())) {

            Class<?> consumer = Class.forName("clean.CleanConsumer", true, loader);
            Method mk = consumer.getMethod("mk");
            Object result = mk.invoke(null);

            assertEquals(List.of("x", "y"), result);
        }
    }

    /**
     * Generate {@code legacy.LegacyConsumer} whose {@code mk()} body calls
     * {@code net.minecraft.util.com.google.common.collect.ImmutableList.of(...)}
     * — a reference no real Guava artifact resolves until the class loader
     * remaps it. We can't compile this with {@code javac} because the
     * legacy class doesn't exist on the test classpath, so the class is
     * synthesised directly with ASM.
     */
    private static byte[] generateLegacyConsumerClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "legacy/LegacyConsumer",
                null, "java/lang/Object", null);

        MethodVisitor init = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(0, 0);
        init.visitEnd();

        MethodVisitor mk = cw.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "mk", "()Ljava/util/List;", null, null);
        mk.visitCode();
        mk.visitLdcInsn("a");
        mk.visitLdcInsn("b");
        mk.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "net/minecraft/util/com/google/common/collect/ImmutableList",
                "of",
                "(Ljava/lang/Object;Ljava/lang/Object;)Lnet/minecraft/util/com/google/common/collect/ImmutableList;",
                false);
        mk.visitInsn(Opcodes.ARETURN);
        mk.visitMaxs(0, 0);
        mk.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    /**
     * Generate {@code clean.CleanConsumer} whose {@code mk()} body calls
     * canonical {@code com.google.common.collect.ImmutableList.of(...)} —
     * verifies the remap fast path passes the bytes through untouched.
     */
    private static byte[] generateCleanConsumerClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "clean/CleanConsumer",
                null, "java/lang/Object", null);

        MethodVisitor init = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(0, 0);
        init.visitEnd();

        MethodVisitor mk = cw.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "mk", "()Ljava/util/List;", null, null);
        mk.visitCode();
        mk.visitLdcInsn("x");
        mk.visitLdcInsn("y");
        mk.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/google/common/collect/ImmutableList",
                "of",
                "(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;",
                false);
        mk.visitInsn(Opcodes.ARETURN);
        mk.visitMaxs(0, 0);
        mk.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}
