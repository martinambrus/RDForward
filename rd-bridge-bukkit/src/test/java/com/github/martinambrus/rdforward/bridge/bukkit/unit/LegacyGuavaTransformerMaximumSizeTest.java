package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.compat.LegacyGuavaTransformer;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the {@link LegacyGuavaTransformer} rewrite for the legacy
 * {@code CacheBuilder.maximumSize(int)} call site. Essentials Pre-2.14
 * was compiled against pre-Guava-11 API ({@code maximumSize(int)}
 * survived only as a no-arg overload until Guava 11 dropped it for the
 * single {@code maximumSize(long)} variant). The transformer must
 * rewrite the int-typed call into an INVOKESTATIC into
 * {@code GuavaCompat.maximumSize(CacheBuilder, int)} so the descriptor
 * link resolves against the 33.5.0 jar without an I2L injection.
 */
class LegacyGuavaTransformerMaximumSizeTest {

    private static final String GUAVA_COMPAT =
            "com/github/martinambrus/rdforward/bridge/bukkit/compat/GuavaCompat";

    @Test
    void rewritesCacheBuilderMaximumSizeIntCallSite() {
        byte[] cls = emitMaximumSizeCaller();
        List<MethodCall> calls = collectMethodCalls(LegacyGuavaTransformer.transform(cls), "callIt");

        assertFalse(containsLegacyCall(calls,
                        "com/google/common/cache/CacheBuilder",
                        "maximumSize",
                        "(I)Lcom/google/common/cache/CacheBuilder;"),
                "legacy INVOKEVIRTUAL maximumSize(I) must be rewritten");
        assertTrue(containsCompatCall(calls, "maximumSize",
                        "(Lcom/google/common/cache/CacheBuilder;I)Lcom/google/common/cache/CacheBuilder;"),
                "rewrite must target GuavaCompat.maximumSize(CacheBuilder,int)");
    }

    @Test
    void leavesModernMaximumSizeLongCallAlone() {
        byte[] cls = emitMaximumSizeLongCaller();
        List<MethodCall> calls = collectMethodCalls(LegacyGuavaTransformer.transform(cls), "callIt");

        assertTrue(containsLegacyCall(calls,
                        "com/google/common/cache/CacheBuilder",
                        "maximumSize",
                        "(J)Lcom/google/common/cache/CacheBuilder;"),
                "modern long-typed call must be left untouched");
    }

    /** {@code static CacheBuilder callIt(CacheBuilder b) { return b.maximumSize(100); }} — int variant. */
    private static byte[] emitMaximumSizeCaller() {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "CallsMaximumSizeInt", null, "java/lang/Object", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "callIt",
                "(Lcom/google/common/cache/CacheBuilder;)Lcom/google/common/cache/CacheBuilder;",
                null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitIntInsn(Opcodes.BIPUSH, 100);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "com/google/common/cache/CacheBuilder",
                "maximumSize",
                "(I)Lcom/google/common/cache/CacheBuilder;",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    /** {@code static CacheBuilder callIt(CacheBuilder b) { return b.maximumSize(100L); }} — long variant. */
    private static byte[] emitMaximumSizeLongCaller() {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "CallsMaximumSizeLong", null, "java/lang/Object", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "callIt",
                "(Lcom/google/common/cache/CacheBuilder;)Lcom/google/common/cache/CacheBuilder;",
                null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitLdcInsn(100L);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "com/google/common/cache/CacheBuilder",
                "maximumSize",
                "(J)Lcom/google/common/cache/CacheBuilder;",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(3, 1);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    private record MethodCall(int opcode, String owner, String name, String descriptor) {}

    private static List<MethodCall> collectMethodCalls(byte[] cls, String inMethodNamed) {
        List<MethodCall> out = new ArrayList<>();
        new ClassReader(cls).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int a, String n, String d, String s, String[] e) {
                if (!inMethodNamed.equals(n)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int op, String o, String mn, String md, boolean iface) {
                        out.add(new MethodCall(op, o, mn, md));
                    }
                };
            }
        }, 0);
        return out;
    }

    private static boolean containsLegacyCall(List<MethodCall> calls, String owner, String name, String desc) {
        for (MethodCall c : calls) {
            if (c.opcode == Opcodes.INVOKEVIRTUAL
                    && c.owner.equals(owner)
                    && c.name.equals(name)
                    && c.descriptor.equals(desc)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsCompatCall(List<MethodCall> calls, String name, String desc) {
        for (MethodCall c : calls) {
            if (c.opcode == Opcodes.INVOKESTATIC
                    && c.owner.equals(GUAVA_COMPAT)
                    && c.name.equals(name)
                    && c.descriptor.equals(desc)) {
                return true;
            }
        }
        return false;
    }
}
