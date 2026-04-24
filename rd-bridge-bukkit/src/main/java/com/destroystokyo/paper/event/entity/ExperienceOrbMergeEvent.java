package com.destroystokyo.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ExperienceOrbMergeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public ExperienceOrbMergeEvent(org.bukkit.entity.ExperienceOrb arg0, org.bukkit.entity.ExperienceOrb arg1) { super((org.bukkit.entity.Entity) null); }
    public ExperienceOrbMergeEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.ExperienceOrb getMergeTarget() {
        return null;
    }
    public org.bukkit.entity.ExperienceOrb getMergeSource() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.ExperienceOrbMergeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
