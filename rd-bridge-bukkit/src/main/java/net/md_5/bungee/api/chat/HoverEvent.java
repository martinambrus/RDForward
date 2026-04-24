package net.md_5.bungee.api.chat;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class HoverEvent {
    public HoverEvent(net.md_5.bungee.api.chat.HoverEvent$Action arg0, net.md_5.bungee.api.chat.hover.content.Content[] arg1) {}
    public HoverEvent(net.md_5.bungee.api.chat.HoverEvent$Action arg0, net.md_5.bungee.api.chat.BaseComponent[] arg1) {}
    public HoverEvent(net.md_5.bungee.api.chat.HoverEvent$Action arg0, java.util.List arg1) {}
    public HoverEvent() {}
    public net.md_5.bungee.api.chat.BaseComponent[] getValue() {
        return new net.md_5.bungee.api.chat.BaseComponent[0];
    }
    public void addContent(net.md_5.bungee.api.chat.hover.content.Content arg0) throws java.lang.UnsupportedOperationException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.md_5.bungee.api.chat.HoverEvent.addContent(Lnet/md_5/bungee/api/chat/hover/content/Content;)V");
    }
    public static java.lang.Class getClass(net.md_5.bungee.api.chat.HoverEvent$Action arg0, boolean arg1) {
        return null;
    }
    public net.md_5.bungee.api.chat.HoverEvent$Action getAction() {
        return null;
    }
    public java.util.List getContents() {
        return java.util.Collections.emptyList();
    }
    public boolean isLegacy() {
        return false;
    }
    public java.lang.String toString() {
        return null;
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.md_5.bungee.api.chat.HoverEvent.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public int hashCode() {
        return 0;
    }
    public void setLegacy(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.md_5.bungee.api.chat.HoverEvent.setLegacy(Z)V");
    }
}
