package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CompassMeta extends org.bukkit.inventory.meta.ItemMeta {
    boolean hasLodestone();
    org.bukkit.Location getLodestone();
    void setLodestone(org.bukkit.Location arg0);
    boolean isLodestoneTracked();
    void setLodestoneTracked(boolean arg0);
    boolean isLodestoneCompass();
    void clearLodestone();
    org.bukkit.inventory.meta.CompassMeta clone();
}
