package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ExperienceOrb extends org.bukkit.entity.Entity {
    int getExperience();
    void setExperience(int arg0);
    int getCount();
    void setCount(int arg0);
    default boolean isFromBottle() {
        return false;
    }
    java.util.UUID getTriggerEntityId();
    java.util.UUID getSourceEntityId();
    org.bukkit.entity.ExperienceOrb$SpawnReason getSpawnReason();
}
