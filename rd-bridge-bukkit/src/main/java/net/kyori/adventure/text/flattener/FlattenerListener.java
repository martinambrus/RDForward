package net.kyori.adventure.text.flattener;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface FlattenerListener {
    default void pushStyle(net.kyori.adventure.text.format.Style arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.flattener.FlattenerListener.pushStyle(Lnet/kyori/adventure/text/format/Style;)V");
    }
    void component(java.lang.String arg0);
    default boolean shouldContinue() {
        return false;
    }
    default void popStyle(net.kyori.adventure.text.format.Style arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.flattener.FlattenerListener.popStyle(Lnet/kyori/adventure/text/format/Style;)V");
    }
}
