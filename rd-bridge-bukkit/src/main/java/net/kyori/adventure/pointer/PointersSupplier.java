package net.kyori.adventure.pointer;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PointersSupplier {
    static net.kyori.adventure.pointer.PointersSupplier$Builder builder() {
        return null;
    }
    net.kyori.adventure.pointer.Pointers view(java.lang.Object arg0);
    boolean supports(net.kyori.adventure.pointer.Pointer arg0);
    java.util.function.Function resolver(net.kyori.adventure.pointer.Pointer arg0);
}
