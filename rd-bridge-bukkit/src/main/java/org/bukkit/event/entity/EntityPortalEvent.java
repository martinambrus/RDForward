package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityPortalEvent extends org.bukkit.event.entity.EntityTeleportEvent {
    public EntityPortalEvent(org.bukkit.entity.Entity arg0, org.bukkit.Location arg1, org.bukkit.Location arg2) { super((org.bukkit.entity.Entity) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public EntityPortalEvent(org.bukkit.entity.Entity arg0, org.bukkit.Location arg1, org.bukkit.Location arg2, int arg3) { super((org.bukkit.entity.Entity) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public EntityPortalEvent(org.bukkit.entity.Entity arg0, org.bukkit.Location arg1, org.bukkit.Location arg2, int arg3, boolean arg4, int arg5) { super((org.bukkit.entity.Entity) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public EntityPortalEvent(org.bukkit.entity.Entity arg0, org.bukkit.Location arg1, org.bukkit.Location arg2, int arg3, boolean arg4, int arg5, org.bukkit.PortalType arg6) { super((org.bukkit.entity.Entity) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public EntityPortalEvent() { super((org.bukkit.entity.Entity) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public org.bukkit.Location getTo() {
        return null;
    }
    public void setTo(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityPortalEvent.setTo(Lorg/bukkit/Location;)V");
    }
    public org.bukkit.PortalType getPortalType() {
        return null;
    }
    public void setSearchRadius(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityPortalEvent.setSearchRadius(I)V");
    }
    public int getSearchRadius() {
        return 0;
    }
    public boolean getCanCreatePortal() {
        return false;
    }
    public void setCanCreatePortal(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityPortalEvent.setCanCreatePortal(Z)V");
    }
    public void setCreationRadius(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityPortalEvent.setCreationRadius(I)V");
    }
    public int getCreationRadius() {
        return 0;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
