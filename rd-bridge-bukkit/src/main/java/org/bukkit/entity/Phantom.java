package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Phantom extends org.bukkit.entity.Flying, org.bukkit.entity.Enemy {
    int getSize();
    void setSize(int arg0);
    java.util.UUID getSpawningEntity();
    boolean shouldBurnInDay();
    void setShouldBurnInDay(boolean arg0);
    org.bukkit.Location getAnchorLocation();
    void setAnchorLocation(org.bukkit.Location arg0);
}
