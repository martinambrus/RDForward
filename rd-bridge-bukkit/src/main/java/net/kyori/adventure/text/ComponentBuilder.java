package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ComponentBuilder extends net.kyori.adventure.builder.AbstractBuilder, net.kyori.adventure.util.Buildable$Builder, net.kyori.adventure.text.ComponentBuilderApplicable, net.kyori.adventure.text.ComponentLike, net.kyori.adventure.text.format.MutableStyleSetter {
    net.kyori.adventure.text.ComponentBuilder append(net.kyori.adventure.text.Component arg0);
    default net.kyori.adventure.text.ComponentBuilder append(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ComponentBuilder.append(Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/text/ComponentBuilder;");
        return this;
    }
    default net.kyori.adventure.text.ComponentBuilder append(net.kyori.adventure.text.ComponentBuilder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ComponentBuilder.append(Lnet/kyori/adventure/text/ComponentBuilder;)Lnet/kyori/adventure/text/ComponentBuilder;");
        return this;
    }
    net.kyori.adventure.text.ComponentBuilder append(net.kyori.adventure.text.Component[] arg0);
    net.kyori.adventure.text.ComponentBuilder append(net.kyori.adventure.text.ComponentLike[] arg0);
    net.kyori.adventure.text.ComponentBuilder append(java.lang.Iterable arg0);
    default net.kyori.adventure.text.ComponentBuilder appendNewline() {
        return null;
    }
    default net.kyori.adventure.text.ComponentBuilder appendSpace() {
        return null;
    }
    default net.kyori.adventure.text.ComponentBuilder apply(java.util.function.Consumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ComponentBuilder.apply(Ljava/util/function/Consumer;)Lnet/kyori/adventure/text/ComponentBuilder;");
        return this;
    }
    net.kyori.adventure.text.ComponentBuilder applyDeep(java.util.function.Consumer arg0);
    net.kyori.adventure.text.ComponentBuilder mapChildren(java.util.function.Function arg0);
    net.kyori.adventure.text.ComponentBuilder mapChildrenDeep(java.util.function.Function arg0);
    java.util.List children();
    net.kyori.adventure.text.ComponentBuilder style(net.kyori.adventure.text.format.Style arg0);
    net.kyori.adventure.text.ComponentBuilder style(java.util.function.Consumer arg0);
    net.kyori.adventure.text.ComponentBuilder font(net.kyori.adventure.key.Key arg0);
    net.kyori.adventure.text.ComponentBuilder color(net.kyori.adventure.text.format.TextColor arg0);
    net.kyori.adventure.text.ComponentBuilder colorIfAbsent(net.kyori.adventure.text.format.TextColor arg0);
    default net.kyori.adventure.text.ComponentBuilder decorations(java.util.Set arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ComponentBuilder.decorations(Ljava/util/Set;Z)Lnet/kyori/adventure/text/ComponentBuilder;");
        return this;
    }
    default net.kyori.adventure.text.ComponentBuilder decorate(net.kyori.adventure.text.format.TextDecoration arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ComponentBuilder.decorate(Lnet/kyori/adventure/text/format/TextDecoration;)Lnet/kyori/adventure/text/ComponentBuilder;");
        return this;
    }
    default net.kyori.adventure.text.ComponentBuilder decorate(net.kyori.adventure.text.format.TextDecoration[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ComponentBuilder.decorate([Lnet/kyori/adventure/text/format/TextDecoration;)Lnet/kyori/adventure/text/ComponentBuilder;");
        return this;
    }
    default net.kyori.adventure.text.ComponentBuilder decoration(net.kyori.adventure.text.format.TextDecoration arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ComponentBuilder.decoration(Lnet/kyori/adventure/text/format/TextDecoration;Z)Lnet/kyori/adventure/text/ComponentBuilder;");
        return this;
    }
    default net.kyori.adventure.text.ComponentBuilder decorations(java.util.Map arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ComponentBuilder.decorations(Ljava/util/Map;)Lnet/kyori/adventure/text/ComponentBuilder;");
        return this;
    }
    net.kyori.adventure.text.ComponentBuilder decoration(net.kyori.adventure.text.format.TextDecoration arg0, net.kyori.adventure.text.format.TextDecoration$State arg1);
    net.kyori.adventure.text.ComponentBuilder decorationIfAbsent(net.kyori.adventure.text.format.TextDecoration arg0, net.kyori.adventure.text.format.TextDecoration$State arg1);
    net.kyori.adventure.text.ComponentBuilder clickEvent(net.kyori.adventure.text.event.ClickEvent arg0);
    net.kyori.adventure.text.ComponentBuilder hoverEvent(net.kyori.adventure.text.event.HoverEventSource arg0);
    net.kyori.adventure.text.ComponentBuilder insertion(java.lang.String arg0);
    default net.kyori.adventure.text.ComponentBuilder mergeStyle(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ComponentBuilder.mergeStyle(Lnet/kyori/adventure/text/Component;)Lnet/kyori/adventure/text/ComponentBuilder;");
        return this;
    }
    default net.kyori.adventure.text.ComponentBuilder mergeStyle(net.kyori.adventure.text.Component arg0, net.kyori.adventure.text.format.Style$Merge[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ComponentBuilder.mergeStyle(Lnet/kyori/adventure/text/Component;[Lnet/kyori/adventure/text/format/Style$Merge;)Lnet/kyori/adventure/text/ComponentBuilder;");
        return this;
    }
    net.kyori.adventure.text.ComponentBuilder mergeStyle(net.kyori.adventure.text.Component arg0, java.util.Set arg1);
    net.kyori.adventure.text.ComponentBuilder resetStyle();
    net.kyori.adventure.text.BuildableComponent build();
    default net.kyori.adventure.text.ComponentBuilder applicableApply(net.kyori.adventure.text.ComponentBuilderApplicable arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ComponentBuilder.applicableApply(Lnet/kyori/adventure/text/ComponentBuilderApplicable;)Lnet/kyori/adventure/text/ComponentBuilder;");
        return this;
    }
    default void componentBuilderApply(net.kyori.adventure.text.ComponentBuilder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ComponentBuilder.componentBuilderApply(Lnet/kyori/adventure/text/ComponentBuilder;)V");
    }
    default net.kyori.adventure.text.Component asComponent() {
        return null;
    }
}
