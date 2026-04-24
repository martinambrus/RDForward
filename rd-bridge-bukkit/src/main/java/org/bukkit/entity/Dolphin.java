package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Dolphin extends org.bukkit.entity.Ageable, org.bukkit.entity.WaterMob {
    int getMoistness();
    void setMoistness(int arg0);
    void setHasFish(boolean arg0);
    boolean hasFish();
    org.bukkit.Location getTreasureLocation();
    void setTreasureLocation(org.bukkit.Location arg0);
}
