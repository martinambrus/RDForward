package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class CreeperPowerEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public CreeperPowerEvent(org.bukkit.entity.Creeper arg0, org.bukkit.entity.LightningStrike arg1, org.bukkit.event.entity.CreeperPowerEvent$PowerCause arg2) { super((org.bukkit.entity.Entity) null); }
    public CreeperPowerEvent(org.bukkit.entity.Creeper arg0, org.bukkit.event.entity.CreeperPowerEvent$PowerCause arg1) { super((org.bukkit.entity.Entity) null); }
    public CreeperPowerEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Creeper getEntity() {
        return null;
    }
    public org.bukkit.entity.LightningStrike getLightning() {
        return null;
    }
    public org.bukkit.event.entity.CreeperPowerEvent$PowerCause getCause() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.CreeperPowerEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
