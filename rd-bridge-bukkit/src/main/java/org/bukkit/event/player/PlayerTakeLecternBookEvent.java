package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerTakeLecternBookEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerTakeLecternBookEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Lectern arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerTakeLecternBookEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.block.Lectern getLectern() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getBook() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerTakeLecternBookEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
