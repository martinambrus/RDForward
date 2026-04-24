package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityDyeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityDyeEvent(org.bukkit.entity.Entity arg0, org.bukkit.DyeColor arg1, org.bukkit.entity.Player arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityDyeEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.DyeColor getColor() {
        return null;
    }
    public void setColor(org.bukkit.DyeColor arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityDyeEvent.setColor(Lorg/bukkit/DyeColor;)V");
    }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityDyeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
