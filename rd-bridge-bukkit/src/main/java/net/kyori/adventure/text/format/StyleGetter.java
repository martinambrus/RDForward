package net.kyori.adventure.text.format;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface StyleGetter {
    net.kyori.adventure.key.Key font();
    net.kyori.adventure.text.format.TextColor color();
    net.kyori.adventure.text.format.ShadowColor shadowColor();
    default boolean hasDecoration(net.kyori.adventure.text.format.TextDecoration arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.StyleGetter.hasDecoration(Lnet/kyori/adventure/text/format/TextDecoration;)Z");
        return false;
    }
    net.kyori.adventure.text.format.TextDecoration$State decoration(net.kyori.adventure.text.format.TextDecoration arg0);
    default java.util.Map decorations() {
        return java.util.Collections.emptyMap();
    }
    net.kyori.adventure.text.event.ClickEvent clickEvent();
    net.kyori.adventure.text.event.HoverEvent hoverEvent();
    java.lang.String insertion();
}
