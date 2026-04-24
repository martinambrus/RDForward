package net.kyori.adventure.text.format;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface StyleSetter {
    net.kyori.adventure.text.format.StyleSetter font(net.kyori.adventure.key.Key arg0);
    net.kyori.adventure.text.format.StyleSetter color(net.kyori.adventure.text.format.TextColor arg0);
    net.kyori.adventure.text.format.StyleSetter colorIfAbsent(net.kyori.adventure.text.format.TextColor arg0);
    net.kyori.adventure.text.format.StyleSetter shadowColor(net.kyori.adventure.util.ARGBLike arg0);
    net.kyori.adventure.text.format.StyleSetter shadowColorIfAbsent(net.kyori.adventure.util.ARGBLike arg0);
    default net.kyori.adventure.text.format.StyleSetter decorate(net.kyori.adventure.text.format.TextDecoration arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.StyleSetter.decorate(Lnet/kyori/adventure/text/format/TextDecoration;)Lnet/kyori/adventure/text/format/StyleSetter;");
        return this;
    }
    default net.kyori.adventure.text.format.StyleSetter decorate(net.kyori.adventure.text.format.TextDecoration[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.StyleSetter.decorate([Lnet/kyori/adventure/text/format/TextDecoration;)Lnet/kyori/adventure/text/format/StyleSetter;");
        return this;
    }
    default net.kyori.adventure.text.format.StyleSetter decoration(net.kyori.adventure.text.format.TextDecoration arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.StyleSetter.decoration(Lnet/kyori/adventure/text/format/TextDecoration;Z)Lnet/kyori/adventure/text/format/StyleSetter;");
        return this;
    }
    net.kyori.adventure.text.format.StyleSetter decoration(net.kyori.adventure.text.format.TextDecoration arg0, net.kyori.adventure.text.format.TextDecoration$State arg1);
    net.kyori.adventure.text.format.StyleSetter decorationIfAbsent(net.kyori.adventure.text.format.TextDecoration arg0, net.kyori.adventure.text.format.TextDecoration$State arg1);
    net.kyori.adventure.text.format.StyleSetter decorations(java.util.Map arg0);
    default net.kyori.adventure.text.format.StyleSetter decorations(java.util.Set arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.StyleSetter.decorations(Ljava/util/Set;Z)Lnet/kyori/adventure/text/format/StyleSetter;");
        return this;
    }
    net.kyori.adventure.text.format.StyleSetter clickEvent(net.kyori.adventure.text.event.ClickEvent arg0);
    net.kyori.adventure.text.format.StyleSetter hoverEvent(net.kyori.adventure.text.event.HoverEventSource arg0);
    net.kyori.adventure.text.format.StyleSetter insertion(java.lang.String arg0);
}
