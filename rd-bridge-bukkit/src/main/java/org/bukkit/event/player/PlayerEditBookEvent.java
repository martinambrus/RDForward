package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerEditBookEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerEditBookEvent(org.bukkit.entity.Player arg0, int arg1, org.bukkit.inventory.meta.BookMeta arg2, org.bukkit.inventory.meta.BookMeta arg3, boolean arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerEditBookEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.inventory.meta.BookMeta getPreviousBookMeta() {
        return null;
    }
    public org.bukkit.inventory.meta.BookMeta getNewBookMeta() {
        return null;
    }
    public int getSlot() {
        return 0;
    }
    public void setNewBookMeta(org.bukkit.inventory.meta.BookMeta arg0) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerEditBookEvent.setNewBookMeta(Lorg/bukkit/inventory/meta/BookMeta;)V");
    }
    public boolean isSigning() {
        return false;
    }
    public void setSigning(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerEditBookEvent.setSigning(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerEditBookEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
