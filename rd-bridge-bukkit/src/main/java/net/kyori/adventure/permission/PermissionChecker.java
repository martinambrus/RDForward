package net.kyori.adventure.permission;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PermissionChecker extends java.util.function.Predicate {
    public static final net.kyori.adventure.pointer.Pointer POINTER = null;
    static net.kyori.adventure.permission.PermissionChecker always(net.kyori.adventure.util.TriState arg0) {
        return null;
    }
    net.kyori.adventure.util.TriState value(java.lang.String arg0);
    default boolean test(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.permission.PermissionChecker.test(Ljava/lang/String;)Z");
        return false;
    }
    default boolean test(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.permission.PermissionChecker.test(Ljava/lang/Object;)Z");
        return false;
    }
}
