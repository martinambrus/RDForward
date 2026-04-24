package io.papermc.paper.event.connection;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerConnectionValidateLoginEvent extends org.bukkit.event.Event {
    public PlayerConnectionValidateLoginEvent(io.papermc.paper.connection.PlayerConnection arg0, net.kyori.adventure.text.Component arg1) {}
    public PlayerConnectionValidateLoginEvent() {}
    public io.papermc.paper.connection.PlayerConnection getConnection() {
        return null;
    }
    public void allow() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent.allow()V");
    }
    public void kickMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent.kickMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    public net.kyori.adventure.text.Component getKickMessage() {
        return null;
    }
    public boolean isAllowed() {
        return false;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
