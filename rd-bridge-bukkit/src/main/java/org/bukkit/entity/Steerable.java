package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Steerable extends org.bukkit.entity.Animals {
    boolean hasSaddle();
    void setSaddle(boolean arg0);
    int getBoostTicks();
    void setBoostTicks(int arg0);
    int getCurrentBoostTicks();
    void setCurrentBoostTicks(int arg0);
    org.bukkit.Material getSteerMaterial();
}
