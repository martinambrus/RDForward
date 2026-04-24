package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class PlayerPickItemEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    protected PlayerPickItemEvent(org.bukkit.entity.Player arg0, boolean arg1, int arg2, int arg3) { super((org.bukkit.entity.Player) null); }
    protected PlayerPickItemEvent() { super((org.bukkit.entity.Player) null); }
    public boolean isIncludeData() {
        return false;
    }
    public int getTargetSlot() {
        return 0;
    }
    public void setTargetSlot(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerPickItemEvent.setTargetSlot(I)V");
    }
    public int getSourceSlot() {
        return 0;
    }
    public void setSourceSlot(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerPickItemEvent.setSourceSlot(I)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerPickItemEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
