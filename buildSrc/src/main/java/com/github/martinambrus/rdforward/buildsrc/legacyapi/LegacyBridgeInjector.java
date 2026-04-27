package com.github.martinambrus.rdforward.buildsrc.legacyapi;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Patches a compiled interface .class file to add legacy-descriptor
 * variants of methods whose return types changed between Bukkit API
 * versions. The added methods are public default bridges that delegate
 * to the existing modern method via {@code invokeinterface} on
 * {@code this} and convert the return value.
 *
 * <p>Idempotent: re-running on an already-patched class is a no-op
 * (the visitor skips methods whose name+descriptor already exist).
 *
 * <p>Invoked from each module's Gradle build that owns a class needing
 * bridging.
 */
public final class LegacyBridgeInjector {

    private LegacyBridgeInjector() {}

    /**
     * Patch {@code classFile} (an interface .class) to add every bridge
     * in {@code bridges}.
     *
     * @param classFile         path to the .class on disk; rewritten in place
     * @param ownerInternalName the interface's internal name
     *                          ({@code "org/bukkit/Server"})
     * @param bridges           bridges to add; existing methods with the
     *                          same name+descriptor are left alone
     */
    public static void inject(Path classFile, String ownerInternalName,
                              List<BridgeSpec> bridges) throws IOException {
        byte[] bytes = Files.readAllBytes(classFile);
        ClassReader reader = new ClassReader(bytes);
        // COMPUTE_FRAMES asks ASM to recompute StackMapTable + maxStack
        // + maxLocals for every method we emit. Required because we
        // emit method bodies without specifying frame info ourselves;
        // Java 8+ class files reject methods missing the
        // StackMapTable, and {@code visitMaxs(0,0)} is a placeholder
        // for ASM to fill in.
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

        BridgeAddingVisitor visitor =
                new BridgeAddingVisitor(writer, ownerInternalName, bridges);
        reader.accept(visitor, 0);

        Files.write(classFile, writer.toByteArray());
    }

    private static final class BridgeAddingVisitor extends ClassVisitor {
        private final String ownerInternalName;
        private final List<BridgeSpec> bridges;
        /** Tracks methods already present (by name+descriptor) so the
         *  injector is idempotent and can't shadow an existing real
         *  declaration. */
        private final java.util.Set<String> present = new java.util.HashSet<>();

        BridgeAddingVisitor(ClassVisitor downstream, String ownerInternalName,
                            List<BridgeSpec> bridges) {
            super(Opcodes.ASM9, downstream);
            this.ownerInternalName = ownerInternalName;
            this.bridges = bridges;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            present.add(name + descriptor);
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            for (BridgeSpec spec : bridges) {
                String legacyKey = spec.methodName() + spec.legacyDescriptor();
                if (present.contains(legacyKey)) continue;
                emitBridge(spec);
            }
            super.visitEnd();
        }

        private void emitBridge(BridgeSpec spec) {
            // Public default method on an interface.
            MethodVisitor mv = super.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    spec.methodName(), spec.legacyDescriptor(),
                    null, null);
            mv.visitCode();

            switch (spec.kind()) {
                case COLLECTION_TO_ARRAY -> emitCollectionToArray(mv, spec);
            }

            mv.visitMaxs(0, 0); // ClassWriter computes
            mv.visitEnd();
        }

        /**
         * Body for {@code legacyMethod()[Lelement;}:
         * <pre>
         *   return this.modernMethod().toArray(new Element[0]);
         * </pre>
         */
        private void emitCollectionToArray(MethodVisitor mv, BridgeSpec spec) {
            // ALOAD 0   (this)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            // INVOKEINTERFACE owner.modernMethod modernDescriptor
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                    ownerInternalName, spec.methodName(), spec.modernDescriptor(),
                    /*itf*/ true);
            // ICONST_0, ANEWARRAY element  (new Element[0])
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, spec.elementInternalName());
            // INVOKEINTERFACE Collection.toArray ([Ljava/lang/Object;)[Ljava/lang/Object;
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                    "java/util/Collection", "toArray",
                    "([Ljava/lang/Object;)[Ljava/lang/Object;",
                    /*itf*/ true);
            // CHECKCAST [Lelement;
            mv.visitTypeInsn(Opcodes.CHECKCAST, "[L" + spec.elementInternalName() + ";");
            // ARETURN
            mv.visitInsn(Opcodes.ARETURN);
        }
    }
}
