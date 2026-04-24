package net.kyori.adventure.pointer;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Pointers$Builder extends net.kyori.adventure.builder.AbstractBuilder, net.kyori.adventure.util.Buildable$Builder {
    default net.kyori.adventure.pointer.Pointers$Builder withStatic(net.kyori.adventure.pointer.Pointer arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.pointer.Pointers$Builder.withStatic(Lnet/kyori/adventure/pointer/Pointer;Ljava/lang/Object;)Lnet/kyori/adventure/pointer/Pointers$Builder;");
        return this;
    }
    net.kyori.adventure.pointer.Pointers$Builder withDynamic(net.kyori.adventure.pointer.Pointer arg0, java.util.function.Supplier arg1);
}
