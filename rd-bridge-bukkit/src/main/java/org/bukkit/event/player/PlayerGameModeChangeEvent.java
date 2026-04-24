package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerGameModeChangeEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerGameModeChangeEvent(org.bukkit.entity.Player arg0, org.bukkit.GameMode arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerGameModeChangeEvent(org.bukkit.entity.Player arg0, org.bukkit.GameMode arg1, org.bukkit.event.player.PlayerGameModeChangeEvent$Cause arg2, net.kyori.adventure.text.Component arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerGameModeChangeEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.GameMode getNewGameMode() {
        return null;
    }
    public org.bukkit.event.player.PlayerGameModeChangeEvent$Cause getCause() {
        return null;
    }
    public net.kyori.adventure.text.Component cancelMessage() {
        return null;
    }
    public void cancelMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerGameModeChangeEvent.cancelMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerGameModeChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
