package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Verifies that {@link LegacyEnumArrayTransformer} can rewrite the
 * exact Citizens-2.0.13 pattern that triggers VerifyError on modern
 * Bukkit: caller passes {@code [LSomeFormerEnum;} (now an interface)
 * to a method declared as {@code [Ljava/lang/Enum;}.
 *
 * <p>Synthesises a class with that shape, applies the transformer,
 * defines the result, and confirms the JVM verifier accepts it.
 */
class LegacyEnumArrayTransformerTest {

    @Test
    void descriptorRewriteCollapsesArrayAndScalarEnumReferences() {
        assertEquals("([Ljava/lang/Object;)Ljava/lang/String;",
                LegacyEnumArrayTransformer.rewriteDescriptor("([Ljava/lang/Enum;)Ljava/lang/String;"));
        assertEquals("([Ljava/lang/Object;)[Ljava/lang/Object;",
                LegacyEnumArrayTransformer.rewriteDescriptor("([Ljava/lang/Enum;)[Ljava/lang/Enum;"));
        assertEquals("(I)V",
                LegacyEnumArrayTransformer.rewriteDescriptor("(I)V"));
        // Scalar Enum is also rewritten — required because the
        // method body may produce Object on stack (after aaload from
        // a now-Object[]) and areturn would fail the verifier when
        // the declared return type is still Enum.
        assertEquals("(Ljava/lang/Object;)V",
                LegacyEnumArrayTransformer.rewriteDescriptor("(Ljava/lang/Enum;)V"));
        assertEquals("([Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;",
                LegacyEnumArrayTransformer.rewriteDescriptor(
                        "([Ljava/lang/Enum;Ljava/lang/String;)Ljava/lang/Enum;"));
    }

    @Test
    void transformedClassPassesVerifierWithSoundLikeArrayPassedToEnumParam() throws Exception {
        // Build:
        //   public class Probe {
        //     public static String run(org.bukkit.Sound[] arr) {
        //       return util(arr);   // util declared as Enum[]
        //     }
        //     public static String util(java.lang.Enum[] arr) { return arr[0].name(); }
        //   }
        // Without rewrite the JVM rejects: [LSound; -> [LEnum;.
        // After rewrite both become [LObject; and Enum.name() becomes
        // a static helper call, so verification + execution succeed.
        byte[] raw = buildProbeClass();

        // Sanity: untransformed bytes fail verification when the
        // class is defined.  We do NOT actually try to define them —
        // that would crash the test harness — but we confirm the
        // transformed output is structurally different and loads.
        byte[] transformed = LegacyEnumArrayTransformer.transform(raw);
        assertTrue(transformed.length != raw.length || !java.util.Arrays.equals(transformed, raw),
                "transformer should have rewritten at least one descriptor");

        // Define + invoke. A throwable here means the JVM verifier
        // still rejects the class — i.e. the transformer missed a
        // rewrite path.
        TestLoader loader = new TestLoader(getClass().getClassLoader());
        Class<?> probe = loader.define("Probe", transformed);
        try {
            // Build a FakeSoundIface[] holding a stub OldEnum-impl,
            // invoke run() via reflection. The probe's util() now takes
            // Object[]; it should call LegacyEnumCompat.name on the element.
            FakeSoundIface[] arr = new FakeSoundIface[] {
                    new OldEnumStub("UNIT_TEST_SOUND")
            };
            // run(FakeSoundIface[]) was generated; reflect with the original
            // (pre-rewrite) parameter type — JVM accepts the shape
            // because the rewritten descriptor is a covariant
            // widening of the original.
            Object result = probe.getMethod("run", arr.getClass())
                    .invoke(null, (Object) arr);
            assertEquals("UNIT_TEST_SOUND", result);
        } catch (Throwable t) {
            fail("transformed class failed to verify or execute: " + t, t);
        }
    }

    @Test
    void scalarEnumReturnFromObjectArrayPassesVerifier() throws Exception {
        // Mirrors Citizens' Util.matchEnum(Enum[], String) -> Enum:
        // method returns one element of the input array.  Without
        // scalar-return rewrite the verifier rejects 'aaload Object;
        // areturn Enum'.  With both array and scalar Enum references
        // collapsed to Object, the method verifies and runs.
        byte[] raw = buildMatchEnumClass();
        byte[] transformed = LegacyEnumArrayTransformer.transform(raw);
        TestLoader loader = new TestLoader(getClass().getClassLoader());
        Class<?> probe = loader.define("Probe2", transformed);
        try {
            FakeSoundIface[] arr = new FakeSoundIface[] {
                    new OldEnumStub("ZERO"), new OldEnumStub("ONE")
            };
            // After rewrite the signature is (Object[], int) -> Object,
            // so the reflective lookup uses Object[].class — even
            // though the actual array carries FakeSoundIface elements.
            Object result = probe.getMethod("pick", Object[].class, int.class)
                    .invoke(null, (Object) arr, 1);
            // After rewrite, pick returns Object (was Enum).  The
            // returned reference is the same OldEnumStub instance.
            assertEquals("ONE", LegacyEnumCompat.name(result));
        } catch (Throwable t) {
            fail("scalar-return rewrite did not produce a verifiable class: " + t, t);
        }
    }

    /** Build the matchEnum-style probe described above. */
    private static byte[] buildMatchEnumClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Probe2", null, "java/lang/Object", null);

        MethodVisitor ctor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();

        // pick(Enum[], int) -> Enum    [returns arr[idx]]
        MethodVisitor pick = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "pick", "([Ljava/lang/Enum;I)Ljava/lang/Enum;", null, null);
        pick.visitCode();
        pick.visitVarInsn(Opcodes.ALOAD, 0);
        pick.visitVarInsn(Opcodes.ILOAD, 1);
        pick.visitInsn(Opcodes.AALOAD);
        pick.visitInsn(Opcodes.ARETURN);
        pick.visitMaxs(0, 0);
        pick.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    /** Build the synthetic Probe class described above. */
    private static byte[] buildProbeClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Probe", null, "java/lang/Object", null);

        MethodVisitor ctor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();

        // run(Sound[]) -> String   [calls util(Enum[])]
        MethodVisitor run = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "run", "([Lcom/github/martinambrus/rdforward/bridge/bukkit/compat/LegacyEnumArrayTransformerTest$FakeSoundIface;)Ljava/lang/String;", null, null);
        run.visitCode();
        run.visitVarInsn(Opcodes.ALOAD, 0);
        run.visitMethodInsn(Opcodes.INVOKESTATIC, "Probe", "util",
                "([Ljava/lang/Enum;)Ljava/lang/String;", false);
        run.visitInsn(Opcodes.ARETURN);
        run.visitMaxs(0, 0);
        run.visitEnd();

        // util(Enum[]) -> String   [calls arr[0].name()]
        MethodVisitor util = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "util", "([Ljava/lang/Enum;)Ljava/lang/String;", null, null);
        util.visitCode();
        util.visitVarInsn(Opcodes.ALOAD, 0);
        util.visitInsn(Opcodes.ICONST_0);
        util.visitInsn(Opcodes.AALOAD);
        util.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Enum",
                "name", "()Ljava/lang/String;", false);
        util.visitInsn(Opcodes.ARETURN);
        util.visitMaxs(0, 0);
        util.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    /** Test classloader that exposes defineClass. */
    private static final class TestLoader extends ClassLoader {
        TestLoader(ClassLoader parent) { super(parent); }
        Class<?> define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    /** Marker interface used in place of org.bukkit.Sound — same
     *  shape (interface extending OldEnum) but no extra abstract
     *  members beyond OldEnum, so the test stub stays minimal. */
    public interface FakeSoundIface extends org.bukkit.util.OldEnum {}

    /** Minimal OldEnum impl whose name() satisfies the helper. */
    public static final class OldEnumStub implements org.bukkit.util.OldEnum, FakeSoundIface {
        private final String n;
        OldEnumStub(String n) { this.n = n; }
        @Override public int compareTo(org.bukkit.util.OldEnum o) { return 0; }
        @Override public String name() { return n; }
        @Override public int ordinal() { return 0; }
    }
}
