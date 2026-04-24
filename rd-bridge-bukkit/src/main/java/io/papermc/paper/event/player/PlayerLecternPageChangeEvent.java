package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerLecternPageChangeEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerLecternPageChangeEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Lectern arg1, org.bukkit.inventory.ItemStack arg2, io.papermc.paper.event.player.PlayerLecternPageChangeEvent$PageChangeDirection arg3, int arg4, int arg5) { super((org.bukkit.entity.Player) null); }
    public PlayerLecternPageChangeEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.block.Lectern getLectern() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getBook() {
        return null;
    }
    public io.papermc.paper.event.player.PlayerLecternPageChangeEvent$PageChangeDirection getPageChangeDirection() {
        return null;
    }
    public int getOldPage() {
        return 0;
    }
    public int getNewPage() {
        return 0;
    }
    public void setNewPage(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerLecternPageChangeEvent.setNewPage(I)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerLecternPageChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
