package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityDamageItemEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityDamageItemEvent(org.bukkit.entity.Entity arg0, org.bukkit.inventory.ItemStack arg1, int arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityDamageItemEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public int getDamage() {
        return 0;
    }
    public void setDamage(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityDamageItemEvent.setDamage(I)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityDamageItemEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
