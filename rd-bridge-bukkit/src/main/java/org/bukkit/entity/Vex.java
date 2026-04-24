package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Vex extends org.bukkit.entity.Monster {
    boolean isCharging();
    void setCharging(boolean arg0);
    org.bukkit.Location getBound();
    void setBound(org.bukkit.Location arg0);
    int getLifeTicks();
    void setLifeTicks(int arg0);
    boolean hasLimitedLife();
    org.bukkit.entity.Mob getSummoner();
    void setSummoner(org.bukkit.entity.Mob arg0);
    boolean hasLimitedLifetime();
    void setLimitedLifetime(boolean arg0);
    int getLimitedLifetimeTicks();
    void setLimitedLifetimeTicks(int arg0);
}
