package io.papermc.paper.event.world.border;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class WorldBorderBoundsChangeEvent extends io.papermc.paper.event.world.border.WorldBorderEvent implements org.bukkit.event.Cancellable {
    public WorldBorderBoundsChangeEvent(org.bukkit.World arg0, org.bukkit.WorldBorder arg1, io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent$Type arg2, double arg3, double arg4, long arg5) { super((org.bukkit.World) null, (org.bukkit.WorldBorder) null); }
    public WorldBorderBoundsChangeEvent() { super((org.bukkit.World) null, (org.bukkit.WorldBorder) null); }
    public io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent$Type getType() {
        return null;
    }
    public double getOldSize() {
        return 0.0;
    }
    public double getNewSize() {
        return 0.0;
    }
    public void setNewSize(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.setNewSize(D)V");
    }
    public long getDurationTicks() {
        return 0L;
    }
    public void setDurationTicks(long arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.setDurationTicks(J)V");
    }
    public long getDuration() {
        return 0L;
    }
    public void setDuration(long arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.setDuration(J)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
