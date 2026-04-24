package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityDamageByBlockEvent extends org.bukkit.event.entity.EntityDamageEvent {
    public EntityDamageByBlockEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Entity arg1, org.bukkit.event.entity.EntityDamageEvent$DamageCause arg2, double arg3) { super((org.bukkit.entity.Entity) null, (org.bukkit.event.entity.EntityDamageEvent$DamageCause) null, (org.bukkit.damage.DamageSource) null, 0.0); }
    public EntityDamageByBlockEvent(org.bukkit.block.Block arg0, org.bukkit.block.BlockState arg1, org.bukkit.entity.Entity arg2, org.bukkit.event.entity.EntityDamageEvent$DamageCause arg3, org.bukkit.damage.DamageSource arg4, double arg5) { super((org.bukkit.entity.Entity) null, (org.bukkit.event.entity.EntityDamageEvent$DamageCause) null, (org.bukkit.damage.DamageSource) null, 0.0); }
    public EntityDamageByBlockEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Entity arg1, org.bukkit.event.entity.EntityDamageEvent$DamageCause arg2, java.util.Map arg3, java.util.Map arg4) { super((org.bukkit.entity.Entity) null, (org.bukkit.event.entity.EntityDamageEvent$DamageCause) null, (org.bukkit.damage.DamageSource) null, 0.0); }
    public EntityDamageByBlockEvent(org.bukkit.block.Block arg0, org.bukkit.block.BlockState arg1, org.bukkit.entity.Entity arg2, org.bukkit.event.entity.EntityDamageEvent$DamageCause arg3, org.bukkit.damage.DamageSource arg4, java.util.Map arg5, java.util.Map arg6) { super((org.bukkit.entity.Entity) null, (org.bukkit.event.entity.EntityDamageEvent$DamageCause) null, (org.bukkit.damage.DamageSource) null, 0.0); }
    public EntityDamageByBlockEvent() { super((org.bukkit.entity.Entity) null, (org.bukkit.event.entity.EntityDamageEvent$DamageCause) null, (org.bukkit.damage.DamageSource) null, 0.0); }
    public org.bukkit.block.Block getDamager() {
        return null;
    }
    public org.bukkit.block.BlockState getDamagerBlockState() {
        return null;
    }
}
