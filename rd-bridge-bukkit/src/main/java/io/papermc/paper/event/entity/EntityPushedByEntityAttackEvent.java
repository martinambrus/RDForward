package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityPushedByEntityAttackEvent extends io.papermc.paper.event.entity.EntityKnockbackEvent {
    public EntityPushedByEntityAttackEvent(org.bukkit.entity.Entity arg0, io.papermc.paper.event.entity.EntityKnockbackEvent$Cause arg1, org.bukkit.entity.Entity arg2, org.bukkit.util.Vector arg3) { super((org.bukkit.entity.Entity) null, (io.papermc.paper.event.entity.EntityKnockbackEvent$Cause) null, (org.bukkit.util.Vector) null); }
    public EntityPushedByEntityAttackEvent() { super((org.bukkit.entity.Entity) null, (io.papermc.paper.event.entity.EntityKnockbackEvent$Cause) null, (org.bukkit.util.Vector) null); }
    public org.bukkit.entity.Entity getPushedBy() {
        return null;
    }
    public org.bukkit.util.Vector getAcceleration() {
        return null;
    }
    public void setAcceleration(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent.setAcceleration(Lorg/bukkit/util/Vector;)V");
    }
}
