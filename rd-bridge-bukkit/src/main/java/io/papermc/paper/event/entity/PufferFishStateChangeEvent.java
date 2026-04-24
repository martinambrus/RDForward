package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PufferFishStateChangeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public PufferFishStateChangeEvent(org.bukkit.entity.PufferFish arg0, int arg1) { super((org.bukkit.entity.Entity) null); }
    public PufferFishStateChangeEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.PufferFish getEntity() {
        return null;
    }
    public int getNewPuffState() {
        return 0;
    }
    public boolean isInflating() {
        return false;
    }
    public boolean isDeflating() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.PufferFishStateChangeEvent.setCancelled(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
