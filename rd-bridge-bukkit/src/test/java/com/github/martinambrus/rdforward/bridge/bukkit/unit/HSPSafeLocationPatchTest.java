package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.compat.HSPSafeLocationPatch;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that {@link HSPSafeLocationPatch} injects {@code target = l}
 * after the "couldn't find nearby safe location" log call in
 * {@code Teleport.safeLocation(Location, Bounds, int)}.
 */
class HSPSafeLocationPatchTest {

    @Test
    void patchInjectsTargetAssignmentAfterInfoCall() {
        byte[] original = emitSafeLocation();
        byte[] patched = HSPSafeLocationPatch.transform(original);

        // Scan patched bytecode for the injected sequence:
        // ALOAD 1 (load parameter 'l') followed by ASTORE 4 (store into 'target')
        // right after an INVOKEINTERFACE Logger.info(String)
        boolean[] found = {false};
        ClassReader cr = new ClassReader(patched);
        int[] afterInfo = {0};
        cr.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                if ("safeLocation".equals(name)) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        boolean lastWasInfo = false;
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String mname,
                                                     String desc, boolean isInterface) {
                            lastWasInfo = opcode == Opcodes.INVOKEINTERFACE
                                    && "info".equals(mname)
                                    && desc.startsWith("(Ljava/lang/String;");
                        }
                        @Override
                        public void visitVarInsn(int opcode, int var) {
                            if (lastWasInfo
                                    && opcode == Opcodes.ALOAD && var == 1) {
                                // Next instruction should be ASTORE 4
                                afterInfo[0] = 1; // found ALOAD 1 after info
                            }
                            if (afterInfo[0] == 1
                                    && opcode == Opcodes.ASTORE && var == 4) {
                                found[0] = true;
                            }
                            lastWasInfo = false;
                        }
                    };
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        }, 0);

        assertTrue(found[0], "patched bytecode must contain ALOAD 1; ASTORE 4 after Logger.info call");
    }

    @Test
    void patchDoesNotCrashOnUnrelatedClass() {
        // The transformer should be a no-op for classes that don't have
        // a safeLocation method with the expected descriptor
        byte[] unrelated = emitUnrelatedClass();
        byte[] result = HSPSafeLocationPatch.transform(unrelated);
        // Should return valid class bytes (possibly identical)
        assertDoesNotThrow(() -> new ClassReader(result));
    }

    /** Emit a minimal Teleport-like class with a safeLocation method. */
    private static byte[] emitSafeLocation() {
        org.objectweb.asm.ClassWriter cw = new org.objectweb.asm.ClassWriter(
                org.objectweb.asm.ClassWriter.COMPUTE_FRAMES | org.objectweb.asm.ClassWriter.COMPUTE_MAXS);

        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC,
                "com/andune/minecraft/hsp/shade/commonlib/Teleport", null, "java/lang/Object", null);

        // Synthetic Logger field
        cw.visitField(Opcodes.ACC_PRIVATE, "log", "Lcom/andune/minecraft/hsp/shade/commonlib/Logger;", null, null);

        // Default constructor
        org.objectweb.asm.MethodVisitor ctor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();

        // safeLocation(Location, Bounds, int) matching the real method signature
        org.objectweb.asm.MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
                "safeLocation",
                "(Lorg/bukkit/Location;Lcom/andune/minecraft/hsp/shade/commonlib/Teleport$Bounds;I)Lorg/bukkit/Location;",
                null, null);
        mv.visitCode();

        // Simulate the else branch: Logger.info("safeLocation: couldn't find nearby...")
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD,
                "com/andune/minecraft/hsp/shade/commonlib/Teleport", "log",
                "Lcom/andune/minecraft/hsp/shade/commonlib/Logger;");
        mv.visitLdcInsn("safeLocation: couldn't find nearby safe location, using original location test");
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                "com/andune/minecraft/hsp/shade/commonlib/Logger", "info",
                "(Ljava/lang/String;)V", true);

        // Original code: just returns null (slot 4 = target, which was never set)
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    /** Emit a simple class with no safeLocation method. */
    private static byte[] emitUnrelatedClass() {
        org.objectweb.asm.ClassWriter cw = new org.objectweb.asm.ClassWriter(
                org.objectweb.asm.ClassWriter.COMPUTE_FRAMES | org.objectweb.asm.ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "test/Unrelated", null, "java/lang/Object", null);
        org.objectweb.asm.MethodVisitor ctor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }
}
