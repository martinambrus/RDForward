package net.kyori.adventure.text.format;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TextDecorationAndState extends net.kyori.examination.Examinable, net.kyori.adventure.text.format.StyleBuilderApplicable {
    net.kyori.adventure.text.format.TextDecoration decoration();
    net.kyori.adventure.text.format.TextDecoration$State state();
    default void styleApply(net.kyori.adventure.text.format.Style$Builder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.TextDecorationAndState.styleApply(Lnet/kyori/adventure/text/format/Style$Builder;)V");
    }
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
}
