package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class VillagerCareerChangeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public VillagerCareerChangeEvent(org.bukkit.entity.Villager arg0, org.bukkit.entity.Villager$Profession arg1, org.bukkit.event.entity.VillagerCareerChangeEvent$ChangeReason arg2) { super((org.bukkit.entity.Entity) null); }
    public VillagerCareerChangeEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Villager getEntity() {
        return null;
    }
    public org.bukkit.entity.Villager$Profession getProfession() {
        return null;
    }
    public void setProfession(org.bukkit.entity.Villager$Profession arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.VillagerCareerChangeEvent.setProfession(Lorg/bukkit/entity/Villager$Profession;)V");
    }
    public org.bukkit.event.entity.VillagerCareerChangeEvent$ChangeReason getReason() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.VillagerCareerChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
