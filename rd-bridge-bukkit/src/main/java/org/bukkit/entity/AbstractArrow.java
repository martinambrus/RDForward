package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface AbstractArrow extends org.bukkit.entity.Projectile {
    int getKnockbackStrength();
    void setKnockbackStrength(int arg0);
    double getDamage();
    void setDamage(double arg0);
    int getPierceLevel();
    void setPierceLevel(int arg0);
    boolean isCritical();
    void setCritical(boolean arg0);
    boolean isInBlock();
    org.bukkit.block.Block getAttachedBlock();
    java.util.List getAttachedBlocks();
    org.bukkit.entity.AbstractArrow$PickupStatus getPickupStatus();
    void setPickupStatus(org.bukkit.entity.AbstractArrow$PickupStatus arg0);
    boolean isShotFromCrossbow();
    void setShotFromCrossbow(boolean arg0);
    org.bukkit.inventory.ItemStack getItem();
    void setItem(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.ItemStack getWeapon();
    void setWeapon(org.bukkit.inventory.ItemStack arg0);
    default org.bukkit.entity.AbstractArrow$PickupRule getPickupRule() {
        return null;
    }
    default void setPickupRule(org.bukkit.entity.AbstractArrow$PickupRule arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.AbstractArrow.setPickupRule(Lorg/bukkit/entity/AbstractArrow$PickupRule;)V");
    }
    org.bukkit.inventory.ItemStack getItemStack();
    void setItemStack(org.bukkit.inventory.ItemStack arg0);
    void setLifetimeTicks(int arg0);
    int getLifetimeTicks();
    org.bukkit.Sound getHitSound();
    void setHitSound(org.bukkit.Sound arg0);
    void setShooter(org.bukkit.projectiles.ProjectileSource arg0, boolean arg1);
}
