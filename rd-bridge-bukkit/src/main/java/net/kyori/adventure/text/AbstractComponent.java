package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class AbstractComponent implements net.kyori.adventure.text.Component {
    protected AbstractComponent(java.util.List arg0, net.kyori.adventure.text.format.Style arg1) {}
    protected AbstractComponent() {}
    public final java.util.List children() {
        return java.util.Collections.emptyList();
    }
    public final net.kyori.adventure.text.format.Style style() {
        return null;
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.AbstractComponent.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public int hashCode() {
        return 0;
    }
    public abstract java.lang.String toString();
    public net.kyori.adventure.text.ComponentBuilder toBuilder() {
        return null;
    }
}
