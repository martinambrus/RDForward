package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Damageable extends org.bukkit.inventory.meta.ItemMeta {
    boolean hasDamage();
    int getDamage();
    void setDamage(int arg0);
    boolean hasDamageValue();
    void resetDamage();
    boolean hasMaxDamage();
    int getMaxDamage();
    void setMaxDamage(java.lang.Integer arg0);
    org.bukkit.inventory.meta.Damageable clone();
}
