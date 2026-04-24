package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerItemDamageEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerItemDamageEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1, int arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerItemDamageEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1, int arg2, int arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerItemDamageEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public int getDamage() {
        return 0;
    }
    public void setDamage(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerItemDamageEvent.setDamage(I)V");
    }
    public int getOriginalDamage() {
        return 0;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerItemDamageEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
