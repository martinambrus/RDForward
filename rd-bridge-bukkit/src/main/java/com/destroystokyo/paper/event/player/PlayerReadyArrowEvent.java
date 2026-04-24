package com.destroystokyo.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerReadyArrowEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerReadyArrowEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.ItemStack arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerReadyArrowEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.inventory.ItemStack getBow() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getArrow() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerReadyArrowEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
