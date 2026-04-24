package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityInsideBlockEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityInsideBlockEvent(org.bukkit.entity.Entity arg0, org.bukkit.block.Block arg1) { super((org.bukkit.entity.Entity) null); }
    public EntityInsideBlockEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.block.Block getBlock() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityInsideBlockEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
