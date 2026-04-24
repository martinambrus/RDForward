package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class HorseJumpEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public HorseJumpEvent(org.bukkit.entity.AbstractHorse arg0, float arg1) { super((org.bukkit.entity.Entity) null); }
    public HorseJumpEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.AbstractHorse getEntity() {
        return null;
    }
    public float getPower() {
        return 0.0f;
    }
    public void setPower(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.HorseJumpEvent.setPower(F)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.HorseJumpEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
