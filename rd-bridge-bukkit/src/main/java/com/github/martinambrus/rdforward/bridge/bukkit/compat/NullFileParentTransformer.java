package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Bytecode rewriter that redirects null-parent {@link java.io.File} constructor
 * calls in plugin classes through {@link BridgeFiles#resolve(String, java.io.File)}.
 *
 * <p>Intercepts the two File constructor overloads where a null parent is
 * meaningful:
 * <ul>
 *   <li>{@code new File(File parent, String child)}</li>
 *   <li>{@code new File(String parent, String child)}</li>
 * </ul>
 *
 * <p>After each matched {@code INVOKESPECIAL File.<init>} call, appends:
 * <pre>
 *   LDC pluginDir       // push the plugin's data directory path
 *   SWAP                // stack: pluginDir, File
 *   INVOKESTATIC BridgeFiles.resolve(String, File)File
 * </pre>
 *
 * <p>At runtime, {@link BridgeFiles#resolve} checks whether the File has a null
 * parent and redirects bare filenames to the plugin's data directory. Files with
 * existing parent directories pass through unchanged — the overhead is a single
 * {@link java.io.File#getParentFile()} null check.
 *
 * <p>Conservative scope: only the two two-arg File constructors are intercepted.
 * {@code File(String)}, {@code File(URI)}, and {@code File(File)} are left alone.
 */
public final class NullFileParentTransformer {

    private static final String FILE_CLASS = "java/io/File";
    private static final String INIT = "<init>";
    private static final String CTOR_FILE_STRING = "(Ljava/io/File;Ljava/lang/String;)V";
    private static final String CTOR_STRING_STRING = "(Ljava/lang/String;Ljava/lang/String;)V";
    private static final String BRIDGE =
            "com/github/martinambrus/rdforward/bridge/bukkit/compat/BridgeFiles";
    private static final String BRIDGE_METHOD = "resolve";
    private static final String BRIDGE_DESC =
            "(Ljava/lang/String;Ljava/io/File;)Ljava/io/File;";

    private NullFileParentTransformer() {}

    /**
     * Rewrite {@code original} class bytes so that every {@code new File(File, String)}
     * and {@code new File(String, String)} call is wrapped through
     * {@link BridgeFiles#resolve(String, java.io.File)}.
     *
     * @param original  raw class bytes from the plugin jar
     * @param pluginDir the plugin's data directory path (e.g. {@code "plugins/HomeSpawnPlus"});
     *                  if {@code null}, the original bytes are returned unchanged
     * @return transformed class bytes, or the original if no rewrites were needed
     */
    public static byte[] transform(byte[] original, String pluginDir) {
        if (pluginDir == null) return original;
        ClassReader reader = new ClassReader(original);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        reader.accept(new RewriterAdapter(writer, pluginDir), 0);
        return writer.toByteArray();
    }

    private static final class RewriterAdapter extends ClassVisitor {
        private final String pluginDir;

        RewriterAdapter(ClassVisitor delegate, String pluginDir) {
            super(Opcodes.ASM9, delegate);
            this.pluginDir = pluginDir;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            return mv == null ? null : new FileCtorRewriter(mv, pluginDir);
        }
    }

    private static final class FileCtorRewriter extends MethodVisitor {
        private final String pluginDir;

        FileCtorRewriter(MethodVisitor mv, String pluginDir) {
            super(Opcodes.ASM9, mv);
            this.pluginDir = pluginDir;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
                                    boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            if (opcode == Opcodes.INVOKESPECIAL
                    && FILE_CLASS.equals(owner)
                    && INIT.equals(name)
                    && (CTOR_FILE_STRING.equals(descriptor)
                        || CTOR_STRING_STRING.equals(descriptor))) {
                // Stack after INVOKESPECIAL: ..., File
                // Append: LDC pluginDir, SWAP, INVOKESTATIC BridgeFiles.resolve
                super.visitLdcInsn(pluginDir);                // ..., File, String
                super.visitInsn(Opcodes.SWAP);                // ..., String, File
                super.visitMethodInsn(Opcodes.INVOKESTATIC, BRIDGE, BRIDGE_METHOD,
                        BRIDGE_DESC, false);          // ..., File
            }
        }
    }
}
