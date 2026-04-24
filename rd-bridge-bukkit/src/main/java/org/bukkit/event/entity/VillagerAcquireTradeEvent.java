package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class VillagerAcquireTradeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public VillagerAcquireTradeEvent(org.bukkit.entity.AbstractVillager arg0, org.bukkit.inventory.MerchantRecipe arg1) { super((org.bukkit.entity.Entity) null); }
    public VillagerAcquireTradeEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.AbstractVillager getEntity() {
        return null;
    }
    public org.bukkit.inventory.MerchantRecipe getRecipe() {
        return null;
    }
    public void setRecipe(org.bukkit.inventory.MerchantRecipe arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.VillagerAcquireTradeEvent.setRecipe(Lorg/bukkit/inventory/MerchantRecipe;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.VillagerAcquireTradeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
