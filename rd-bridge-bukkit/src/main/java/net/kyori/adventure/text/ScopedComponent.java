package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ScopedComponent extends net.kyori.adventure.text.Component {
    default net.kyori.adventure.text.Component asComponent() {
        return null;
    }
    net.kyori.adventure.text.Component children(java.util.List arg0);
    net.kyori.adventure.text.Component style(net.kyori.adventure.text.format.Style arg0);
    default net.kyori.adventure.text.Component style(java.util.function.Consumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.style(Ljava/util/function/Consumer;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component style(net.kyori.adventure.text.format.Style$Builder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.style(Lnet/kyori/adventure/text/format/Style$Builder;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component style(java.util.function.Consumer arg0, net.kyori.adventure.text.format.Style$Merge$Strategy arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.style(Ljava/util/function/Consumer;Lnet/kyori/adventure/text/format/Style$Merge$Strategy;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component mergeStyle(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.mergeStyle(Lnet/kyori/adventure/text/Component;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component mergeStyle(net.kyori.adventure.text.Component arg0, net.kyori.adventure.text.format.Style$Merge[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.mergeStyle(Lnet/kyori/adventure/text/Component;[Lnet/kyori/adventure/text/format/Style$Merge;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component append(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.append(Lnet/kyori/adventure/text/Component;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component append(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.append(Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component append(net.kyori.adventure.text.ComponentBuilder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.append(Lnet/kyori/adventure/text/ComponentBuilder;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component append(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.append(Ljava/util/List;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component append(net.kyori.adventure.text.ComponentLike[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.append([Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component appendNewline() {
        return null;
    }
    default net.kyori.adventure.text.Component appendSpace() {
        return null;
    }
    default net.kyori.adventure.text.Component applyFallbackStyle(net.kyori.adventure.text.format.StyleBuilderApplicable[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.applyFallbackStyle([Lnet/kyori/adventure/text/format/StyleBuilderApplicable;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component applyFallbackStyle(net.kyori.adventure.text.format.Style arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.applyFallbackStyle(Lnet/kyori/adventure/text/format/Style;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component mergeStyle(net.kyori.adventure.text.Component arg0, java.util.Set arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.mergeStyle(Lnet/kyori/adventure/text/Component;Ljava/util/Set;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component color(net.kyori.adventure.text.format.TextColor arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.color(Lnet/kyori/adventure/text/format/TextColor;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component colorIfAbsent(net.kyori.adventure.text.format.TextColor arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.colorIfAbsent(Lnet/kyori/adventure/text/format/TextColor;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component shadowColor(net.kyori.adventure.util.ARGBLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.shadowColor(Lnet/kyori/adventure/util/ARGBLike;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component shadowColorIfAbsent(net.kyori.adventure.util.ARGBLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.shadowColorIfAbsent(Lnet/kyori/adventure/util/ARGBLike;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component decorate(net.kyori.adventure.text.format.TextDecoration arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.decorate(Lnet/kyori/adventure/text/format/TextDecoration;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component decoration(net.kyori.adventure.text.format.TextDecoration arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.decoration(Lnet/kyori/adventure/text/format/TextDecoration;Z)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component decoration(net.kyori.adventure.text.format.TextDecoration arg0, net.kyori.adventure.text.format.TextDecoration$State arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.decoration(Lnet/kyori/adventure/text/format/TextDecoration;Lnet/kyori/adventure/text/format/TextDecoration$State;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component decorationIfAbsent(net.kyori.adventure.text.format.TextDecoration arg0, net.kyori.adventure.text.format.TextDecoration$State arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.decorationIfAbsent(Lnet/kyori/adventure/text/format/TextDecoration;Lnet/kyori/adventure/text/format/TextDecoration$State;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component decorations(java.util.Map arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.decorations(Ljava/util/Map;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component clickEvent(net.kyori.adventure.text.event.ClickEvent arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.clickEvent(Lnet/kyori/adventure/text/event/ClickEvent;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component hoverEvent(net.kyori.adventure.text.event.HoverEventSource arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.hoverEvent(Lnet/kyori/adventure/text/event/HoverEventSource;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component insertion(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.insertion(Ljava/lang/String;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component font(net.kyori.adventure.key.Key arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.ScopedComponent.font(Lnet/kyori/adventure/key/Key;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
}
