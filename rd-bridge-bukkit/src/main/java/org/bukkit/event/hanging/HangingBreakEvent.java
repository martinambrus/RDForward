package org.bukkit.event.hanging;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class HangingBreakEvent extends org.bukkit.event.hanging.HangingEvent implements org.bukkit.event.Cancellable {
    public HangingBreakEvent(org.bukkit.entity.Hanging arg0, org.bukkit.event.hanging.HangingBreakEvent$RemoveCause arg1) { super((org.bukkit.entity.Hanging) null); }
    public HangingBreakEvent() { super((org.bukkit.entity.Hanging) null); }
    public org.bukkit.event.hanging.HangingBreakEvent$RemoveCause getCause() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.hanging.HangingBreakEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
