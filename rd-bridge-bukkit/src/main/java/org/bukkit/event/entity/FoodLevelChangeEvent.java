package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FoodLevelChangeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public FoodLevelChangeEvent(org.bukkit.entity.HumanEntity arg0, int arg1) { super((org.bukkit.entity.Entity) null); }
    public FoodLevelChangeEvent(org.bukkit.entity.HumanEntity arg0, int arg1, org.bukkit.inventory.ItemStack arg2) { super((org.bukkit.entity.Entity) null); }
    public FoodLevelChangeEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.HumanEntity getEntity() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public int getFoodLevel() {
        return 0;
    }
    public void setFoodLevel(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.FoodLevelChangeEvent.setFoodLevel(I)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.FoodLevelChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
