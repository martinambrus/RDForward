package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface WanderingTrader extends org.bukkit.entity.AbstractVillager {
    int getDespawnDelay();
    void setDespawnDelay(int arg0);
    void setCanDrinkPotion(boolean arg0);
    boolean canDrinkPotion();
    void setCanDrinkMilk(boolean arg0);
    boolean canDrinkMilk();
    org.bukkit.Location getWanderingTowards();
    void setWanderingTowards(org.bukkit.Location arg0);
}
