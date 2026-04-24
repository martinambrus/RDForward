package net.kyori.adventure.text.minimessage.tag;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Modifying extends net.kyori.adventure.text.minimessage.tag.Tag {
    default void visit(net.kyori.adventure.text.minimessage.tree.Node arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.tag.Modifying.visit(Lnet/kyori/adventure/text/minimessage/tree/Node;I)V");
    }
    default void postVisit() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.minimessage.tag.Modifying.postVisit()V");
    }
    net.kyori.adventure.text.Component apply(net.kyori.adventure.text.Component arg0, int arg1);
}
