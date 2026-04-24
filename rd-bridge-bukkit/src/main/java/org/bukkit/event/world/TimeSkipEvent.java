package org.bukkit.event.world;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class TimeSkipEvent extends org.bukkit.event.world.WorldEvent implements org.bukkit.event.Cancellable {
    public TimeSkipEvent(org.bukkit.World arg0, org.bukkit.event.world.TimeSkipEvent$SkipReason arg1, long arg2) { super((org.bukkit.World) null); }
    public TimeSkipEvent() { super((org.bukkit.World) null); }
    public org.bukkit.event.world.TimeSkipEvent$SkipReason getSkipReason() {
        return null;
    }
    public long getSkipAmount() {
        return 0L;
    }
    public void setSkipAmount(long arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.TimeSkipEvent.setSkipAmount(J)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.TimeSkipEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
