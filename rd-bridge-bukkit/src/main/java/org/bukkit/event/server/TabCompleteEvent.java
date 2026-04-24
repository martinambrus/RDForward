package org.bukkit.event.server;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class TabCompleteEvent extends org.bukkit.event.Event implements org.bukkit.event.Cancellable {
    public TabCompleteEvent(org.bukkit.command.CommandSender arg0, java.lang.String arg1, java.util.List arg2) {}
    public TabCompleteEvent(org.bukkit.command.CommandSender arg0, java.lang.String arg1, java.util.List arg2, boolean arg3, org.bukkit.Location arg4) {}
    public TabCompleteEvent() {}
    public org.bukkit.command.CommandSender getSender() {
        return null;
    }
    public java.lang.String getBuffer() {
        return null;
    }
    public java.util.List getCompletions() {
        return java.util.Collections.emptyList();
    }
    public void setCompletions(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.server.TabCompleteEvent.setCompletions(Ljava/util/List;)V");
    }
    public boolean isCommand() {
        return false;
    }
    public org.bukkit.Location getLocation() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.server.TabCompleteEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
