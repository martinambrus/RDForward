package com.github.martinambrus.rdforward.codegen;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts an ASM {@link ClassNode} into Java stub source. Output
 * follows the Hybrid Semantics Contract (see {@code PLAN-FULL-STUBS.md}
 * §2): classifier picks a body shape per method, resolver picks a
 * default return expression for non-void methods.
 *
 * <p>Scope limits in the first pass:
 *
 * <ul>
 *   <li>Skips enum, annotation, and synthetic class files.</li>
 *   <li>Emits raw types (erases generics).</li>
 *   <li>Emits public and protected members only; package-private and
 *       private are stripped.</li>
 *   <li>Skips methods flagged {@code ACC_BRIDGE} or
 *       {@code ACC_SYNTHETIC}, and methods annotated
 *       {@code @Deprecated(forRemoval = true)}.</li>
 *   <li>Fields emit only if {@code public}; static final literal
 *       initializers (primitives, Strings) carry their upstream value,
 *       others use the resolver's default expression.</li>
 *   <li>Inner classes emit as separate top-level files whose name
 *       retains the {@code $} separator — {@code Foo$Bar} becomes
 *       {@code Foo$Bar.java} with {@code public class Foo$Bar {...}},
 *       which is legal Java and matches the binary layout plugin code
 *       expects at link time.</li>
 * </ul>
 */
public final class StubEmitter {

    static final String PRESERVE_MARKER = "// @rdforward:preserve";

    static final String STUB_CALL_LOG_FQN =
            "com.github.martinambrus.rdforward.api.stub.StubCallLog";

    /**
     * Supertypes that cannot be extended from our emitted stubs because
     * they are package-private or protected nested classes in libraries
     * outside our generator allowlist. For these, the emitter drops the
     * extends clause and leaves the stub as a plain {@code Object}
     * descendant — still link-safe for plugin code that only references
     * the outer type.
     */
    private static final Set<String> INACCESSIBLE_SUPERS = Set.of(
            "org/yaml/snakeyaml/representer/SafeRepresenter$RepresentMap",
            "org/yaml/snakeyaml/constructor/SafeConstructor$ConstructYamlNull",
            "org/yaml/snakeyaml/constructor/AbstractConstruct");

    private final DefaultValueResolver resolver;
    private final String upstreamArtifact;
    private final Map<String, ClassNode> classIndex;

    public StubEmitter(DefaultValueResolver resolver, String upstreamArtifact) {
        this(resolver, upstreamArtifact, Map.of());
    }

    public StubEmitter(DefaultValueResolver resolver, String upstreamArtifact,
                       Map<String, ClassNode> classIndex) {
        this.resolver = resolver;
        this.upstreamArtifact = upstreamArtifact;
        this.classIndex = classIndex;
    }

    /**
     * Produce the Java source for {@code node}, or {@link Optional#empty()}
     * if the node is skipped by the current scope rules (enum, annotation,
     * or synthetic).
     */
    public Optional<String> emit(ClassNode node) {
        if (shouldSkipClass(node)) return Optional.empty();
        StringBuilder sb = new StringBuilder();
        emitHeader(sb, node);
        emitDeclaration(sb, node);
        sb.append(" {\n");
        boolean isAnnotation = (node.access & Opcodes.ACC_ANNOTATION) != 0;
        boolean isRecord = "java/lang/Record".equals(node.superName);
        if (isAnnotation) {
            emitAnnotationMembers(sb, node);
        } else {
            if ((node.access & Opcodes.ACC_ENUM) != 0) {
                emitEnumConstants(sb, node);
            }
            // For records, skip fields/ctors (implicit from components) but
            // still emit methods so explicit interface impls (`test(...)`,
            // `asComponent()`, etc.) are carried through.
            if (!isRecord) {
                emitFields(sb, node);
                emitConstructors(sb, node);
            }
            emitMethods(sb, node);
        }
        sb.append("}\n");
        return Optional.of(sb.toString());
    }

