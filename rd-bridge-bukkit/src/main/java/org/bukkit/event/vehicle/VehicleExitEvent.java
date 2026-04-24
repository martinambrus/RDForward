package org.bukkit.event.vehicle;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class VehicleExitEvent extends org.bukkit.event.vehicle.VehicleEvent implements org.bukkit.event.Cancellable {
    public VehicleExitEvent(org.bukkit.entity.Vehicle arg0, org.bukkit.entity.LivingEntity arg1, boolean arg2) { super((org.bukkit.entity.Vehicle) null); }
    public VehicleExitEvent(org.bukkit.entity.Vehicle arg0, org.bukkit.entity.LivingEntity arg1) { super((org.bukkit.entity.Vehicle) null); }
    public VehicleExitEvent() { super((org.bukkit.entity.Vehicle) null); }
    public org.bukkit.entity.LivingEntity getExited() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.vehicle.VehicleExitEvent.setCancelled(Z)V");
    }
    public boolean isCancellable() {
        return false;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
