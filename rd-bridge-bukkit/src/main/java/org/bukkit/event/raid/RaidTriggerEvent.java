package org.bukkit.event.raid;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class RaidTriggerEvent extends org.bukkit.event.raid.RaidEvent implements org.bukkit.event.Cancellable {
    public RaidTriggerEvent(org.bukkit.Raid arg0, org.bukkit.World arg1, org.bukkit.entity.Player arg2) { super((org.bukkit.Raid) null, (org.bukkit.World) null); }
    public RaidTriggerEvent() { super((org.bukkit.Raid) null, (org.bukkit.World) null); }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.raid.RaidTriggerEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
