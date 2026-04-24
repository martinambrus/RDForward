package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerInsertLecternBookEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerInsertLecternBookEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, org.bukkit.inventory.ItemStack arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerInsertLecternBookEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.block.Block getBlock() {
        return null;
    }
    public org.bukkit.block.Lectern getLectern() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getBook() {
        return null;
    }
    public void setBook(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerInsertLecternBookEvent.setBook(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerInsertLecternBookEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
