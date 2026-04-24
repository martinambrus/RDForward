package org.bukkit.event.server;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BroadcastMessageEvent extends org.bukkit.event.server.ServerEvent implements org.bukkit.event.Cancellable {
    public BroadcastMessageEvent(java.lang.String arg0, java.util.Set arg1) {}
    public BroadcastMessageEvent(boolean arg0, java.lang.String arg1, java.util.Set arg2) {}
    public BroadcastMessageEvent(net.kyori.adventure.text.Component arg0, java.util.Set arg1) {}
    public BroadcastMessageEvent(boolean arg0, net.kyori.adventure.text.Component arg1, java.util.Set arg2) {}
    public BroadcastMessageEvent() {}
    public net.kyori.adventure.text.Component message() {
        return null;
    }
    public void message(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.server.BroadcastMessageEvent.message(Lnet/kyori/adventure/text/Component;)V");
    }
    public java.lang.String getMessage() {
        return null;
    }
    public void setMessage(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.server.BroadcastMessageEvent.setMessage(Ljava/lang/String;)V");
    }
    public java.util.Set getRecipients() {
        return java.util.Collections.emptySet();
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.server.BroadcastMessageEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
