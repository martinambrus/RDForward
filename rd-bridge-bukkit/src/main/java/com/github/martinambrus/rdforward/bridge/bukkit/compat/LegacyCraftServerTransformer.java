package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Rewrites references to {@code org.bukkit.craftbukkit.v1_4_R1.CraftServer}
 * to {@code org.bukkit.craftbukkit.v1_21_R1.CraftServer} in plugin classes.
 *
 * HomeSpawnPlus 1.7.4 hard-casts {@code Bukkit.getServer()} to the v1_4_R1
 * type to access the command map. RDForward's server instance is the
 * v1_21_R1 variant; rewriting constant pool + bytecode lets the cast succeed.
 */
public final class LegacyCraftServerTransformer {

    private static final String OLD = "org/bukkit/craftbukkit/v1_4_R1/CraftServer";
    private static final String NEW = "org/bukkit/craftbukkit/v1_21_R1/CraftServer";

    public static byte[] transform(byte[] classBytes) {
        // Quick scan: skip if no reference to the old class
        String asString = new String(classBytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        if (!asString.contains("v1_4_R1")) return classBytes;

        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new RewritingVisitor(writer), 0);
        return writer.toByteArray();
    }

    private static String rw(String s) {
        return s != null && s.indexOf("v1_4_R1") >= 0
                ? s.replace("v1_4_R1", "v1_21_R1") : s;
    }

    private static String[] rwArr(String[] a) {
        if (a == null) return null;
        String[] out = new String[a.length];
        for (int i = 0; i < a.length; i++) out[i] = rw(a[i]);
        return out;
    }

    private static class RewritingVisitor extends ClassVisitor {
        RewritingVisitor(ClassVisitor cv) { super(Opcodes.ASM9, cv); }

        @Override
        public void visit(int v, int access, String name, String sig,
                          String superName, String[] ifaces) {
            super.visit(v, access, name, sig, rw(superName), rwArr(ifaces));
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String sig, String[] exceptions) {
            return new MethodRewriter(super.visitMethod(access, name, rwDesc(desc), sig, exceptions));
        }

        @Override
        public void visitInnerClass(String name, String outer, String inner, int access) {
            super.visitInnerClass(rw(name), rw(outer), inner, access);
        }
    }

    /** Rewrites type references inside method bytecode (checkcast, new, invokes, etc). */
    private static class MethodRewriter extends MethodVisitor {
        MethodRewriter(MethodVisitor mv) { super(Opcodes.ASM9, mv); }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            super.visitTypeInsn(opcode, rw(type));
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, rw(owner), name, rwDesc(desc));
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name,
                                    String desc, boolean itf) {
            super.visitMethodInsn(opcode, rw(owner), name, rwDesc(desc), itf);
        }

        @Override
        public void visitLdcInsn(Object value) {
            if (value instanceof org.objectweb.asm.Type) {
                org.objectweb.asm.Type t = (org.objectweb.asm.Type) value;
                super.visitLdcInsn(rwType(t));
            } else {
                super.visitLdcInsn(value);
            }
        }
    }

    /** Rewrite descriptors (method and field) — replaces internal class refs. */
    private static String rwDesc(String desc) {
        return desc != null && desc.indexOf("v1_4_R1") >= 0
                ? desc.replace("v1_4_R1", "v1_21_R1") : desc;
    }

    private static org.objectweb.asm.Type rwType(org.objectweb.asm.Type t) {
        if (t.getSort() == org.objectweb.asm.Type.OBJECT
                || t.getSort() == org.objectweb.asm.Type.ARRAY) {
            String internal = t.getInternalName();
            if (internal.indexOf("v1_4_R1") >= 0) {
                return org.objectweb.asm.Type.getObjectType(internal.replace("v1_4_R1", "v1_21_R1"));
            }
        }
        return t;
    }
}
