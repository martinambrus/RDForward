package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PigZapEvent extends com.destroystokyo.paper.event.entity.EntityZapEvent implements org.bukkit.event.Cancellable {
    public PigZapEvent(org.bukkit.entity.Pig arg0, org.bukkit.entity.LightningStrike arg1, org.bukkit.entity.PigZombie arg2) { super((org.bukkit.entity.Entity) null, (org.bukkit.entity.LightningStrike) null, (org.bukkit.entity.Entity) null); }
    public PigZapEvent() { super((org.bukkit.entity.Entity) null, (org.bukkit.entity.LightningStrike) null, (org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Pig getEntity() {
        return null;
    }
    public org.bukkit.entity.LightningStrike getLightning() {
        return null;
    }
    public org.bukkit.entity.PigZombie getPigZombie() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PigZapEvent.setCancelled(Z)V");
    }
}
