package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerOpenSignEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerOpenSignEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Sign arg1, org.bukkit.block.sign.Side arg2, io.papermc.paper.event.player.PlayerOpenSignEvent$Cause arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerOpenSignEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.block.Sign getSign() {
        return null;
    }
    public org.bukkit.block.sign.Side getSide() {
        return null;
    }
    public io.papermc.paper.event.player.PlayerOpenSignEvent$Cause getCause() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerOpenSignEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
