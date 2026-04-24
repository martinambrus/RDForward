package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerItemFrameChangeEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerItemFrameChangeEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.ItemFrame arg1, org.bukkit.inventory.ItemStack arg2, io.papermc.paper.event.player.PlayerItemFrameChangeEvent$ItemFrameChangeAction arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerItemFrameChangeEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.entity.ItemFrame getItemFrame() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getItemStack() {
        return null;
    }
    public void setItemStack(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerItemFrameChangeEvent.setItemStack(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public io.papermc.paper.event.player.PlayerItemFrameChangeEvent$ItemFrameChangeAction getAction() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerItemFrameChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
