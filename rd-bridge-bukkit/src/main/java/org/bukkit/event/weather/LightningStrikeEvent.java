package org.bukkit.event.weather;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class LightningStrikeEvent extends org.bukkit.event.weather.WeatherEvent implements org.bukkit.event.Cancellable {
    public LightningStrikeEvent(org.bukkit.World arg0, org.bukkit.entity.LightningStrike arg1) { super((org.bukkit.World) null); }
    public LightningStrikeEvent(org.bukkit.World arg0, org.bukkit.entity.LightningStrike arg1, org.bukkit.event.weather.LightningStrikeEvent$Cause arg2) { super((org.bukkit.World) null); }
    public LightningStrikeEvent() { super((org.bukkit.World) null); }
    public org.bukkit.entity.LightningStrike getLightning() {
        return null;
    }
    public org.bukkit.event.weather.LightningStrikeEvent$Cause getCause() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.weather.LightningStrikeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
