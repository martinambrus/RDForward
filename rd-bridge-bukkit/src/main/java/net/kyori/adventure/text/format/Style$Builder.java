package net.kyori.adventure.text.format;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Style$Builder extends net.kyori.adventure.builder.AbstractBuilder, net.kyori.adventure.util.Buildable$Builder, net.kyori.adventure.text.format.MutableStyleSetter {
    net.kyori.adventure.text.format.Style$Builder font(net.kyori.adventure.key.Key arg0);
    net.kyori.adventure.text.format.Style$Builder color(net.kyori.adventure.text.format.TextColor arg0);
    net.kyori.adventure.text.format.Style$Builder colorIfAbsent(net.kyori.adventure.text.format.TextColor arg0);
    default net.kyori.adventure.text.format.Style$Builder decorate(net.kyori.adventure.text.format.TextDecoration arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style$Builder.decorate(Lnet/kyori/adventure/text/format/TextDecoration;)Lnet/kyori/adventure/text/format/Style$Builder;");
        return this;
    }
    default net.kyori.adventure.text.format.Style$Builder decorate(net.kyori.adventure.text.format.TextDecoration[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style$Builder.decorate([Lnet/kyori/adventure/text/format/TextDecoration;)Lnet/kyori/adventure/text/format/Style$Builder;");
        return this;
    }
    default net.kyori.adventure.text.format.Style$Builder decoration(net.kyori.adventure.text.format.TextDecoration arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style$Builder.decoration(Lnet/kyori/adventure/text/format/TextDecoration;Z)Lnet/kyori/adventure/text/format/Style$Builder;");
        return this;
    }
    default net.kyori.adventure.text.format.Style$Builder decorations(java.util.Map arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style$Builder.decorations(Ljava/util/Map;)Lnet/kyori/adventure/text/format/Style$Builder;");
        return this;
    }
    net.kyori.adventure.text.format.Style$Builder decoration(net.kyori.adventure.text.format.TextDecoration arg0, net.kyori.adventure.text.format.TextDecoration$State arg1);
    net.kyori.adventure.text.format.Style$Builder decorationIfAbsent(net.kyori.adventure.text.format.TextDecoration arg0, net.kyori.adventure.text.format.TextDecoration$State arg1);
    net.kyori.adventure.text.format.Style$Builder clickEvent(net.kyori.adventure.text.event.ClickEvent arg0);
    net.kyori.adventure.text.format.Style$Builder hoverEvent(net.kyori.adventure.text.event.HoverEventSource arg0);
    net.kyori.adventure.text.format.Style$Builder insertion(java.lang.String arg0);
    default net.kyori.adventure.text.format.Style$Builder merge(net.kyori.adventure.text.format.Style arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style$Builder.merge(Lnet/kyori/adventure/text/format/Style;)Lnet/kyori/adventure/text/format/Style$Builder;");
        return this;
    }
    default net.kyori.adventure.text.format.Style$Builder merge(net.kyori.adventure.text.format.Style arg0, net.kyori.adventure.text.format.Style$Merge$Strategy arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style$Builder.merge(Lnet/kyori/adventure/text/format/Style;Lnet/kyori/adventure/text/format/Style$Merge$Strategy;)Lnet/kyori/adventure/text/format/Style$Builder;");
        return this;
    }
    default net.kyori.adventure.text.format.Style$Builder merge(net.kyori.adventure.text.format.Style arg0, net.kyori.adventure.text.format.Style$Merge[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style$Builder.merge(Lnet/kyori/adventure/text/format/Style;[Lnet/kyori/adventure/text/format/Style$Merge;)Lnet/kyori/adventure/text/format/Style$Builder;");
        return this;
    }
    default net.kyori.adventure.text.format.Style$Builder merge(net.kyori.adventure.text.format.Style arg0, net.kyori.adventure.text.format.Style$Merge$Strategy arg1, net.kyori.adventure.text.format.Style$Merge[] arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style$Builder.merge(Lnet/kyori/adventure/text/format/Style;Lnet/kyori/adventure/text/format/Style$Merge$Strategy;[Lnet/kyori/adventure/text/format/Style$Merge;)Lnet/kyori/adventure/text/format/Style$Builder;");
        return this;
    }
    default net.kyori.adventure.text.format.Style$Builder merge(net.kyori.adventure.text.format.Style arg0, java.util.Set arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style$Builder.merge(Lnet/kyori/adventure/text/format/Style;Ljava/util/Set;)Lnet/kyori/adventure/text/format/Style$Builder;");
        return this;
    }
    net.kyori.adventure.text.format.Style$Builder merge(net.kyori.adventure.text.format.Style arg0, net.kyori.adventure.text.format.Style$Merge$Strategy arg1, java.util.Set arg2);
    default net.kyori.adventure.text.format.Style$Builder apply(net.kyori.adventure.text.format.StyleBuilderApplicable arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.Style$Builder.apply(Lnet/kyori/adventure/text/format/StyleBuilderApplicable;)Lnet/kyori/adventure/text/format/Style$Builder;");
        return this;
    }
    net.kyori.adventure.text.format.Style build();
}
