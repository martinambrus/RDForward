package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Component extends net.kyori.adventure.text.ComponentBuilderApplicable, net.kyori.adventure.text.ComponentLike, net.kyori.examination.Examinable, net.kyori.adventure.text.event.HoverEventSource, net.kyori.adventure.text.format.StyleGetter, net.kyori.adventure.text.format.StyleSetter {
    public static final java.util.function.BiPredicate EQUALS = null;
    public static final java.util.function.BiPredicate EQUALS_IDENTITY = null;
    public static final java.util.function.Predicate IS_NOT_EMPTY = null;
    static net.kyori.adventure.text.TextComponent empty() {
        return null;
    }
    static net.kyori.adventure.text.TextComponent newline() {
        return null;
    }
    static net.kyori.adventure.text.TextComponent space() {
        return null;
    }
    static net.kyori.adventure.text.TextComponent join(net.kyori.adventure.text.ComponentLike arg0, net.kyori.adventure.text.ComponentLike[] arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent join(net.kyori.adventure.text.ComponentLike arg0, java.lang.Iterable arg1) {
        return null;
    }
    static net.kyori.adventure.text.Component join(net.kyori.adventure.text.JoinConfiguration$Builder arg0, net.kyori.adventure.text.ComponentLike[] arg1) {
        return null;
    }
    static net.kyori.adventure.text.Component join(net.kyori.adventure.text.JoinConfiguration$Builder arg0, java.lang.Iterable arg1) {
        return null;
    }
    static net.kyori.adventure.text.Component join(net.kyori.adventure.text.JoinConfiguration arg0, net.kyori.adventure.text.ComponentLike[] arg1) {
        return null;
    }
    static net.kyori.adventure.text.Component join(net.kyori.adventure.text.JoinConfiguration arg0, java.lang.Iterable arg1) {
        return null;
    }
    static java.util.stream.Collector toComponent() {
        return null;
    }
    static java.util.stream.Collector toComponent(net.kyori.adventure.text.Component arg0) {
        return null;
    }
    static net.kyori.adventure.text.BlockNBTComponent$Builder blockNBT() {
        return null;
    }
    static net.kyori.adventure.text.BlockNBTComponent blockNBT(java.util.function.Consumer arg0) {
        return null;
    }
    static net.kyori.adventure.text.BlockNBTComponent blockNBT(java.lang.String arg0, net.kyori.adventure.text.BlockNBTComponent$Pos arg1) {
        return null;
    }
    static net.kyori.adventure.text.BlockNBTComponent blockNBT(java.lang.String arg0, boolean arg1, net.kyori.adventure.text.BlockNBTComponent$Pos arg2) {
        return null;
    }
    static net.kyori.adventure.text.BlockNBTComponent blockNBT(java.lang.String arg0, boolean arg1, net.kyori.adventure.text.ComponentLike arg2, net.kyori.adventure.text.BlockNBTComponent$Pos arg3) {
        return null;
    }
    static net.kyori.adventure.text.EntityNBTComponent$Builder entityNBT() {
        return null;
    }
    static net.kyori.adventure.text.EntityNBTComponent entityNBT(java.util.function.Consumer arg0) {
        return null;
    }
    static net.kyori.adventure.text.EntityNBTComponent entityNBT(java.lang.String arg0, java.lang.String arg1) {
        return null;
    }
    static net.kyori.adventure.text.KeybindComponent$Builder keybind() {
        return null;
    }
    static net.kyori.adventure.text.KeybindComponent keybind(java.util.function.Consumer arg0) {
        return null;
    }
    static net.kyori.adventure.text.KeybindComponent keybind(java.lang.String arg0) {
        return null;
    }
    static net.kyori.adventure.text.KeybindComponent keybind(net.kyori.adventure.text.KeybindComponent$KeybindLike arg0) {
        return null;
    }
    static net.kyori.adventure.text.KeybindComponent keybind(java.lang.String arg0, net.kyori.adventure.text.format.Style arg1) {
        return null;
    }
    static net.kyori.adventure.text.KeybindComponent keybind(net.kyori.adventure.text.KeybindComponent$KeybindLike arg0, net.kyori.adventure.text.format.Style arg1) {
        return null;
    }
    static net.kyori.adventure.text.KeybindComponent keybind(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1) {
        return null;
    }
    static net.kyori.adventure.text.KeybindComponent keybind(net.kyori.adventure.text.KeybindComponent$KeybindLike arg0, net.kyori.adventure.text.format.TextColor arg1) {
        return null;
    }
    static net.kyori.adventure.text.KeybindComponent keybind(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.format.TextDecoration[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.KeybindComponent keybind(net.kyori.adventure.text.KeybindComponent$KeybindLike arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.format.TextDecoration[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.KeybindComponent keybind(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2) {
        return null;
    }
    static net.kyori.adventure.text.KeybindComponent keybind(net.kyori.adventure.text.KeybindComponent$KeybindLike arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2) {
        return null;
    }
    static net.kyori.adventure.text.ObjectComponent$Builder object() {
        return null;
    }
    static net.kyori.adventure.text.ObjectComponent object(java.util.function.Consumer arg0) {
        return null;
    }
    static net.kyori.adventure.text.ObjectComponent object(net.kyori.adventure.text.object.ObjectContents arg0) {
        return null;
    }
    static net.kyori.adventure.text.ScoreComponent$Builder score() {
        return null;
    }
    static net.kyori.adventure.text.ScoreComponent score(java.util.function.Consumer arg0) {
        return null;
    }
    static net.kyori.adventure.text.ScoreComponent score(java.lang.String arg0, java.lang.String arg1) {
        return null;
    }
    static net.kyori.adventure.text.ScoreComponent score(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2) {
        return null;
    }
    static net.kyori.adventure.text.SelectorComponent$Builder selector() {
        return null;
    }
    static net.kyori.adventure.text.SelectorComponent selector(java.util.function.Consumer arg0) {
        return null;
    }
    static net.kyori.adventure.text.SelectorComponent selector(java.lang.String arg0) {
        return null;
    }
    static net.kyori.adventure.text.SelectorComponent selector(java.lang.String arg0, net.kyori.adventure.text.ComponentLike arg1) {
        return null;
    }
    static net.kyori.adventure.text.StorageNBTComponent$Builder storageNBT() {
        return null;
    }
    static net.kyori.adventure.text.StorageNBTComponent storageNBT(java.util.function.Consumer arg0) {
        return null;
    }
    static net.kyori.adventure.text.StorageNBTComponent storageNBT(java.lang.String arg0, net.kyori.adventure.key.Key arg1) {
        return null;
    }
    static net.kyori.adventure.text.StorageNBTComponent storageNBT(java.lang.String arg0, boolean arg1, net.kyori.adventure.key.Key arg2) {
        return null;
    }
    static net.kyori.adventure.text.StorageNBTComponent storageNBT(java.lang.String arg0, boolean arg1, net.kyori.adventure.text.ComponentLike arg2, net.kyori.adventure.key.Key arg3) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent$Builder text() {
        return null;
    }
    static net.kyori.adventure.text.TextComponent textOfChildren(net.kyori.adventure.text.ComponentLike[] arg0) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(java.util.function.Consumer arg0) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(java.lang.String arg0) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(java.lang.String arg0, net.kyori.adventure.text.format.Style arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.format.TextDecoration[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(boolean arg0) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(boolean arg0, net.kyori.adventure.text.format.Style arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(boolean arg0, net.kyori.adventure.text.format.TextColor arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(boolean arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.format.TextDecoration[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(boolean arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(char arg0) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(char arg0, net.kyori.adventure.text.format.Style arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(char arg0, net.kyori.adventure.text.format.TextColor arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(char arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.format.TextDecoration[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(char arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(double arg0) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(double arg0, net.kyori.adventure.text.format.Style arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(double arg0, net.kyori.adventure.text.format.TextColor arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(double arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.format.TextDecoration[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(double arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(float arg0) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(float arg0, net.kyori.adventure.text.format.Style arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(float arg0, net.kyori.adventure.text.format.TextColor arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(float arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.format.TextDecoration[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(float arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(int arg0) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(int arg0, net.kyori.adventure.text.format.Style arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(int arg0, net.kyori.adventure.text.format.TextColor arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(int arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.format.TextDecoration[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(int arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(long arg0) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(long arg0, net.kyori.adventure.text.format.Style arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(long arg0, net.kyori.adventure.text.format.TextColor arg1) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(long arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.format.TextDecoration[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TextComponent text(long arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2) {
        return null;
    }
    static net.kyori.adventure.text.VirtualComponent virtual(java.lang.Class arg0, net.kyori.adventure.text.VirtualComponentRenderer arg1) {
        return null;
    }
    static net.kyori.adventure.text.VirtualComponent virtual(java.lang.Class arg0, net.kyori.adventure.text.VirtualComponentRenderer arg1, net.kyori.adventure.text.format.Style arg2) {
        return null;
    }
    static net.kyori.adventure.text.VirtualComponent virtual(java.lang.Class arg0, net.kyori.adventure.text.VirtualComponentRenderer arg1, net.kyori.adventure.text.format.StyleBuilderApplicable[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.VirtualComponent virtual(java.lang.Class arg0, net.kyori.adventure.text.VirtualComponentRenderer arg1, java.lang.Iterable arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent$Builder translatable() {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.util.function.Consumer arg0) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, java.lang.String arg1) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, java.lang.String arg1) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, net.kyori.adventure.text.format.Style arg1) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, net.kyori.adventure.text.format.Style arg1) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, java.lang.String arg1, net.kyori.adventure.text.format.Style arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, java.lang.String arg1, net.kyori.adventure.text.format.Style arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, java.lang.String arg1, net.kyori.adventure.text.format.StyleBuilderApplicable[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, java.lang.String arg1, java.lang.Iterable arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, java.lang.String arg1, net.kyori.adventure.text.ComponentLike[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, java.lang.String arg1, net.kyori.adventure.text.ComponentLike[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, java.lang.String arg1, net.kyori.adventure.text.format.Style arg2, net.kyori.adventure.text.ComponentLike[] arg3) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, java.lang.String arg1, net.kyori.adventure.text.format.Style arg2, net.kyori.adventure.text.ComponentLike[] arg3) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, java.lang.String arg1, net.kyori.adventure.text.format.Style arg2, java.util.List arg3) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, java.lang.String arg1, net.kyori.adventure.text.format.Style arg2, java.util.List arg3) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, java.lang.String arg1, java.util.List arg2, java.lang.Iterable arg3) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, java.lang.String arg1, java.util.List arg2, java.lang.Iterable arg3) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, java.lang.String arg1, java.util.List arg2, net.kyori.adventure.text.format.StyleBuilderApplicable[] arg3) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, java.lang.String arg1, java.util.List arg2, net.kyori.adventure.text.format.StyleBuilderApplicable[] arg3) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, net.kyori.adventure.text.format.TextColor arg1) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.format.TextDecoration[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.format.TextDecoration[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, net.kyori.adventure.text.ComponentLike[] arg1) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, net.kyori.adventure.text.ComponentLike[] arg1) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, net.kyori.adventure.text.format.Style arg1, net.kyori.adventure.text.ComponentLike[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, net.kyori.adventure.text.format.Style arg1, net.kyori.adventure.text.ComponentLike[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.ComponentLike[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, net.kyori.adventure.text.format.TextColor arg1, net.kyori.adventure.text.ComponentLike[] arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2, net.kyori.adventure.text.ComponentLike[] arg3) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2, net.kyori.adventure.text.ComponentLike[] arg3) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, java.util.List arg1) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, java.util.List arg1) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, net.kyori.adventure.text.format.Style arg1, java.util.List arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, net.kyori.adventure.text.format.Style arg1, java.util.List arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.List arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.List arg2) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(java.lang.String arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2, java.util.List arg3) {
        return null;
    }
    static net.kyori.adventure.text.TranslatableComponent translatable(net.kyori.adventure.translation.Translatable arg0, net.kyori.adventure.text.format.TextColor arg1, java.util.Set arg2, java.util.List arg3) {
        return null;
    }
    java.util.List children();
    net.kyori.adventure.text.Component children(java.util.List arg0);
    default boolean contains(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.contains(Lnet/kyori/adventure/text/Component;)Z");
        return false;
    }
    default boolean contains(net.kyori.adventure.text.Component arg0, java.util.function.BiPredicate arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.contains(Lnet/kyori/adventure/text/Component;Ljava/util/function/BiPredicate;)Z");
        return false;
    }
    default void detectCycle(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.detectCycle(Lnet/kyori/adventure/text/Component;)V");
    }
    default net.kyori.adventure.text.Component append(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.append(Lnet/kyori/adventure/text/Component;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component append(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.append(Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component append(net.kyori.adventure.text.ComponentBuilder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.append(Lnet/kyori/adventure/text/ComponentBuilder;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component appendNewline() {
        return null;
    }
    default net.kyori.adventure.text.Component appendSpace() {
        return null;
    }
    default net.kyori.adventure.text.Component append(net.kyori.adventure.text.ComponentLike[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.append([Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component append(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.append(Ljava/util/List;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component applyFallbackStyle(net.kyori.adventure.text.format.Style arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.applyFallbackStyle(Lnet/kyori/adventure/text/format/Style;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component applyFallbackStyle(net.kyori.adventure.text.format.StyleBuilderApplicable[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.applyFallbackStyle([Lnet/kyori/adventure/text/format/StyleBuilderApplicable;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    net.kyori.adventure.text.format.Style style();
    net.kyori.adventure.text.Component style(net.kyori.adventure.text.format.Style arg0);
    default net.kyori.adventure.text.Component style(java.util.function.Consumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.style(Ljava/util/function/Consumer;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component style(java.util.function.Consumer arg0, net.kyori.adventure.text.format.Style$Merge$Strategy arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.style(Ljava/util/function/Consumer;Lnet/kyori/adventure/text/format/Style$Merge$Strategy;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component style(net.kyori.adventure.text.format.Style$Builder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.style(Lnet/kyori/adventure/text/format/Style$Builder;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component mergeStyle(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.mergeStyle(Lnet/kyori/adventure/text/Component;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component mergeStyle(net.kyori.adventure.text.Component arg0, net.kyori.adventure.text.format.Style$Merge[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.mergeStyle(Lnet/kyori/adventure/text/Component;[Lnet/kyori/adventure/text/format/Style$Merge;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component mergeStyle(net.kyori.adventure.text.Component arg0, java.util.Set arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.mergeStyle(Lnet/kyori/adventure/text/Component;Ljava/util/Set;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.key.Key font() {
        return null;
    }
    default net.kyori.adventure.text.Component font(net.kyori.adventure.key.Key arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.font(Lnet/kyori/adventure/key/Key;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.format.TextColor color() {
        return null;
    }
    default net.kyori.adventure.text.format.ShadowColor shadowColor() {
        return null;
    }
    default net.kyori.adventure.text.Component color(net.kyori.adventure.text.format.TextColor arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.color(Lnet/kyori/adventure/text/format/TextColor;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component colorIfAbsent(net.kyori.adventure.text.format.TextColor arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.colorIfAbsent(Lnet/kyori/adventure/text/format/TextColor;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component shadowColor(net.kyori.adventure.util.ARGBLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.shadowColor(Lnet/kyori/adventure/util/ARGBLike;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component shadowColorIfAbsent(net.kyori.adventure.util.ARGBLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.shadowColorIfAbsent(Lnet/kyori/adventure/util/ARGBLike;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default boolean hasDecoration(net.kyori.adventure.text.format.TextDecoration arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.hasDecoration(Lnet/kyori/adventure/text/format/TextDecoration;)Z");
        return false;
    }
    default net.kyori.adventure.text.Component decorate(net.kyori.adventure.text.format.TextDecoration arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.decorate(Lnet/kyori/adventure/text/format/TextDecoration;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.format.TextDecoration$State decoration(net.kyori.adventure.text.format.TextDecoration arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.decoration(Lnet/kyori/adventure/text/format/TextDecoration;)Lnet/kyori/adventure/text/format/TextDecoration$State;");
        return null;
    }
    default net.kyori.adventure.text.Component decoration(net.kyori.adventure.text.format.TextDecoration arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.decoration(Lnet/kyori/adventure/text/format/TextDecoration;Z)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component decoration(net.kyori.adventure.text.format.TextDecoration arg0, net.kyori.adventure.text.format.TextDecoration$State arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.decoration(Lnet/kyori/adventure/text/format/TextDecoration;Lnet/kyori/adventure/text/format/TextDecoration$State;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component decorationIfAbsent(net.kyori.adventure.text.format.TextDecoration arg0, net.kyori.adventure.text.format.TextDecoration$State arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.decorationIfAbsent(Lnet/kyori/adventure/text/format/TextDecoration;Lnet/kyori/adventure/text/format/TextDecoration$State;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default java.util.Map decorations() {
        return java.util.Collections.emptyMap();
    }
    default net.kyori.adventure.text.Component decorations(java.util.Map arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.decorations(Ljava/util/Map;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.event.ClickEvent clickEvent() {
        return null;
    }
    default net.kyori.adventure.text.Component clickEvent(net.kyori.adventure.text.event.ClickEvent arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.clickEvent(Lnet/kyori/adventure/text/event/ClickEvent;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.event.HoverEvent hoverEvent() {
        return null;
    }
    default net.kyori.adventure.text.Component hoverEvent(net.kyori.adventure.text.event.HoverEventSource arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.hoverEvent(Lnet/kyori/adventure/text/event/HoverEventSource;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default java.lang.String insertion() {
        return null;
    }
    default net.kyori.adventure.text.Component insertion(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.insertion(Ljava/lang/String;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default boolean hasStyling() {
        return false;
    }
    default net.kyori.adventure.text.Component replaceText(java.util.function.Consumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.replaceText(Ljava/util/function/Consumer;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component replaceText(net.kyori.adventure.text.TextReplacementConfig arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.replaceText(Lnet/kyori/adventure/text/TextReplacementConfig;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component compact() {
        return null;
    }
    default net.kyori.adventure.text.Component compact(net.kyori.adventure.text.format.Style arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.compact(Lnet/kyori/adventure/text/format/Style;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default java.lang.Iterable iterable(net.kyori.adventure.text.ComponentIteratorType arg0, net.kyori.adventure.text.ComponentIteratorFlag[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.iterable(Lnet/kyori/adventure/text/ComponentIteratorType;[Lnet/kyori/adventure/text/ComponentIteratorFlag;)Ljava/lang/Iterable;");
        return java.util.Collections.emptyList();
    }
    default java.lang.Iterable iterable(net.kyori.adventure.text.ComponentIteratorType arg0, java.util.Set arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.iterable(Lnet/kyori/adventure/text/ComponentIteratorType;Ljava/util/Set;)Ljava/lang/Iterable;");
        return java.util.Collections.emptyList();
    }
    default java.util.Iterator iterator(net.kyori.adventure.text.ComponentIteratorType arg0, net.kyori.adventure.text.ComponentIteratorFlag[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.iterator(Lnet/kyori/adventure/text/ComponentIteratorType;[Lnet/kyori/adventure/text/ComponentIteratorFlag;)Ljava/util/Iterator;");
        return null;
    }
    default java.util.Iterator iterator(net.kyori.adventure.text.ComponentIteratorType arg0, java.util.Set arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.iterator(Lnet/kyori/adventure/text/ComponentIteratorType;Ljava/util/Set;)Ljava/util/Iterator;");
        return null;
    }
    default java.util.Spliterator spliterator(net.kyori.adventure.text.ComponentIteratorType arg0, net.kyori.adventure.text.ComponentIteratorFlag[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.spliterator(Lnet/kyori/adventure/text/ComponentIteratorType;[Lnet/kyori/adventure/text/ComponentIteratorFlag;)Ljava/util/Spliterator;");
        return null;
    }
    default java.util.Spliterator spliterator(net.kyori.adventure.text.ComponentIteratorType arg0, java.util.Set arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.spliterator(Lnet/kyori/adventure/text/ComponentIteratorType;Ljava/util/Set;)Ljava/util/Spliterator;");
        return null;
    }
    default net.kyori.adventure.text.Component replaceText(java.lang.String arg0, net.kyori.adventure.text.ComponentLike arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.replaceText(Ljava/lang/String;Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component replaceText(java.util.regex.Pattern arg0, java.util.function.Function arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.replaceText(Ljava/util/regex/Pattern;Ljava/util/function/Function;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component replaceFirstText(java.lang.String arg0, net.kyori.adventure.text.ComponentLike arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.replaceFirstText(Ljava/lang/String;Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component replaceFirstText(java.util.regex.Pattern arg0, java.util.function.Function arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.replaceFirstText(Ljava/util/regex/Pattern;Ljava/util/function/Function;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component replaceText(java.lang.String arg0, net.kyori.adventure.text.ComponentLike arg1, int arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.replaceText(Ljava/lang/String;Lnet/kyori/adventure/text/ComponentLike;I)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component replaceText(java.util.regex.Pattern arg0, java.util.function.Function arg1, int arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.replaceText(Ljava/util/regex/Pattern;Ljava/util/function/Function;I)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component replaceText(java.lang.String arg0, net.kyori.adventure.text.ComponentLike arg1, net.kyori.adventure.util.IntFunction2 arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.replaceText(Ljava/lang/String;Lnet/kyori/adventure/text/ComponentLike;Lnet/kyori/adventure/util/IntFunction2;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    default net.kyori.adventure.text.Component replaceText(java.util.regex.Pattern arg0, java.util.function.Function arg1, net.kyori.adventure.util.IntFunction2 arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.replaceText(Ljava/util/regex/Pattern;Ljava/util/function/Function;Lnet/kyori/adventure/util/IntFunction2;)Lnet/kyori/adventure/text/Component;");
        return this;
    }
    net.kyori.adventure.text.ComponentBuilder toBuilder();
    default void componentBuilderApply(net.kyori.adventure.text.ComponentBuilder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.componentBuilderApply(Lnet/kyori/adventure/text/ComponentBuilder;)V");
    }
    default net.kyori.adventure.text.Component asComponent() {
        return null;
    }
    default net.kyori.adventure.text.event.HoverEvent asHoverEvent(java.util.function.UnaryOperator arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.Component.asHoverEvent(Ljava/util/function/UnaryOperator;)Lnet/kyori/adventure/text/event/HoverEvent;");
        return null;
    }
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
}
