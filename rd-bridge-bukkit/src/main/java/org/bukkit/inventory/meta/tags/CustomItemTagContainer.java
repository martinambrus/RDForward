package org.bukkit.inventory.meta.tags;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CustomItemTagContainer {
    void setCustomTag(org.bukkit.NamespacedKey arg0, org.bukkit.inventory.meta.tags.ItemTagType arg1, java.lang.Object arg2);
    boolean hasCustomTag(org.bukkit.NamespacedKey arg0, org.bukkit.inventory.meta.tags.ItemTagType arg1);
    java.lang.Object getCustomTag(org.bukkit.NamespacedKey arg0, org.bukkit.inventory.meta.tags.ItemTagType arg1);
    void removeCustomTag(org.bukkit.NamespacedKey arg0);
    boolean isEmpty();
    org.bukkit.inventory.meta.tags.ItemTagAdapterContext getAdapterContext();
}
