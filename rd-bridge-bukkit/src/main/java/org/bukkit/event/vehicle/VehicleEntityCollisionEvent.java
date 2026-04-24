package org.bukkit.event.vehicle;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class VehicleEntityCollisionEvent extends org.bukkit.event.vehicle.VehicleCollisionEvent implements org.bukkit.event.Cancellable {
    public VehicleEntityCollisionEvent(org.bukkit.entity.Vehicle arg0, org.bukkit.entity.Entity arg1) { super((org.bukkit.entity.Vehicle) null); }
    public VehicleEntityCollisionEvent() { super((org.bukkit.entity.Vehicle) null); }
    public org.bukkit.entity.Entity getEntity() {
        return null;
    }
    public boolean isPickupCancelled() {
        return false;
    }
    public void setPickupCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.vehicle.VehicleEntityCollisionEvent.setPickupCancelled(Z)V");
    }
    public boolean isCollisionCancelled() {
        return false;
    }
    public void setCollisionCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.vehicle.VehicleEntityCollisionEvent.setCollisionCancelled(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.vehicle.VehicleEntityCollisionEvent.setCancelled(Z)V");
    }
}