    /**
     * Emit annotation element methods for an {@code @interface}. Each
     * element is an abstract, no-arg method with a return type — and
     * optionally a {@code default} clause when the bytecode carries an
     * {@link MethodNode#annotationDefault}. Synthesising the default
     * keeps the emitted {@code @interface} compilable when downstream
     * code applies the annotation without specifying every element.
     */
    private void emitAnnotationMembers(StringBuilder sb, ClassNode node) {
        if (node.methods == null) return;
        for (MethodNode m : node.methods) {
            if ((m.access & Opcodes.ACC_SYNTHETIC) != 0) continue;
            if ("<clinit>".equals(m.name) || "<init>".equals(m.name)) continue;
            Type ret = Type.getReturnType(m.desc);
            sb.append("    ").append(toSourceName(ret)).append(' ').append(m.name).append("()");
            if (m.annotationDefault != null) {
                String literal = annotationDefaultLiteral(m.annotationDefault, ret);
                if (literal != null) sb.append(" default ").append(literal);
            }
            sb.append(";\n");
        }
    }

    /**
     * Render an annotation element default. Only the shapes paper-api
     * actually uses are supported; anything exotic falls back to the
     * type's zero value via {@link DefaultValueResolver}.
     */
    private String annotationDefaultLiteral(Object v, Type ret) {
        if (v instanceof String s) return "\"" + escape(s) + "\"";
        if (v instanceof Boolean b) return b.toString();
        if (v instanceof Character c) return "'" + c + "'";
        if (v instanceof Number n) {
            return switch (ret.getSort()) {
                case Type.LONG -> n.longValue() + "L";
                case Type.FLOAT -> n.floatValue() + "f";
                case Type.DOUBLE -> String.valueOf(n.doubleValue());
                case Type.BYTE -> "(byte) " + n.intValue();
                case Type.SHORT -> "(short) " + n.intValue();
                case Type.CHAR -> "(char) " + n.intValue();
                default -> String.valueOf(n.intValue());
            };
        }
        // ASM represents an enum-constant annotation default as
        // String[] { enumTypeDescriptor, constantName }. Emit as
        // FullyQualifiedEnumName.CONSTANT so the default resolves at
        // compile time.
        if (v instanceof String[] pair && pair.length == 2) {
            return Type.getType(pair[0]).getClassName() + "." + pair[1];
        }
        // Class reference defaults ({@code Class<?> c() default Foo.class}):
        // ASM exposes these as asm.Type for the referenced class.
        if (v instanceof Type t) {
            return toSourceName(t) + ".class";
        }
        // Array defaults are lists — render as { a, b, c } with the
        // array's component type driving per-element rendering.
        if (v instanceof List<?> items) {
            Type component = ret.getSort() == Type.ARRAY
                    ? ret.getElementType()
                    : ret;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Object item : items) {
                if (!first) sb.append(", ");
                first = false;
                sb.append(annotationDefaultLiteral(item, component));
            }
            sb.append('}');
            return sb.toString();
        }
        // Fallback: drop the default clause entirely. Returning the
        // resolver's zero expression would emit `null` for reference
        // types, which javac rejects as an annotation default.
        return null;
    }

    private void emitEnumConstants(StringBuilder sb, ClassNode node) {
        if (node.fields == null) return;
        List<String> names = new ArrayList<>();
        for (FieldNode f : node.fields) {
            if ((f.access & Opcodes.ACC_ENUM) == 0) continue;
            names.add(f.name);
        }
        if (names.isEmpty()) {
            sb.append("    ;\n");
            return;
        }
        sb.append("    ").append(String.join(", ", names)).append(";\n");
    }

    static boolean shouldSkipClass(ClassNode node) {
        int a = node.access;
        if ((a & Opcodes.ACC_SYNTHETIC) != 0) return true;
        if (isAnonymousInner(node.name)) return true;
        return false;
    }

    /**
     * Detect anonymous/local inner classes emitted by javac — they
     * appear as {@code Outer$1}, {@code Outer$2}, or
     * {@code Outer$MyClass$1} when an enum constant overrides methods
     * in its body. These are compiler artifacts with no public API
     * surface and must not be emitted as stand-alone source files.
     */
    static boolean isAnonymousInner(String internalName) {
        int dollar = internalName.lastIndexOf('$');
        if (dollar < 0) return false;
        String tail = internalName.substring(dollar + 1);
        if (tail.isEmpty()) return false;
        for (int i = 0; i < tail.length(); i++) {
            if (!Character.isDigit(tail.charAt(i))) return false;
        }
        return true;
    }

    private void emitHeader(StringBuilder sb, ClassNode node) {
        String pkg = packageOf(node.name);
        if (!pkg.isEmpty()) {
            sb.append("package ").append(pkg).append(";\n\n");
        }
        sb.append("/** Auto-generated stub from ")
                .append(upstreamArtifact)
                .append(". See PLAN-FULL-STUBS.md. */\n");
        sb.append("@SuppressWarnings({\"unchecked\", \"rawtypes\", \"unused\"})\n");
    }

    private void emitDeclaration(StringBuilder sb, ClassNode node) {
        int a = node.access;
        boolean isInterface = (a & Opcodes.ACC_INTERFACE) != 0;
        boolean isEnum = (a & Opcodes.ACC_ENUM) != 0;
        boolean isAnnotation = (a & Opcodes.ACC_ANNOTATION) != 0;
        boolean isRecord = node.superName != null
                && "java/lang/Record".equals(node.superName);
        if ((a & Opcodes.ACC_PUBLIC) != 0) sb.append("public ");
        // Intentionally omit `final`: paper-api sometimes marks a class final
        // yet ships subclasses in the same jar (deprecated shims), so keeping
        // the modifier would reject valid upstream input during regeneration.
        // Emit `abstract` for concrete classes that bytecode marks abstract —
        // without it the stub would need to implement every abstract method
        // inherited from interfaces that the real (abstract) upstream leaves
        // unimplemented, which our emitter cannot synthesize safely.
        if (!isInterface && !isEnum && !isAnnotation && !isRecord
                && (a & Opcodes.ACC_ABSTRACT) != 0) {
            sb.append("abstract ");
        }
        if (isAnnotation) sb.append("@interface ");
        else if (isEnum) sb.append("enum ");
        else if (isRecord) sb.append("record ");
        else sb.append(isInterface ? "interface " : "class ");
        sb.append(simpleNameOf(node.name));

        if (isRecord) {
            sb.append('(').append(recordComponentList(node)).append(')');
        }

        if (!isInterface && !isEnum && !isRecord && !isAnnotation
                && node.superName != null
                && !"java/lang/Object".equals(node.superName)
                && !INACCESSIBLE_SUPERS.contains(node.superName)) {
            sb.append(" extends ").append(toSourceName(node.superName));
        }

        // Annotations implicitly extend java.lang.annotation.Annotation and
        // cannot declare `extends` in source — skip the interfaces clause
        // entirely for them.
        if (isAnnotation) return;
        List<String> ifaces = node.interfaces == null ? List.of() : node.interfaces;
        if (!ifaces.isEmpty()) {
            sb.append(isInterface ? " extends " : " implements ");
            sb.append(ifaces.stream().map(this::toSourceName)
                    .collect(Collectors.joining(", ")));
        }
    }

    private String recordComponentList(ClassNode node) {
        if (node.recordComponents == null || node.recordComponents.isEmpty()) return "";
        List<String> parts = new ArrayList<>(node.recordComponents.size());
        for (var rc : node.recordComponents) {
            Type t = Type.getType(rc.descriptor);
            parts.add(toSourceName(t) + " " + rc.name);
        }
        return String.join(", ", parts);
    }

    private void emitFields(StringBuilder sb, ClassNode node) {
        if (node.fields == null) return;
        for (FieldNode f : node.fields) {
            if ((f.access & Opcodes.ACC_PUBLIC) == 0) continue;
            if ((f.access & Opcodes.ACC_SYNTHETIC) != 0) continue;
            if ((f.access & Opcodes.ACC_ENUM) != 0) continue;
            sb.append("    public ");
            if ((f.access & Opcodes.ACC_STATIC) != 0) sb.append("static ");
            if ((f.access & Opcodes.ACC_FINAL) != 0) sb.append("final ");
            Type ft = Type.getType(f.desc);
            sb.append(toSourceName(ft)).append(' ').append(f.name).append(" = ");
            sb.append(fieldInitializer(f, ft));
            sb.append(";\n");
        }
    }

    private String fieldInitializer(FieldNode f, Type ft) {
        if (f.value != null) {
            return literal(f.value, ft);
        }
        return resolver.defaultExpression(ft);
    }

    private static String literal(Object v, Type ft) {
        if (v instanceof String s) return "\"" + escape(s) + "\"";
        if (v instanceof Integer i) {
            return switch (ft.getSort()) {
                case Type.BOOLEAN -> (i != 0) ? "true" : "false";
                case Type.BYTE -> "(byte) " + i;
                case Type.SHORT -> "(short) " + i;
                case Type.CHAR -> "(char) " + i;
                default -> i.toString();
            };
        }
        if (v instanceof Long l) return l + "L";
        if (v instanceof Float f) return f + "f";
        if (v instanceof Double d) return d.toString();
        return String.valueOf(v);
    }

    private static String escape(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    private void emitConstructors(StringBuilder sb, ClassNode node) {
        if (node.methods == null) return;
        boolean isInterface = (node.access & Opcodes.ACC_INTERFACE) != 0;
        boolean isEnum = (node.access & Opcodes.ACC_ENUM) != 0;
        if (isInterface || isEnum) return;
        String superCall = resolveSuperCall(node);
        boolean sawNoArg = false;
        for (MethodNode m : node.methods) {
            if (!"<init>".equals(m.name)) continue;
            if (!isEmittableAccess(m.access)) continue;
            if (isSkippedMethod(m)) continue;
            if ("()V".equals(m.desc)) sawNoArg = true;
            sb.append("    ");
            if ((m.access & Opcodes.ACC_PUBLIC) != 0) sb.append("public ");
            else sb.append("protected ");
            sb.append(simpleNameOf(node.name));
            sb.append('(').append(paramList(m.desc)).append(')');
            appendThrows(sb, m.exceptions);
            if (superCall.isEmpty()) {
                sb.append(" {}\n");
            } else {
                sb.append(" { ").append(superCall).append(" }\n");
            }
        }
        // Always synthesize a no-arg constructor unless the class already
        // declares one. Plugin code that reflectively invokes
        // {@code Class.getDeclaredConstructor()} depends on this being
        // present, and both subclass stubs and the class itself need an
        // anchor for the implicit {@code super()} that javac would
        // otherwise insert. Abstract classes are included: javac still
        // emits a default constructor for them if none are declared, and
        // any such default must chain to the actual parent ctor.
        if (!sawNoArg) {
            boolean isAbstract = (node.access & Opcodes.ACC_ABSTRACT) != 0;
            sb.append("    ")
                    .append(isAbstract ? "protected " : "public ")
                    .append(simpleNameOf(node.name)).append("()");
            if (superCall.isEmpty()) {
                sb.append(" {}\n");
            } else {
                sb.append(" { ").append(superCall).append(" }\n");
            }
        }
    }

    /**
     * Returns the explicit {@code super(...)} call required at the top
     * of every constructor body of this class, or {@code ""} if the
     * super class has an accessible no-arg constructor (in which case
     * the implicit {@code super()} is fine). Returning a synthesized
     * call lets emitted subclass stubs compile against generated parent
     * stubs whose own bytecode constructors take parameters.
     */
    private String resolveSuperCall(ClassNode node) {
        if (node.superName == null) return "";
        if ("java/lang/Object".equals(node.superName)) return "";
        if (INACCESSIBLE_SUPERS.contains(node.superName)) return "";
        ClassNode parent = classIndex.get(node.superName);
        if (parent != null) {
            MethodNode noArg = findCtor(parent, true);
            if (noArg != null) return "";
            MethodNode anyCtor = findCtor(parent, false);
            if (anyCtor == null) return "";
            Type[] superArgs = Type.getArgumentTypes(anyCtor.desc);
            return formatSuperCall(superArgs);
        }
        return resolveJdkSuperCall(node.superName);
    }

    /**
     * Fallback for super classes not in the scanned classIndex — JDK
     * types like {@code java.util.logging.Logger} or
     * {@code java.net.URLClassLoader}. Uses reflection to find an
     * accessible no-arg ctor first, else the public/protected ctor with
     * fewest parameters. Returns {@code ""} if the JDK class has an
     * accessible no-arg constructor.
     */
    private String resolveJdkSuperCall(String superInternalName) {
        try {
            Class<?> sup = Class.forName(superInternalName.replace('/', '.'));
            java.lang.reflect.Constructor<?> best = null;
            for (java.lang.reflect.Constructor<?> c : sup.getDeclaredConstructors()) {
                int mods = c.getModifiers();
                boolean accessible = java.lang.reflect.Modifier.isPublic(mods)
                        || java.lang.reflect.Modifier.isProtected(mods);
                if (!accessible) continue;
                if (c.getParameterCount() == 0) return "";
                if (best == null
                        || c.getParameterCount() < best.getParameterCount()) {
                    best = c;
                }
            }
            if (best == null) return "";
            Class<?>[] params = best.getParameterTypes();
            Type[] superArgs = new Type[params.length];
            for (int i = 0; i < params.length; i++) {
                superArgs[i] = Type.getType(params[i]);
            }
            return formatSuperCall(superArgs);
        } catch (Throwable t) {
            return "";
        }
    }

    private String formatSuperCall(Type[] superArgs) {
        List<String> defaults = new ArrayList<>(superArgs.length);
        for (Type t : superArgs) defaults.add(typedDefault(t));
        return "super(" + String.join(", ", defaults) + ");";
    }

    /**
     * Return the default expression for {@code t} with an explicit cast
     * when the value is {@code null}, so that overload resolution picks
     * the intended super constructor. Primitive defaults already carry
     * their type via the literal itself, but {@code null} is ambiguous
     * between every reference type.
     */
    private String typedDefault(Type t) {
        String expr = resolver.defaultExpression(t);
        if ("null".equals(expr)) {
            return "(" + toSourceName(t) + ") null";
        }
        return expr;
    }

    private static MethodNode findCtor(ClassNode parent, boolean noArgOnly) {
        if (parent.methods == null) return null;
        for (MethodNode m : parent.methods) {
            if (!"<init>".equals(m.name)) continue;
            if (!isEmittableAccess(m.access)) continue;
            // Must match the same filter emitConstructors applies, otherwise
            // we synthesize a super(...) call referencing a ctor that was
            // skipped during emission of the parent stub.
            if (isSkippedMethod(m)) continue;
            if (isForRemoval(m)) continue;
            boolean isNoArg = "()V".equals(m.desc);
            if (noArgOnly ? isNoArg : true) return m;
        }
        return null;
    }

    private void emitMethods(StringBuilder sb, ClassNode node) {
        if (node.methods == null) return;
        boolean isInterface = (node.access & Opcodes.ACC_INTERFACE) != 0;
        boolean isEnum = (node.access & Opcodes.ACC_ENUM) != 0;
        boolean isRecord = "java/lang/Record".equals(node.superName);
        Set<String> recordComponentNames = recordComponentNames(node);

        // Group candidates by (name + param descriptor). ASM includes both
        // the real method and compiler-generated bridge methods (one per
        // covariant super type). In raw-types stub source these all
        // collapse to the same Java signature, so emit only one copy —
        // but picking the right one is non-trivial because the real
        // method's return type may be incompatible with what our
        // (already raw-erased) super class declares.
        Map<String, List<MethodNode>> grouped = new LinkedHashMap<>();
        for (MethodNode m : node.methods) {
            if ("<init>".equals(m.name) || "<clinit>".equals(m.name)) continue;
            if (!isEmittableAccess(m.access)) continue;
            if (isSkippedMethod(m)) continue;
            if (isEnum && isImplicitEnumMethod(node, m)) continue;
            if (isRecord && isImplicitRecordMethod(m, recordComponentNames)) continue;
            String key = m.name + "(" + paramDesc(m.desc) + ")";
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
        }

        List<MethodNode> chosen = new ArrayList<>(grouped.size());
        for (Map.Entry<String, List<MethodNode>> e : grouped.entrySet()) {
            chosen.add(pickBestOverload(node, e.getValue()));
        }

        for (MethodNode m : chosen) {
            StubSemantics sem = MethodClassifier.classify(node, m);
            if (sem == StubSemantics.STATIC_INITIALIZER) continue;
            emitMethod(sb, node, m, sem, isInterface);
        }
    }

    /**
     * Choose which of several same-(name+params) methods to emit. When
     * javac compiles a class with covariant overrides it writes the real
     * method plus a bridge per inherited parent return type — all with
     * the same Java signature after erasure. Strategy:
     *
     * <ol>
     *   <li>If one candidate's return is a subtype of every other
     *       candidate's, pick it — one return satisfies every covariant
     *       slot at once (handles {@code BuildableComponent.toBuilder},
     *       {@code clone()} returning a subtype of {@code Object}).</li>
     *   <li>Otherwise match the return declared on the nearest
     *       inherited method in {@link #classIndex} — needed when the
     *       real return is an intersection bound the raw-types stub
     *       can't express (e.g. {@code SimpleRegistry.get} returning
     *       {@code T extends Enum & Keyed}: bytecode erases to
     *       {@code Enum} but the stub must emit {@code Keyed} to
     *       override {@code Registry.get}).</li>
     *   <li>Otherwise prefer the non-bridge so the specific return
     *       carries useful API info.</li>
     * </ol>
     */
    private MethodNode pickBestOverload(ClassNode owner, List<MethodNode> candidates) {
        if (candidates.size() == 1) return candidates.get(0);
        for (MethodNode cand : candidates) {
            Type candRet = Type.getReturnType(cand.desc);
            boolean dominates = true;
            for (MethodNode other : candidates) {
                if (other == cand) continue;
                Type otherRet = Type.getReturnType(other.desc);
                if (!isSubtypeOrEqual(candRet, otherRet)) {
                    dominates = false;
                    break;
                }
            }
            if (dominates) return cand;
        }
        String inheritedRet = findInheritedReturn(
                owner, candidates.get(0).name, paramDesc(candidates.get(0).desc),
                new java.util.HashSet<>());
        if (inheritedRet != null) {
            for (MethodNode m : candidates) {
                if (Type.getReturnType(m.desc).getDescriptor().equals(inheritedRet)) {
                    return m;
                }
            }
        }
        for (MethodNode m : candidates) {
            if ((m.access & Opcodes.ACC_BRIDGE) == 0) return m;
        }
        return candidates.get(0);
    }

    private String findInheritedReturn(ClassNode node, String name, String params,
                                       Set<String> visited) {
        List<String> parents = new ArrayList<>();
        if (node.superName != null) parents.add(node.superName);
        if (node.interfaces != null) parents.addAll(node.interfaces);
        for (String parent : parents) {
            if (!visited.add(parent)) continue;
            ClassNode p = classIndex.get(parent);
            if (p == null) continue;
            if (p.methods != null) {
                for (MethodNode m : p.methods) {
                    if (!m.name.equals(name)) continue;
                    if (!paramDesc(m.desc).equals(params)) continue;
                    if (!isEmittableAccess(m.access)) continue;
                    if (isSkippedMethod(m)) continue;
                    if ((m.access & Opcodes.ACC_BRIDGE) != 0) continue;
                    return Type.getReturnType(m.desc).getDescriptor();
                }
            }
            String r = findInheritedReturn(p, name, params, visited);
            if (r != null) return r;
        }
        return null;
    }

    private boolean isSubtypeOrEqual(Type a, Type b) {
        if (a.equals(b)) return true;
        if (a.getSort() != Type.OBJECT || b.getSort() != Type.OBJECT) return false;
        return isSubtypeOrEqualInternal(
                a.getInternalName(), b.getInternalName(), new java.util.HashSet<>());
    }

    private boolean isSubtypeOrEqualInternal(String a, String b, Set<String> visited) {
        if (a.equals(b)) return true;
        if ("java/lang/Object".equals(b)) return true;
        if (!visited.add(a)) return false;
        ClassNode cn = classIndex.get(a);
        if (cn != null) {
            if (cn.superName != null
                    && isSubtypeOrEqualInternal(cn.superName, b, visited)) return true;
            if (cn.interfaces != null) {
                for (String iface : cn.interfaces) {
                    if (isSubtypeOrEqualInternal(iface, b, visited)) return true;
                }
            }
            return false;
        }
        try {
            Class<?> ca = Class.forName(a.replace('/', '.'));
            Class<?> cb = Class.forName(b.replace('/', '.'));
            return cb.isAssignableFrom(ca);
        } catch (Throwable t) {
            return false;
        }
    }

    private static Set<String> recordComponentNames(ClassNode node) {
        if (node.recordComponents == null || node.recordComponents.isEmpty()) {
            return Set.of();
        }
        Set<String> names = new java.util.HashSet<>(node.recordComponents.size());
        for (var rc : node.recordComponents) names.add(rc.name);
        return names;
    }

    private static boolean isImplicitRecordMethod(MethodNode m, Set<String> componentNames) {
        String paramDescOnly = paramDesc(m.desc);
        if (paramDescOnly.isEmpty()) {
            if (componentNames.contains(m.name)) return true;
            if ("toString".equals(m.name) && m.desc.equals("()Ljava/lang/String;")) return true;
            if ("hashCode".equals(m.name) && m.desc.equals("()I")) return true;
        }
        if ("equals".equals(m.name) && m.desc.equals("(Ljava/lang/Object;)Z")) return true;
        return false;
    }

    private static String paramDesc(String methodDesc) {
        int end = methodDesc.lastIndexOf(')');
        return methodDesc.substring(1, end);
    }

    private static boolean isImplicitEnumMethod(ClassNode owner, MethodNode m) {
        if ((m.access & Opcodes.ACC_STATIC) == 0) return false;
        if ("values".equals(m.name) && m.desc.equals("()[L" + owner.name + ";")) return true;
        if ("valueOf".equals(m.name)
                && m.desc.equals("(Ljava/lang/String;)L" + owner.name + ";")) return true;
        return false;
    }

    private void emitMethod(StringBuilder sb, ClassNode owner, MethodNode m,
                            StubSemantics sem, boolean isInterface) {
        Type retType = Type.getReturnType(m.desc);
        boolean isStatic = (m.access & Opcodes.ACC_STATIC) != 0;
        boolean isAbstract = (m.access & Opcodes.ACC_ABSTRACT) != 0;
        boolean ownerIsEnum = (owner.access & Opcodes.ACC_ENUM) != 0;
        // Enums with abstract methods rely on each constant providing a
        // concrete subclass body. We drop those synthetic subclasses, so
        // the enum is left without implementations — emit the method as a
        // concrete default-returning stub instead of abstract.
        if (isAbstract && ownerIsEnum) isAbstract = false;

        sb.append("    ");
        if (isInterface) {
            if (isStatic) sb.append("static ");
            else if (!isAbstract) sb.append("default ");
        } else {
            if ((m.access & Opcodes.ACC_PUBLIC) != 0) sb.append("public ");
            else if ((m.access & Opcodes.ACC_PROTECTED) != 0) sb.append("protected ");
            if (isStatic) sb.append("static ");
            if (isAbstract) sb.append("abstract ");
            else if ((m.access & Opcodes.ACC_FINAL) != 0) sb.append("final ");
        }

        sb.append(toSourceName(retType)).append(' ').append(m.name)
                .append('(').append(paramList(m.desc)).append(')');
        appendThrows(sb, m.exceptions);
        if (isAbstract) {
            sb.append(";\n");
            return;
        }
        sb.append(" {\n");
        emitBody(sb, owner, m, sem, retType);
        sb.append("    }\n");
    }

    private void emitBody(StringBuilder sb, ClassNode owner, MethodNode m,
                          StubSemantics sem, Type retType) {
        switch (sem) {
            case LIFECYCLE -> { /* empty body */ }
            case GETTER_DEFAULT, STATIC_FACTORY -> emitReturnDefault(sb, retType);
            case SETTER_WARN_NOOP -> {
                emitWarnCall(sb, owner, m);
                if (retType.getSort() != Type.VOID) emitReturnDefault(sb, retType);
            }
            case BUILDER_CHAIN -> {
                emitWarnCall(sb, owner, m);
                sb.append("        return this;\n");
            }
            default -> {
                if (retType.getSort() != Type.VOID) emitReturnDefault(sb, retType);
            }
        }
    }

    private void emitReturnDefault(StringBuilder sb, Type retType) {
        String expr = resolver.defaultExpression(retType);
        sb.append("        return ").append(expr).append(";\n");
    }

    private void emitWarnCall(StringBuilder sb, ClassNode owner, MethodNode m) {
        String sig = toSourceName(owner.name) + "." + m.name + m.desc;
        sb.append("        ").append(STUB_CALL_LOG_FQN)
                .append(".logOnce(null, \"").append(escape(sig)).append("\");\n");
    }

    private void appendThrows(StringBuilder sb, List<String> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) return;
        sb.append(" throws ");
        sb.append(exceptions.stream().map(this::toSourceName)
                .collect(Collectors.joining(", ")));
    }

    private String paramList(String desc) {
        Type[] args = Type.getArgumentTypes(desc);
        List<String> parts = new ArrayList<>(args.length);
        for (int i = 0; i < args.length; i++) {
            parts.add(toSourceName(args[i]) + " arg" + i);
        }
        return String.join(", ", parts);
    }

    private static boolean isEmittableAccess(int access) {
        return (access & Opcodes.ACC_PUBLIC) != 0
                || (access & Opcodes.ACC_PROTECTED) != 0;
    }

    private static boolean isSkippedMethod(MethodNode m) {
        // Bridge methods also carry ACC_SYNTHETIC — allow them through so
        // the raw-types erasure used by generated stubs still provides the
        // erased-signature overrides (Comparable.compareTo(Object), etc.).
        boolean isBridge = (m.access & Opcodes.ACC_BRIDGE) != 0;
        if (!isBridge && (m.access & Opcodes.ACC_SYNTHETIC) != 0) return true;
        // Deprecated-for-removal methods are still part of the upstream
        // API surface until they are actually removed; dropping them now
        // would also strip overrides required by interfaces the class
        // implements (e.g. Iterable.iterator() on ServerListPingEvent).
        return false;
    }

    private static boolean isForRemoval(MethodNode m) {
        if (m.visibleAnnotations == null) return false;
        for (AnnotationNode ann : m.visibleAnnotations) {
            if (!"Ljava/lang/Deprecated;".equals(ann.desc)) continue;
            if (ann.values == null) return false;
            for (int i = 0; i + 1 < ann.values.size(); i += 2) {
                if ("forRemoval".equals(ann.values.get(i))
                        && Boolean.TRUE.equals(ann.values.get(i + 1))) {
                    return true;
                }
            }
        }
        return false;
    }

    static String packageOf(String internalName) {
        int slash = internalName.lastIndexOf('/');
        return slash < 0 ? "" : internalName.substring(0, slash).replace('/', '.');
    }

    static String simpleNameOf(String internalName) {
        int slash = internalName.lastIndexOf('/');
        return slash < 0 ? internalName : internalName.substring(slash + 1);
    }

    /**
     * Convert an internal class name to a Java source reference.
     * External classes (not in {@link #classIndex}) use {@code .} as the
     * nested-class separator — {@code java/util/ResourceBundle$Control}
     * becomes {@code java.util.ResourceBundle.Control}. Classes we emit
     * ourselves keep the {@code $} in the name because each inner class
     * is flattened into its own top-level compilation unit.
     */
    String toSourceName(String internalName) {
        if (classIndex.containsKey(internalName)) {
            return internalName.replace('/', '.');
        }
        return internalName.replace('/', '.').replace('$', '.');
    }

    String toSourceName(Type t) {
        return switch (t.getSort()) {
            case Type.VOID -> "void";
            case Type.BOOLEAN -> "boolean";
            case Type.CHAR -> "char";
            case Type.BYTE -> "byte";
            case Type.SHORT -> "short";
            case Type.INT -> "int";
            case Type.LONG -> "long";
            case Type.FLOAT -> "float";
            case Type.DOUBLE -> "double";
            case Type.OBJECT -> toSourceName(t.getInternalName());
            case Type.ARRAY -> {
                Type elem = t.getElementType();
                StringBuilder s = new StringBuilder(toSourceName(elem));
                for (int i = 0; i < t.getDimensions(); i++) s.append("[]");
                yield s.toString();
            }
            default -> "java.lang.Object";
        };
    }
}
