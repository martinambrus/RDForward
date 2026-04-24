package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerSwapHandItemsEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerSwapHandItemsEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.ItemStack arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerSwapHandItemsEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.inventory.ItemStack getMainHandItem() {
        return null;
    }
    public void setMainHandItem(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerSwapHandItemsEvent.setMainHandItem(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public org.bukkit.inventory.ItemStack getOffHandItem() {
        return null;
    }
    public void setOffHandItem(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerSwapHandItemsEvent.setOffHandItem(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerSwapHandItemsEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
