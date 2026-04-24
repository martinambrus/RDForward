package com.destroystokyo.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityKnockbackByEntityEvent extends io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent {
    public EntityKnockbackByEntityEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.entity.Entity arg1, io.papermc.paper.event.entity.EntityKnockbackEvent$Cause arg2, float arg3, org.bukkit.util.Vector arg4) { super((org.bukkit.entity.Entity) null, (io.papermc.paper.event.entity.EntityKnockbackEvent$Cause) null, (org.bukkit.entity.Entity) null, (org.bukkit.util.Vector) null); }
    public EntityKnockbackByEntityEvent() { super((org.bukkit.entity.Entity) null, (io.papermc.paper.event.entity.EntityKnockbackEvent$Cause) null, (org.bukkit.entity.Entity) null, (org.bukkit.util.Vector) null); }
    public org.bukkit.entity.LivingEntity getEntity() {
        return null;
    }
    public float getKnockbackStrength() {
        return 0.0f;
    }
    public org.bukkit.entity.Entity getHitBy() {
        return null;
    }
}
