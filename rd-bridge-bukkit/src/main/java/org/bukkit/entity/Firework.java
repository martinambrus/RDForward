package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Firework extends org.bukkit.entity.Projectile {
    org.bukkit.inventory.meta.FireworkMeta getFireworkMeta();
    void setFireworkMeta(org.bukkit.inventory.meta.FireworkMeta arg0);
    boolean setAttachedTo(org.bukkit.entity.LivingEntity arg0);
    org.bukkit.entity.LivingEntity getAttachedTo();
    boolean setLife(int arg0);
    int getLife();
    boolean setMaxLife(int arg0);
    int getMaxLife();
    void detonate();
    boolean isDetonated();
    boolean isShotAtAngle();
    void setShotAtAngle(boolean arg0);
    java.util.UUID getSpawningEntity();
    default org.bukkit.entity.LivingEntity getBoostedEntity() {
        return null;
    }
    org.bukkit.inventory.ItemStack getItem();
    void setItem(org.bukkit.inventory.ItemStack arg0);
    int getTicksFlown();
    void setTicksFlown(int arg0);
    int getTicksToDetonate();
    void setTicksToDetonate(int arg0);
}
