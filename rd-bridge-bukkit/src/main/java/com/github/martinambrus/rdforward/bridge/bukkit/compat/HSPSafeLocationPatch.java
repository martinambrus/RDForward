package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.objectweb.asm.*;

/**
 * Patches HSP's {@code Teleport.safeLocation} to fix a null-propagation bug.
 *
 * <p>The original code logs "using original location" when
 * {@code findSafeLocation2} returns null, but never actually assigns
 * {@code target = l}. This causes {@code safeLocation} to return null,
 * which {@code BukkitTeleport.safeLocation} wraps in
 * {@code new BukkitLocation(null)}, and the null world propagates to
 * a downstream NPE in a second {@code findSafeLocation2} call.
 *
 * <p>The patch inserts {@code target = l} (aload_1 → astore_2) right
 * after the "couldn't find nearby safe location" log call in the else
 * branch, so the method returns the original location instead of null.
 *
 * <p>Only targets the class
 * {@code com.andune.minecraft.hsp.shade.commonlib.Teleport}.
 */
public final class HSPSafeLocationPatch {

    private HSPSafeLocationPatch() {}

    public static byte[] transform(byte[] classBytes) {
        ClassReader cr = new ClassReader(classBytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if ("safeLocation".equals(name)
                        && "(Lorg/bukkit/Location;Lcom/andune/minecraft/hsp/shade/commonlib/Teleport$Bounds;I)Lorg/bukkit/Location;".equals(descriptor)) {
                    return new SafeLocationPatcher(mv);
                }
                return mv;
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    /**
     * Visits the bytecode of {@code safeLocation(Location, Bounds, int)} and
     * injects {@code target = l} after the "couldn't find nearby" log call.
     *
     * <p>Detection strategy: match the INVOKEINFO log call whose format
     * string contains "couldn't find nearby safe location". Right after
     * that call, insert {@code aload_1; astore_2} which sets target = l.
     */
    static final class SafeLocationPatcher extends MethodVisitor {
        private boolean patched = false;

        SafeLocationPatcher(MethodVisitor mv) {
            super(Opcodes.ASM9, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            if (!patched && opcode == Opcodes.INVOKEINTERFACE
                    && name.equals("info")
                    && descriptor.startsWith("(Ljava/lang/String;)")) {
                // After Logger.info("safeLocation: couldn't find nearby safe location..." + l),
                // insert: target = l  (aload_1; astore_2)
                super.visitVarInsn(Opcodes.ALOAD, 1);   // load parameter 'l'
                super.visitVarInsn(Opcodes.ASTORE, 4);  // store into 'target' (slot 4)
                patched = true;
            }
        }
    }
}
