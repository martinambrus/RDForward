package net.kyori.adventure.pointer;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Pointered {
    default java.util.Optional get(net.kyori.adventure.pointer.Pointer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.pointer.Pointered.get(Lnet/kyori/adventure/pointer/Pointer;)Ljava/util/Optional;");
        return java.util.Optional.empty();
    }
    default java.lang.Object getOrDefault(net.kyori.adventure.pointer.Pointer arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.pointer.Pointered.getOrDefault(Lnet/kyori/adventure/pointer/Pointer;Ljava/lang/Object;)Ljava/lang/Object;");
        return null;
    }
    default java.lang.Object getOrDefaultFrom(net.kyori.adventure.pointer.Pointer arg0, java.util.function.Supplier arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.pointer.Pointered.getOrDefaultFrom(Lnet/kyori/adventure/pointer/Pointer;Ljava/util/function/Supplier;)Ljava/lang/Object;");
        return null;
    }
    default net.kyori.adventure.pointer.Pointers pointers() {
        return null;
    }
}
