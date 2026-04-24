package org.bukkit.event.command;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class UnknownCommandEvent extends org.bukkit.event.Event {
    public UnknownCommandEvent(io.papermc.paper.command.brigadier.CommandSourceStack arg0, java.lang.String arg1, net.kyori.adventure.text.Component arg2) {}
    public UnknownCommandEvent() {}
    public org.bukkit.command.CommandSender getSender() {
        return null;
    }
    public io.papermc.paper.command.brigadier.CommandSourceStack getCommandSource() {
        return null;
    }
    public java.lang.String getCommandLine() {
        return null;
    }
    public java.lang.String getMessage() {
        return null;
    }
    public void setMessage(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.command.UnknownCommandEvent.setMessage(Ljava/lang/String;)V");
    }
    public net.kyori.adventure.text.Component message() {
        return null;
    }
    public void message(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.command.UnknownCommandEvent.message(Lnet/kyori/adventure/text/Component;)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
