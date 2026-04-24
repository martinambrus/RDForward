package org.bukkit.event.weather;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ThunderChangeEvent extends org.bukkit.event.weather.WeatherEvent implements org.bukkit.event.Cancellable {
    public ThunderChangeEvent(org.bukkit.World arg0, boolean arg1, org.bukkit.event.weather.ThunderChangeEvent$Cause arg2) { super((org.bukkit.World) null); }
    public ThunderChangeEvent(org.bukkit.World arg0, boolean arg1) { super((org.bukkit.World) null); }
    public ThunderChangeEvent() { super((org.bukkit.World) null); }
    public boolean toThunderState() {
        return false;
    }
    public org.bukkit.event.weather.ThunderChangeEvent$Cause getCause() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.weather.ThunderChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
