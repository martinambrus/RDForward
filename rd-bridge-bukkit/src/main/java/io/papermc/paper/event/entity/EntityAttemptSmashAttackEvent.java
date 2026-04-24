package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityAttemptSmashAttackEvent extends org.bukkit.event.entity.EntityEvent {
    public EntityAttemptSmashAttackEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.entity.LivingEntity arg1, org.bukkit.inventory.ItemStack arg2, boolean arg3) { super((org.bukkit.entity.Entity) null); }
    public EntityAttemptSmashAttackEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.LivingEntity getTarget() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getWeapon() {
        return null;
    }
    public boolean getOriginalResult() {
        return false;
    }
    public org.bukkit.event.Event$Result getResult() {
        return null;
    }
    public void setResult(org.bukkit.event.Event$Result arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityAttemptSmashAttackEvent.setResult(Lorg/bukkit/event/Event$Result;)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
