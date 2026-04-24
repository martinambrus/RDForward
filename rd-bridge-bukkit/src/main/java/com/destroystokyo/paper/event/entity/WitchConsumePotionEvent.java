package com.destroystokyo.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class WitchConsumePotionEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public WitchConsumePotionEvent(org.bukkit.entity.Witch arg0, org.bukkit.inventory.ItemStack arg1) { super((org.bukkit.entity.Entity) null); }
    public WitchConsumePotionEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Witch getEntity() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getPotion() {
        return null;
    }
    public void setPotion(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.WitchConsumePotionEvent.setPotion(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.WitchConsumePotionEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
