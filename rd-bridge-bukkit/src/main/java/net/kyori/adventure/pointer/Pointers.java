package net.kyori.adventure.pointer;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Pointers extends net.kyori.adventure.util.Buildable {
    static net.kyori.adventure.pointer.Pointers empty() {
        return null;
    }
    static net.kyori.adventure.pointer.Pointers$Builder builder() {
        return null;
    }
    java.util.Optional get(net.kyori.adventure.pointer.Pointer arg0);
    default java.lang.Object getOrDefault(net.kyori.adventure.pointer.Pointer arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.pointer.Pointers.getOrDefault(Lnet/kyori/adventure/pointer/Pointer;Ljava/lang/Object;)Ljava/lang/Object;");
        return null;
    }
    default java.lang.Object getOrDefaultFrom(net.kyori.adventure.pointer.Pointer arg0, java.util.function.Supplier arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.pointer.Pointers.getOrDefaultFrom(Lnet/kyori/adventure/pointer/Pointer;Ljava/util/function/Supplier;)Ljava/lang/Object;");
        return null;
    }
    boolean supports(net.kyori.adventure.pointer.Pointer arg0);
}
