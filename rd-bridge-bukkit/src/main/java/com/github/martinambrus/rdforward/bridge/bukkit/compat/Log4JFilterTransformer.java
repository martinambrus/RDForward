// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Patches plugin classes that implement {@code org.apache.logging.log4j.core.Filter}
 * directly (without extending {@code AbstractFilter}) and therefore lack the
 * {@code LifeCycle} methods ({@code start}, {@code stop}, {@code isStarted},
 * {@code isStopped}). AuthMe's {@code Log4JFilter} is the known offender — it
 * compiled against an older Log4j2 where those methods had default implementations,
 * but Log4j2 2.25.x made them abstract.
 *
 * <p>The transformer adds four trivially-correct methods:
 * <ul>
 *   <li>{@code start()} — no-op</li>
 *   <li>{@code stop()} — no-op</li>
 *   <li>{@code isStarted()} — returns {@code true}</li>
 *   <li>{@code isStopped()} — returns {@code false}</li>
 * </ul>
 *
 * <p>Only classes that directly implement {@code Filter} AND are missing at least
 * one of the four methods are touched. All other classes pass through unchanged.
 */
public final class Log4JFilterTransformer {

    private static final String FILTER_INTERNAL = "org/apache/logging/log4j/core/Filter";

    private Log4JFilterTransformer() {}

    /** Rewrite {@code classBytes} if it is a Filter impl missing LifeCycle methods. */
    public static byte[] transform(byte[] classBytes) {
        ClassReader cr = new ClassReader(classBytes);
        // Quick reject: only classes that implement Filter directly
        boolean implementsFilter = false;
        for (String iface : cr.getInterfaces()) {
            if (FILTER_INTERNAL.equals(iface)) {
                implementsFilter = true;
                break;
            }
        }
        if (!implementsFilter) return classBytes;

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        PatchingVisitor pv = new PatchingVisitor(cw);
        cr.accept(pv, 0);
        return pv.needsPatch() ? cw.toByteArray() : classBytes;
    }

    private static final class PatchingVisitor extends ClassVisitor {
        private boolean hasStart;
        private boolean hasStop;
        private boolean hasIsStarted;
        private boolean hasIsStopped;
        private String className;

        PatchingVisitor(ClassVisitor cv) {
            super(Opcodes.ASM9, cv);
        }

        boolean needsPatch() {
            return !hasStart || !hasStop || !hasIsStarted || !hasIsStopped;
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            this.className = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            if ("start".equals(name) && "()V".equals(descriptor)) hasStart = true;
            if ("stop".equals(name) && "()V".equals(descriptor)) hasStop = true;
            if ("isStarted".equals(name) && "()Z".equals(descriptor)) hasIsStarted = true;
            if ("isStopped".equals(name) && "()Z".equals(descriptor)) hasIsStopped = true;
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            if (!hasStart) {
                MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "start", "()V", null, null);
                mv.visitCode();
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 1);
                mv.visitEnd();
            }
            if (!hasStop) {
                MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "stop", "()V", null, null);
                mv.visitCode();
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 1);
                mv.visitEnd();
            }
            if (!hasIsStarted) {
                MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "isStarted", "()Z", null, null);
                mv.visitCode();
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitInsn(Opcodes.IRETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            if (!hasIsStopped) {
                MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "isStopped", "()Z", null, null);
                mv.visitCode();
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitInsn(Opcodes.IRETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            super.visitEnd();
        }
    }
}
