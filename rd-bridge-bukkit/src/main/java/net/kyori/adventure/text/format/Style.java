package net.kyori.adventure.text.format;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Style extends net.kyori.adventure.util.Buildable, net.kyori.examination.Examinable, net.kyori.adventure.text.format.StyleGetter, net.kyori.adventure.text.format.StyleSetter {
    public static final net.kyori.adventure.key.Key DEFAULT_FONT = null;
    static net.kyori.adventure.text.format.Style empty() {
        return null;
    }
    static net.kyori.adventure.text.format.Style$Builder style() {
        return null;
    }
    static net.kyori.adventure.text.format.Style style(java.util.function.Consumer arg0) {
        return null;
    }
    static net.kyori.adventure.text.format.Style style(net.kyori.adventure.text.format.TextColor arg0) {
        return null;
    }
    static net.kyori.adventure.text.format.Style style(net.kyori.adventure.text.format.TextDecoration arg0) {
        return null;
    }
    static net.kyori.adventure.text.format.Style style(net.kyori.adventure.text.format.TextColor arg0, net.kyori.adventure.text.format.TextDecoration[] arg1) {
        return null;
    }
    static net.kyori.adventure.text.format.Style style(net.kyori.adventure.text.format.TextColor arg0, java.util.Set arg1) {
        return null;
    }
    static net.kyori.adventure.text.format.Style style(net.kyori.adventure.text.format.StyleBuilderApplicable[] arg0) {
        return null;
    }
    static net.kyori.adventure.text.format.Style style(java.lang.Iterable arg0) {
        return null;
    }
    default net.kyori.adventure.text.format.Style edit(java.util.function.Consumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style.edit(Ljava/util/function/Consumer;)Lnet/kyori/adventure/text/format/Style;");
        return this;
    }
    default net.kyori.adventure.text.format.Style edit(java.util.function.Consumer arg0, net.kyori.adventure.text.format.Style$Merge$Strategy arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style.edit(Ljava/util/function/Consumer;Lnet/kyori/adventure/text/format/Style$Merge$Strategy;)Lnet/kyori/adventure/text/format/Style;");
        return this;
    }
    net.kyori.adventure.key.Key font();
    net.kyori.adventure.text.format.Style font(net.kyori.adventure.key.Key arg0);
    net.kyori.adventure.text.format.TextColor color();
    net.kyori.adventure.text.format.Style color(net.kyori.adventure.text.format.TextColor arg0);
    net.kyori.adventure.text.format.Style colorIfAbsent(net.kyori.adventure.text.format.TextColor arg0);
    default boolean hasDecoration(net.kyori.adventure.text.format.TextDecoration arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style.hasDecoration(Lnet/kyori/adventure/text/format/TextDecoration;)Z");
        return false;
    }
    net.kyori.adventure.text.format.TextDecoration$State decoration(net.kyori.adventure.text.format.TextDecoration arg0);
    default net.kyori.adventure.text.format.Style decorate(net.kyori.adventure.text.format.TextDecoration arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style.decorate(Lnet/kyori/adventure/text/format/TextDecoration;)Lnet/kyori/adventure/text/format/Style;");
        return this;
    }
    default net.kyori.adventure.text.format.Style decoration(net.kyori.adventure.text.format.TextDecoration arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style.decoration(Lnet/kyori/adventure/text/format/TextDecoration;Z)Lnet/kyori/adventure/text/format/Style;");
        return this;
    }
    net.kyori.adventure.text.format.Style decoration(net.kyori.adventure.text.format.TextDecoration arg0, net.kyori.adventure.text.format.TextDecoration$State arg1);
    net.kyori.adventure.text.format.Style decorationIfAbsent(net.kyori.adventure.text.format.TextDecoration arg0, net.kyori.adventure.text.format.TextDecoration$State arg1);
    default java.util.Map decorations() {
        return java.util.Collections.emptyMap();
    }
    net.kyori.adventure.text.format.Style decorations(java.util.Map arg0);
    net.kyori.adventure.text.event.ClickEvent clickEvent();
    net.kyori.adventure.text.format.Style clickEvent(net.kyori.adventure.text.event.ClickEvent arg0);
    net.kyori.adventure.text.event.HoverEvent hoverEvent();
    net.kyori.adventure.text.format.Style hoverEvent(net.kyori.adventure.text.event.HoverEventSource arg0);
    java.lang.String insertion();
    net.kyori.adventure.text.format.Style insertion(java.lang.String arg0);
    default net.kyori.adventure.text.format.Style merge(net.kyori.adventure.text.format.Style arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style.merge(Lnet/kyori/adventure/text/format/Style;)Lnet/kyori/adventure/text/format/Style;");
        return this;
    }
    default net.kyori.adventure.text.format.Style merge(net.kyori.adventure.text.format.Style arg0, net.kyori.adventure.text.format.Style$Merge$Strategy arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style.merge(Lnet/kyori/adventure/text/format/Style;Lnet/kyori/adventure/text/format/Style$Merge$Strategy;)Lnet/kyori/adventure/text/format/Style;");
        return this;
    }
    default net.kyori.adventure.text.format.Style merge(net.kyori.adventure.text.format.Style arg0, net.kyori.adventure.text.format.Style$Merge arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style.merge(Lnet/kyori/adventure/text/format/Style;Lnet/kyori/adventure/text/format/Style$Merge;)Lnet/kyori/adventure/text/format/Style;");
        return this;
    }
    default net.kyori.adventure.text.format.Style merge(net.kyori.adventure.text.format.Style arg0, net.kyori.adventure.text.format.Style$Merge$Strategy arg1, net.kyori.adventure.text.format.Style$Merge arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style.merge(Lnet/kyori/adventure/text/format/Style;Lnet/kyori/adventure/text/format/Style$Merge$Strategy;Lnet/kyori/adventure/text/format/Style$Merge;)Lnet/kyori/adventure/text/format/Style;");
        return this;
    }
    default net.kyori.adventure.text.format.Style merge(net.kyori.adventure.text.format.Style arg0, net.kyori.adventure.text.format.Style$Merge[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style.merge(Lnet/kyori/adventure/text/format/Style;[Lnet/kyori/adventure/text/format/Style$Merge;)Lnet/kyori/adventure/text/format/Style;");
        return this;
    }
    default net.kyori.adventure.text.format.Style merge(net.kyori.adventure.text.format.Style arg0, net.kyori.adventure.text.format.Style$Merge$Strategy arg1, net.kyori.adventure.text.format.Style$Merge[] arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style.merge(Lnet/kyori/adventure/text/format/Style;Lnet/kyori/adventure/text/format/Style$Merge$Strategy;[Lnet/kyori/adventure/text/format/Style$Merge;)Lnet/kyori/adventure/text/format/Style;");
        return this;
    }
    default net.kyori.adventure.text.format.Style merge(net.kyori.adventure.text.format.Style arg0, java.util.Set arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style.merge(Lnet/kyori/adventure/text/format/Style;Ljava/util/Set;)Lnet/kyori/adventure/text/format/Style;");
        return this;
    }
    net.kyori.adventure.text.format.Style merge(net.kyori.adventure.text.format.Style arg0, net.kyori.adventure.text.format.Style$Merge$Strategy arg1, java.util.Set arg2);
    net.kyori.adventure.text.format.Style unmerge(net.kyori.adventure.text.format.Style arg0);
    boolean isEmpty();
    net.kyori.adventure.text.format.Style$Builder toBuilder();
}
