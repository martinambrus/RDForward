package com.destroystokyo.paper.event.server;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class AsyncTabCompleteEvent extends org.bukkit.event.Event implements org.bukkit.event.Cancellable {
    public AsyncTabCompleteEvent(org.bukkit.command.CommandSender arg0, java.lang.String arg1, boolean arg2, org.bukkit.Location arg3) {}
    public AsyncTabCompleteEvent(org.bukkit.command.CommandSender arg0, java.util.List arg1, java.lang.String arg2, boolean arg3, org.bukkit.Location arg4) {}
    public AsyncTabCompleteEvent() {}
    public org.bukkit.command.CommandSender getSender() {
        return null;
    }
    public java.util.List getCompletions() {
        return java.util.Collections.emptyList();
    }
    public void setCompletions(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.setCompletions(Ljava/util/List;)V");
    }
    public java.util.List completions() {
        return java.util.Collections.emptyList();
    }
    public void completions(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.completions(Ljava/util/List;)V");
    }
    public java.lang.String getBuffer() {
        return null;
    }
    public boolean isCommand() {
        return false;
    }
    public org.bukkit.Location getLocation() {
        return null;
    }
    public boolean isHandled() {
        return false;
    }
    public void setHandled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.setHandled(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
