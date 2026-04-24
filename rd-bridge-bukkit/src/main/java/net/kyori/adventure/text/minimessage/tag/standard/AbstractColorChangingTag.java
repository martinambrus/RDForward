package net.kyori.adventure.text.minimessage.tag.standard;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
abstract class AbstractColorChangingTag implements net.kyori.adventure.text.minimessage.tag.Modifying, net.kyori.examination.Examinable {
    protected AbstractColorChangingTag() {}
    protected final int size() {
        return 0;
    }
    public final void visit(net.kyori.adventure.text.minimessage.tree.Node arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.tag.standard.AbstractColorChangingTag.visit(Lnet/kyori/adventure/text/minimessage/tree/Node;I)V");
    }
    public final void postVisit() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.tag.standard.AbstractColorChangingTag.postVisit()V");
    }
    public final net.kyori.adventure.text.Component apply(net.kyori.adventure.text.Component arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.tag.standard.AbstractColorChangingTag.apply(Lnet/kyori/adventure/text/Component;I)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    protected abstract void init();
    protected abstract void advanceColor();
    protected abstract net.kyori.adventure.text.format.TextColor color();
    protected abstract java.util.function.Consumer preserveData();
    public abstract java.util.stream.Stream examinableProperties();
    public final java.lang.String toString() {
        return null;
    }
    public abstract boolean equals(java.lang.Object arg0);
    public abstract int hashCode();
}
