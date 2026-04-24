package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Item extends org.bukkit.entity.Entity, io.papermc.paper.entity.Frictional {
    org.bukkit.inventory.ItemStack getItemStack();
    void setItemStack(org.bukkit.inventory.ItemStack arg0);
    int getPickupDelay();
    void setPickupDelay(int arg0);
    void setUnlimitedLifetime(boolean arg0);
    boolean isUnlimitedLifetime();
    void setOwner(java.util.UUID arg0);
    java.util.UUID getOwner();
    void setThrower(java.util.UUID arg0);
    java.util.UUID getThrower();
    boolean canMobPickup();
    void setCanMobPickup(boolean arg0);
    boolean canPlayerPickup();
    void setCanPlayerPickup(boolean arg0);
    boolean willAge();
    void setWillAge(boolean arg0);
    int getHealth();
    void setHealth(int arg0);
}
