package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerRiptideEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerRiptideEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.util.Vector arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerRiptideEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerRiptideEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public org.bukkit.util.Vector getVelocity() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerRiptideEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
