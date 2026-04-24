package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface EnderSignal extends org.bukkit.entity.Entity {
    org.bukkit.Location getTargetLocation();
    void setTargetLocation(org.bukkit.Location arg0);
    void setTargetLocation(org.bukkit.Location arg0, boolean arg1);
    boolean getDropItem();
    void setDropItem(boolean arg0);
    org.bukkit.inventory.ItemStack getItem();
    void setItem(org.bukkit.inventory.ItemStack arg0);
    int getDespawnTimer();
    void setDespawnTimer(int arg0);
}
