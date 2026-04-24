package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PiglinBarterEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public PiglinBarterEvent(org.bukkit.entity.Piglin arg0, org.bukkit.inventory.ItemStack arg1, java.util.List arg2) { super((org.bukkit.entity.Entity) null); }
    public PiglinBarterEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Piglin getEntity() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getInput() {
        return null;
    }
    public java.util.List getOutcome() {
        return java.util.Collections.emptyList();
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PiglinBarterEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
