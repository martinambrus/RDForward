package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Warden extends org.bukkit.entity.Monster {
    int getAnger();
    int getAnger(org.bukkit.entity.Entity arg0);
    int getHighestAnger();
    void increaseAnger(org.bukkit.entity.Entity arg0, int arg1);
    void setAnger(org.bukkit.entity.Entity arg0, int arg1);
    void clearAnger(org.bukkit.entity.Entity arg0);
    org.bukkit.entity.LivingEntity getEntityAngryAt();
    void setDisturbanceLocation(org.bukkit.Location arg0);
    org.bukkit.entity.Warden$AngerLevel getAngerLevel();
}
