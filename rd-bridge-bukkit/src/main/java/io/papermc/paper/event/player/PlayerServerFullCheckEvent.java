package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerServerFullCheckEvent extends org.bukkit.event.Event {
    public PlayerServerFullCheckEvent(com.destroystokyo.paper.profile.PlayerProfile arg0, net.kyori.adventure.text.Component arg1, boolean arg2) {}
    public PlayerServerFullCheckEvent() {}
    public net.kyori.adventure.text.Component kickMessage() {
        return null;
    }
    public void deny(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerServerFullCheckEvent.deny(Lnet/kyori/adventure/text/Component;)V");
    }
    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        return null;
    }
    public void allow(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerServerFullCheckEvent.allow(Z)V");
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
