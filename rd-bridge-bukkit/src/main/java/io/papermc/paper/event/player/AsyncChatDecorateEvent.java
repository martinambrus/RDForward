package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class AsyncChatDecorateEvent extends org.bukkit.event.server.ServerEvent implements org.bukkit.event.Cancellable {
    public AsyncChatDecorateEvent(org.bukkit.entity.Player arg0, net.kyori.adventure.text.Component arg1) {}
    public AsyncChatDecorateEvent() {}
    public org.bukkit.entity.Player player() {
        return null;
    }
    public net.kyori.adventure.text.Component originalMessage() {
        return null;
    }
    public net.kyori.adventure.text.Component result() {
        return null;
    }
    public void result(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.AsyncChatDecorateEvent.result(Lnet/kyori/adventure/text/Component;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.AsyncChatDecorateEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
