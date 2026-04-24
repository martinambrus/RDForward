package net.kyori.adventure.text.event;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ClickCallback {
    public static final java.time.Duration DEFAULT_LIFETIME = null;
    public static final int UNLIMITED_USES = -1;
    static net.kyori.adventure.text.event.ClickCallback widen(net.kyori.adventure.text.event.ClickCallback arg0, java.lang.Class arg1, java.util.function.Consumer arg2) {
        return null;
    }
    static net.kyori.adventure.text.event.ClickCallback widen(net.kyori.adventure.text.event.ClickCallback arg0, java.lang.Class arg1) {
        return null;
    }
    void accept(net.kyori.adventure.audience.Audience arg0);
    default net.kyori.adventure.text.event.ClickCallback filter(java.util.function.Predicate arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.event.ClickCallback.filter(Ljava/util/function/Predicate;)Lnet/kyori/adventure/text/event/ClickCallback;");
        return this;
    }
    default net.kyori.adventure.text.event.ClickCallback filter(java.util.function.Predicate arg0, java.util.function.Consumer arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.event.ClickCallback.filter(Ljava/util/function/Predicate;Ljava/util/function/Consumer;)Lnet/kyori/adventure/text/event/ClickCallback;");
        return this;
    }
    default net.kyori.adventure.text.event.ClickCallback requiringPermission(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.event.ClickCallback.requiringPermission(Ljava/lang/String;)Lnet/kyori/adventure/text/event/ClickCallback;");
        return this;
    }
    default net.kyori.adventure.text.event.ClickCallback requiringPermission(java.lang.String arg0, java.util.function.Consumer arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.event.ClickCallback.requiringPermission(Ljava/lang/String;Ljava/util/function/Consumer;)Lnet/kyori/adventure/text/event/ClickCallback;");
        return this;
    }
}
