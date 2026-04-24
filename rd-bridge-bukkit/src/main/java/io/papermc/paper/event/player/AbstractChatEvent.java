package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class AbstractChatEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    protected AbstractChatEvent() { super((org.bukkit.entity.Player) null); }
    public final java.util.Set viewers() {
        return java.util.Collections.emptySet();
    }
    public final void renderer(io.papermc.paper.chat.ChatRenderer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.AbstractChatEvent.renderer(Lio/papermc/paper/chat/ChatRenderer;)V");
    }
    public final io.papermc.paper.chat.ChatRenderer renderer() {
        return null;
    }
    public final net.kyori.adventure.text.Component message() {
        return null;
    }
    public final void message(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.AbstractChatEvent.message(Lnet/kyori/adventure/text/Component;)V");
    }
    public final net.kyori.adventure.text.Component originalMessage() {
        return null;
    }
    public final net.kyori.adventure.chat.SignedMessage signedMessage() {
        return null;
    }
    public final boolean isCancelled() {
        return false;
    }
    public final void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.AbstractChatEvent.setCancelled(Z)V");
    }
}
