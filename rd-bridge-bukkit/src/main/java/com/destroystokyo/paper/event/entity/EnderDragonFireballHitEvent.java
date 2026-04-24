package com.destroystokyo.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EnderDragonFireballHitEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EnderDragonFireballHitEvent(org.bukkit.entity.DragonFireball arg0, java.util.Collection arg1, org.bukkit.entity.AreaEffectCloud arg2) { super((org.bukkit.entity.Entity) null); }
    public EnderDragonFireballHitEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.DragonFireball getEntity() {
        return null;
    }
    public java.util.Collection getTargets() {
        return java.util.Collections.emptyList();
    }
    public org.bukkit.entity.AreaEffectCloud getAreaEffectCloud() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.EnderDragonFireballHitEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
