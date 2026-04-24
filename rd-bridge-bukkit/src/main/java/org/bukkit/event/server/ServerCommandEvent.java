package org.bukkit.event.server;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ServerCommandEvent extends org.bukkit.event.server.ServerEvent implements org.bukkit.event.Cancellable {
    public ServerCommandEvent(org.bukkit.command.CommandSender arg0, java.lang.String arg1) {}
    public ServerCommandEvent() {}
    public org.bukkit.command.CommandSender getSender() {
        return null;
    }
    public java.lang.String getCommand() {
        return null;
    }
    public void setCommand(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.server.ServerCommandEvent.setCommand(Ljava/lang/String;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.server.ServerCommandEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
