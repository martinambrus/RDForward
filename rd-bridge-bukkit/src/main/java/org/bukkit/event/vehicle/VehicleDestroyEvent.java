package org.bukkit.event.vehicle;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class VehicleDestroyEvent extends org.bukkit.event.vehicle.VehicleEvent implements org.bukkit.event.Cancellable {
    public VehicleDestroyEvent(org.bukkit.entity.Vehicle arg0, org.bukkit.entity.Entity arg1) { super((org.bukkit.entity.Vehicle) null); }
    public VehicleDestroyEvent() { super((org.bukkit.entity.Vehicle) null); }
    public org.bukkit.entity.Entity getAttacker() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.vehicle.VehicleDestroyEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
