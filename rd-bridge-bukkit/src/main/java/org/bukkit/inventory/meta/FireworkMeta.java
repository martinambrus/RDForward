package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface FireworkMeta extends org.bukkit.inventory.meta.ItemMeta {
    void addEffect(org.bukkit.FireworkEffect arg0) throws java.lang.IllegalArgumentException;
    void addEffects(org.bukkit.FireworkEffect[] arg0) throws java.lang.IllegalArgumentException;
    void addEffects(java.lang.Iterable arg0) throws java.lang.IllegalArgumentException;
    java.util.List getEffects();
    int getEffectsSize();
    void removeEffect(int arg0) throws java.lang.IndexOutOfBoundsException;
    void clearEffects();
    boolean hasEffects();
    boolean hasPower();
    int getPower();
    void setPower(int arg0) throws java.lang.IllegalArgumentException;
    org.bukkit.inventory.meta.FireworkMeta clone();
}
